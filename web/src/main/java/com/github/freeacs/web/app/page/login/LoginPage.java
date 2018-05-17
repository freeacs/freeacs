package com.github.freeacs.web.app.page.login;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

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

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#requiresNoCache()
	 */
	@Override
	public boolean requiresNoCache() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser req, Output outputHandler, DataSource xapsDataSource, DataSource syslogDataSource) throws Exception {
		LoginData inputData = (LoginData) InputDataRetriever.parseInto(new LoginData(), req);

		if (inputData.getLogoff().getString() != null && inputData.getLogoff().getString().equals("true")) {
			clearSession(req);
			outputHandler.setDirectToPage(Page.LOGIN);
			return;
		}

		SessionData sessionData = SessionCache.getSessionData(req.getSession().getId());

		String target = sessionData.getLastLoginTarget();
		if (target == null || target.contains("?page=login"))
			outputHandler.setDirectToPage(Page.SEARCH);
		else
			outputHandler.setRedirectTarget(target);

		sessionData.setLastLoginTarget(null);
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
