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
import spark.Request;
import spark.Response;

public class App {

  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    DataSource mainDs = dataSource(config.getConfig("main"));
    routes(config, mainDs);
  }

  public static void routes(Config config, DataSource mainDs) {
    DBAccess dbAccess = new DBAccess(FACILITY_TR069, VERSION, mainDs, mainDs);
    Properties properties = new Properties(config);
    TR069Method tr069Method = new TR069Method(properties);
    path(
        "/tr069",
        () -> {
          Provisioning provisioning = new Provisioning(dbAccess, tr069Method, properties);
          provisioning.init();
          post(
              "/prov",
              (req, res) -> {
                SimpleResponseWrapper response = new SimpleResponseWrapper(200, "text/xml");
                return process(provisioning::service, req, res, response);
              });
          FileServlet fileServlet = new FileServlet(dbAccess, "/tr069/file/");
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
    responseWrapper.getHeaders().forEach(response::header);
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
}
