package com.owera.xaps.core;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.owera.common.db.ConnectionProvider;
import com.owera.common.log.Logger;
import com.owera.common.scheduler.Schedule;
import com.owera.common.scheduler.ScheduleType;
import com.owera.common.scheduler.Scheduler;
import com.owera.common.scheduler.ShowScheduleQueue;
import com.owera.common.util.Sleep;
import com.owera.xaps.core.task.DeleteOldJobs;
import com.owera.xaps.core.task.DeleteOldScripts;
import com.owera.xaps.core.task.DeleteOldSyslog;
import com.owera.xaps.core.task.HeartbeatDetection;
import com.owera.xaps.core.task.JobRuleEnforcer;
import com.owera.xaps.core.task.ReportGenerator;
import com.owera.xaps.core.task.ScriptExecutor;
import com.owera.xaps.core.task.TriggerReleaser;
import com.owera.xaps.dbi.util.XAPSVersionCheck;

public class CoreServlet extends HttpServlet {

	private static final long serialVersionUID = -3217484543967391741L;

	public static String version = "1.5.44";

	private static Scheduler scheduler = null;

	static {
		com.owera.common.log.Log.initialize("xaps-core-logs.properties");
	}

	public static Logger log = new Logger();

	public void destroy() {
		log.notice("Server shutdown...");
		Sleep.terminateApplication();
	}

	public void init() {
		try {
			log.notice("Server starts...");
			XAPSVersionCheck.versionCheck(ConnectionProvider.getConnectionProperties("xaps-core.properties", "db.xaps"));
			scheduler = new Scheduler();
			Thread t = new Thread(scheduler);
			t.setName("Core (Scheduler)");
			t.start();

			// only to test: scheduler.registerTask(new Schedule(60 * 60000, false, ScheduleType.INTERVAL, new ReportGenerator("ReportGeneratorHourly", ScheduleType.INTERVAL)));
			//Run at 00 every hour - heavy task
			scheduler.registerTask(new Schedule(0, false, ScheduleType.HOURLY, new ReportGenerator("ReportGeneratorHourly", ScheduleType.HOURLY)));
			// Run at 0015 every night - very heavy task
			scheduler.registerTask(new Schedule(15 * 60000, false, ScheduleType.DAILY, new ReportGenerator("ReportGeneratorDaily", ScheduleType.DAILY)));
			// Run at 0500 every night - very heavy task
			scheduler.registerTask(new Schedule(5 * 60 * 60000, false, ScheduleType.DAILY, new DeleteOldSyslog("DeleteOldSyslogEntries")));
			// Run at 0530 every night - light task
			scheduler.registerTask(new Schedule((5 * 60 + 30) * 60000, false, ScheduleType.DAILY, new DeleteOldJobs("DeleteOldJobs")));
			

			// Run every second - light task
			scheduler.registerTask(new Schedule(1000, false, ScheduleType.INTERVAL, new JobRuleEnforcer("JobRuleEnforcer")));
			if (XAPSVersionCheck.triggerSupported) {
				// Run at 30(sec) every minute - light task 
				scheduler.registerTask(new Schedule(30000, false, ScheduleType.MINUTELY, new TriggerReleaser("TriggerReleaser")));
			}
			if (XAPSVersionCheck.scriptExecutionSupported) {
				// Run every 100 ms - very light task
				scheduler.registerTask(new Schedule(100, false, ScheduleType.INTERVAL, new ScriptExecutor("ScriptExecutor")));
				// Run at 45 every hour - light task
				scheduler.registerTask(new Schedule(45 * 1000, false, ScheduleType.MINUTELY, new DeleteOldScripts("DeleteOldScripts")));
			}
			if (XAPSVersionCheck.heartbeatSupported) {
				// Run every 5 minute - moderate task
				scheduler.registerTask(new Schedule(5 * 60000, false, ScheduleType.INTERVAL, new HeartbeatDetection("HeartbeatDetection")));
			}
			// Run at 59 every hour - very light task
			scheduler.registerTask(new Schedule(60000, false, ScheduleType.HOURLY, new ShowScheduleQueue("ShowScheduleQueue", scheduler)));

		} catch (Throwable t) {
			log.error("Error occured in init(), some daemons may not have been started", t);
		}

	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		PrintWriter out = res.getWriter();

		if (req.getParameterMap().size() > 0) {
			for (Schedule s : scheduler.getScheduleList().getSchedules()) {
				Throwable t = s.getTask().getThrowable();
				if (t != null) {
					out.println(t + "\n");
					for (StackTraceElement ste : t.getStackTrace())
						out.println(ste.toString());
					s.getTask().setThrowable(null);
					out.close();
					return;
				}
			}
		}
		out.println("XAPSOK " + version);
		out.close();
	}
}
