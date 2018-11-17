package com.github.freeacs.web;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.web.app.Main;
import com.github.freeacs.web.app.Monitor;
import com.github.freeacs.web.app.menu.MenuServlet;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.WebProperties;
import com.github.freeacs.web.config.SecurityConfig;
import com.github.freeacs.web.config.UserService;
import com.github.freeacs.web.help.HelpServlet;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariDataSource;
import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import static spark.Spark.redirect;
import static spark.Spark.staticFiles;

public class App {
  public static void main(String[] args) throws ServletException {
    staticFiles.location("/public");
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    ExecutorWrapper executorWrapper = ExecutorWrapperFactory.create(1);
    WebProperties properties = new WebProperties(config);
    String ctxPath = WebProperties.CONTEXT_PATH;
    before((req, res) -> {
      Session session = req.session(false);
      System.out.println(req.url());
      if ((req.url() == null || !req.url().endsWith("/login")) && (session == null || session.attribute("loggedIn") == null)) {
        res.redirect(ctxPath + "/login");
        halt();
      }
    });
    routes(mainDs, properties, executorWrapper, ctxPath);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                  executorWrapper.shutdown();
                }));
  }

  public static void routes(DataSource mainDs, WebProperties properties, ExecutorWrapper executorWrapper, String ctxPath)
      throws ServletException {
    UserService userService = new UserService(mainDs);
    SecurityConfig config = new SecurityConfig(userService);
    LogoutServlet logoutServlet = new LogoutServlet();
    logoutServlet.init();
    get(ctxPath + "/logout", (req, res) -> {
      logoutServlet.doGet(req.raw(), res.raw());
      return null;
    });
    Main main = new Main(mainDs, mainDs);
    main.init();
    get(ctxPath + "/index", (req, res) -> {
      main.doGet(req.raw(), res.raw());
      return null;
    });
    post(ctxPath + "/index", (req, res) -> {
      main.doGet(req.raw(), res.raw());
      return null;
    });
    Configuration configuration = Freemarker.initFreemarker();
    get(ctxPath + "/login", (req, res) -> {
      return new FreeMarkerEngine(configuration).render(new ModelAndView(null, "login.ftl"));
    });
    post(ctxPath + "/login", (req, res) -> {
      String username = req.raw().getParameter("username");
      String password = req.raw().getParameter("password");
      UserDetails userDetails = userService.loadUserByUsername(username);
      if (Objects.equals(userDetails.getPassword(), config.encoder().encode(password))) {
        req.session(true).attribute("loggedIn", true);
        res.redirect(ctxPath + "/index");
        return null;
      }
      return new FreeMarkerEngine(configuration).render(new ModelAndView(null, "login.ftl"));
    });
  }
  @Bean
  @Primary
  @Qualifier("main")
  @ConfigurationProperties("main.datasource")
  public DataSource mainDs() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }

  @Bean
  ServletRegistrationBean<Monitor> monitor() {
    ServletRegistrationBean<Monitor> srb = new ServletRegistrationBean<>();
    srb.setServlet(new Monitor());
    srb.setUrlMappings(Arrays.asList("/monitor", "/ok"));
    return srb;
  }

  @Bean
  ServletRegistrationBean<Main> main(
      @Qualifier("main") DataSource mainDataSource,
      @Value("${syslog.server.host}") String syslogServerHost) {
    SyslogClient.SYSLOG_SERVER_HOST = syslogServerHost;
    ServletRegistrationBean<Main> srb = new ServletRegistrationBean<>();
    srb.setServlet(new Main(mainDataSource, mainDataSource));
    srb.setName("main");
    srb.setUrlMappings(Collections.singletonList(Main.servletMapping));
    return srb;
  }

  @Bean
  ServletRegistrationBean<HelpServlet> helpServlet() {
    ServletRegistrationBean<HelpServlet> srb = new ServletRegistrationBean<>();
    srb.setServlet(new HelpServlet());
    srb.setUrlMappings(Collections.singletonList("/help"));
    return srb;
  }

  @Bean
  ServletRegistrationBean<MenuServlet> menuServlet() {
    ServletRegistrationBean<MenuServlet> srb = new ServletRegistrationBean<>();
    srb.setServlet(new MenuServlet());
    srb.setUrlMappings(Collections.singletonList("/menu"));
    return srb;
  }

  @Bean
  ServletRegistrationBean<LogoutServlet> logoutServlet() {
    ServletRegistrationBean<LogoutServlet> srb = new ServletRegistrationBean<>();
    srb.setServlet(new LogoutServlet());
    srb.setUrlMappings(Collections.singletonList("/logout"));
    return srb;
  }

  @Bean
  public FreeMarkerViewResolver freemarkerViewResolver() {
    FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
    resolver.setCache(true);
    resolver.setPrefix("");
    resolver.setSuffix(".ftl");
    return resolver;
  }

  @Bean
  public FreeMarkerConfigurer freemarkerConfig() {
    FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
    freeMarkerConfigurer.setConfiguration(Freemarker.initFreemarker());
    return freeMarkerConfigurer;
  }
}
