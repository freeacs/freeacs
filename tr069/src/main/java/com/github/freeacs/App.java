package com.github.freeacs;

import static com.github.freeacs.common.spark.ResponseHelper.process;
import static com.github.freeacs.dbi.SyslogConstants.FACILITY_TR069;
import static com.github.freeacs.tr069.Provisioning.VERSION;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.FileServlet;
import com.github.freeacs.base.http.OKServlet;
import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.http.SimpleResponseWrapper;
import com.github.freeacs.common.jetty.JettyFactory;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.Provisioning;
import com.github.freeacs.tr069.methods.TR069Method;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.sql.DataSource;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;

public class App {

  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    /* THREADPOOL BEGIN */
    // Possible to add a new property server.jetty.threadpool.type? could be standard and custom.
    // My thought is that custom thread pool is ExecutorThreadPool, while standard is .. well,
    // standard ;) Queued.
    int maxThreads = getInt(config, "server.jetty.threadpool.maxThreads");
    int minThreads = getInt(config, "server.jetty.threadpool.minThreads");
    int timeOutMillis = getInt(config, "server.jetty.threadpool.timeOutMillis");
    Spark.threadPool(maxThreads, minThreads, timeOutMillis);
    /* THREADPOOL END */
    boolean httpOnly = config.getBoolean("server.servlet.session.cookie.http-only");
    int maxHttpPostSize = getInt(config, "server.jetty.max-http-post-size");
    int maxFormKeys = getInt(config, "server.jetty.max-form-keys");
    EmbeddedServers.add(
        EmbeddedServers.Identifiers.JETTY,
        new JettyFactory(httpOnly, maxHttpPostSize, maxFormKeys));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    routes(mainDs, new Properties(config));
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                }));
    System.out.println("Application Terminating ...");
  }

  private static int getInt(Config config, String s) {
    return config.hasPath(s) ? config.getInt(s) : -1;
  }

  public static void routes(DataSource mainDs, Properties properties) {
    DBAccess dbAccess = new DBAccess(FACILITY_TR069, VERSION, mainDs, mainDs);
    path(
        properties.getContextPath(),
        () -> {
          TR069Method tr069Method = new TR069Method(properties);
          Provisioning provisioning = new Provisioning(dbAccess, tr069Method, properties);
          provisioning.init();
          post(
              "/prov",
              (req, res) -> {
                SimpleResponseWrapper response = new SimpleResponseWrapper(200, "text/xml");
                return process(provisioning::service, req, res, response);
              });
          FileServlet fileServlet =
              new FileServlet(dbAccess, properties.getContextPath() + "/file/");
          get(
              "/file/*",
              (req, res) -> {
                SimpleResponseWrapper response =
                    new SimpleResponseWrapper(200, "application/octet-stream");
                return process(fileServlet::service, req, res, response);
              });
          OKServlet okServlet = new OKServlet(dbAccess);
          get(
              "/ok",
              (req, res) -> {
                SimpleResponseWrapper response = new SimpleResponseWrapper(200, "text/html");
                return process(okServlet::service, req, res, response);
              });
        });
  }
}
