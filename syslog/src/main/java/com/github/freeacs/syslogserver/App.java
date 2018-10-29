package com.github.freeacs.syslogserver;

import static spark.Spark.get;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.quartz.QuartzWrapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.sql.DataSource;
import org.quartz.SchedulerException;
import spark.Spark;

public class App {
  public static void main(String[] args) throws SchedulerException {
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    DataSource mainDs = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    Properties properties = new Properties(config);
    QuartzWrapper quartzWrapper = new QuartzWrapper();
    quartzWrapper.init();
    SyslogServlet syslogServlet = new SyslogServlet(mainDs, properties, quartzWrapper);
    syslogServlet.init();
    get(properties.getContextPath() + "/ok", (req, res) -> syslogServlet.health());
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  SyslogServlet.destroy();
                  try {
                    quartzWrapper.shutdown();
                  } catch (SchedulerException e) {
                    e.printStackTrace();
                  }
                }));
  }
}
