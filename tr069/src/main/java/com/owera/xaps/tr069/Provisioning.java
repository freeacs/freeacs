package com.owera.xaps.tr069;

import com.owera.common.db.ConnectionMetaData;
import com.owera.common.db.ConnectionPoolData;
import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.util.Sleep;
import com.owera.xaps.Properties.Module;
import com.owera.xaps.base.BaseCache;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.db.DBAccess;
import com.owera.xaps.base.http.Authenticator;
import com.owera.xaps.base.http.ThreadCounter;
import com.owera.xaps.dbi.*;
import com.owera.xaps.tr069.background.BackgroundProcesses;
import com.owera.xaps.tr069.background.ScheduledKickTask;
import com.owera.xaps.tr069.exception.TR069Exception;
import com.owera.xaps.tr069.exception.TR069ExceptionShortMessage;
import com.owera.xaps.tr069.methods.DecisionMaker;
import com.owera.xaps.tr069.methods.HTTPRequestProcessor;
import com.owera.xaps.tr069.methods.HTTPResponseCreator;
import com.owera.xaps.tr069.methods.TR069Method;
import com.owera.xaps.tr069.test.system1.TestDatabase;
import com.owera.xaps.tr069.test.system1.TestDatabaseObject;
import com.owera.xaps.tr069.test.system2.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import static com.owera.xaps.tr069.Properties.*;

/**
 * This is the "main-class" of TR069 Provisioning. It receives the HTTP-request
 * from the CPE and returns an HTTP-response. The content of the request/reponse
 * can be both TR-069 request/response.
 * 
 * @author morten
 * 
 */
public class Provisioning extends HttpServlet {

	private static final long serialVersionUID = -3020450686422484143L;

	public static final String VERSION = "3.1.2";

//	private static BackgroundProcesses backgroundProcesses = new BackgroundProcesses();

	private static ScriptExecutions executions;

	/**
	 * Starts background processes, initializes logging system
	 */
	static {
		DBAccess.init(Module.TR069, SyslogConstants.FACILITY_TR069, VERSION);
		Log.notice(Provisioning.class, "Server starts...");
		try {
			BackgroundProcesses.initiate(DBAccess.getDBI());
		} catch (Throwable t) {
			Log.fatal(Provisioning.class, "Couldn't start BackgroundProcesses correctly ", t);
		}
		try {
			executions = new ScriptExecutions(DBAccess.getXAPSProperties());
		} catch (Throwable t) {
			Log.fatal(Provisioning.class, "Couldn't initialize ScriptExecutions - not possible to run SHELL-jobs", t);
		}
	}

