package com.owera.xaps.syslogserver;

import com.owera.common.scheduler.Schedule;
import com.owera.common.scheduler.ScheduleType;
import com.owera.common.scheduler.Scheduler;
import com.owera.common.util.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class SyslogServlet extends HttpServlet {

	private static final long serialVersionUID = 3972885964801548360L;

	public static SyslogServer server = null;

	public static String version = "1.4.32";

	private static Logger logger = LoggerFactory.getLogger(SyslogServlet.class);

	public void destroy() {
		logger.info("Server shutdown...");
		Sleep.terminateApplication();
	}

	public void init() {
		if (server == null)
			server = new SyslogServer();
		if (!SyslogServer.isStarted()) {
			logger.info("Server startup...");
			Thread serverThread = new Thread(server);
			serverThread.setName("Receive-thread");
			serverThread.start();
		}

		Scheduler scheduler = new Scheduler();

		scheduler.registerTask(new Schedule(60000, true, ScheduleType.INTERVAL, new SummaryLogger("SummaryLogger")));
		scheduler.registerTask(new Schedule(60000, true, ScheduleType.INTERVAL, new StateLogger("StateLogger")));
		scheduler.registerTask(new Schedule(60000, false, ScheduleType.INTERVAL, new DiskSpaceCheck("DiskSpaceCheck")));
		
		Thread t = new Thread(scheduler);
		t.setName("Syslog (Scheduler)");
		t.start();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		PrintWriter out = res.getWriter();
		String status = "";
		if (!FailoverFileReader.isOk()) {
			status = FailoverFileReader.getThrowable() + "\n";
			for (StackTraceElement ste : FailoverFileReader.getThrowable().getStackTrace())
				status += ste.toString() + "\n\n";
			status += "\n";
		}

		if (!Syslog2DB.isOk()) {
			status += Syslog2DB.getThrowable() + "\n";
			for (StackTraceElement ste : Syslog2DB.getThrowable().getStackTrace())
				status += ste.toString() + "\n";
			status += "\n";
		}
		if (!SyslogServer.isOk()) {
			status += SyslogServer.getThrowable() + "\n";
			for (StackTraceElement ste : SyslogServer.getThrowable().getStackTrace())
				status += ste.toString() + "\n";
			status += "\n";
		}
		if (status.equals(""))
			status = "XAPSOK " + version;
		out.println(status);
		out.close();
	}
}
