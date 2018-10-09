package com.owera.xaps.monitor;

import static com.github.freeacs.common.spark.ResponseHelper.process;
import static spark.Spark.path;

import com.github.freeacs.common.http.SimpleResponseWrapper;
import com.github.freeacs.common.jetty.JettyFactory;
import com.github.freeacs.common.util.Sleep;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.servlet.ServletException;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;

public class App {

  public static void main(String[] args) throws ServletException {
    Config config = ConfigFactory.load();
    Spark.port(config.getInt("server.port"));
    EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new JettyFactory(true, -1, -1));
    routes(new Properties(config));
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  Sleep.terminateApplication();
                }));
    System.out.println("Application Terminating ...");
  }

  public static void routes(Properties properties) throws ServletException {
    MonitorServlet servlet = new MonitorServlet(properties);
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
