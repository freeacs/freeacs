package com.github.freeacs;

import static com.github.freeacs.common.spark.ResponseHelper.process;
import static com.github.freeacs.dbi.SyslogConstants.FACILITY_TR069;
import static spark.Spark.get;
import static spark.Spark.post;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.FileServlet;
import com.github.freeacs.base.http.OKServlet;
import com.github.freeacs.common.http.SimpleResponseWrapper;
import com.github.freeacs.common.jetty.JettyFactory;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.common.spark.SparkApp;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.Provisioning;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import spark.Request;
import spark.Route;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;

public class App extends SparkApp {
  private static final List<String> ALLOWED_CONTENT_TYPES =
      Arrays.asList("application/soap+xml", "application/xml", "text/xml", "text/html", "");

  public static void main(String[] args) throws SQLException {
    final App app = new App();
    SyslogClient.SYSLOG_SERVER_HOST = app.config.getString("syslog.server.host");
    int maxThreads = app.getIntOrMinusOne("server.jetty.threadpool.maxThreads");
    int minThreads = app.getIntOrMinusOne("server.jetty.threadpool.minThreads");
    int timeOutMillis = app.getIntOrMinusOne("server.jetty.threadpool.timeOutMillis");
    Spark.threadPool(maxThreads, minThreads, timeOutMillis);
    boolean httpOnly = app.config.getBoolean("server.servlet.session.cookie.http-only");
    int maxHttpPostSize = app.getIntOrMinusOne("server.jetty.max-http-post-size");
    int maxFormKeys = app.getIntOrMinusOne("server.jetty.max-form-keys");
    EmbeddedServers.add(
        EmbeddedServers.Identifiers.JETTY,
        new JettyFactory(httpOnly, maxHttpPostSize, maxFormKeys, null));
    ExecutorWrapper executorWrapper = ExecutorWrapperFactory.create(4);
    routes(app.datasource, new Properties(app.config), executorWrapper);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                  executorWrapper.shutdown();
                }));
  }

  static void routes(DataSource mainDs, Properties properties, ExecutorWrapper executorWrapper) throws SQLException {
    String ctxPath = properties.getContextPath();
    DBAccess.createInstance(FACILITY_TR069, "latest", mainDs);
    Provisioning provisioning = new Provisioning(properties, executorWrapper);
    provisioning.init();
    FileServlet fileServlet = new FileServlet(ctxPath + "/file/", properties);
    OKServlet okServlet = new OKServlet();

    post(ctxPath, processRequest(provisioning));
    get(ctxPath, processHealth(okServlet));
    post(ctxPath + "/", processRequest(provisioning));
    post(ctxPath + "/prov", processRequest(provisioning));
    get(ctxPath + "/file/*", processFile(fileServlet));
    get(ctxPath + "/ok", processHealth(okServlet));
  }

  private static Route processHealth(OKServlet okServlet) {
    return (req, res) -> {
      if (req.queryParams("clearCache") != null) {
        BaseCache.clearCache();
        Log.info(App.class, "Cleared base cache");
      }
      SimpleResponseWrapper response = new SimpleResponseWrapper(200, "text/html");
      return process(okServlet::doGet, req, res, response);
    };
  }

  private static Route processFile(FileServlet fileServlet) {
    return (req, res) -> {
      SimpleResponseWrapper response = new SimpleResponseWrapper(200, "application/octet-stream");
      return process(fileServlet::doPost, req, res, response);
    };
  }

  private static Route processRequest(Provisioning provisioning) {
    return (req, res) -> {
      if (ALLOWED_CONTENT_TYPES.contains(getContentType(req))) {
        SimpleResponseWrapper response = new SimpleResponseWrapper(200, "text/xml");
        return process(provisioning::doPost, req, res, response);
      }
      Log.warn(App.class, "Got unexpected content type: " + req.contentType());
      res.status(415);
      return "";
    };
  }

  private static String getContentType(Request req) {
    return Optional.ofNullable(req.contentType())
        .map(s -> s.toLowerCase().split(";")[0])
        .orElse("");
  }
}
