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
import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.http.SimpleResponseWrapper;
import com.github.freeacs.common.jetty.JettyFactory;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.Provisioning;
import com.github.freeacs.tr069.methods.TR069Method;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.quartz.SchedulerException;
import spark.Request;
import spark.Route;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;

public class App {
  private static final List<String> ALLOWED_CONTENT_TYPES =
      Arrays.asList("application/soap+xml", "application/xml", "text/xml", "text/html", "");

  public static void main(String[] args) throws SchedulerException {
    Config config = ConfigFactory.load();
    SyslogClient.SYSLOG_SERVER_HOST = config.getString("syslog.server.host");
    Spark.port(config.getInt("server.port"));
    setupThreadPool(config);
    boolean httpOnly = config.getBoolean("server.servlet.session.cookie.http-only");
    int maxHttpPostSize = getInt(config, "server.jetty.max-http-post-size");
    int maxFormKeys = getInt(config, "server.jetty.max-form-keys");
    EmbeddedServers.add(
        EmbeddedServers.Identifiers.JETTY,
        new JettyFactory(httpOnly, maxHttpPostSize, maxFormKeys));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    ExecutorWrapper executorWrapper = new ExecutorWrapper(4);
    routes(mainDs, new Properties(config), executorWrapper);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                  executorWrapper.shutdown();
                }));
  }

  private static void setupThreadPool(final Config config) {
    /* THREADPOOL BEGIN */
    // Possible to add a new property server.jetty.threadpool.type? could be standard and custom.
    // My thought is that custom thread pool is ExecutorThreadPool, while standard is .. well,
    // standard ;) Queued.
    int maxThreads = getInt(config, "server.jetty.threadpool.maxThreads");
    int minThreads = getInt(config, "server.jetty.threadpool.minThreads");
    int timeOutMillis = getInt(config, "server.jetty.threadpool.timeOutMillis");
    Spark.threadPool(maxThreads, minThreads, timeOutMillis);
    /* THREADPOOL END */
  }

  private static int getInt(Config config, String s) {
    return config.hasPath(s) ? config.getInt(s) : -1;
  }

  public static void routes(
      DataSource mainDs, Properties properties, ExecutorWrapper executorWrapper)
      throws SchedulerException {
    String ctxPath = properties.getContextPath();
    DBAccess dbAccess = new DBAccess(FACILITY_TR069, "latest", mainDs, mainDs);
    TR069Method tr069Method = new TR069Method(properties);
    Provisioning provisioning =
        new Provisioning(dbAccess, tr069Method, properties, executorWrapper);
    provisioning.init();
    FileServlet fileServlet = new FileServlet(dbAccess, ctxPath + "/file/", properties);
    OKServlet okServlet = new OKServlet(dbAccess);
    post(ctxPath, processRequest(provisioning));
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
      return process(okServlet::service, req, res, response);
    };
  }

  private static Route processFile(FileServlet fileServlet) {
    return (req, res) -> {
      SimpleResponseWrapper response = new SimpleResponseWrapper(200, "application/octet-stream");
      return process(fileServlet::service, req, res, response);
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
