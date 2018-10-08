package com.github.freeacs;

import static com.github.freeacs.dbi.SyslogConstants.FACILITY_TR069;
import static com.github.freeacs.tr069.Provisioning.VERSION;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.FileServlet;
import com.github.freeacs.base.http.OKServlet;
import com.github.freeacs.http.SimpleResponseWrapper;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.Provisioning;
import com.github.freeacs.tr069.methods.TR069Method;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import spark.ExceptionMapper;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyServer;
import spark.embeddedserver.jetty.JettyHandler;
import spark.embeddedserver.jetty.JettyServerFactory;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

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
    EmbeddedServers.add(
        EmbeddedServers.Identifiers.JETTY,
        (Routes routeMatcher,
            StaticFilesConfiguration staticFilesConfiguration,
            ExceptionMapper exceptionMapper,
            boolean hasMultipleHandler) -> {
          MatcherFilter matcherFilter =
              new MatcherFilter(
                  routeMatcher,
                  staticFilesConfiguration,
                  exceptionMapper,
                  false,
                  hasMultipleHandler);
          matcherFilter.init(null);
          JettyHandler handler = new JettyHandler(matcherFilter);
          handler
              .getSessionCookieConfig()
              .setHttpOnly(config.getBoolean("server.servlet.session.cookie.http-only"));
          return new EmbeddedJettyServer(
              new JettyServer(getInt(config, "server.jetty.max-http-post-size")), handler);
        });
    DataSource mainDs = dataSource(config.getConfig("main"));
    routes(mainDs, new Properties(config));
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

  @FunctionalInterface
  public interface CheckedConsumer<L, R> {
    void apply(L l, R r) throws ServletException, IOException;
  }

  private static byte[] process(
      CheckedConsumer<HttpServletRequest, HttpServletResponse> service,
      Request request,
      Response response,
      SimpleResponseWrapper responseWrapper)
      throws ServletException, IOException {
    service.apply(request.raw(), responseWrapper);
    response.status(responseWrapper.getStatus());
    response.type(responseWrapper.getContentType());
    responseWrapper.getHeaders().forEach((k, v) -> response.header(k, v.toString()));
    return responseWrapper.getResponseAsBytes();
  }

  private static DataSource dataSource(Config config) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName(config.getString("datasource.driverClassName"));
    hikariConfig.setJdbcUrl(config.getString("datasource.jdbcUrl"));
    hikariConfig.setUsername(config.getString("datasource.username"));
    hikariConfig.setPassword(config.getString("datasource.password"));

    hikariConfig.setMinimumIdle(config.getInt("datasource.minimum-idle"));
    hikariConfig.setMaximumPoolSize(config.getInt("datasource.maximum-pool-size"));
    hikariConfig.setConnectionTestQuery("SELECT 1");
    hikariConfig.setPoolName(config.getString("datasource.poolName"));

    hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
    hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

    return new HikariDataSource(hikariConfig);
  }

  /** Creates Jetty Server instances. */
  static class JettyServer implements JettyServerFactory {

    private final int maxPostSize;

    private JettyServer(int anInt) {
      this.maxPostSize = anInt;
    }

    /**
     * Creates a Jetty server.
     *
     * @param maxThreads maxThreads
     * @param minThreads minThreads
     * @param threadTimeoutMillis threadTimeoutMillis
     * @return a new jetty server instance
     */
    public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
      Server server;
      if (maxThreads > 0) {
        int min = (minThreads > 0) ? minThreads : 8;
        int idleTimeout = (threadTimeoutMillis > 0) ? threadTimeoutMillis : 60000;

        server = new Server(new QueuedThreadPool(maxThreads, min, idleTimeout));
      } else {
        server = new Server();
      }
      server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", maxPostSize);
      return server;
    }

    /**
     * Creates a Jetty server with supplied thread pool
     *
     * @param threadPool thread pool
     * @return a new jetty server instance
     */
    @Override
    public Server create(ThreadPool threadPool) {
      Server server = threadPool != null ? new Server(threadPool) : new Server();
      server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", maxPostSize);
      return server;
    }
  }
}
