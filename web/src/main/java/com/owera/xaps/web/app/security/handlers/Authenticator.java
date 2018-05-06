package com.owera.xaps.web.app.security.handlers;

import com.owera.xaps.web.app.security.WebUser;



/**
 * The interface for defining authentication handlers.
 * 
 * @author Jarl Andre Hubenthal
 */
public interface Authenticator {
	
	/**
	 * Tells xAPS Web wether or not to scramble the password before sending it to the login servlet.
	 * 
	 * @return true or false
	 */
	public boolean scramblePasswordWithMD5();
	
	/**
	 * Authenticates a username and password.
	 * 
	 * @param username the username
	 * @param password the password
	 * @param sessionId the servlet session id
	 * @return WebUser the validated (or rejected) user
	 * @throws Exception throws everything
	 */
	public WebUser authenticateUser(String username,String password, String sessionId) throws Exception;
}
