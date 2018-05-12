package com.github.freeacs.web.app.security.handlers;

import com.github.freeacs.web.app.security.WebUser;
import org.apache.commons.lang.NotImplementedException;


/**
 * The Class LdapAuthenticator.
 *
 * @author Jarl Andre Hubenthal
 */
@Deprecated
public class LdapAuthenticator implements Authenticator{

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.security.AuthenticationInterface#scramblePasswordWithMD5()
	 */
	@Override
	public boolean scramblePasswordWithMD5() {
		throw new NotImplementedException("LdapAuthenticator.scramblePasswordWithMD5() is not implemented");
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.security.AuthenticationInterface#authenticateUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public WebUser authenticateUser(String username, String password, String sessionId) throws Exception {
		throw new NotImplementedException("LdapAuthenticator.authenticateUser() is not implemented");
	}
}
