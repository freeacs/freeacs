package com.github.freeacs.web.app.page.monitor;

import com.github.freeacs.common.ssl.EasySSLProtocolSocketFactory;
import com.github.freeacs.common.ssl.HTTPSManager;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.WebProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for retrieving and displaying the GET outputHandler from the xAPS Monitor Server.
 *
 * @author Jarl Andre Hubenthal
 */
public class MonitorPage extends AbstractWebPage {
  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(MonitorPage.class);

  static {
    ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();
    Protocol https = new Protocol("https", socketFactory, 443);
    Protocol.registerProtocol("https", https);
  }

  public boolean useWrapping() {
    return true;
  }

  public void process(
      ParameterParser req,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    ACS acs = ACSLoader.getXAPS(req.getSession().getId(), xapsDataSource, syslogDataSource);

    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    String cmd = req.getParameter("page");
    String baseURL = WebProperties.getInstance().getMonitorLocation();
    if (!baseURL.endsWith("/")) {
      baseURL += "/";
    }
    String url = null;
    if (cmd != null) {
      if ("status".equalsIgnoreCase(cmd) || "monitor".equalsIgnoreCase(cmd)) {
        url = baseURL + "monitor/web?page=status&html=no";
      } else if ("history".equalsIgnoreCase(cmd)) {
        url = baseURL + "monitor/web?page=history&html=no";
      }
    } else {
      cmd = "";
      url = baseURL + "monitor/web?html=no";
    }

    if (url != null && url.startsWith("https://")) {
      try {
        HTTPSManager.installCertificate(url, WebProperties.getInstance().getKeystorePass());
      } catch (Exception e) {
        logger.error("Could not install server certificate for " + url, e);
      }
    }
    String outputHandlerString = getStringFromURL(url, req);

    if (outputHandlerString == null) {
      Map<String, String> root = new HashMap<>();
      root.put("message", "Monitor Service is not running!");
      outputHandler.getTemplateMap().putAll(root);
      outputHandlerString = outputHandler.compileTemplate("/exception.ftl");
    }

    outputHandler.setDirectResponse(outputHandlerString);
  }

  /**
   * Gets the string from url.
   *
   * @param url the url
   * @param req the req
   * @return the string from url
   */
  private String getStringFromURL(String url, ParameterParser req) {
    HttpClient client = new HttpClient();
    HttpMethod method = null;
    if (url.contains("page=history")) {
      method = new PostMethod(url);
      PostMethod pm = (PostMethod) method;
      String limit = req.getParameter("limit");
      String system = req.getParameter("system");
      if (limit != null && system != null) {
        pm.addParameter("limit", limit);
        pm.addParameter("system", system);
      }
    } else {
      method = new GetMethod(url);
    }

    String outputHandler = null;

    try {
      client.executeMethod(method);
      if (method.getStatusCode() == HttpStatus.SC_OK) {
        outputHandler = method.getResponseBodyAsString();
      }
    } catch (IOException e) {
      logger.warn("Could not find the monitor server", e);
    } finally {
      method.releaseConnection();
    }

    return outputHandler;
  }
}
