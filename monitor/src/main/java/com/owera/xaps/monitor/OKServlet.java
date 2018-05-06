package com.owera.xaps.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.owera.common.log.Logger;
import com.owera.xaps.monitor.task.ModuleMonitorTask;
import com.owera.xaps.monitor.task.MonitorInfo;

/**
 * Servlet implementation class Welcome
 */
public class OKServlet extends HttpServlet {

	private static final long serialVersionUID = 3084245407652408884L;

	private Logger logger = new Logger();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		try {
			String overallStatus = "XAPSOK " + MonitorServlet.VERSION;
			List<String> statusList = new ArrayList<String>();
			for (MonitorInfo mi : ModuleMonitorTask.getMonitorInfoSet()) {
				if (mi.getStatus() != null && !mi.getStatus().equals("OK") &&
						mi.getUrl() != null && mi.getUrl().indexOf("xapsmonitor") == -1) {
					overallStatus = "ERROR";
					statusList.add("Module " + mi.getModule() + " tested using <a href=\"" + mi.getUrl() + "\">" + mi.getUrl() + "</a>, error: " + mi.getErrorMessage() + "<br>\n");
				}
			}
			for (String status : statusList) {
				out.println(status);
				logger.debug("Monitoring: OKServlet: " + status); 
			}
			if (overallStatus.contains("ERROR"))
				out.println("<p><b>ADVICE:</b> Restart Tomcat unless you explictely know that MySQL is working hard and some querys are blocked/interrupted/delayed - in that case you may wait 15-20 min");
			else
				out.println(overallStatus);
		} catch (Throwable t) {
			out.println("ERROR: Not possible to run OK-servlet:" + t.getMessage());
			logger.error("Monitoring: OKServlet: An error occurred: " + t.getMessage(), t);
		}
		
	}
}