	/**
	 * doGet prints some information about the server, focus on database connections and memory usage
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getParameter("clearCache") != null)
			BaseCache.clearCache();
		PrintWriter pw = res.getWriter();
		String html = "";
		html += "<title>xAPS TR-069 Server Monitoring Page</title>";
		html += "<h1>Monitoring of the TR-069 Server v. " + VERSION + "</h1>";
		ConnectionProperties props = ConnectionProvider.getConnectionProperties(getUrl("xaps"), getMaxAge("xaps"), getMaxConn("xaps"));
		ConnectionPoolData poolData = ConnectionProvider.getConnectionPoolData(props);
		if (poolData != null) {
			html += "<h1>Database connection</h1>\n";
			html += "This server is connected to " + poolData.getProps().getUrl() + " with user " + poolData.getProps().getUser() + "<br>\n";
			ConnectionMetaData metaData = poolData.getMetaData().clone();
			html += "<ul>Accessed   : " + metaData.getAccessed() + "<br>\n";
			html += "Retries        : " + metaData.getRetries() + "<br>\n";
			html += "Denied         : " + metaData.getDenied() + "<br>\n";
			html += "Denied %       : " + metaData.calculateDeniedPercent() + "<br>\n";
			html += "Free           : " + poolData.getFreeConn().size() + "<br>\n";
			html += "Currently used : " + poolData.getUsedConn().size() + "<br>\n";
			html += "Used %         : " + metaData.calculateUsedPercent() + "<br>\n<ul>";
			int[] accessedSim = metaData.getAccessedSim();
			for (int i = 1; i < accessedSim.length; i++) {
				if (accessedSim[i] == 0 && accessedSim[i + 1] == 0 && accessedSim[i + 2] == 0)
					break;
				float percent = ((float) accessedSim[i] / metaData.getAccessed()) * 100f;
				html += String.format("Used " + i + " connection(s) simultaneously: %8.5f", percent);
				html += "% (accessed: " + accessedSim[i] + ")<br>\n";
			}
			html += "</ul>\n</ul><br>\n";
		}
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long used = total - free;
		html += "<h1>Memory Usage</h1>\n";
		html += "The JVM uses " + getBytesFormatted(used) + " of memory. " + getBytesFormatted(free) + " of memory available on the heap.<br>";
		pw.print(html);
	}

	/**
	 * Reads the XML input into a string and store it in the SessionData object
	 * @param reqRes
	 * @return
	 * @throws TR069Exception 
	 * @throws IOException
	 */
	private static long extractRequest(HTTPReqResData reqRes) throws TR069Exception {
		try {
			long tms = System.currentTimeMillis();
			InputStreamReader isr = new InputStreamReader(reqRes.getReq().getInputStream());
			BufferedReader br = new BufferedReader(isr);
			StringBuilder requestSB = new StringBuilder(1000);
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				requestSB.append(line + "\n");
			}
			reqRes.getRequest().setXml(requestSB.toString());
			return System.currentTimeMillis() - tms;
		} catch (IOException e) {
			throw new TR069Exception("TR-069 client aborted (not possible to read more input)", TR069ExceptionShortMessage.IOABORTED, e);
		}
	}

	/**
	 * Some devices may send a CONTINUE header - server always reply "yes" - do continue
	 * @param req
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
  private static boolean hasContinueHeader(HttpServletRequest req, HttpServletResponse res) throws IOException {
		// Support 100 Continue header - always YES - CONTINUE!
		if (req.getHeader("Expect") != null && req.getHeader("Expect").indexOf("100-continue") > -1) {
			res.setStatus(HttpServletResponse.SC_CONTINUE);
			res.getWriter().print("");
			return true;
		}
		return false;
	}

	/**
	 * This is the entry point for TR-069 Clients - everything starts here!!!
	 * 
	 * A TR-069 session consists of many rounds of HTTP request/responses, however
	 * each request/response non-the-less follows a standard pattern:
	 * 
	 * 1. Check special HTTP headers for a "early return" (CONTINUE)
	 * 2. Check authentication - challenge client if necessary. If not authenticated - return
	 * 3. Check concurrent sessions from same unit - if detected: return 
	 * 4. Extract XML from request - store in sessionData object
	 * 5. Process HTTP Request (xml-parsing, find methodname, test-verification)
	 * 6. Decide upon next step - may contain logic that processes the request and decide response
	 * 7. Produce HTTP Response (xml-creation)
	 * 8. Some details about the xml-response like content-type/Empty response
	 * 9. Return response to TR-069 client
	 * 
	 * At the end we have error handling, to make sure that no matter what, we do return
	 * an EMTPY response to the client - to signal end of conversation/TR-069-session.
	 * 
	 * In the finally loop we check if a TR-069 Session is in-fact completed (one way
	 * or the other) and if so, logging is performed. Also, if unit-parameters are queued
	 * up for writing, those will be written now (instead of writing some here and some there
	 * along the entire TR-069 session). 
	 * 
	 * In special cases the server will kick the device to "come back" and continue testing a new test case.
	 * 
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// 1. If HTTP CONTINUE header present, return "yes" and return - should be correct behavior - the client will return 
//    if (hasContinueHeader(req, res))
//      return;
		HTTPReqResData reqRes = null;
		try {
			// Create the main object which contains all objects concerning the entire
			// session. This object also contains the SessionData object 
			reqRes = new HTTPReqResData(req, res);
			// 2. Authenticate the client (first issue challenge, then authenticate)
			if (!Authenticator.authenticate(reqRes))
				return;
			// 3. Do not continue if concurrent sessions from the same unit is on going
			if (reqRes.getSessionData() != null && !ThreadCounter.isRequestAllowed(reqRes.getSessionData()))
				return;

			// 4. Read the request from the client - store in reqRes object
			extractRequest(reqRes);
			// 5.Process request (parsing xml/data)
			HTTPRequestProcessor.processRequest(reqRes);
			// 6. Decide next step in TR-069 session (sometimes trivial, sometimes complex)
			DecisionMaker.process(reqRes);
			// 7. Create TR-069 response
			HTTPResponseCreator.createResponse(reqRes);
			// 8. Set correct headers in response
			if (reqRes.getResponse().getXml() != null && reqRes.getResponse().getXml().length() > 0) {
				res.setHeader("SOAPAction", "");
				res.setContentType("text/xml");
			}
			// 8. No need to send Content-length as it will only be informational for 204 HTTP messages
			if (reqRes.getResponse().getMethod().equals("Empty"))
				res.setStatus(HttpServletResponse.SC_NO_CONTENT);
			// 9. Print response to output
			res.getWriter().print(reqRes.getResponse().getXml());
		} catch (Throwable t) {
			// Make sure we return an EMPTY response to the TR-069 client
			if (t instanceof TR069Exception) {
				TR069Exception tex = (TR069Exception) t;
				Throwable stacktraceThrowable = t;
				if (tex.getCause() != null)
					stacktraceThrowable = tex.getCause();
				if (tex.getShortMsg() == TR069ExceptionShortMessage.MISC || tex.getShortMsg() == TR069ExceptionShortMessage.DATABASE)
					Log.error(Provisioning.class, "An error ocurred: " + t.getMessage(), stacktraceThrowable);
				if (tex.getShortMsg() == TR069ExceptionShortMessage.IOABORTED)
					Log.warn(Provisioning.class, t.getMessage());
				else
					Log.error(Provisioning.class, t.getMessage()); // No stacktrace printed to log
			}
			if (reqRes != null)
				reqRes.setThrowable(t);
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
			res.getWriter().print("");
		} finally {
			// Run at end of every TR-069 session
			if (reqRes != null && endOfSession(reqRes)) {
				Log.debug(Provisioning.class, "End of session is reached, will write queued unit parameters if unit (" + reqRes.getSessionData().getUnit() + ") is not null");
				// Logging of the entire session, both to tr069-event.log and syslog
				if (reqRes.getSessionData().getUnit() != null) {
					//					reqRes.getSessionData().getUnit().toWriteQueue(SystemParameters.PROVISIONING_STATE, ProvisioningState.READY.toString());
					writeQueuedUnitParameters(reqRes);
				}
				SessionLogging.log(reqRes);
				if (Util.testEnabled(reqRes, true))
					initiateNewTestSession(reqRes);
				else if (reqRes.getSessionData().isTestMode()) {
					String row = TestDatabase.database.select(reqRes.getSessionData().getUnitId());
					if (row != null && new TestDatabaseObject(row).getRun().equals("true"))
						initiateNewTestSession(reqRes);
				}
				BaseCache.removeSessionData(reqRes.getSessionData().getUnitId());
				BaseCache.removeSessionData(reqRes.getSessionData().getId());
				res.setHeader("Connection", "close");
			}
		}
		if (reqRes != null && reqRes.getSessionData() != null)
			ThreadCounter.responseDelivered(reqRes.getSessionData());
	}

	private static void initiateNewTestSession(HTTPReqResData reqRes) {
		try {
			List<HTTPReqResData> reqResList = reqRes.getSessionData().getReqResList();
			boolean deviceHasAlreadyBooted = false;
			for (HTTPReqResData rr : reqResList) {
				String method = rr.getResponse().getMethod();
				// No need to kick device if a reboot or reset has been part of the test-flow 
				if (method != null && (method.equals(TR069Method.FACTORY_RESET) || method.equals(TR069Method.REBOOT)))
					deviceHasAlreadyBooted = true;
			}
			if (!deviceHasAlreadyBooted) {
				ScheduledKickTask.addUnit(reqRes.getSessionData().getUnit());
			}
		} catch (Throwable t) {
			Log.warn(Provisioning.class, "Could not initiate kick after completed session in test mode", t);
		}
	}

	private static void writeQueuedUnitParameters(HTTPReqResData reqRes) {
		try {
			Unit unit = reqRes.getSessionData().getUnit();
			if (unit != null) {
				XAPS xaps = reqRes.getSessionData().getDbAccess().getXaps();
				XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
				xapsUnit.addOrChangeQueuedUnitParameters(unit);
			}
		} catch (Throwable t) {
			Log.error(Provisioning.class, "An error occured when writing queued unit parameters to Fusion. May affect provisioning", t);
		}
	}

	private static boolean endOfSession(HTTPReqResData reqRes) {
		try {
			SessionData sessionData = reqRes.getSessionData();
			HTTPReqData reqData = reqRes.getRequest();
			HTTPResData resData = reqRes.getResponse();
			if (reqRes.getThrowable() != null)
				return true;
			if (reqData.getMethod() != null && resData != null && resData.getMethod().equals(TR069Method.EMPTY)) {
				boolean terminationQuirk = Properties.isTerminationQuirk(sessionData);
				if (terminationQuirk && reqData.getMethod().equals(TR069Method.EMPTY))
					return true;
				if (!terminationQuirk)
					return true;
			}
			return false;
		} catch (Throwable t) {
			Log.warn(Provisioning.class, "An error occured when determining endOfSession. Does not affect provisioning", t);
			return false;
		}
	}

	private static String getBytesFormatted(long bytes) {
		if (bytes > 1024 * 1024 * 1024)
			return bytes / (1024 * 1024 * 1024) + " GB";
		else if (bytes > 1024 * 1024)
			return bytes / (1024 * 1024) + " MB";
		else if (bytes > 1024)
			return bytes / (1024) + " KB";
		return bytes + " B";
	}

	public void destroy() {
		Sleep.terminateApplication();
	}

	public static ScriptExecutions getExecutions() {
		return executions;
	}
}
