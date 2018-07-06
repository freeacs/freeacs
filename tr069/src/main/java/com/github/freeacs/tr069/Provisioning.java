package com.github.freeacs.tr069;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.Authenticator;
import com.github.freeacs.base.http.ThreadCounter;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.ScriptExecutions;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.tr069.background.BackgroundProcesses;
import com.github.freeacs.tr069.background.ScheduledKickTask;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.methods.DecisionMaker;
import com.github.freeacs.tr069.methods.HTTPRequestProcessor;
import com.github.freeacs.tr069.methods.HTTPResponseCreator;
import com.github.freeacs.tr069.methods.TR069Method;
import com.github.freeacs.tr069.test.system1.TestDatabase;
import com.github.freeacs.tr069.test.system1.TestDatabaseObject;
import com.github.freeacs.tr069.test.system2.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;


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

	private static ScriptExecutions executions;
	private final DBAccess dbAccess;
	private final TR069Method tr069Method;
	private final Properties properties;

	public Provisioning(DBAccess dbAccess, TR069Method tr069Method, Properties properties) {
		this.dbAccess = dbAccess;
		this.tr069Method = tr069Method;
		this.properties = properties;
	}

	public void init() {
		Log.notice(Provisioning.class, "Server starts...");
		try {
			BackgroundProcesses.initiate(dbAccess.getDBI());
		} catch (Throwable t) {
			Log.fatal(Provisioning.class, "Couldn't start BackgroundProcesses correctly ", t);
		}
		try {
			executions = new ScriptExecutions(dbAccess.getMainDataSource());
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
				requestSB.append(line).append("\n");
			}
			reqRes.getRequest().setXml(requestSB.toString());
			return System.currentTimeMillis() - tms;
		} catch (IOException e) {
			throw new TR069Exception("TR-069 client aborted (not possible to read more input)", TR069ExceptionShortMessage.IOABORTED, e);
		}
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
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		HTTPReqResData reqRes = null;
		try {
			// Create the main object which contains all objects concerning the entire
			// session. This object also contains the SessionData object 
			reqRes = new HTTPReqResData(req, res, dbAccess);
			// 2. Authenticate the client (first issue challenge, then authenticate)
			if (!Authenticator.authenticate(reqRes))
				return;
			// 3. Do not continue if concurrent sessions from the same unit is on going
			if (reqRes.getSessionData() != null && !ThreadCounter.isRequestAllowed(reqRes.getSessionData()))
				return;

			// 4. Read the request from the client - store in reqRes object
			extractRequest(reqRes);
			// 5.Process request (parsing xml/data)
			HTTPRequestProcessor.processRequest(reqRes, tr069Method.getRequestMap(), properties);
			// 6. Decide next step in TR-069 session (sometimes trivial, sometimes complex)
			DecisionMaker.process(reqRes, tr069Method.getRequestMap());
			// 7. Create TR-069 response
			HTTPResponseCreator.createResponse(reqRes, tr069Method.getResponseMap());
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
				SessionLogging.log(reqRes, tr069Method.getAbbrevMap());
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

	private void writeQueuedUnitParameters(HTTPReqResData reqRes) {
		try {
			Unit unit = reqRes.getSessionData().getUnit();
			if (unit != null) {
				ACS acs = reqRes.getSessionData().getDbAccessSession().getAcs();
				ACSUnit acsUnit = DBAccess.getXAPSUnit(acs);
				acsUnit.addOrChangeQueuedUnitParameters(unit);
			}
		} catch (Throwable t) {
			Log.error(Provisioning.class, "An error occured when writing queued unit parameters to Fusion. May affect provisioning", t);
		}
	}

	private boolean endOfSession(HTTPReqResData reqRes) {
		try {
			SessionData sessionData = reqRes.getSessionData();
			HTTPReqData reqData = reqRes.getRequest();
			HTTPResData resData = reqRes.getResponse();
			if (reqRes.getThrowable() != null)
				return true;
			if (reqData.getMethod() != null && resData != null && resData.getMethod().equals(TR069Method.EMPTY)) {
				boolean terminationQuirk = properties.isTerminationQuirk(sessionData);
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
