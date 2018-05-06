package com.owera.xaps.spp.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Context;
import com.owera.xaps.base.BaseCache;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.NoDataAvailableException;
import com.owera.xaps.base.http.Util;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.spp.SessionData;

public class DigestAuthenticator {

	private static void sendChallenge(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// Send challenge
		String nonce = generateNonce(req);
		setAuthenticateHeader(res, nonce);
		res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		//res.flushBuffer();
	}

	public static boolean authenticate(HttpServletRequest req, HttpServletResponse res, SessionData sessionData) throws ServletException, IOException {

		String authorization = req.getHeader("authorization");
		if (authorization == null) {
			Log.notice(DigestAuthenticator.class, "Send challenge to CPE, located on IP-address " + req.getRemoteHost());
			sendChallenge(req, res);
			return false;
		} else {
			return (verify(req, res, authorization, sessionData));
		}
	}

	/**
	 * MD5 message digest provider.
	 */
	protected static MessageDigest md5Helper;

	/**
	 * Private key.
	 */
	protected static String key = "MortenRuler";

	/**
	 * Generate a unique token. The token is generated according to the
	 * following pattern. NOnceToken = Base64 ( MD5 ( client-IP ":"
	 * time-stamp ":" private-key ) ).
	 *
	 * @param request HTTP Servlet request
	 */
	private static String generateNonce(HttpServletRequest req) {

		long currentTime = System.currentTimeMillis();

		String nOnce = req.getRemoteAddr() + ":" + currentTime + ":" + key;

		return DigestUtils.md5Hex(nOnce);
	}

	/**
	 * Generates the WWW-Authenticate header.
	 * 
	 * @param request HTTP Servlet request
	 * @param response HTTP Servlet response
	 * @param nonce nonce token
	 */
	private static void setAuthenticateHeader(HttpServletResponse res, String nonce) {

		String realm = Util.getRealm();

		String authenticateHeader = "Digest realm=\"" + realm + "\", " + "qop=\"auth\", nonce=\"" + nonce + "\", " + "opaque=\"" + DigestUtils.md5Hex(nonce) + "\"";
		res.setHeader("WWW-Authenticate", authenticateHeader);
	}

	private static String passwordMd5(String username, String password, String method, String uri, String nonce, String nc, String cnonce, String qop) {
		String realm = Util.getRealm();
		String a1 = username + ":" + realm + ":" + password;
		String md5a1 = DigestUtils.md5Hex(a1);
		String a2 = method + ":" + uri;
		String md5a2 = DigestUtils.md5Hex(a2);
		String a3 = md5a1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + md5a2;
		//String a3 = md5a1 + ":" + nonce + ":" + md5a2;
		String md5a3 = DigestUtils.md5Hex(a3);

		return md5a3;
	}

	/**
	 * Verifies login against database
	 * 
	 * @param request HTTP servlet request
	 * @param authorization Authorization credentials from this request
	 */
	private static boolean verify(HttpServletRequest req, HttpServletResponse res, String authorization, SessionData sessionData) throws ServletException, IOException {

		authorization = authorization.trim();
		authorization = Util.removePrefix(authorization, "digest");
		authorization = authorization.trim();

		String[] tokens = authorization.split(",(?=(?:[^\"]*\"[^\"]*\")+$)");

		String username = null;
		String realm = null;
		String nonce = null;
		String nc = null;
		String cnonce = null;
		String qop = null;
		String uri = null;
		String response = null;
		String method = req.getMethod();

		for (int i = 0; i < tokens.length; i++) {
			String currentToken = tokens[i];
			if (currentToken.length() == 0)
				continue;

			int equalSign = currentToken.indexOf('=');
			if (equalSign < 0) {
				res.sendError(HttpServletResponse.SC_FORBIDDEN);
				return false;
			}
			String currentTokenName = currentToken.substring(0, equalSign).trim();
			String currentTokenValue = currentToken.substring(equalSign + 1).trim();
			if ("username".equals(currentTokenName))
				username = Util.removeQuotes(currentTokenValue);
			if ("realm".equals(currentTokenName))
				realm = Util.removeQuotes(currentTokenValue, true);
			if ("nonce".equals(currentTokenName))
				nonce = Util.removeQuotes(currentTokenValue);
			if ("nc".equals(currentTokenName))
				nc = Util.removeQuotes(currentTokenValue);
			if ("cnonce".equals(currentTokenName))
				cnonce = Util.removeQuotes(currentTokenValue);
			if ("qop".equals(currentTokenName))
				qop = Util.removeQuotes(currentTokenValue);
			if ("uri".equals(currentTokenName))
				uri = Util.removeQuotes(currentTokenValue);
			if ("response".equals(currentTokenName))
				response = Util.removeQuotes(currentTokenValue);
		}

		if ((username == null) || username.length() < 6 || (realm == null) || (nonce == null) || (uri == null) || (response == null)) {
			Log.error(DigestAuthenticator.class, "Authentication fails, some parameters from CPE are missing (CPE IP address: " + req.getRemoteHost() + ") (username: " + username + ")");
			res.sendError(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}

		//System.out.println("To be authenticated:");
		//System.out.println("username=" + username + ", response=" + response);

		// Do database read parameters and then perform verification
		String unitId = Util.username2unitId(username);
		Context.put(Context.X, unitId, BaseCache.SESSIONDATA_CACHE_TIMEOUT);
		try {
			sessionData.setUnitId(unitId);
			sessionData.updateParametersFromDB(unitId);
			//			String secret = sessionData.getOweraParameters().getValue(SystemParameters.SHARED_SECRET);
			BaseCache.putSessionData(unitId, sessionData);
			//			if (secret == null)
			String secret = sessionData.getOweraParameters().getValue(SystemParameters.SECRET);
			if (secret != null && secret.length() > 16 && !passwordMd5(username, secret, method, uri, nonce, nc, cnonce, qop).equals(response))
				secret = secret.substring(0, 16);
			//			if (secret == null)
			//				secret = sessionData.getOweraParameters().getValue(SystemParameters.TR069_SECRET);
			if (secret == null) {
				Log.error(DigestAuthenticator.class, "Unable to obtain TR-069 secret (CPE IP address: " + req.getRemoteHost() + ")");
				res.sendError(HttpServletResponse.SC_FORBIDDEN);
			} else {
				String sharedSecretMd5 = passwordMd5(username, secret, method, uri, nonce, nc, cnonce, qop);
				if (!sharedSecretMd5.equals(response)) {
					Log.warn(DigestAuthenticator.class, "Incorrect TR-069 secret (CPE IP address: " + req.getRemoteHost() + ")");
					res.sendError(HttpServletResponse.SC_FORBIDDEN);
				} else {
					Log.notice(DigestAuthenticator.class, "Authentication verified (CPE IP address: " + req.getRemoteHost() + ")");
					return true;
				}
			}
		} catch (NoAvailableConnectionException e) {
			Log.error(DigestAuthenticator.class, "Authentication failed because of no available database connections (CPE IP address: " + req.getRemoteHost() + ")");
			res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		} catch (SQLException e) {
			Log.fatal(DigestAuthenticator.class, "Authentication failed because of database error (CPE IP address: " + req.getRemoteHost() + ")", e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (NoDataAvailableException e) {
			Log.warn(DigestAuthenticator.class, "Authentication failed because unitid was not found (CPE IP address: " + req.getRemoteHost() + ")");
			res.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (UnsupportedEncodingException uee) {
			Log.error(DigestAuthenticator.class, "Authentication failed because the URL-decoding of the unitid failed (CPE IP address: " + req.getRemoteHost() + ")", uee);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		return false;
	}
}
