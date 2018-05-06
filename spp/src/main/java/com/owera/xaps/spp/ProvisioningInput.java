package com.owera.xaps.spp;

import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.owera.xaps.base.Log;

public class ProvisioningInput {

	private static String findParameter(String param, List<String> patterns, String strToMatchWith) {
		for (String patternStr : patterns) {
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(strToMatchWith);
			if (matcher.find()) {
				String match = matcher.group(1);
				Log.info(ProvisioningInput.class, "Found " + param + " (" + match + ") using " + patternStr);
				return match;
			} else {
				Log.debug(ProvisioningInput.class, "No match for " + param + " using " + patternStr);
			}
		}
		if (patterns.size() == 0)
			Log.debug(ProvisioningInput.class, "No patterns specified for " + param);
		return null;
	}

	protected static void parseRequestFile(String url, SessionData sessionData) {
		Log.debug(ProvisioningInput.class, "Examining request-filename (exluding hostname and possibly context-path): " + url);
		sessionData.setMac(findParameter("mac", Properties.getReqFilePatterns("mac"), url));
		sessionData.setSerialNumber(findParameter("serialnumber", Properties.getReqFilePatterns("serialnumber"), url));
		sessionData.setSoftwareVersion(findParameter("softwareversion", Properties.getReqFilePatterns("softwareversion"), url));
		sessionData.setModelName(findParameter("modelname", Properties.getReqFilePatterns("modelname"), url));
	}

	protected static void parseRequestHeaders(HttpServletRequest req, SessionData sessionData) {
		Enumeration<String> headers = req.getHeaderNames();
		while (headers.hasMoreElements()) {
			if (sessionData.getMac() != null && sessionData.getSerialNumber() != null && sessionData.getSoftwareVersion() != null && sessionData.getModelName() != null) {
				Log.debug(ProvisioningInput.class, "All information is found - no need to parse more headers");
				break;
			}
			String headerName = headers.nextElement();
			String headerInfo = req.getHeader(headerName);
			Log.debug(ProvisioningInput.class, "Examining request-header " + headerName + ": " + headerInfo);
			if (sessionData.getMac() == null)
				sessionData.setMac(findParameter("mac", Properties.getReqHeaderPatterns("mac", headerName), headerInfo));
			if (sessionData.getSerialNumber() == null)
				sessionData.setSerialNumber(findParameter("serialnumber", Properties.getReqHeaderPatterns("serialnumber", headerName), headerInfo));
			if (sessionData.getSoftwareVersion() == null)
				sessionData.setSoftwareVersion(findParameter("softwareversion", Properties.getReqHeaderPatterns("softwareversion", headerName), headerInfo));
			if (sessionData.getModelName() == null)
				sessionData.setModelName(findParameter("modelname", Properties.getReqHeaderPatterns("modelname", headerName), headerInfo));
		}
	}

	protected static void parseRequestParameters(HttpServletRequest req, SessionData sessionData) {
		Enumeration<String> params = req.getParameterNames();
		while (params.hasMoreElements()) {
			if (sessionData.getMac() != null && sessionData.getSerialNumber() != null && sessionData.getSoftwareVersion() != null && sessionData.getModelName() != null) {
				Log.debug(ProvisioningInput.class, "All information is found - no need to parse more headers");
				break;
			}
			String paramName = params.nextElement();
			String paramInfo = req.getParameter(paramName);
			Log.debug(ProvisioningInput.class, "Examining request-param " + paramName + ": " + paramInfo);
			if (sessionData.getMac() == null)
				sessionData.setMac(findParameter("mac", Properties.getReqParamPatterns("mac", paramName), paramInfo));
			if (sessionData.getSerialNumber() == null)
				sessionData.setSerialNumber(findParameter("serialnumber", Properties.getReqParamPatterns("serialnumber", paramName), paramInfo));
			if (sessionData.getSoftwareVersion() == null)
				sessionData.setSoftwareVersion(findParameter("softwareversion", Properties.getReqParamPatterns("softwareversion", paramName), paramInfo));
			if (sessionData.getModelName() == null)
				sessionData.setModelName(findParameter("modelname", Properties.getReqParamPatterns("modelname", paramName), paramInfo));
		}
	}

}
