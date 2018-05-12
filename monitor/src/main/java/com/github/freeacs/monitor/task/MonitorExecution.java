package com.github.freeacs.monitor.task;

import com.github.freeacs.common.ssl.HTTPSManager;
import com.github.freeacs.monitor.Properties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/*
 * Http check implementation
 */
public class MonitorExecution implements Runnable {

	private HttpClient client = new HttpClient();

	private static Logger logger = LoggerFactory.getLogger(MonitorExecution.class);

	private String url;

	private String status;

	private String errorMessage;

	private String version;

	private long startTms;

	MonitorExecution(String url) {
		this.url = url;
		this.startTms = System.currentTimeMillis();
	}

	public void run() {
		HttpMethod method = null;
		String errorMessage = null;
		String status = "ERROR";
		String version = "";
		try {
			if (url.startsWith("https://"))
				HTTPSManager.installCertificate(url, Properties.getString("keystore.pass", "changeit"));
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
					if (response != null && response.contains("XAPSOK")) {
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
