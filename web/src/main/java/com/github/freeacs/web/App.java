package com.github.freeacs.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.jetty.JettyFactory;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebProperties;
import com.github.freeacs.web.routes.HealthRoute;
import com.github.freeacs.web.routes.HelpRoute;
import com.github.freeacs.web.routes.LoginFilter;
import com.github.freeacs.web.routes.LoginRoute;
import com.github.freeacs.web.routes.LogoutRoute;
import com.github.freeacs.web.routes.MainRoute;
import com.github.freeacs.web.routes.MenuRoute;
import com.github.freeacs.web.routes.UnitDashboardRoute;
import com.github.freeacs.web.routes.UnittypeParametersRoute;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariDataSource;
import freemarker.template.Configuration;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.sql.DataSource;
import java.sql.SQLException;

import static com.github.freeacs.web.app.util.WebConstants.INDEX_URI;
import static com.github.freeacs.web.app.util.WebConstants.LOGIN_URI;
import static com.github.freeacs.web.app.util.WebConstants.LOGOUT_URI;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.redirect;
import static spark.Spark.staticFiles;

public class App {

  public static void main(String[] args) {
    final Config config = ConfigFactory.load();
    boolean httpOnly = config.getBoolean("server.servlet.session.cookie.http-only");
    int maxHttpPostSize = getInt(config, "server.jetty.max-http-post-size");
    int maxFormKeys = getInt(config, "server.jetty.max-form-keys");
    WebProperties properties = new WebProperties(config);
    EmbeddedServers.add(
        EmbeddedServers.Identifiers.JETTY,
        new JettyFactory(httpOnly, maxHttpPostSize, maxFormKeys, new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent httpSessionEvent) {
                httpSessionEvent.getSession().setMaxInactiveInterval(properties.getSessionTimeout() * 60);
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
                  String sessionId = httpSessionEvent.getSession().getId();
                DBI dbi = SessionCache.getDBI(sessionId);
                if (dbi != null) {
                    dbi.setRunning(false);
                }
                SessionCache.removeSession(sessionId);
            }
        }));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    Configuration configuration = Freemarker.initFreemarker();
    ObjectMapper objectMapper = new ObjectMapper();
    routes(mainDs, properties, configuration, objectMapper);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                  Spark.stop();
                  try {
                    mainDs.unwrap(HikariDataSource.class).close();
                  } catch (SQLException e) {
                    e.printStackTrace();
                  }
                  System.out.println("Halted");
                }));
  }

  public static void routes(
      final DataSource mainDs,
      final WebProperties properties,
      final Configuration configuration,
      final ObjectMapper objectMapper) {
    final String ctxPath = properties.getContextPath();
    SyslogClient.SYSLOG_SERVER_HOST = properties.getSyslogServerHost();
    Spark.port(properties.getServerPort());
    staticFiles.location("/public");
    redirect.get("/", ctxPath + INDEX_URI);
    redirect.get(ctxPath, ctxPath + INDEX_URI);
    before("*", new LoginFilter(ctxPath));
    path(
        ctxPath,
        () -> {
          path(INDEX_URI, new MainRoute(mainDs));
          path(LOGIN_URI, new LoginRoute(ctxPath, configuration, mainDs));
          get(LOGOUT_URI, new LogoutRoute(ctxPath));
          get("/ok", new HealthRoute());
          get("/help", new HelpRoute());
          get("/menu", new MenuRoute(configuration));
          path(
              "/app",
              () -> {
                path(
                    "/unit-dashboard", new UnitDashboardRoute(configuration, objectMapper, mainDs));
                get("/parameters/list", new UnittypeParametersRoute(mainDs));
              });
        });
  }

  private static int getInt(Config config, String s) {
    return config.hasPath(s) ? config.getInt(s) : -1;
  }
}
