package com.github.freeacs.stun;

import static spark.Spark.get;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.jetty.JettyFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.sql.DataSource;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;

public class App {

  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new JettyFactory(true, -1, -1));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    Properties properties = new Properties(config);
    StunServlet stunServlet = new StunServlet(mainDs, properties);
    stunServlet.init();
    get(properties.getContextPath() + "/ok", (req, res) -> "FREEACSOK");
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  StunServlet.destroy();
                }));
  }
}
