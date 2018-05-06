package com.owera.xaps.web.app.page.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import com.owera.common.log.Logger;
import com.owera.common.util.PropertyReader;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.page.staging.logic.HTTPSManager;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.WebProperties;
import com.owera.xaps.web.app.util.XAPSLoader;


/**
 * Responsible for retrieving and displaying the GET outputHandler from the xAPS Monitor Server.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public class MonitorPage extends AbstractWebPage {
	
	/** The logger. */
	private static Logger logger = new Logger();
	
	static{
		ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory( );
		Protocol https = new Protocol( "https", socketFactory, 443);
		Protocol.registerProtocol( "https", https );
	}
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#useWrapping()
	 */
	public boolean useWrapping(){
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser req, Output outputHandler) throws Exception {
		XAPS xaps = XAPSLoader.getXAPS(req.getSession().getId());

		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		String cmd = req.getParameter("page");
		PropertyReader pr = new PropertyReader("xaps-web.properties");
		String baseURL = pr.getProperty("monitor.location");
		if (!baseURL.endsWith("/"))
			baseURL += "/";
		String url = null;
		if (cmd != null) {
			if (cmd.equalsIgnoreCase("status") || cmd.equalsIgnoreCase("monitor"))
				url = baseURL + "monitor/web?page=status&html=no";
			else if (cmd.equalsIgnoreCase("history"))
				url = baseURL + "monitor/web?page=history&html=no";
		} else{
			cmd = "";
			url = baseURL + "monitor/web?html=no";
		}
		
		if(url.startsWith("https://")){
			try{
				HTTPSManager.installCertificate(url, WebProperties.getWebProperties());
			}catch(Exception e){
				logger.error("Could not install server certificate for "+url,e);
			}
		}
		String outputHandlerString = getStringFromURL(url, req);

		if (outputHandlerString == null) {
			Map<String, String> root = new HashMap<String, String>();
			root.put("message", "Monitor Service is not running!");
			outputHandler.getTemplateMap().putAll(root);
			outputHandlerString = outputHandler.compileTemplate("/exception.ftl");
		}

		outputHandlerString = "<script src='javascript/xaps.module.monitor.js'></script>"+outputHandlerString;
		
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
		if (url.indexOf("page=history") > -1) {
			method = new PostMethod(url);
			PostMethod pm = (PostMethod) method;
			String limit = req.getParameter("limit");
			String system = req.getParameter("system");
			if (limit != null && system != null) {
				pm.addParameter("limit", limit);
				pm.addParameter("system", system);
			}
		} else
			method = new GetMethod(url);

		String outputHandler = null;

		try {
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK)
				outputHandler = method.getResponseBodyAsString();
		} catch (IOException e) {
			logger.warn("Could not find the monitor server", e);
		} finally {
			method.releaseConnection();
		}
		
		return outputHandler;
	}

}
