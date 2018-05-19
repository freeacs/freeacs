package com.github.freeacs.base.http;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.NoDataAvailableException;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069AuthenticationException;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

public class BasicAuthenticator {

	private static void sendChallenge(HttpServletResponse res) {
		res.setHeader("WWW-Authenticate", "Basic realm=\"" + Util.getRealm() + "\"");
		res.setStatus(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
	}

	public static boolean authenticate(HTTPReqResData reqRes) throws TR069AuthenticationException {

		String authorization = reqRes.getReq().getHeader("authorization");
		if (authorization == null) {
			Log.notice(BasicAuthenticator.class, "Send challenge to CPE, located on IP-address " + reqRes.getReq().getRemoteHost());
			sendChallenge(reqRes.getRes());
			return false;
		} else {
			return (verify(reqRes, authorization));
		}

	}

	/**
	 * Verifies login against database
	 * 
	 * @param reqRes
	 *            HTTP servlet request
	 * @param authorization
	 *            Authorization credentials from this request
	 * @throws TR069AuthenticationException 
	 */
	private static boolean verify(HTTPReqResData reqRes, String authorization) throws TR069AuthenticationException {

		Log.debug(BasicAuthenticator.class, "Basic verification of CPE starts, located on IP-address " + reqRes.getReq().getRemoteHost());
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

		// Do database read parameters and then perform verification
		String unitId = Util.username2unitId(username);
		Log.debug(DigestAuthenticator.class, "Basic verification identifed unit id " + unitId + " from CPE IP-address " + reqRes.getReq().getRemoteHost());
		try {
			SessionData sessionData = reqRes.getSessionData();
			sessionData.setUnitId(unitId);
			sessionData.updateParametersFromDB(unitId); // Unit is now stored in sessionData
			String secret = null;
			if (sessionData.isFirstConnect() && Properties.DISCOVERY_MODE) {
				for (String blocked : Properties.DISCOVERY_BLOCK) {
					if (unitId.contains(blocked))
						throw new TR069AuthenticationException("ACS Username is blocked by \"" + blocked + "\" in discovery mode. Access denied", null, HttpServletResponse.SC_FORBIDDEN);
				}
				secret = password;
				sessionData.setSecret(secret);
				Log.warn(DigestAuthenticator.class, "Authentication not verified, but accepted since in Discovery Mode");
			}
			BaseCache.putSessionData(unitId, sessionData);
			if (secret == null) {
				secret = sessionData.getFreeacsParameters().getValue(SystemParameters.SECRET);
				if (secret != null && !secret.equals(password) && secret.length() > 16)
					secret = secret.substring(0, 16);
			}
			if (secret == null) {
				throw new TR069AuthenticationException("No ACS Password found in database (CPE IP address: " + reqRes.getReq().getRemoteHost() + ") (username: " + username + ")", null,
						HttpServletResponse.SC_FORBIDDEN);
			} else if (!secret.equals(password)) {
				throw new TR069AuthenticationException("Incorrect ACS Password (CPE IP address: " + reqRes.getReq().getRemoteHost() + ") (username: " + username + ")", null,
						HttpServletResponse.SC_FORBIDDEN);
			} else {
				Log.notice(BasicAuthenticator.class, "Authentication verified (CPE IP address: " + reqRes.getReq().getRemoteHost() + ")");
				return true;
			}
		} catch (SQLException e) {
			throw new TR069AuthenticationException("Authentication failed because of database error (CPE IP address: " + reqRes.getReq().getRemoteHost() + ") (username: " + username + ")", e,
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (NoDataAvailableException e) {
			throw new TR069AuthenticationException("Authentication failed because unitid was not found (CPE IP address: " + reqRes.getReq().getRemoteHost() + ") (username: " + username + ")", e,
					HttpServletResponse.SC_FORBIDDEN);
		}
	}
}
