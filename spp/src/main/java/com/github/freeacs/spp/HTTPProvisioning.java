package com.github.freeacs.spp;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.spp.http.Authenticator;
import com.github.freeacs.spp.telnet.TelnetProvisioning;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvOutput;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvStatus;
import com.github.freeacs.dbi.util.SyslogClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * This is the "main-class" of TR069Provisioning. It receives the HTTP-request
 * from the CPE and returns an HTTP-response. The content of the request/reponse
 * can be both TR-069 request/response.
 * 
 * @author morten
 * 
 */
public class HTTPProvisioning extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String VERSION = "1.5.0";

	private final DBAccess dbAccess;

	public HTTPProvisioning(DBAccess dbAccess) {
		this.dbAccess = dbAccess;
	}

	public void init() {
		Log.notice(HTTPProvisioning.class, "HTTP/TFTP-Server and Telnet Provisioning Controller starts...");
		try {
			// Start the server
			PrintStream ps = new PrintStream(System.out);
			TFTPServer tftpS = new TFTPServer(new File("."), new File("."), Properties.getTFTPPort(), TFTPServer.ServerMode.GET_ONLY, ps, ps, dbAccess);
			tftpS.setSocketTimeout(5000);
		} catch (Throwable t) {
			Log.fatal(HTTPProvisioning.class, "An error occurred - TFTP server did not start (triggered from HTTPProvisioning", t);
		}
		try {
			TelnetProvisioning tpc = new TelnetProvisioning(DBAccess.getXAPSProperties(), dbAccess.getDBI());
			Thread t = new Thread(tpc);
			t.start();
		} catch (Throwable t) {
			Log.fatal(HTTPProvisioning.class, "An error occurred - Telnet Provisioning Controller did not start (triggered from HTTPProvisioning", t);
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		SessionData sessionData = new SessionData();
		try {
			/* HTTP specific */
			String reqURL = req.getRequestURL().toString();
			int cpPos = reqURL.indexOf(req.getContextPath());
			String reqFile = reqURL.substring(cpPos + req.getContextPath().length());
			ProvisioningInput.parseRequestFile(reqFile, sessionData);
			ProvisioningInput.parseRequestHeaders(req, sessionData);
			ProvisioningInput.parseRequestParameters(req, sessionData);
			sessionData.setIpAddress(req.getRemoteHost());
			sessionData.setReqURL(reqURL);
			sessionData.setContextPath(req.getContextPath());

			if (!Authenticator.authenticate(req, res, sessionData)) {
				return;
			}

			/* xAPS specific */
			byte[] output = SPP.provision(sessionData, dbAccess);

			/* HTTP specific */
			if (sessionData.isEncrypted() || sessionData.isBinaries()) {// use encrypted response
				Log.debug(HTTPProvisioning.class, "The output is encrypted or binaries, setting contenttype to application/octet-stream. Length is " + output.length);
				OutputStream out = res.getOutputStream();
				res.setContentType("application/octet-stream");
				res.setContentLength((int) output.length);
				out.write(output);
				out.flush();
				out.close();
				Log.debug(HTTPProvisioning.class, "The stream is sent and closed - provisioning is completed");
			} else {
				PrintWriter pw = res.getWriter();
				res.setContentType(sessionData.getResp().getContentType());
				pw.print(new String(output));
				pw.flush();
			}
		} catch (Throwable t) {
			ProvisioningMessage pm = sessionData.getProvisioningMessage();
			pm.setErrorResponsibility(ErrorResponsibility.SERVER);
			pm.setProvStatus(ProvStatus.ERROR);
			pm.setProvOutput(ProvOutput.EMPTY);
			pm.setErrorMessage("HTTPProvisioning failed fatally:", t);
			pm.setParamsWritten(null);
			pm.setIpAddress(req.getRemoteHost());
			Log.error(HTTPProvisioning.class, "An fatal error occurred in HTTPProvisioning, provisioning continues", t);
		} finally {
			ProvisioningMessage pm = sessionData.getProvisioningMessage();
			pm.setProvProtocol(ProvisioningProtocol.HTTP);
			pm.setIpAddress(req.getRemoteHost());
			SyslogClient.send(pm.syslogMsg(dbAccess.getFacility(), null, Users.USER_ADMIN));
			Log.event(sessionData, pm.logMsg());
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

	public void destroy() {
		Log.info(HTTPProvisioning.class, "Server shutdown...");
		Sleep.terminateApplication();
	}
}
