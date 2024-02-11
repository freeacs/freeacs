package com.github.freeacs.stun;

import static spark.Spark.get;

import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.common.spark.SparkApp;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class App extends SparkApp {

  public static void main(String[] args) {
    final App app = new App();
    Properties properties = new Properties(app.config);
    ExecutorWrapper executorWrapper = ExecutorWrapperFactory.create(2);
    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
    StunServlet stunServlet = new StunServlet(app.datasource, properties, executorWrapper);
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
