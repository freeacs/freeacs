package com.github.freeacs.stun;

import static spark.Spark.get;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.sql.DataSource;
import spark.Spark;

public class App {
  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    Properties properties = new Properties(config);
    ExecutorWrapper executorWrapper = ExecutorWrapperFactory.create(2);
    StunServlet stunServlet = new StunServlet(mainDs, properties, executorWrapper);
    stunServlet.init();
    get(properties.getContextPath() + "/ok", (req, res) -> "FREEACSOK");
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  StunServlet.destroy();
                  executorWrapper.shutdown();
                }));
  }
}
