package com.github.freeacs.base.db;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.Users;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DBAccess {

  private static DBAccess instance;

  private final DataSource dataSource;
  private final String facilityVersion;
  private final int facility;

  private DBI dbi;

  private DBAccess(int facilityInt, String facilityVersionStr, DataSource dataSource) {
    this.facility = facilityInt;
    this.facilityVersion = facilityVersionStr;
    this.dataSource = dataSource;
  }

  public Syslog getSyslog() throws SQLException {
    Users users = new Users(getDataSource());
    Identity id = new Identity(facility, facilityVersion, users.getUnprotected(Users.USER_ADMIN));
    return new Syslog(dataSource, id);
  }

  public synchronized DBI getDBI() throws SQLException {
    ACS.setStrictOrder(false);
    if (dbi == null) {
      Syslog syslog = getSyslog();
      dbi = new DBI(Integer.MAX_VALUE, getDataSource(), syslog);
    }
    return dbi;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public static DBAccess createInstance(int facilityInt, String facilityVersionStr, DataSource dataSource) {
    instance = new DBAccess(facilityInt, facilityVersionStr, dataSource);
    return instance;
  }

  public static DBAccess getInstance() {
    return instance;
  }
}
