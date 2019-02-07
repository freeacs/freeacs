package com.github.freeacs.stun;

import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import de.javawi.jstun.test.demo.StunServer;
import java.net.InetAddress;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StunServlet {
  public static StunServer server;

  private static Logger logger = LoggerFactory.getLogger(StunServlet.class);

  private final DataSource mainDs;
  private final Properties properties;
  private final ExecutorWrapper executorWrapper;

  public StunServlet(DataSource mainDs, Properties properties, ExecutorWrapper executorWrapper) {
    this.mainDs = mainDs;
    this.properties = properties;
    this.executorWrapper = executorWrapper;
  }

  public static void destroy() {
    Sleep.terminateApplication();
    server.shutdown();
  }

  public void init() {
    trigger();
  }

  private static DBI initializeDBI(DataSource mainDs) throws SQLException {
    Users users = new Users(mainDs);
    User user = users.getUnprotected(Users.USER_ADMIN);
    Identity id = new Identity(SyslogConstants.FACILITY_STUN, "latest", user);
    Syslog syslog = new Syslog(mainDs, id);
    return new DBI(Integer.MAX_VALUE, mainDs, syslog);
  }

  private synchronized void trigger() {
    try {
      DBI dbi = initializeDBI(mainDs);

      if (properties.isRunWithStun()) {
        if (server == null) {
          int pPort = properties.getPrimaryPort();
          String pIp = properties.getPrimaryIp();
          int sPort = properties.getSecondaryPort();
          String sIp = properties.getSecondaryIp();
          server =
              new StunServer(pPort, InetAddress.getByName(pIp), sPort, InetAddress.getByName(sIp));
        }
        if (!StunServer.isStarted()) {
          logger.info("Server startup...");
          server.start();
        }
      }

      ActiveDeviceDetection activeDeviceDetection =
          new ActiveDeviceDetection(mainDs, dbi, "ActiveDeviceDetection");
      executorWrapper.scheduleCron(
          "15 * * ? * * *",
          (tms) ->
              () -> {
                activeDeviceDetection.setThisLaunchTms(tms);
                activeDeviceDetection.run();
              });

      Thread singleKickThread = new Thread(new SingleKickThread(mainDs, dbi, properties));
      singleKickThread.setName("Scheduler STUN");
      singleKickThread.start();

      Thread jobKickThread = new Thread(new JobKickThread(mainDs, dbi, properties));
      jobKickThread.setName("STUN Job Kick Thread");
      jobKickThread.start();

    } catch (Throwable t) {
      logger.error("An error occurred while starting Stun Server", t);
    }
  }
}
