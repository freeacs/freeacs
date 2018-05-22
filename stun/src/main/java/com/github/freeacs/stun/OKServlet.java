package com.github.freeacs.stun;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class OKServlet extends HttpServlet {

	private static final long serialVersionUID = -3217484543967391741L;

	private static Throwable startupError = null;
	private static Throwable stunServerError = null;
	private static Throwable singleKickError = null;
	private static Throwable jobKickError = null;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		PrintWriter out = res.getWriter();
		String status = "FREEACSOK " + StunServlet.VERSION;
		if (startupError != null) {
			status = "ERROR: Server did not start properly :" + startupError + "<br>\n";
			for (StackTraceElement ste : startupError.getStackTrace())
				status += ste.toString() + "<br>";
		}
		else if (stunServerError != null) {
			status = "ERROR: STUN Server experienced an error :" + stunServerError + "<br>\n";
			for (StackTraceElement ste : stunServerError.getStackTrace())
				status += ste.toString() + "<br>";
		}
		else if (singleKickError != null) {
			status = "ERROR: SingleKickThread experienced an error :" + singleKickError + "<br>\n";
			for (StackTraceElement ste : singleKickError.getStackTrace())
				status += ste.toString() + "<br>";
		}
		else if (jobKickError != null) {
			status = "ERROR: JobKickThread experienced an error :" + jobKickError + "<br>\n";
			for (StackTraceElement ste : jobKickError.getStackTrace())
				status += ste.toString() + "<br>";
		}
		res.setContentType("text/html");
		out.println(status);
		out.close();
	}

	public static void setStartupError(Throwable startupError) {
		OKServlet.startupError = startupError;
	}

	public static void setStunServerError(Throwable stunServerError) {
		OKServlet.stunServerError = stunServerError;
	}

	public static void setSingleKickError(Throwable singleKickError) {
		OKServlet.singleKickError = singleKickError;
	}

	public static void setJobKickError(Throwable jobKickError) {
		OKServlet.jobKickError = jobKickError;
	}
}
