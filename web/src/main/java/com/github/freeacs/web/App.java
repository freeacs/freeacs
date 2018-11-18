package com.github.freeacs.web;

import static spark.Spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.web.app.Main;
import com.github.freeacs.web.app.Monitor;
import com.github.freeacs.web.app.menu.MenuServlet;
import com.github.freeacs.web.app.page.unit.UnitStatusPage;
import com.github.freeacs.web.app.page.unittype.UnittypeParametersPage;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import spark.ModelAndView;
import spark.Request;
import spark.Session;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

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
    routes(mainDs, properties, ctxPath);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                }));
  }

  public static void routes(DataSource mainDs, WebProperties properties, String ctxPath) {
    redirect.get("/", ctxPath);
    before(
        "*",
        (req, res) -> {
          ThreadUser.setUserDetails(null);
          Session session = req.session(false);
          if (!req.url().endsWith("/login")
              && !req.url().endsWith("/ok")
              && (session == null || session.attribute("loggedIn") == null)) {
            res.redirect(ctxPath + "/login");
            halt();
          } else if (session != null && session.attribute("loggedIn") != null) {
            ThreadUser.setUserDetails(session.attribute("loggedIn"));
          }
        });
    ObjectMapper objectMapper = new ObjectMapper();
    get(
        ctxPath + "/logout",
        (req, res) -> {
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
    get(
        ctxPath + Main.servletMapping,
        (req, res) -> {
          main.doGet(req.raw(), res.raw());
          return null;
        });
    post(
        ctxPath + Main.servletMapping,
        (req, res) -> {
          main.doGet(req.raw(), res.raw());
          return null;
        });
    Configuration configuration = Freemarker.initFreemarker();
    get(ctxPath + "/login", (req, res) -> displayLogin(configuration, req));
    post(
        ctxPath + "/login",
        (req, res) -> {
          String username = req.raw().getParameter("username");
          String password = req.raw().getParameter("password");
          String csrf = req.raw().getParameter("csrf");
          if (csrf == null || !Objects.equals(req.session().attribute("csrf"), csrf)) {
            res.redirect(ctxPath + Main.servletMapping);
            return null;
          }
          WebUser userDetails = UserService.loadUserByUsername(mainDs, username);
          if (Objects.equals(userDetails.getPassword(), encoder.encode(password))) {
            req.session(true).attribute("loggedIn", userDetails);
            ThreadUser.setUserDetails(userDetails);
            res.redirect(ctxPath + Main.servletMapping);
            return null;
          }
          return displayLogin(configuration, req);
        });
    Monitor monitorServlet = new Monitor();
    get(
        ctxPath + "/ok",
        (req, res) -> {
          monitorServlet.service(req.raw(), res.raw());
          return null;
        });
    HelpServlet helpServlet = new HelpServlet();
    get(
        ctxPath + "/help",
        (req, res) -> {
          helpServlet.service(req.raw(), res.raw());
          return null;
        });
    MenuServlet menuServlet = new MenuServlet();
    menuServlet.init();
    get(
        ctxPath + "/menu",
        (req, res) -> {
          menuServlet.service(req.raw(), res.raw());
          return null;
        });
    UnitStatusPage unitStatusPage = new UnitStatusPage();
    unitStatusPage.setMainDataSource(mainDs);
    get(
        ctxPath + "/app/unit-dashboard/linesup",
        (req, res) -> {
          String unitId = req.raw().getParameter("unitId");
          Map<String, Boolean> result =
              unitStatusPage.getLineStatus(unitId, req.raw().getSession());
          return objectMapper.writeValueAsString(result);
        });
    get(
        ctxPath + "/app/unit-dashboard/chartimage",
        (req, res) -> {
          String pageType = req.raw().getParameter("type");
          String periodType = req.raw().getParameter("period");
          String method = req.raw().getParameter("method");
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          String syslogFilter = req.raw().getParameter("syslogFilter");
          String[] aggregations = req.raw().getParameterValues("aggregate");
          unitStatusPage.getChartImage(
              pageType,
              periodType,
              method,
              startTms,
              endTms,
              unitId,
              syslogFilter,
              aggregations,
              res.raw(),
              req.raw().getSession());
          return null;
        });
    get(
        ctxPath + "/app/unit-dashboard/charttable",
        (req, res) -> {
          String pageType = req.raw().getParameter("type");
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          String syslogFilter = req.raw().getParameter("syslogFilter");
          ModelAndView modelAndView =
              unitStatusPage.getChartTable(
                  pageType,
                  startTms,
                  endTms,
                  unitId,
                  syslogFilter,
                  req.raw(),
                  req.raw().getSession());
          return new FreeMarkerEngine(configuration).render(modelAndView);
        });
    get(
        ctxPath + "/app/unit-dashboard/totalscore-effect",
        (req, res) -> {
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          Map<String, Object> result =
              unitStatusPage.getTotalScoreEffect(startTms, endTms, unitId, req.raw().getSession());
          return objectMapper.writeValueAsString(result);
        });
    get(
        ctxPath + "/app/unit-dashboard/totalscore-number",
        (req, res) -> {
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          return unitStatusPage.getTotalScoreNumber(
              startTms, endTms, unitId, req.raw().getSession());
        });
    get(
        ctxPath + "/app/unit-dashboard/overallstatus",
        (req, res) -> {
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          unitStatusPage.getOverallStatusSpeedometer(
              startTms, endTms, unitId, res.raw(), req.raw().getSession());
          return null;
        });
    UnittypeParametersPage unittypeParametersPage = new UnittypeParametersPage();
    unittypeParametersPage.setMainDataSource(mainDs);
    get(
        ctxPath + "/app/parameters/list",
        (req, res) -> {
          String unittype = req.raw().getParameter("unittype");
          String term = req.raw().getParameter("term");
          return unittypeParametersPage.getUnittypeParameters(
              unittype, term, req.raw().getSession());
        });
  }

  private static String displayLogin(Configuration configuration, Request req) {
    String uuid = UUID.randomUUID().toString();
    req.session(true).attribute("csrf", uuid);
    return new FreeMarkerEngine(configuration)
        .render(
            new ModelAndView(
                new HashMap<String, String>() {
                  {
                    put("csrf", uuid);
                  }
                },
                "login.ftl"));
  }
}
