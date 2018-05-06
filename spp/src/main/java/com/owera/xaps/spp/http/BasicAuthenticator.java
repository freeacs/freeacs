package com.owera.xaps.spp.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Context;
import com.owera.xaps.base.BaseCache;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.NoDataAvailableException;
import com.owera.xaps.base.http.Util;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.spp.Properties;
import com.owera.xaps.spp.SessionData;

public class BasicAuthenticator {

	private static void sendChallenge(HttpServletResponse res) throws ServletException, IOException {
		// Send challenge
		String authParam = "Basic realm=\"" + Util.getRealm() + "\"";
		res.addHeader("WWW-Authenticate", authParam);
		res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	public static boolean authenticate(HttpServletRequest req, HttpServletResponse res, SessionData sessionData) throws ServletException, IOException {

		String authorization = req.getHeader("authorization");
		if (authorization == null) {
			Log.notice(BasicAuthenticator.class, "Send challenge to CPE, located on IP-address " + req.getRemoteHost());
			sendChallenge(res);
			return false;
		} else {
			return (verify(req, res, sessionData, authorization));
		}

	}

	/**
	 * Verifies login against database
	 * 
	 * @param request
	 *            HTTP servlet request
	 * @param authorization
	 *            Authorization credentials from this request
	 */
	private static boolean verify(HttpServletRequest req, HttpServletResponse res, SessionData sessionData, String authorization) throws ServletException, IOException {

		authorization = authorization.trim();
		authorization = Util.removePrefix(authorization, "basic");
		authorization = authorization.trim();
		String userpass = Util.base64decode(authorization);

		// Validate any credentials already included with this request
		String username = null;
		String password = null;

		// Get username and password
		int colon = userpass.indexOf(':');
		if (colon < 0) {
			username = userpass;
		} else {
			username = userpass.substring(0, colon);
			password = userpass.substring(colon + 1, userpass.length());
		}

		// System.out.println("To be authenticated:");
		// System.out.println("username=" + username + ", password=" +
		// password);

		// Do database read parameters and then perform verification
		String unitId = Util.username2unitId(username);
		Context.put(Context.X, unitId, BaseCache.SESSIONDATA_CACHE_TIMEOUT);
		try {
			sessionData.setUnitId(unitId);
			sessionData.updateParametersFromDB(unitId);
			String secret = null;
			if (sessionData.isFirstConnect() && Properties.isDiscoveryMode()) {
				secret = password;
				sessionData.setSecret(secret);
				Log.warn(DigestAuthenticator.class, "Authentication not verified, but accepted since in Discovery Mode");
			}
			BaseCache.putSessionData(unitId, sessionData);
			//			if (secret == null)
			//				secret = sessionData.getOweraParameters().getValue(SystemParameters.SHARED_SECRET);
			if (secret == null) {
				secret = sessionData.getOweraParameters().getValue(SystemParameters.SECRET);
				if (secret != null && !secret.equals(password) && secret.length() > 16)
					secret = secret.substring(0, 16);
			}
			//			if (secret == null)
			//				secret = sessionData.getOweraParameters().getValue(SystemParameters.TR069_SECRET);
			if (secret == null) {
				Log.error(BasicAuthenticator.class, "Unable to obtain TR-069 secret (CPE IP address: " + req.getRemoteHost() + ")");
				res.sendError(HttpServletResponse.SC_FORBIDDEN);
			} else if (!secret.equals(password)) {
				Log.warn(BasicAuthenticator.class, "Incorrect TR-069 secret (CPE IP address: " + req.getRemoteHost() + ")");
				res.sendError(HttpServletResponse.SC_FORBIDDEN);
			} else {
				Log.notice(BasicAuthenticator.class, "Authentication verified (CPE IP address: " + req.getRemoteHost() + ")");
				return true;
			}
		} catch (NoAvailableConnectionException e) {
			Log.error(BasicAuthenticator.class, "Authentication failed because of no available database connections (CPE IP address: " + req.getRemoteHost() + ")");
			res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		} catch (SQLException e) {
			Log.fatal(BasicAuthenticator.class, "Authentication failed because of database error (CPE IP address: " + req.getRemoteHost() + ")", e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (NoDataAvailableException e) {
			Log.warn(BasicAuthenticator.class, "Authentication failed because unitid was not found (CPE IP address: " + req.getRemoteHost() + ")");
			res.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (UnsupportedEncodingException uee) {
			Log.error(BasicAuthenticator.class, "Authentication failed because the URL- decoding of the unitid failed (CPE IP address: " + req.getRemoteHost() + ")", uee);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return false;
	}

}
