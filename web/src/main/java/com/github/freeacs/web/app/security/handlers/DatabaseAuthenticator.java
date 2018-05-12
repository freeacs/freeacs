package com.github.freeacs.web.app.security.handlers;

import com.github.freeacs.common.db.ConnectionProperties;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.owera.xaps.web.app.page.login.LoginPage;
import com.owera.xaps.web.app.security.WebUser;
import com.owera.xaps.web.app.util.SessionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DatabaseAuthenticator.
 *
 * @author Jarl Andre Hubenthal
 */
public class DatabaseAuthenticator implements Authenticator {
	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(DatabaseAuthenticator.class);

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.security.AuthenticationInterface#authenticateUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	public WebUser authenticateUser(String username, String password, String sessionId) throws Exception {
		ConnectionProperties props = SessionCache.getXAPSConnectionProperties(sessionId);
		logger.debug("Running authenticateUser in DatabaseAuthenticator");
		if (props == null) {
			props = LoginPage.getXAPSConnectionProperties();
			if (props == null)
				throw new LoginException("Login handler is set to database, but connection properties is not overriden.");
		}
		Users users = new Users(props);
		WebUser dbUser = null;
		User userObject = users.getUnprotected(username);
		if (userObject != null) {
			logger.debug("Password from database [" + userObject.getSecret().toUpperCase() + "]");
			logger.debug("Password from user     [" + password.toUpperCase() + "]");
			boolean authenticated = userObject.getSecret().toUpperCase().equals(password.toUpperCase());
			if (authenticated)
				logger.info("Found user with name " + username + ", password matched - login is accepted");
			else
				logger.warn("Found user with name " + username + ", password did not match");
			dbUser = new WebUser(userObject, authenticated);
		} else {
			logger.warn("Did not find user with name " + username);
			dbUser = new WebUser(userObject, false);
		}
		return dbUser;
	}

	public class LoginException extends Exception {
		private static final long serialVersionUID = 1L;

		public LoginException(String msg) {
			super("A security exception occured: " + msg);
		}
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.security.AuthenticationInterface#scramblePasswordWithMD5()
	 */
	public boolean scramblePasswordWithMD5() {
		return true;
	}

}
