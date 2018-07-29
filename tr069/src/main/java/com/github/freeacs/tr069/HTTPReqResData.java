package com.github.freeacs.tr069;

import com.github.freeacs.base.BaseCache;
import com.github.freeacs.base.BaseCacheException;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.tr069.xml.TR069TransactionID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Optional;

public class HTTPReqResData {
	private final DBAccess dbAccess;

	private HTTPReqData request;

	private HTTPResData response;

	private HttpServletRequest req;

	private HttpServletResponse res;

	private Throwable throwable;

	private TR069TransactionID TR069TransactionID;

	private SessionData sessionData;

	public HTTPReqResData(HttpServletRequest req, HttpServletResponse res, DBAccess dbAccess) throws SQLException {
		this.req = req;
		this.res = res;
		this.request = new HTTPReqData();
		this.response = new HTTPResData();
		this.dbAccess = dbAccess;

		String sessionId = req.getSession().getId();
		try {
			sessionData = (SessionData) BaseCache.getSessionData(sessionId);
		} catch (BaseCacheException tr069Ex) {
			HttpSession session = req.getSession();
			Log.debug(HTTPReqResData.class, "Sessionid " + sessionId + " did not return a SessionData object from cache, must create a new SessionData object");
			Log.debug(HTTPReqResData.class, "Sessionid " + session.getId() + " created: " + session.getCreationTime() + ", lastAccess:" + session.getLastAccessedTime() + ", mxInactiveInterval:" + session.getMaxInactiveInterval());
			sessionData = new SessionData(sessionId, dbAccess.getDBI().getAcs());
			BaseCache.putSessionData(sessionId, sessionData);
		}
		if (sessionData.getStartupTmsForSession() == null)
			sessionData.setStartupTmsForSession(System.currentTimeMillis());
		Log.debug(HTTPReqResData.class, "Adding a HTTPReqResData object to the list");
		sessionData.getReqResList().add(this);
	}

	public HTTPReqData getRequest() {
		return request;
	}

	public HTTPResData getResponse() {
		return response;
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

	public HttpServletRequest getReq() {
		return req;
	}

	public HttpServletResponse getRes() {
		return res;
	}

	public SessionData getSessionData() {
		return sessionData;
	}

	public DBAccess getDbAccess() {
		return dbAccess;
	}

	public String getRealIPAddress() {
		return Optional.ofNullable(req.getHeader("X-Real-IP")).orElseGet(() -> req.getRemoteAddr());
	}
}
