package com.github.freeacs.syslogserver;

import static spark.Spark.get;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
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
    ExecutorWrapper executorWrapper = new ExecutorWrapper(3);
    SyslogServlet syslogServlet = new SyslogServlet(mainDs, properties, executorWrapper);
    syslogServlet.init();
    get(properties.getContextPath() + "/ok", (req, res) -> syslogServlet.health());
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  SyslogServlet.destroy();
                  executorWrapper.shutdown();
                }));
  }
}
