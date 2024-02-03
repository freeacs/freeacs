package com.github.freeacs.tr069.http;

import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.base.BaseCache;
import com.github.freeacs.tr069.base.BaseCacheException;
import com.github.freeacs.tr069.xml.TR069TransactionID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Getter
@Slf4j
public class HTTPRequestResponseData {

  private final HTTPRequestData requestData;

  private final HTTPResponseData responseData;

  private final HttpServletRequest rawRequest;

  @Setter
  private Throwable throwable;

  @Setter
  private TR069TransactionID TR069TransactionID;

  private SessionData sessionData;

  public HTTPRequestResponseData(HttpServletRequest rawRequest) {
    this.rawRequest = rawRequest;
    this.requestData = new HTTPRequestData();
    this.responseData = new HTTPResponseData();

    String sessionId = rawRequest.getSession().getId();
    try {
      sessionData = (SessionData) BaseCache.getSessionData(sessionId);
    } catch (BaseCacheException tr069Ex) {
      HttpSession session = rawRequest.getSession();
      log.debug("Sessionid "
              + sessionId
              + " did not return a SessionData object from cache, must create a new SessionData object");
      log.debug("Sessionid "
              + session.getId()
              + " created: "
              + session.getCreationTime()
              + ", lastAccess:"
              + session.getLastAccessedTime()
              + ", mxInactiveInterval:"
              + session.getMaxInactiveInterval());
      sessionData = new SessionData(sessionId);
      BaseCache.putSessionData(sessionId, sessionData);
    }
    if (sessionData.getStartupTmsForSession() == null) {
      sessionData.setStartupTmsForSession(System.currentTimeMillis());
    }
    log.debug("Adding a HTTPReqResData object to the list");
    sessionData.getReqResList().add(this);
  }

  public String getRealIPAddress() {
    return Optional.ofNullable(rawRequest.getHeader("X-Real-IP")).orElseGet(() -> rawRequest.getRemoteAddr());
  }
}
