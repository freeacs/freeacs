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
import com.github.freeacs.web.security.LogoutServlet;
import com.github.freeacs.web.security.ThreadUser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import freemarker.template.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.util.Objects;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class App {
  public static void main(String[] args) throws ServletException {
    staticFiles.location("/public");
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    ExecutorWrapper executorWrapper = ExecutorWrapperFactory.create(1);
    WebProperties properties = new WebProperties(config);
    SyslogClient.SYSLOG_SERVER_HOST = WebProperties.SYSLOG_SERVER_HOST;
    String ctxPath = WebProperties.CONTEXT_PATH;
    before("*", (req, res) -> {
      ThreadUser.setUserDetails(null);
      Session session = req.session(false);
      if ((req.url() == null || !req.url().endsWith("/login")) && (session == null || session.attribute("loggedIn") == null)) {
        res.redirect(ctxPath + "/login");
        halt();
      } else if (session != null && session.attribute("loggedIn") != null) {
        ThreadUser.setUserDetails(session.attribute("loggedIn"));
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

  private static void routes(DataSource mainDs, WebProperties properties, ExecutorWrapper executorWrapper, String ctxPath)
      throws ServletException {
    UserService userService = new UserService(mainDs);
    SecurityConfig config = new SecurityConfig(userService);
    LogoutServlet logoutServlet = new LogoutServlet();
    logoutServlet.init();
    get(ctxPath + "/logout", (req, res) -> {
      logoutServlet.doGet(req.raw(), res.raw());
      res.redirect(ctxPath + Main.servletMapping);
      req.session().removeAttribute("loggedIn");
      ThreadUser.setUserDetails(null);
      return null;
    });
    Main main = new Main(mainDs, mainDs);
    main.init();
    get(ctxPath + Main.servletMapping, (req, res) -> {
      main.doGet(req.raw(), res.raw());
      return null;
    });
    post(ctxPath + Main.servletMapping, (req, res) -> {
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
        req.session(true).attribute("loggedIn", userDetails);
        ThreadUser.setUserDetails(userDetails);
        res.redirect(ctxPath + Main.servletMapping);
        return null;
      }
      return new FreeMarkerEngine(configuration).render(new ModelAndView(null, "login.ftl"));
    });
    Monitor monitorServlet = new Monitor();
    monitorServlet.init();
    get(ctxPath + "/ok", (req, res) -> {
        monitorServlet.service(req.raw(), res.raw());
        return null;
    });
    HelpServlet helpServlet = new HelpServlet();
    get(ctxPath + "/help", (req, res) -> {
        helpServlet.service(req.raw(), res.raw());
        return null;
    });
    MenuServlet menuServlet = new MenuServlet();
    get(ctxPath + "/menu", (req, res) -> {
        menuServlet.service(req.raw(), res.raw());
        return null;
    });
  }
}
