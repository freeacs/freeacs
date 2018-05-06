package com.owera.xaps.spp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.owera.common.util.Sleep;
import com.owera.xaps.Properties.Module;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.db.DBAccess;
import com.owera.xaps.dbi.SyslogConstants;
import com.owera.xaps.dbi.Unittype.ProvisioningProtocol;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.dbi.util.ProvisioningMessage;
import com.owera.xaps.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.owera.xaps.dbi.util.ProvisioningMessage.ProvOutput;
import com.owera.xaps.dbi.util.ProvisioningMessage.ProvStatus;
import com.owera.xaps.dbi.util.SyslogClient;
import com.owera.xaps.spp.TFTPServer.ServerMode;
import com.owera.xaps.spp.telnet.TelnetProvisioning;

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

	static {
		DBAccess.init(Module.SPP, SyslogConstants.FACILITY_SPP, VERSION);
		com.owera.common.log.Log.initialize("xaps-spp-logs.properties");
		Log.notice(HTTPProvisioning.class, "HTTP/TFTP-Server and Telnet Provisioning Controller starts...");
		try {
			// Start the server
			PrintStream ps = new PrintStream(System.out);
			TFTPServer tftpS = new TFTPServer(new File("."), new File("."), Properties.getTFTPPort(), ServerMode.GET_ONLY, ps, ps);
			tftpS.setSocketTimeout(5000);
		} catch (Throwable t) {
			Log.fatal(HTTPProvisioning.class, "An error occurred - TFTP server did not start (triggered from HTTPProvisioning", t);
		}
		try {
			TelnetProvisioning tpc = new TelnetProvisioning(DBAccess.getXAPSProperties(), DBAccess.getDBI());
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

			if (!com.owera.xaps.spp.http.Authenticator.authenticate(req, res, sessionData)) {
				return;
			}

			/* xAPS specific */
			byte[] output = SPP.provision(sessionData);

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
			SyslogClient.send(pm.syslogMsg(DBAccess.getFacility(), null, Users.USER_ADMIN));
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
