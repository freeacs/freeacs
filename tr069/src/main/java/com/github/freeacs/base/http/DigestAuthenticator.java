package com.github.freeacs.base.http;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.NoDataAvailableException;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069AuthenticationException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;

public class DigestAuthenticator {
  private static void sendChallenge(
      String remoteAddr, HttpServletResponse res, String digestSecret) {
    long now = System.currentTimeMillis();
    setAuthenticateHeader(res, DigestUtils.md5Hex(remoteAddr + ":" + now + ":" + digestSecret));
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
  }

  public static boolean authenticate(
      HTTPRequestResponseData reqRes, boolean isDiscoveryMode, String digestSecret)
      throws TR069AuthenticationException {
    String authorization = reqRes.getRawRequest().getHeader("authorization");
    if (authorization == null) {
      Log.notice(
          DigestAuthenticator.class,
          "Send challenge to CPE, located on IP-address " + reqRes.getRawRequest().getRemoteHost());
      sendChallenge(reqRes.getRealIPAddress(), reqRes.getRawResponse(), digestSecret);
      return false;
    } else {
      return verify(reqRes, authorization, isDiscoveryMode);
    }
  }

  /**
   * Generates the WWW-Authenticate header.
   *
   * @param res HTTP Servlet response
   * @param nonce nonce token
   */
  private static void setAuthenticateHeader(HttpServletResponse res, String nonce) {
    String realm = Util.getRealm();

    String authenticateHeader =
        "Digest realm=\""
            + realm
            + "\", "
            + "qop=\"auth\", nonce=\""
            + nonce
            + "\", "
            + "opaque=\""
            + DigestUtils.md5Hex(nonce)
            + "\"";
    res.setHeader("WWW-Authenticate", authenticateHeader);
  }

  public static String passwordMd5(
      String username,
      String password,
      String method,
      String uri,
      String nonce,
      String nc,
      String cnonce,
      String qop) {
    String realm = Util.getRealm();
    String a1 = username + ":" + realm + ":" + password;
    String md5a1 = DigestUtils.md5Hex(a1);
    String a2 = method + ":" + uri;
    String md5a2 = DigestUtils.md5Hex(a2);
    String a3 = md5a1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + md5a2;
    return DigestUtils.md5Hex(a3);
  }

  /**
   * Verifies login against database.
   *
   * @param reqRes HTTP servlet request
   * @param authorization Authorization credentials from this request
   */
  private static boolean verify(
      HTTPRequestResponseData reqRes, String authorization, boolean isDiscoveryMode)
      throws TR069AuthenticationException {
    Log.debug(
        DigestAuthenticator.class,
        "Digest verification of CPE starts, located on IP-address "
            + reqRes.getRawRequest().getRemoteHost());
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
    String method = reqRes.getRawRequest().getMethod();

    for (String currentToken : tokens) {
      if (currentToken.isEmpty()) {
        continue;
      }

      int equalSign = currentToken.indexOf('=');
      if (equalSign < 0) {
        throw new TR069AuthenticationException(
            "Digest challenge response has incorrect format (CPE IP address: "
                + reqRes.getRawRequest().getRemoteHost()
                + ")",
            null,
            HttpServletResponse.SC_FORBIDDEN);
      }
      String currentTokenName = currentToken.substring(0, equalSign).trim();
      String currentTokenValue = currentToken.substring(equalSign + 1).trim();
      if ("username".equals(currentTokenName)) {
        username = Util.removeQuotes(currentTokenValue);
      }
      if ("realm".equals(currentTokenName)) {
        realm = Util.removeQuotes(currentTokenValue, true);
      }
      if ("nonce".equals(currentTokenName)) {
        nonce = Util.removeQuotes(currentTokenValue);
      }
      if ("nc".equals(currentTokenName)) {
        nc = Util.removeQuotes(currentTokenValue);
      }
      if ("cnonce".equals(currentTokenName)) {
        cnonce = Util.removeQuotes(currentTokenValue);
      }
      if ("qop".equals(currentTokenName)) {
        qop = Util.removeQuotes(currentTokenValue);
      }
      if ("uri".equals(currentTokenName)) {
        uri = Util.removeQuotes(currentTokenValue);
      }
      if ("response".equals(currentTokenName)) {
        response = Util.removeQuotes(currentTokenValue);
      }
    }

    if (username == null
        || username.length() < 6
        || realm == null
        || nonce == null
        || uri == null
        || response == null) {
      throw new TR069AuthenticationException(
          "Digest challenge response does not contain all necessary parameters (CPE IP address: "
              + reqRes.getRawRequest().getRemoteHost()
              + ") (username: "
              + username
              + ")",
          null,
          HttpServletResponse.SC_FORBIDDEN);
    }

    // Do database read parameters and then perform verification
    String unitId = Util.username2unitId(username);
    Log.debug(
        DigestAuthenticator.class,
        "Digest verification identifed unit id "
            + unitId
            + " from CPE IP-address "
            + reqRes.getRawRequest().getRemoteHost());
    try {
      SessionData sessionData = reqRes.getSessionData();
      sessionData.setUnitId(unitId);
      sessionData.updateParametersFromDB(unitId, isDiscoveryMode);
      BaseCache.putSessionData(unitId, sessionData);
      String secret = sessionData.getAcsParameters().getValue(SystemParameters.SECRET);
      if (secret != null
          && secret.length() > 16
          && !passwordMd5(username, secret, method, uri, nonce, nc, cnonce, qop).equals(response)) {
        secret = secret.substring(0, 16);
      }
      if (secret == null) {
        throw new TR069AuthenticationException(
            "No ACS Password found in database (CPE IP address: "
                + reqRes.getRawRequest().getRemoteHost()
                + ") (username: "
                + username
                + ")",
            null,
            HttpServletResponse.SC_FORBIDDEN);
      } else {
        String sharedSecretMd5 = passwordMd5(username, secret, method, uri, nonce, nc, cnonce, qop);
        if (!sharedSecretMd5.equals(response)) {
          throw new TR069AuthenticationException(
              "Incorrect ACS Password (CPE IP address: "
                  + reqRes.getRawRequest().getRemoteHost()
                  + ") (username: "
                  + username
                  + ")",
              null,
              HttpServletResponse.SC_FORBIDDEN);
        } else {
          Log.notice(
              DigestAuthenticator.class,
              "Authentication verified (CPE IP address: " + reqRes.getRawRequest().getRemoteHost() + ")");
          return true;
        }
      }
    } catch (SQLException e) {
      throw new TR069AuthenticationException(
          "Authentication failed because of database error (CPE IP address: "
              + reqRes.getRawRequest().getRemoteHost()
              + ") (username: "
              + username
              + ")",
          e,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (NoDataAvailableException e) {
      throw new TR069AuthenticationException(
          "Authentication failed because unitid was not found (CPE IP address: "
              + reqRes.getRawRequest().getRemoteHost()
              + ") (username: "
              + username
              + ")",
          e,
          HttpServletResponse.SC_FORBIDDEN);
    }
  }
}
