package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.ws.xml.Login;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ACSFactory {
  private static Logger logger = LoggerFactory.getLogger(ACSFactory.class);

  private DataSource xapsDataSource;
  private DataSource syslogDataSource;
  private Identity id;
  private ACS acs;
  private boolean initialized;

  public ACSFactory(Login login, int lifetimeSec, DataSource xaps, DataSource syslog)
      throws RemoteException {
    this.xapsDataSource = xaps;
    this.syslogDataSource = syslog;
    init(login, lifetimeSec);
  }

  public ACS getAcs() {
    return acs;
  }

  public ACSUnit getXAPSUnit(ACS acs) throws RemoteException {
    try {
      return new ACSUnit(xapsDataSource, acs, acs.getSyslog());
    } catch (Throwable t) {
      String msg = "An exception occured while retrieving XAPSUnit object";
      logger.error(msg, t);
      throw new RemoteException(msg, t);
    }
  }

  private synchronized void init(Login login, int lifetimeSec) throws RemoteException {
    if (!initialized) {
      try {
        Users users = new Users(xapsDataSource);
        User user = users.getUnprotected(login.getUsername());
        if (user == null) {
          throw error("The user " + login.getUsername() + " is unknown");
        }

        id = new Identity(SyslogConstants.FACILITY_WEBSERVICE, "2.0.1-SNAPSHOT", user);
        //	private Unittypes allowedUnittypes;
        Syslog syslog = new Syslog(syslogDataSource, id);
        DBI dbi = new DBI(lifetimeSec + 30, xapsDataSource, syslog);
        acs = dbi.getAcs();
        if (!login.getPassword().getValue().equals(user.getSecret())
            && !user.isCorrectSecret(login.getPassword().getValue())) {
          throw error("The password is incorrect");
        }
        // At this stage we have a positive authentication of the user
        initialized = true;
      } catch (Throwable t) {
        String msg = "An exception occured while initializing XAPSWS object: " + t;
        logger.error(msg, t);
        throw new RemoteException(msg, t);
      }
    }
  }

  public Unit getUnitByMAC(ACSUnit acsUnit, Unittype unittype, Profile profile, String searchStr)
      throws RemoteException, SQLException {
    Unit unitFoundByMac = acsUnit.getUnitByValue(searchStr, unittype, profile);
    if (unitFoundByMac != null) {
      return unitFoundByMac;
    } else {
      throw error(
          "The serialNumber/unique value was not found in xAPS on unittype "
              + unittype.getName()
              + " and profile "
              + profile.getName());
    }
  }

  private RemoteException error(String msg) {
    logger.error(msg);
    return new RemoteException(msg);
  }

  public static RemoteException error(Logger logger, String msg) throws RemoteException {
    logger.error(msg);
    return new RemoteException(msg);
  }

  public static RemoteException error(Logger logger, Throwable t) throws RemoteException {
    String msg = "An exception occurred: " + t.getMessage();
    logger.error(msg, t);
    return new RemoteException(msg, t);
  }

  /**
   * This method will return the object from DBI if it can find it. Reasons for not finding it is
   * wrong name specified by the clients or no access to the object for this user. However,
   * returning the object does not implicate all actions are allowed on this object (like
   * add/change/delete). This permission check is performed in DBI, and will throw
   * IllegalArgumentExceptions if it occur.
   *
   * @param unittypeName
   * @return
   * @throws RemoteException
   */
  protected Unittype getUnittypeFromXAPS(String unittypeName) throws RemoteException {
    if (unittypeName == null) {
      throw error("The unittype name is not specified");
    }
    Unittype unittype = acs.getUnittype(unittypeName);
    if (unittype == null) {
      throw error("The unittype " + unittypeName + " is not found/allowed in xAPS");
    }
    return unittype;
  }

  /**
   * This method will return the object from DBI if it can find it. Reasons for not finding it is
   * wrong name specified by the clients or no access to the object for this user. However,
   * returning the object does not implicate all actions are allowed on this object (like
   * add/change/delete). This permission check is performed in DBI, and will throw
   * IllegalArgumentExceptions if it occur.
   *
   * @param unittypeName
   * @param profileName
   * @return
   * @throws RemoteException
   */
  public Profile getProfileFromXAPS(String unittypeName, String profileName)
      throws RemoteException {
    Unittype unittype = getUnittypeFromXAPS(unittypeName);
    if (profileName == null) {
      throw error("The profile name is not specified");
    }
    Profile profile = unittype.getProfiles().getByName(profileName);
    if (profile == null) {
      throw error("The profile " + profileName + " is not found/allowed in xAPS");
    }
    return profile;
  }

  public Identity getId() {
    return id;
  }
}
