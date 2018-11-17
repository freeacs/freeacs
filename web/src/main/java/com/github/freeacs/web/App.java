package com.github.freeacs.web;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.web.app.Main;
import com.github.freeacs.web.app.Monitor;
import com.github.freeacs.web.app.menu.MenuServlet;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebProperties;
import com.github.freeacs.web.config.PasswordEncoder;
import com.github.freeacs.web.config.SecurityConfig;
import com.github.freeacs.web.config.UserService;
import com.github.freeacs.web.help.HelpServlet;
import com.github.freeacs.web.security.ThreadUser;
import com.github.freeacs.web.security.WebUser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static spark.Spark.*;

public class App {

  private static final PasswordEncoder encoder = SecurityConfig.encoder();

  public static void main(String[] args) throws ServletException {
    staticFiles.location("/public");
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    WebProperties properties = new WebProperties(config);
    SyslogClient.SYSLOG_SERVER_HOST = WebProperties.SYSLOG_SERVER_HOST;
    String ctxPath = WebProperties.CONTEXT_PATH;
    redirect.get("/", ctxPath);
    before("*", (req, res) -> {
      ThreadUser.setUserDetails(null);
      Session session = req.session(false);
      if ((req.url() == null
              || !req.url().endsWith("/login")) && (session == null
              || session.attribute("loggedIn") == null)) {
        res.redirect(ctxPath + "/login");
        halt();
      } else if (session != null && session.attribute("loggedIn") != null) {
        ThreadUser.setUserDetails(session.attribute("loggedIn"));
      }
    });
    routes(mainDs, properties, ctxPath);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                }));
  }

  private static void routes(DataSource mainDs, WebProperties properties, String ctxPath)
      throws ServletException {
    UserService userService = new UserService(mainDs);
    get(ctxPath + "/logout", (req, res) -> {
      HttpSession session = req.raw().getSession(false);
      if (session != null) {
        DBI dbi = SessionCache.getDBI(session.getId());
        if (dbi != null) {
          dbi.setRunning(false);
        }
        SessionCache.removeSession(session.getId());
      }
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
      String uuid = UUID.randomUUID().toString();
      req.session(true).attribute("csrf", uuid);
      return new FreeMarkerEngine(configuration).render(new ModelAndView(new HashMap<String, String>() {{
        put("csrf", uuid);
      }}, "login.ftl"));
    });
    post(ctxPath + "/login", (req, res) -> {
      String username = req.raw().getParameter("username");
      String password = req.raw().getParameter("password");
      String csrf = req.raw().getParameter("csrf");
      if (csrf == null || !Objects.equals(req.session().attribute("csrf"), csrf)) {
        res.redirect(ctxPath + Main.servletMapping);
        return null;
      }
      WebUser userDetails = userService.loadUserByUsername(username);
      if (Objects.equals(userDetails.getPassword(), encoder.encode(password))) {
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
    helpServlet.init();
    get(ctxPath + "/help", (req, res) -> {
        helpServlet.service(req.raw(), res.raw());
        return null;
    });
    MenuServlet menuServlet = new MenuServlet();
    menuServlet.init();
    get(ctxPath + "/menu", (req, res) -> {
        menuServlet.service(req.raw(), res.raw());
        return null;
    });
  }
}
