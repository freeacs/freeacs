package com.github.freeacs.web.app;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.web.app.util.SessionCache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * Responds to incoming requests from xAPS Monitor Server.
 * 
 * Just a (very) simple servlet that returns XAPSOK and the version number.
 * 
 * @author Jarl Andre Hubenthal
 */
@SuppressWarnings("serial")
public class Monitor extends HttpServlet {

	private static Throwable lastDBILogin = null;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		PrintWriter out = res.getWriter();
		Cache cache = SessionCache.getCache();
		Hashtable<Object, CacheValue> map = cache.getMap();
		String status = "XAPSOK " + Main.version;
		for (Object o : map.keySet()) {
			if (o instanceof String) {
				String s = (String) o;
				if (s.endsWith("dbi")) {
					// We've found a DBI-object in the cache
					DBI dbi = (DBI) map.get(o).getObject();
					if (dbi != null && dbi.getDbiThrowable() != null) {
						status = "ERROR: DBI reported error:\n" + dbi.getDbiThrowable() + "\n";
						for (StackTraceElement ste : dbi.getDbiThrowable().getStackTrace())
							status += ste.toString();
					}
				}
			}
		}
		if (lastDBILogin != null) {
			status = "ERROR: DBI reported error:\n" + lastDBILogin + "\n";
			for (StackTraceElement ste : lastDBILogin.getStackTrace())
				status += ste.toString();
		}
		out.println(status);
		out.close();
	}

	public static void setLastDBILogin(Throwable lastDBILogin) {
		Monitor.lastDBILogin = lastDBILogin;
	}
}
