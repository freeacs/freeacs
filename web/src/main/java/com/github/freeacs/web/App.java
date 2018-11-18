package com.github.freeacs.web;

import static spark.Spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.web.app.Main;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.WebProperties;
import com.github.freeacs.web.routes.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariDataSource;
import freemarker.template.Configuration;
import java.sql.SQLException;
import javax.sql.DataSource;
import spark.Spark;

public class App {

  public static void main(String[] args) {
    final Config config = ConfigFactory.load();
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    WebProperties properties = new WebProperties(config);
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
    redirect.get("/", ctxPath + Main.servletMapping);
    redirect.get(ctxPath, ctxPath + Main.servletMapping);
    before("*", new LoginFilter(ctxPath));
    path(
        ctxPath,
        () -> {
          path(Main.servletMapping, new MainRoute(mainDs));
          get("/logout", new LogoutRoute(ctxPath));
          path("/login", new LoginRoute(ctxPath, configuration, mainDs));
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
}
