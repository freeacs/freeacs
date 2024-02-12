package com.github.freeacs.core;

import static spark.Spark.get;

import com.github.freeacs.cache.HazelcastConfig;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.common.spark.SparkApp;
import com.github.freeacs.dbi.*;
import com.hazelcast.core.HazelcastInstance;

import java.sql.SQLException;

public class App extends SparkApp {

  public static void main(String[] args) throws SQLException {
    final App app = new App();
    Properties properties = new Properties(app.config);
    ExecutorWrapper executorwrapper = ExecutorWrapperFactory.create(10);
    HazelcastInstance hazelcastInstance = HazelcastConfig.getHazelcastInstance();
    Syslog syslog = getSyslog(app);
    CoreServlet coreServlet = new CoreServlet(app.datasource, syslog, properties, executorwrapper);
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

    private static Syslog getSyslog(App app) throws SQLException {
        final Users users = new Users(app.datasource);
        final User adminUser = users.getUnprotected(Users.USER_ADMIN);
        final Identity id = new Identity(SyslogConstants.FACILITY_CORE, "latest", adminUser);
        return new Syslog(app.datasource, id);
    }
}
