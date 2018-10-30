package com.github.freeacs.stun;

import com.github.freeacs.common.quartz.QuartzWrapper;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import de.javawi.jstun.test.demo.StabilityLogger;
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
  private final QuartzWrapper quartzWrapper;

  public StunServlet(DataSource mainDs, Properties properties, QuartzWrapper quartzWrapper) {
    this.mainDs = mainDs;
    this.properties = properties;
    this.quartzWrapper = quartzWrapper;
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

      StabilityLogger stabilityLogger = new StabilityLogger("StabilityLogger STUN");
      quartzWrapper.scheduleCron(
          stabilityLogger.getTaskName(),
          "Syslog",
          "0 * * ? * * *",
          (tms) -> {
            stabilityLogger.setThisLaunchTms(tms);
            stabilityLogger.run();
          });

      ActiveDeviceDetection activeDeviceDetection =
          new ActiveDeviceDetection(mainDs, dbi, "ActiveDeviceDetection");
      quartzWrapper.scheduleCron(
          activeDeviceDetection.getTaskName(),
          "Syslog",
          "15 * * ? * * *",
          (tms) -> {
            activeDeviceDetection.setThisLaunchTms(tms);
            activeDeviceDetection.run();
          });

      SingleKickThread singleKickThread = new SingleKickThread(mainDs, dbi, properties);
      quartzWrapper.scheduleOnce("Scheduler STUN", "Syslog", (tms) -> singleKickThread.run());

      JobKickThread jobKickThread = new JobKickThread(mainDs, dbi, properties);
      quartzWrapper.scheduleOnce("STUN Job Kick Thread", "Syslog", (tms) -> jobKickThread.run());

    } catch (Throwable t) {
      logger.error("An error occurred while starting Stun Server", t);
    }
  }
}
