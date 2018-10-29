package com.owera.xaps.monitor;

import static com.github.freeacs.common.spark.ResponseHelper.process;
import static spark.Spark.path;

import com.github.freeacs.common.http.SimpleResponseWrapper;
import com.github.freeacs.common.quartz.QuartzWrapper;
import com.github.freeacs.common.util.Sleep;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.servlet.ServletException;
import org.quartz.SchedulerException;
import spark.Spark;

public class App {
  public static void main(String[] args) throws ServletException, SchedulerException {
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    QuartzWrapper quartzWrapper = new QuartzWrapper();
    quartzWrapper.init();
    routes(new Properties(config), quartzWrapper);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                  try {
                    quartzWrapper.shutdown();
                  } catch (SchedulerException e) {
                    e.printStackTrace();
                  }
                }));
  }

  public static void routes(Properties properties, QuartzWrapper quartzWrapper)
      throws ServletException {
    MonitorServlet servlet = new MonitorServlet(properties, quartzWrapper);
    servlet.init(null);
    path(
        properties.getContextPath(),
        () -> {
          Spark.get(
              "/web",
              (req, res) -> {
                SimpleResponseWrapper response = new SimpleResponseWrapper(200, "text/html");
                return process(servlet::service, req, res, response);
              });
        });
  }
}
