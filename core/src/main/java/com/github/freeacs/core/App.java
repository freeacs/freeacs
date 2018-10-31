package com.github.freeacs.core;

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
    ExecutorWrapper executorwrapper = ExecutorWrapperFactory.create(10);
    CoreServlet coreServlet = new CoreServlet(mainDs, properties, executorwrapper);
    coreServlet.init();
    get(properties.getContextPath() + "/ok", (req, res) -> coreServlet.health());
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  coreServlet.destroy();
                  executorwrapper.shutdown();
                }));
  }
}
