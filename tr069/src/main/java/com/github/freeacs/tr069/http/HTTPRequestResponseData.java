package com.github.freeacs.tr069.http;

import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.base.BaseCache;
import com.github.freeacs.tr069.base.BaseCacheException;
import com.github.freeacs.tr069.xml.TR069TransactionID;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Slf4j
public class HTTPRequestResponseData {

  private HTTPRequestData requestData;

  private HTTPResponseData responseData;

  private HttpServletRequest rawRequest;

  private Throwable throwable;

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

  public HTTPRequestData getRequestData() {
    return requestData;
  }

  public HTTPResponseData getResponseData() {
    return responseData;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  public TR069TransactionID getTR069TransactionID() {
    return TR069TransactionID;
  }

  public void setTR069TransactionID(TR069TransactionID transactionID) {
    TR069TransactionID = transactionID;
  }

  public HttpServletRequest getRawRequest() {
    return rawRequest;
  }

  public SessionData getSessionData() {
    return sessionData;
  }

  public String getRealIPAddress() {
    return Optional.ofNullable(rawRequest.getHeader("X-Real-IP")).orElseGet(() -> rawRequest.getRemoteAddr());
  }
}
