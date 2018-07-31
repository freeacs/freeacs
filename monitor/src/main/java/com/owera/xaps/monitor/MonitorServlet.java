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

import com.github.freeacs.common.scheduler.Schedule;
import com.github.freeacs.common.scheduler.ScheduleType;
import com.github.freeacs.common.scheduler.Scheduler;
import com.github.freeacs.common.util.Sleep;
import com.owera.xaps.monitor.https.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import com.owera.xaps.monitor.task.ModuleMonitorTask;
import com.owera.xaps.monitor.task.MonitorInfo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class Welcome
 */
public class MonitorServlet extends HttpServlet {

	private static String VERSION = "1.3.9";

	private static final long serialVersionUID = 3051630277238752841L;

	private final Properties properties;

	private Configuration config;

	static {
		ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();
		Protocol https = new Protocol("https", socketFactory, 443);
		Protocol.registerProtocol("https", https);
	}

	private static Logger log = LoggerFactory.getLogger(MonitorServlet.class);

	public MonitorServlet(Properties properties) {
		this.properties = properties;
	}

	@Override
	public void init(ServletConfig serlvetConfig) throws ServletException {
		try {
			ServletContext context = serlvetConfig.getServletContext();
			config = new Freemarker().initFreemarker(context);

			Scheduler scheduler = new Scheduler();
			Thread t = new Thread(scheduler);
			t.setName("Monitor (Scheduler)");
			t.start();
			log.info("Scheduler started");

			// Run at every 5 minute - light task - run module monitoring (check OK-servlet response for all modules)
			scheduler.registerTask(new Schedule(60000, false, ScheduleType.INTERVAL, new ModuleMonitorTask("ModuleMonitorTask", properties)));
		} catch (Exception ex) {
			log.error("Error while initializing Monitor: " + ex.getLocalizedMessage(), ex);
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
