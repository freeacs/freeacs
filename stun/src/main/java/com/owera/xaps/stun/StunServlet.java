package com.owera.xaps.stun;

import com.github.freeacs.common.db.ConnectionProperties;
import com.github.freeacs.common.db.ConnectionProvider;
import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.common.scheduler.Schedule;
import com.github.freeacs.common.scheduler.ScheduleType;
import com.github.freeacs.common.scheduler.Scheduler;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.*;
import com.owera.xaps.dbi.*;
import de.javawi.jstun.test.demo.StabilityLogger;
import de.javawi.jstun.test.demo.StunServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.SQLException;

import static com.owera.xaps.stun.Properties.getMaxAge;
import static com.owera.xaps.stun.Properties.getMaxConn;
import static com.owera.xaps.stun.Properties.getUrl;

public class StunServlet extends HttpServlet {

	public static String VERSION = "1.3.23";

	private static final long serialVersionUID = 3972885964801548360L;

	public static StunServer server = null;

	private static Logger logger = LoggerFactory.getLogger(StunServlet.class);

	public void destroy() {
		Sleep.terminateApplication();
		server.shutdown();
	}

	public void init() {
		trigger();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		PrintWriter out = res.getWriter();
		out.println("Fusion STUN Server v" + VERSION);
		out.close();
	}

	public static DBI initializeDBI(ConnectionProperties xapsCp) throws SQLException, NoAvailableConnectionException {
		Users users = new Users(xapsCp);
		User user = users.getUnprotected(Users.USER_ADMIN);
		Identity id = new Identity(SyslogConstants.FACILITY_STUN, StunServlet.VERSION, user);
		ConnectionProperties sysCp = ConnectionProvider.getConnectionProperties(getUrl("syskog"), getMaxAge("syslog"), getMaxConn("syslog"));
		Syslog syslog = new Syslog(sysCp, id);
		return new DBI(Integer.MAX_VALUE, xapsCp, syslog);
	}

	private static synchronized void trigger() {
		try {
			if (Properties.runWithStun()) {
				if (server == null) {
					int pPort = Properties.getPrimaryPort();
					String pIp = Properties.getPrimaryIp();
					int sPort = Properties.getSecondayPort();
					String sIp = Properties.getSecondaryIp();
					server = new StunServer(pPort, InetAddress.getByName(pIp), sPort, InetAddress.getByName(sIp));
				}
				if (!StunServer.isStarted()) {
					logger.info("Server startup...");
					server.start();
				}
			}

			ConnectionProperties xapsCp = ConnectionProvider.getConnectionProperties(getUrl("xaps"), getMaxAge("xaps"), getMaxConn("xaps"));
			DBI dbi = initializeDBI(xapsCp);

			Scheduler scheduler = new Scheduler();
			scheduler.registerTask(new Schedule(60000, true, ScheduleType.INTERVAL, new StabilityLogger("StabilityLogger STUN")));

			scheduler.registerTask(new Schedule(60000, true, ScheduleType.INTERVAL, new ActiveDeviceDetection(xapsCp, dbi, "ActiveDeviceDetection")));
			Thread schedulerThread = new Thread(scheduler);
			schedulerThread.setName("Scheduler STUN");
			schedulerThread.start();

			logger.info("Starting Single Kick Thread");
			Thread kickThread = new Thread(new SingleKickThread(xapsCp, dbi));
			kickThread.setName("STUN Single Kick Thread");
			kickThread.start();

			logger.info("Starting Job Kick Thread");
			kickThread = new Thread(new JobKickThread(xapsCp, dbi));
			kickThread.setName("STUN Job Kick Thread");
			kickThread.start();

		} catch (Throwable t) {
			OKServlet.setStartupError(t);
			logger.error("An error occurred while starting Stun Server", t);
		}

	}
}
