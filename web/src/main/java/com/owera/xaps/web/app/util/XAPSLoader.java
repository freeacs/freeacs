package com.owera.xaps.web.app.util;

import java.sql.SQLException;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Identity;
import com.owera.xaps.dbi.Syslog;
import com.owera.xaps.dbi.SyslogConstants;
import com.owera.xaps.dbi.User;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.web.app.Main;
import com.owera.xaps.web.app.Monitor;

/**
 * This class contains code for 
 * loading xaps objects from cache 
 * and storing them to cache.
 * Even though it is possible to make another XAPS object by your self,
 * with this function you can do it with a one liner!
 * 
 * @author Jarl Andre Hubenthal
 * 
 */
public class XAPSLoader {

	/**
	 * Gets the dBI.
	 *
	 * @param sessionId the session id
	 * @return the dBI
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public static DBI getDBI(String sessionId) throws SQLException, NoAvailableConnectionException {
		DBI dbi = SessionCache.getDBI(sessionId);
		try {
			int sessionTimeoutSecs = getSessionTimeout() * 60;
			if (dbi == null || dbi.isFinished()) {
				ConnectionProperties cp = SessionCache.getXAPSConnectionProperties(sessionId);
				if (cp == null)
					return null;
				Identity ident = getIdentity(sessionId);
				ConnectionProperties syscp = SessionCache.getSyslogConnectionProperties(sessionId);
				if (syscp == null)
					return null;
				Syslog syslog = new Syslog(syscp, ident);
				dbi = new DBI(sessionTimeoutSecs, cp, syslog);
				SessionCache.putDBI(sessionId, dbi, sessionTimeoutSecs);
			}
			Monitor.setLastDBILogin(null);
		} catch (Throwable t) {
			Monitor.setLastDBILogin(t);
			// Make sure all exceptions are thrown out of this method
			if (t instanceof SQLException)
				throw (SQLException) t;
			if (t instanceof NoAvailableConnectionException)
				throw (NoAvailableConnectionException) t;
			if (t instanceof RuntimeException)
				throw (RuntimeException) t;
		}
		return dbi;
	}

	/**
	 * Gets the xAPS.
	 *
	 * @param sessionId the session id
	 * @return the xAPS
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public static XAPS getXAPS(String sessionId) throws NoAvailableConnectionException, SQLException {
		DBI dbi = getDBI(sessionId);
		if (dbi != null)
			return dbi.getXaps();
		return null;
	}

	/**
	 * Gets the session timeout.
	 *
	 * @return the session timeout
	 */
	public static int getSessionTimeout() {
		return WebProperties.getSessionTimeout();
	}

	/**
	 * Gets the identity.
	 *
	 * @param sessionId the session id
	 * @return the identity
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public static Identity getIdentity(String sessionId) throws SQLException, NoAvailableConnectionException {
		User user = SessionCache.getSessionData(sessionId).getUser();
		if (user == null)
			user = getDefaultUser(sessionId);
		return new Identity(SyslogConstants.FACILITY_WEB, Main.version, user);
	}

	/**
	 * Gets the default user.
	 *
	 * @param sessionId the session id
	 * @return the default user
	 * @throws NoAvailableConnectionException 
	 * @throws SQLException 
	 */
	private static User getDefaultUser(String sessionId) throws SQLException, NoAvailableConnectionException {
		Users users = new Users(SessionCache.getXAPSConnectionProperties(sessionId));
		User user = new User("anonymous", null, null, false, users);
		return user;
	}

	/**
	 * Gets the xAPS unit.
	 *
	 * @param sessionId the session id
	 * @return the xAPS unit
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public static XAPSUnit getXAPSUnit(String sessionId) throws NoAvailableConnectionException, SQLException {
		ConnectionProperties cp = SessionCache.getXAPSConnectionProperties(sessionId);
		if (cp == null)
			return null;
		XAPS xaps = getDBI(sessionId).getXaps();
		if (xaps == null)
			return null;
		Identity ident = getIdentity(sessionId);
		ConnectionProperties syscp = SessionCache.getSyslogConnectionProperties(sessionId);
		if (syscp == null)
			return null;
		Syslog syslog = new Syslog(syscp, ident);
		return new XAPSUnit(cp, xaps, syslog);
	}
}