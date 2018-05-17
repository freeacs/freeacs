package com.github.freeacs.web.app.security.handlers;


import com.github.freeacs.web.app.security.WebUser;

import javax.sql.DataSource;

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
	boolean scramblePasswordWithMD5();
	
	/**
	 * Authenticates a username and password.
	 * 
	 * @param username the username
	 * @param password the password
	 * @param sessionId the servlet session id
	 * @return WebUser the validated (or rejected) user
	 * @throws Exception throws everything
	 */
	WebUser authenticateUser(String username, String password, String sessionId, DataSource xapsDataSource) throws Exception;
}
