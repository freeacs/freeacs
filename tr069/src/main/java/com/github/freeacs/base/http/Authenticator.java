package com.github.freeacs.base.http;

import com.github.freeacs.base.Log;
import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.exception.TR069AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Authenticator {

  private static Cache blockedClients = new Cache();
  private static int blockedClientsCount = 0;

  private static String computeBlockedClientKey(HttpServletRequest req) {
    String remoteHost = req.getRemoteHost();
    String authorization = req.getHeader("authorization");
    if (authorization == null)
      return null; // probably a challenge - no blocking at that stage
    String username = authorization; // use raw header if basic auth, since the
                                     // header is always the same
    int startPos = authorization.indexOf("username=") + 9;

    if (startPos > 8) { // digest auth, must extract username, since header
                        // varies in every request
      int endPos = authorization.indexOf("=", startPos);
      if (endPos > -1)
        username = authorization.substring(startPos, endPos);
      else
        username = authorization.substring(startPos);
    }
    return remoteHost + username;
  }

  public static int getAndResetBlockedClientsCount() {
    int tmp = blockedClientsCount;
    blockedClientsCount = 0;
    return tmp;
  }

  private static boolean block(HTTPReqResData reqRes, String bcKey) {
    if (bcKey != null) {
      CacheValue cv = blockedClients.get(bcKey);
      if (cv != null) {
        int count = (Integer) cv.getObject();
        if (count >= 5) {
          reqRes.getRes().setStatus(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
          blockedClientsCount++;
          return true;
        }
      }
    }
    return false;
  }

  private static void incBlockCounter(String bcKey) {
    if (bcKey != null) {
      CacheValue cv = blockedClients.get(bcKey);
      if (cv != null) {
        cv.setObject(((Integer) cv.getObject()) + 1);
      } else {
        blockedClients.put(bcKey, new CacheValue(1, Cache.SESSION, 5 * 60 * 1000));
      }
    }
  }

  public static boolean authenticate(HTTPReqResData reqRes) throws TR069AuthenticationException {

    SessionData sessionData = reqRes.getSessionData();
    if (sessionData.isAuthenticated()) {
      return true;
    }

    // Code for early return of a non-authorized device with no logging or fuss
    // - minimize impact of
    // devices which constantly tries to log on to Fusion (without success).
    // If a device performs 5 non-authorized login attempts with less than 5
    // minutes
    // between each attempt, the device will be blocked by this code. The only
    // way for the
    // device to be allowed into verification is to be silent for more than 5
    // minutes.
    String bcKey = computeBlockedClientKey(reqRes.getReq());
    if (block(reqRes, bcKey))
      return false;

    // Start of normal authentication procedure
    boolean authenticated = true; // default
    String auth_method = Properties.AUTH_METHOD;
    try {
      if (Properties.DISCOVERY_MODE) {
        authenticated = BasicAuthenticator.authenticate(reqRes);
      } else {
        if (auth_method.equalsIgnoreCase("basic"))
          authenticated = BasicAuthenticator.authenticate(reqRes);
        else if (auth_method.equalsIgnoreCase("digest"))
          authenticated = DigestAuthenticator.authenticate(reqRes);
        else if (auth_method.equalsIgnoreCase("none")) {
          authenticated = true;
          Log.debug(Authenticator.class, "No authentication method was required");
        } else {
          throw new TR069AuthenticationException("The authentication method is " + auth_method + ", but no impl. exist for this method (CPE IP address: " + reqRes.getReq().getRemoteHost() + ")",
              null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
      }
    } catch (TR069AuthenticationException ex) {
      incBlockCounter(bcKey);
      // Something failed in the verification step - the device is not allowed
      // or something like that
      throw ex;
    }
    if (authenticated) {
      sessionData.setAuthenticated(true);
    }
    if (authenticated) { // all checks are passed - cleanup
      blockedClients.remove(bcKey);
    }
    return authenticated;
  }
}
