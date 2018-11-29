package com.github.freeacs.http;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.BaseCacheException;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.xml.TR069TransactionID;
import java.sql.SQLException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HTTPRequestResponseData {
  private final DBAccess dbAccess;

  private HTTPRequestData requestData;

  private HTTPResponseData responseData;

  private HttpServletRequest rawRequest;

  private HttpServletResponse rawResponse;

  private Throwable throwable;

  private TR069TransactionID TR069TransactionID;

  private SessionData sessionData;

  protected HTTPRequestResponseData(HttpServletRequest rawRequest, HttpServletResponse rawResponse, DBAccess dbAccess)
      throws SQLException {
    this.rawRequest = rawRequest;
    this.rawResponse = rawResponse;
    this.requestData = new HTTPRequestData();
    this.responseData = new HTTPResponseData();
    this.dbAccess = dbAccess;

    String sessionId = rawRequest.getSession().getId();
    try {
      sessionData = (SessionData) BaseCache.getSessionData(sessionId);
    } catch (BaseCacheException tr069Ex) {
      HttpSession session = rawRequest.getSession();
      Log.debug(
          HTTPRequestResponseData.class,
          "Sessionid "
              + sessionId
              + " did not return a SessionData object from cache, must create a new SessionData object");
      Log.debug(
          HTTPRequestResponseData.class,
          "Sessionid "
              + session.getId()
              + " created: "
              + session.getCreationTime()
              + ", lastAccess:"
              + session.getLastAccessedTime()
              + ", mxInactiveInterval:"
              + session.getMaxInactiveInterval());
      sessionData = new SessionData(sessionId, dbAccess.getDBI().getAcs());
      BaseCache.putSessionData(sessionId, sessionData);
    }
    if (sessionData.getStartupTmsForSession() == null) {
      sessionData.setStartupTmsForSession(System.currentTimeMillis());
    }
    Log.debug(HTTPRequestResponseData.class, "Adding a HTTPReqResData object to the list");
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

  public HttpServletResponse getRawResponse() {
    return rawResponse;
  }

  public SessionData getSessionData() {
    return sessionData;
  }

  public DBAccess getDbAccess() {
    return dbAccess;
  }

  public String getRealIPAddress() {
    return Optional.ofNullable(rawRequest.getHeader("X-Real-IP")).orElseGet(() -> rawRequest.getRemoteAddr());
  }
}
