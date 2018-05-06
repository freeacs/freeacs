package com.owera.tr069client;

import java.net.BindException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.owera.tr069client.messages.Download;
import com.owera.tr069client.messages.Empty;
import com.owera.tr069client.messages.GetParameterNames;
import com.owera.tr069client.messages.GetParameterValues;
import com.owera.tr069client.messages.Inform;
import com.owera.tr069client.messages.Reboot;
import com.owera.tr069client.messages.SetParameterValues;
import com.owera.tr069client.messages.TransferComplete;
import com.owera.tr069client.monitor.Status;

/**
 * A Session is responsible for actually running an entire TR069-session against
 * the provisioning server.
 * 
 * A session looks like this INFORM-REQ EMPTY-REQ
 * 
 * followed by one out of 3 scenarios 1. GET_PARAMETER_VALUES-RES 1.
 * SET_PARAMETER_VALUES-RES
 * 
 * 2. GET_PARAMETER_NAMES-RES 2. GET_PARAMETER_VALUES-RES 2.
 * SET_PARAMETER_VALUES-RES
 * 
 * 3. DOWNLOAD-RES 3. Actually download software 3. TRANSFER_COMPLETE-REQ
 * 
 * 
 * followed by a closing of the session EMPTY-REQ
 * 
 * @author morten
 * 
 */

public class Session implements Runnable {

	private static Logger logger = Logger.getLogger(Session.class);

	private Arguments args;

	private boolean stop = false;

	private boolean stopped = false;

	private boolean notYetStarted = true;

	private Status status;

	private HttpHandler httpHandler;

	private static Random random = new Random();

	public Session(Arguments args) {
		this.args = args.clone();
	}

	private TR069Client runImpl2(TR069Client tr069Client) throws Exception {
		if (logger.isDebugEnabled())
			logger.debug("Conversation starts");
		status.setCurrentOperation(Status.IN);
		String response = Inform.execute(args, httpHandler, status, tr069Client);
		if (response == null || response.trim().equals(""))
			throw new Exception("No data found in xAPS database for this client " + tr069Client.getSerialNumberStr());
		if (tr069Client.getNextConnectType().equals(TR069Client.TYPE_IN_TC_EM)) {
			status.setCurrentOperation(Status.TC);
			TransferComplete.execute(args, httpHandler, status);
		}
		status.setCurrentOperation(Status.EM);
		response = Empty.execute(args, httpHandler, status);
		String method = TR069Method.getMethod(response);
		status.setCurrentOperation(TR069Method.getState(method));
		// GPN might be injected at this point
		if (method.equals(TR069Method.GET_PARAMETER_NAMES)) {
			response = GetParameterNames.execute(args, httpHandler, status);
			method = TR069Method.getMethod(response);
			status.setCurrentOperation(TR069Method.getState(method));
		}
		tr069Client.parseGPVReq(response);
		response = GetParameterValues.execute(args, httpHandler, status, tr069Client);
		method = TR069Method.getMethod(response);
		status.setCurrentOperation(TR069Method.getState(method));
		if (method.equals(TR069Method.DOWNLOAD)) {
			if (args.getFailureEvery() == 0 || httpHandler.getSerialNumberInt() % args.getFailureEvery() != 0)
				tr069Client.parseDOReq(response);
			Download.execute(args, httpHandler, status);
			if (args.getHangupEvery() > 0 && httpHandler.getSerialNumberInt() % args.getHangupEvery() == 0) {
				tr069Client.setNextConnectTms(-1);
				return tr069Client;
			} else {
				tr069Client.setNextConnectType(TR069Client.TYPE_IN_TC_EM);
				if (args.getDownload() > 0)
					tr069Client.setNextConnectTms(System.currentTimeMillis() + random.nextInt(args.getDownload() * 1000));
				else
					tr069Client.setNextConnectTms(System.currentTimeMillis());
				return tr069Client;
			}
		}
		tr069Client.setNextConnectType(TR069Client.TYPE_IN_EM);
		if (method.equals(TR069Method.REBOOT)) {
			Reboot.execute(args, httpHandler, status);
			if (args.getHangupEvery() > 0 && httpHandler.getSerialNumberInt() % args.getHangupEvery() == 0)
				tr069Client.setNextConnectTms(-1);
			if (args.getDownload() > 0)
				tr069Client.setNextConnectTms(System.currentTimeMillis() + random.nextInt(args.getDownload() * 1000));
			else
				tr069Client.setNextConnectTms(System.currentTimeMillis());
			return tr069Client;
		}
		//		if (method != null && method.indexOf("#") > -1)
		//			tr069Client.setNextConnectTms(Long.parseLong(method.substring(method.indexOf("#") + 1)));
		tr069Client.parseSPVReq(response, args);
		SetParameterValues.execute(args, httpHandler, status);
		//		status.setCurrentOperation(Status.EM);
		//		Empty.execute(args, httpHandler, status);
		if (args.getHangupEvery() > 0 && httpHandler.getSerialNumberInt() % args.getHangupEvery() == 0)
			tr069Client.setNextConnectTms(-1);
		return tr069Client;
	}

	public void run() {
		try {
			notYetStarted = false;
			// This is kind of cheating, to reuse the httpHandler for every iteration
			//		if (args.getProvUrl().indexOf("https") == -1)
			//			httpHandler = new HttpHandler(args);
			while (!stop) {
				//			if (args.getProvUrl().indexOf("https") > -1)
        status = new Status();
				httpHandler = new HttpHandler(args, status);
				TR069Client tr069Client = TR069ClientFactory.makeTR069Client();
				httpHandler.setSerialNumber(tr069Client.getSerialNumberStr());
				httpHandler.setSerialNumberInt(tr069Client.getSerialNumber());
				NDC.push(tr069Client.getSerialNumberStr());
				TestCenter.getVerboseOutput().addStatus(status);
				long startConv = System.currentTimeMillis();
				long nextPITms = Long.MAX_VALUE;
				try {
					tr069Client = runImpl2(tr069Client);
				} catch (Exception ex) {
					logger.error("An exception occurred : " + ex);
					ex.printStackTrace();
					status.setServedOK(status.getServedOK() - 1);
					status.setServedFailed(status.getServedFailed() + 1);
					status.setErrorOcurred(status.getCurrentOperation());
					if (ex instanceof BindException) {
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} finally {
					if (nextPITms == -1)
						httpHandler = new HttpHandler(args, status);
					TR069ClientFactory.finishedSession(tr069Client);
					long timeSpentOnConv = System.currentTimeMillis() - startConv - status.getRetrySleep();
					if (logger.isDebugEnabled())
						logger.debug("Conversation performed in " + timeSpentOnConv + " ms.");
					status.setServedOK(status.getServedOK() + 1);
					status.setCurrentOperation(Status.FIN);
					NDC.pop();
				}
			}
			stopped = true;
		} catch (Throwable t) {
			logger.fatal("Error occured: " + t, t);
		}
	}

	public boolean isNotYetStarted() {
		return notYetStarted;
	}

	public void setNotYetStarted(boolean notYetStarted) {
		this.notYetStarted = notYetStarted;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public boolean isStopped() {
		return stopped;
	}

}
