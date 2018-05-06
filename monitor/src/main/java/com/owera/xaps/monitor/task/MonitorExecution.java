package com.owera.xaps.monitor.task;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.owera.common.log.Logger;
import com.owera.xaps.monitor.Properties;
import com.owera.xaps.web.app.page.staging.logic.HTTPSManager;
import com.owera.xaps.web.app.util.WebProperties;

/*
 * Http check implementation
 */
public class MonitorExecution implements Runnable {

	//	/**
	//	 * If exception occur during monitoring, retry a number of times, until 5 minutes
	//	 * of non-connectivity is established as a fact.
	//	 * @author Morten
	//	 *
	//	 */
	//	public class MonitorExecutionRetry implements HttpMethodRetryHandler {
	//
	//		private long startTms;
	//
	//		public MonitorExecutionRetry(long startTms) {
	//			this.startTms = startTms;
	//		}
	//
	//		@Override
	//		public boolean retryMethod(HttpMethod httpMethod, IOException exception, int executionCount) {
	//			if (System.currentTimeMillis() - startTms >= Properties.getRetrySeconds() * 1000) {
	//				logger.debug("Monitoring: FiveMinuteRetry.retryMethod() invoked - return false -> no more retry since " + Properties.getRetrySeconds() + " seconds since start");
	//				return false;
	//			}
	//			try {
	//				Thread.sleep(Properties.getRetrySeconds() * 1000 / 10);
	//
	//			} catch (InterruptedException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//			logger.debug("FiveMinuteRetry.retryMethod() invoked - return true -> retry");
	//			return true;
	//		}
	//
	//	}

	private HttpClient client = new HttpClient();

	private static Logger logger = new Logger();

	private String url;

	private String status;

	private String errorMessage;

	private String version;

	private long startTms;

	public MonitorExecution(String url) {
		this.url = url;
		this.startTms = System.currentTimeMillis();
		//		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new MonitorExecutionRetry(startTms));
	}

	public void run() {
		HttpMethod method = null;
		String errorMessage = null;
		String status = "ERROR";
		String version = "";
		try {
			if (url.startsWith("https://"))
				HTTPSManager.installCertificate(url, WebProperties.getReader("xaps-monitor.properties"));
			while (System.currentTimeMillis() - startTms < Properties.getRetrySeconds() * 1000) {
				try {
					method = new GetMethod(url);
					int returnCode = client.executeMethod(method);
					String response = null;
					if (returnCode == HttpStatus.SC_OK)
						response = getResponse(method);
					else {
						errorMessage = "HTTP Return Code: " + returnCode;
					}
					if (response != null && response.indexOf("XAPSOK") > -1) {
						status = "OK";
						errorMessage = null;
						int pos = response.indexOf("XAPSOK");
						version = response.substring(pos + 6).trim();
						break;
					} else if (response != null) {
						errorMessage = response;
					} else {
						errorMessage = "No data retrieved";
					}
					logger.debug("Monitoring: MonitorExecution: Error detected in " + url + " - will retry in " + Properties.getRetrySeconds() / 10 + " seconds");
					Thread.sleep(Properties.getRetrySeconds() * 1000 / 10);
				} catch (Throwable t) { // Thrown by executeMethod
					Thread.sleep(Properties.getRetrySeconds() * 1000 / 10);
					errorMessage = "Exception occurred : " + t.getMessage();
				}
			}
		} catch (Throwable t) { // Thrown by executeMethod
			errorMessage = "Installation of SSL Certificate failed, not possible to monitor (" + t.getMessage() + ")";
		} finally {
			updateFields(status, errorMessage, version);
			if (errorMessage == null)
				logger.debug("Monitoring: MonitorExecution: URL " + url + " has status " + status);
			else
				logger.info("Monitoring: MonitorExecution: URL " + url + " has status " + status + ", error is " + errorMessage);
			if (method != null)
				method.releaseConnection();
		}
	}

	/**
	 * This method cannot throw an exception due to the fact that method.getResponseBodyAsStream only throws IOException
	 * and this is catched tidely.
	 *
	 * @param method HttpMethod
	 * @return The one line response
	 * @throws IOException 
	 */
	private String getResponse(HttpMethod method) throws IOException {
		String body = method.getResponseBodyAsString();
		if (body == null || body.trim().length() == 0)
			return "Could not get response (No data)";
		return body;
	}

	private synchronized void updateFields(String status, String errorMessage, String version) {
		this.status = status;
		this.errorMessage = errorMessage;
		this.version = version;

	}

	public synchronized String getStatus() {
		return status;
	}

	public synchronized String getUrl() {
		return url;
	}

	public synchronized String getErrorMessage() {
		return errorMessage;
	}

	public synchronized String getVersion() {
		return version;
	}

}
