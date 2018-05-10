package com.owera.xaps.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.owera.common.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.log.Log;
import com.owera.common.log.Logger;
import com.owera.common.scheduler.Schedule;
import com.owera.common.scheduler.ScheduleType;
import com.owera.common.scheduler.Scheduler;
import com.owera.common.scheduler.ShowScheduleQueue;
import com.owera.common.util.Sleep;
import com.owera.xaps.monitor.task.ModuleMonitorTask;
import com.owera.xaps.monitor.task.MonitorHeartbeatTask;
import com.owera.xaps.monitor.task.MonitorInfo;
import com.owera.xaps.monitor.task.SendEmailTask;
import com.owera.xaps.monitor.task.TriggerNotificationHourly;
import com.owera.xaps.monitor.task.TriggerNotificationSecondly;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import static com.owera.xaps.monitor.Properties.*;

/**
 * Servlet implementation class Welcome
 */
public class MonitorServlet extends HttpServlet {

	public static String VERSION = "1.3.9";

	private static final long serialVersionUID = 3051630277238752841L;
	private static Scheduler scheduler = null;

	private ServletContext context;
	private Configuration config;

	static {
		Log.initialize("xaps-monitor-logs.properties");
		ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();
		Protocol https = new Protocol("https", socketFactory, 443);
		Protocol.registerProtocol("https", https);
	}

	private static Logger log = new Logger();

	@Override
	public void init(ServletConfig serlvetConfig) throws ServletException {
		try {
			context = serlvetConfig.getServletContext();
			config = new Freemarker().initFreemarker();

			scheduler = new Scheduler();
			Thread t = new Thread(scheduler);
			t.setName("Monitor (Scheduler)");
			t.start();
			log.info("Scheduler started");

			// Run at every 5 minute - light task - run module monitoring (check OK-servlet response for all modules)
			scheduler.registerTask(new Schedule(60000, false, ScheduleType.INTERVAL, new ModuleMonitorTask("ModuleMonitorTask")));
			// Run at every 60 sec  -  light task - will monitor the status of all the HttpMonitorTasks and send email if status has changed
			scheduler.registerTask(new Schedule(60000, false, ScheduleType.INTERVAL, new SendEmailTask("SendEmailTask")));
			// Run every morning at 0700 - light task - will send email if monitor-server is up
			scheduler.registerTask(new Schedule(7 * 60 * 60000, false, ScheduleType.DAILY, new MonitorHeartbeatTask("MonitorHeartbeatTask")));

			ConnectionProperties connProps = ConnectionProvider.getConnectionProperties(getUrl("xaps"), getMaxAge("xaps"), getMaxConn("xaps"));
			// Run every second - very light usually - check if there's a trigger release message (and process)
			scheduler.registerTask(new Schedule(60000, false, ScheduleType.INTERVAL, new TriggerNotificationSecondly("TriggerNotificationSecondly", connProps)));
			// Run every hour - very light usually - check if there's a trigger release we've missed 
			scheduler.registerTask(new Schedule(60 * 60000, false, ScheduleType.INTERVAL, new TriggerNotificationHourly("TriggerNotificationHourly", connProps)));

			// Run at 59 every hour - very light task
			scheduler.registerTask(new Schedule(60000, false, ScheduleType.HOURLY, new ShowScheduleQueue("ShowScheduleQueue", scheduler)));

		} catch (Exception ex) {
			log.fatal("Error while initializing Monitor: " + ex.getLocalizedMessage(), ex);
			throw new ServletException(ex);
		}
	}

	@Override
	public void destroy() {
		Sleep.terminateApplication();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		Template page = config.getTemplate("main.ftl");
		Map<String, Object> rootMap = new HashMap<String, Object>();
		String async = request.getParameter("async");
		if (async != null)
			rootMap.put("async", async);
		rootMap.put("version", VERSION);
		List<MonitorInfo> events = new ArrayList<MonitorInfo>();
		for (MonitorInfo mi : ModuleMonitorTask.getMonitorInfoSet()) {
			if (mi.getModule().equals("monitor"))
				continue;
			events.add(mi);
		}
		rootMap.put("events", events);
		try {
			String embedded = request.getParameter("html");
			if (embedded != null && embedded.equalsIgnoreCase("no"))
				page.process(rootMap, out);
			else {
				Template index = config.getTemplate("index.ftl");
				rootMap.put("main", "main.ftl");
				index.process(rootMap, out);
			}
		} catch (TemplateException e) {
			log.error("A template exception occured: " + e.getLocalizedMessage(), e);
			out.println(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
}
