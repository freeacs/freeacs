package com.github.freeacs.base.db;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.Users;
import lombok.Data;
import lombok.Getter;

import java.sql.SQLException;
import javax.sql.DataSource;

@Data
public class DBAccess {

  @Getter
  private static DBAccess instance;

  private final DataSource dataSource;
  private final String facilityVersion;
  private final int facility;

  private final DBI dbi;

  private DBAccess(int facilityInt, String facilityVersionStr, DataSource dataSource) throws SQLException {
    this.facility = facilityInt;
    this.facilityVersion = facilityVersionStr;
    this.dataSource = dataSource;
    ACS.setStrictOrder(false);
    this.dbi = new DBI(Integer.MAX_VALUE, getDataSource(), getSyslog());
  }

  public Syslog getSyslog() throws SQLException {
    Users users = new Users(getDataSource());
    Identity id = new Identity(facility, facilityVersion, users.getUnprotected(Users.USER_ADMIN));
    return new Syslog(dataSource, id);
  }

  public static void createInstance(int facilityInt, String facilityVersionStr, DataSource dataSource) throws SQLException {
    instance = new DBAccess(facilityInt, facilityVersionStr, dataSource);
  }
}
