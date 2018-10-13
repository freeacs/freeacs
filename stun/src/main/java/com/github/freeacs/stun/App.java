package com.github.freeacs.stun;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.jetty.JettyFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;

import javax.sql.DataSource;

import static spark.Spark.get;

public class App {

  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    EmbeddedServers.add(
            EmbeddedServers.Identifiers.JETTY,
            new JettyFactory(true, -1, -1));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    new Properties(config);
    routes(mainDs);
    Runtime.getRuntime()
            .addShutdownHook(
                    new Thread(
                            () -> {
                              System.out.println("Shutdown Hook is running !");
                              StunServlet.destroy();
                            }));
  }

  private static void routes(DataSource ds) {
    StunServlet stunServlet = new StunServlet(ds, ds);
    stunServlet.init();
    get("/health", (req, res) -> "FREEACSOK");
  }
}
