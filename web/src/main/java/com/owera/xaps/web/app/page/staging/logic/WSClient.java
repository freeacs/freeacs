package com.owera.xaps.web.app.page.staging.logic;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.owera.common.log.Logger;
import com.owera.xaps.dbi.ProfileParameter;
import com.owera.xaps.dbi.ProfileParameters;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.web.app.util.WebProperties;
//import com.owera.xapsws.XAPSWSProxy;


/**
 * This class will perform all logic concerning the web service invocation to
 * the provider's xAPS. It should detect which version of the web service is
 * running at the provider and then adapt accordingly.
 * 
 * This class is not in use at the moment, as it seems like we don't need
 * to support two versions of the xAPS WS right now. We anticipate this class
 * will be necessary in the future, hence we let it stand a year or so.
 * 
 * July 22. 2010
 * 
 * @author morten
 * 
 */
public class WSClient {

	/** The provider. */
	private com.owera.xaps.dbi.Profile provider;
	
	/** The units. */
	@SuppressWarnings("unused")
	private List<com.owera.xaps.dbi.Unit> units;

	/** The logger. */
	private static Logger logger = new Logger();

	/**
	 * Instantiates a new wS client.
	 *
	 * @param provider the provider
	 * @param units the units
	 */
	public WSClient(com.owera.xaps.dbi.Profile provider, List<com.owera.xaps.dbi.Unit> units) {
		this.provider = provider;
		this.units = units;
	}

	/**
	 * Gets the content.
	 *
	 * @param url the url
	 * @return the content
	 * @throws Exception the exception
	 */
	private String getContent(String url) throws Exception {
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		String outputHandler = null;
		try {
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK)
				outputHandler = method.getResponseBodyAsString();
		} finally {
			method.releaseConnection();
		}
		return outputHandler;
	}

	/**
	 * Gets the version.
	 *
	 * @param url the url
	 * @return the version
	 * @throws Exception the exception
	 */
	private int getVersion(String url) throws Exception {
		String wsdl = getContent(url + "?wsdl");
		int versionPos = wsdl.indexOf("WSDL Version ") + 13;
		if (versionPos > 13)
			return new Integer(wsdl.substring(versionPos, wsdl.indexOf(" ", versionPos)));
		return 1;
	}

	/**
	 * Execute.
	 *
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unused")
	public void execute() throws Exception {
		ProfileParameters pps = provider.getProfileParameters();
		ProfileParameter urlpp = pps.getByName(SystemParameters.STAGING_PROVIDER_WSURL);
		String url = urlpp.getValue();
		String unittypeName = pps.getByName(SystemParameters.STAGING_PROVIDER_UNITTYPE).getValue();
		String profileName = pps.getByName(SystemParameters.STAGING_PROVIDER_PROFILE).getValue();
		if (url.startsWith("https")) {
			logger.debug("Provider URL is HTTPS, will check certificates and install if needed");
			HTTPSManager.installCertificate(url,WebProperties.getWebProperties());
		}
		int version = getVersion(url);
		if (version == 1 || version == 2) {
			//XAPSWSProxy tp = new XAPSWSProxy();
			//tp.setEndpoint(url);
		} 
	}

}
