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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StunServlet  {

  public static StunServer server = null;

  private static Logger logger = LoggerFactory.getLogger(StunServlet.class);
  private final DataSource xapsCp;
  private final DataSource sysCp;

  public StunServlet(DataSource xapsCp, DataSource sysCp) {
    this.xapsCp = xapsCp;
    this.sysCp = sysCp;
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

      if (Properties.RUN_WITH_STUN) {
        if (server == null) {
          int pPort = Properties.PRIMARY_PORT;
          String pIp = Properties.PRIMARY_IP;
          int sPort = Properties.SECONDARY_PORT;
          String sIp = Properties.SECONDARY_IP;
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
      Thread kickThread = new Thread(new JobKickThread(xapsCp, dbi));
      kickThread.setName("STUN Job Kick Thread");
      kickThread.start();

    } catch (Throwable t) {
      OKServlet.setStartupError(t);
      logger.error("An error occurred while starting Stun Server", t);
    }
  }
}
