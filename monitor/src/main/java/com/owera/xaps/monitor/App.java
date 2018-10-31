package com.owera.xaps.monitor;

import static com.github.freeacs.common.spark.ResponseHelper.process;
import static spark.Spark.path;

import com.github.freeacs.common.http.SimpleResponseWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.common.util.Sleep;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.servlet.ServletException;
import spark.Spark;

public class App {
  public static void main(String[] args) throws ServletException {
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    ExecutorWrapper executorWrapper = ExecutorWrapperFactory.create(1);
    routes(new Properties(config), executorWrapper);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                  executorWrapper.shutdown();
                }));
  }

  public static void routes(Properties properties, ExecutorWrapper executorWrapper)
      throws ServletException {
    MonitorServlet servlet = new MonitorServlet(properties, executorWrapper);
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
