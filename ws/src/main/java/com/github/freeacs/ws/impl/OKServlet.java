package com.github.freeacs.ws.impl;

import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.ws.netadmin.MessageListener;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;

public class OKServlet extends HttpServlet {

	private static final long serialVersionUID = -3217484543967391741L;
	
	private static Throwable error = null;

	private final DataSource xapsDs, syslogDs;

	public OKServlet(DataSource xapsDs, DataSource syslogDs) {
		this.xapsDs = xapsDs;
		this.syslogDs = syslogDs;
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		PrintWriter out = res.getWriter();
		String status = "XAPSOK " + ACSWS.VERSION;
		if (error != null) {
			status = "ERROR: WS Server experienced an error :" + error + "<br>\n";
			for (StackTraceElement ste : error.getStackTrace())
				status += ste.toString() + "<br>";
		}
		res.setContentType("text/html");
		out.println(status);
		out.close();
	}

	public void init() {
		Thread t = new Thread(new MessageListener(xapsDs, syslogDs));
		t.setName("XAPSWS - MessageListener");
		t.start();
	}
	
	public void destroy() {
		Sleep.terminateApplication();
	}

	public static void setError(Throwable error) {
		OKServlet.error = error;
	}

}
