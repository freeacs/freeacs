package com.github.freeacs.stun;

import com.github.freeacs.common.scheduler.Schedule;
import com.github.freeacs.common.scheduler.ScheduleType;
import com.github.freeacs.common.scheduler.Scheduler;
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

  public static StunServer server = null;

  private static Logger logger = LoggerFactory.getLogger(StunServlet.class);
  private final DataSource xapsCp;
  private final DataSource sysCp;
  private final Properties properties;

  public StunServlet(DataSource xapsCp, DataSource sysCp, Properties properties) {
    this.xapsCp = xapsCp;
    this.sysCp = sysCp;
    this.properties = properties;
  }

  public static void destroy() {
    Sleep.terminateApplication();
    server.shutdown();
  }

  public void init() {
    trigger();
  }

  private static DBI initializeDBI(DataSource xapsCp, DataSource sysCp) throws SQLException {
    Users users = new Users(xapsCp);
    User user = users.getUnprotected(Users.USER_ADMIN);
    Identity id = new Identity(SyslogConstants.FACILITY_STUN, "N/A", user);
    Syslog syslog = new Syslog(sysCp, id);
    return new DBI(Integer.MAX_VALUE, xapsCp, syslog);
  }

  private synchronized void trigger() {
    try {
      DBI dbi = initializeDBI(xapsCp, sysCp);

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

      Scheduler scheduler = new Scheduler();
      scheduler.registerTask(
          new Schedule(
              60000, true, ScheduleType.INTERVAL, new StabilityLogger("StabilityLogger STUN")));

      scheduler.registerTask(
          new Schedule(
              60000,
              true,
              ScheduleType.INTERVAL,
              new ActiveDeviceDetection(xapsCp, dbi, "ActiveDeviceDetection")));
      Thread schedulerThread = new Thread(scheduler);
      schedulerThread.setName("Scheduler STUN");
      schedulerThread.start();

      logger.info("Starting Job Kick Thread");
      Thread kickThread = new Thread(new JobKickThread(xapsCp, dbi, properties));
      kickThread.setName("STUN Job Kick Thread");
      kickThread.start();

    } catch (Throwable t) {
      OKServlet.setStartupError(t);
      logger.error("An error occurred while starting Stun Server", t);
    }
  }
}
