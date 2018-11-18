package com.github.freeacs.web.app.util;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.app.Monitor;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * This class contains code for loading xaps objects from cache and storing them to cache. Even
 * though it is possible to make another XAPS object by your self, with this function you can do it
 * with a one liner!
 *
 * @author Jarl Andre Hubenthal
 */
public class ACSLoader {
  /**
   * Gets the dBI.
   *
   * @param sessionId the session id
   * @param mainDataSource
   * @param syslogDataSource
   * @return the dBI
   * @throws SQLException the sQL exception the no available connection exception
   */
  public static DBI getDBI(String sessionId, DataSource mainDataSource, DataSource syslogDataSource)
      throws SQLException {
    DBI dbi = SessionCache.getDBI(sessionId);
    try {
      int sessionTimeoutSecs = getSessionTimeout() * 60;
      if (dbi == null || dbi.isFinished()) {
        Identity ident = getIdentity(sessionId, mainDataSource);
        Syslog syslog = new Syslog(syslogDataSource, ident);
        dbi = new DBI(sessionTimeoutSecs, mainDataSource, syslog);
        SessionCache.putDBI(sessionId, dbi, sessionTimeoutSecs);
      }
      Monitor.setLastDBILogin(null);
    } catch (Throwable t) {
      Monitor.setLastDBILogin(t);
      // Make sure all exceptions are thrown out of this method
      if (t instanceof SQLException) {
        throw (SQLException) t;
      }
      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
    }
    return dbi;
  }

  /**
   * Gets the xAPS.
   *
   * @param sessionId the session id
   * @param mainDataSource
   * @param syslogDataSource
   * @return the xAPS the no available connection exception
   * @throws SQLException the sQL exception
   */
  public static ACS getXAPS(
      String sessionId, DataSource mainDataSource, DataSource syslogDataSource)
      throws SQLException {
    DBI dbi = getDBI(sessionId, mainDataSource, syslogDataSource);
    if (dbi != null) {
      return dbi.getAcs();
    }
    return null;
  }

  /**
   * Gets the session timeout.
   *
   * @return the session timeout
   */
  public static int getSessionTimeout() {
    return WebProperties.getInstance().getSessionTimeout();
  }

  /**
   * Gets the identity.
   *
   * @param sessionId the session id
   * @param dataSource
   * @return the identity
   * @throws SQLException the sQL exception the no available connection exception
   */
  public static Identity getIdentity(String sessionId, DataSource dataSource) throws SQLException {
    User user = SessionCache.getSessionData(sessionId).getUser();
    if (user == null) {
      user = getDefaultUser(dataSource);
    }
    return new Identity(SyslogConstants.FACILITY_WEB, "latest", user);
  }

  /**
   * Gets the default user.
   *
   * @param dataSource
   * @return the default user
   * @throws SQLException
   */
  private static User getDefaultUser(DataSource dataSource) throws SQLException {
    Users users = new Users(dataSource);
    return new User("anonymous", null, null, false, users);
  }

  /**
   * Gets the xAPS unit.
   *
   * @param sessionId the session id
   * @param mainDataSource
   * @param syslogDataSource
   * @return the xAPS unit the no available connection exception
   * @throws SQLException the sQL exception
   */
  public static ACSUnit getACSUnit(
      String sessionId, DataSource mainDataSource, DataSource syslogDataSource)
      throws SQLException {
    ACS acs = getDBI(sessionId, mainDataSource, syslogDataSource).getAcs();
    if (acs == null) {
      return null;
    }
    Identity ident = getIdentity(sessionId, acs.getDataSource());
    Syslog syslog = new Syslog(syslogDataSource, ident);
    return new ACSUnit(acs.getDataSource(), acs, syslog);
  }
}
