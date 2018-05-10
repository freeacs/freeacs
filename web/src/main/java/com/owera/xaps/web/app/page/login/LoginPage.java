package com.owera.xaps.web.app.page.login;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.SessionData;

import javax.servlet.http.HttpSession;

import static com.owera.xaps.web.app.util.WebProperties.*;

/**
 * The Login page is responsible for retrieving connection properties and clearing session (logging out).
 * 
 * This page is not the same as the security login page, that is handled by the LoginServlet.
 * 
 * Does not effectively log you in, the naming is a left over from refactoring. 
 * 
 * It checks if connection properties are set, and if, redirects to last target it set, and if not, to the search page.
 * 
 * @author Jarl Andre Hubenthal
 */
public class LoginPage extends AbstractWebPage {

	/** The input data. */
	private LoginData inputData;

	/** The session id. */
	private String sessionId;

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#requiresNoCache()
	 */
	@Override
	public boolean requiresNoCache() {
		return true;
	}

	/**
	 * Gets the xAPS connection properties.
	 * @return the xAPS connection properties
	 */
	public static ConnectionProperties getXAPSConnectionProperties() {
		String url = getUrl("xaps");
		Long maxAge = getMaxAge("xaps");
		Integer maxConn = getMaxConn("xaps");
		return ConnectionProvider.getConnectionProperties(url, maxAge, maxConn);
	}

	public static ConnectionProperties getSyslogConnectionProperties() {
		String url = getUrl("syslog");
		Long maxAge = getMaxAge("syslog");
		Integer maxConn = getMaxConn("syslog");
		return ConnectionProvider.getConnectionProperties(url, maxAge, maxConn);
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser req, Output outputHandler) throws Exception {
		inputData = (LoginData) InputDataRetriever.parseInto(new LoginData(), req);

		sessionId = req.getSession().getId();

		if (inputData.getLogoff().getString() != null && inputData.getLogoff().getString().equals("true")) {
			clearSession(req);

			outputHandler.setDirectToPage(Page.LOGIN);

			return;
		}

		if (SessionCache.getXAPSConnectionProperties(req.getSession().getId()) != null) {
			SessionData sessionData = SessionCache.getSessionData(req.getSession().getId());

			String target = sessionData.getLastLoginTarget();
			if (target == null || target.contains("?page=login"))
				outputHandler.setDirectToPage(Page.SEARCH);
			else
				outputHandler.setRedirectTarget(target);

			sessionData.setLastLoginTarget(null);

			return;
		}

		SessionCache.putXAPSConnectionProperties(sessionId, null);

		outputHandler.setTemplatePath("/databasespage.ftl");
	}

	/**
	 * Clear session.
	 *
	 * @param req the req
	 */
	public static void clearSession(ParameterParser req) {
		HttpSession session = req.getSession();
		SessionCache.putXAPSConnectionProperties(session.getId(), null);
		SessionCache.putSyslogConnectionProperties(session.getId(), null);
		SessionCache.removeSessionData(session.getId());
		DBI dbi = SessionCache.getDBI(session.getId());
		if (dbi != null) {
			dbi.setLifetimeSec(0);
			SessionCache.putDBI(session.getId(), null, 0);
		}
		session.invalidate();
	}
}
