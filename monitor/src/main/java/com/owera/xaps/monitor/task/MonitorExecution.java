package com.owera.xaps.monitor.task;

import com.github.freeacs.common.ssl.HTTPSManager;
import com.owera.xaps.monitor.Properties;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Http check implementation. */
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
      if (url.startsWith("https://")) {
        HTTPSManager.installCertificate(url, "changeit");
      }
      while (System.currentTimeMillis() - startTms < Properties.RETRY_SECS * 1000) {
        try {
          method = new GetMethod(url);
          int returnCode = client.executeMethod(method);
          String response = null;
          if (returnCode == HttpStatus.SC_OK) {
            response = getResponse(method);
          } else {
            errorMessage = "HTTP Return Code: " + returnCode;
          }
          if (response != null && response.contains("FREEACSOK")) {
            status = "OK";
            errorMessage = null;
            int pos = response.indexOf("FREEACSOK");
            version = response.substring(pos + 9).trim();
            break;
          } else if (response != null) {
            errorMessage = response;
          } else {
            errorMessage = "No data retrieved";
          }
          logger.debug(
              "Monitoring: MonitorExecution: Error detected in "
                  + url
                  + " - will retry in "
                  + Properties.RETRY_SECS / 10
                  + " seconds");
          Thread.sleep(Properties.RETRY_SECS * 1000 / 10);
        } catch (Throwable t) { // Thrown by executeMethod
          Thread.sleep(Properties.RETRY_SECS * 1000 / 10);
          errorMessage = "Exception occurred : " + t.getMessage();
        }
      }
    } catch (Throwable t) { // Thrown by executeMethod
      errorMessage =
          "Installation of SSL Certificate failed, not possible to monitor ("
              + t.getMessage()
              + ")";
    } finally {
      updateFields(status, errorMessage, version);
      if (errorMessage != null) {
        logger.info(
            "Monitoring: MonitorExecution: URL "
                + url
                + " has status "
                + status
                + ", error is "
                + errorMessage);
      } else {
        logger.debug("Monitoring: MonitorExecution: URL " + url + " has status " + status);
      }
      if (method != null) {
        method.releaseConnection();
      }
    }
  }

  /**
   * This method cannot throw an exception due to the fact that method.getResponseBodyAsStream only
   * throws IOException and this is catched tidely.
   *
   * @param method HttpMethod
   * @return The one line response
   * @throws IOException
   */
  private String getResponse(HttpMethod method) throws IOException {
    String body = method.getResponseBodyAsString();
    if (body == null || body.trim().isEmpty()) {
      return "Could not get response (No data)";
    }
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
