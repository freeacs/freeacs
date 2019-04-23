package com.github.freeacs.base.db;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.Users;
import lombok.Data;

import java.sql.SQLException;
import javax.sql.DataSource;

@Data
public class DBAccess {

  private final DataSource dataSource;

  private final DBI dbi;

  public DBAccess(int facilityInt, String facilityVersionStr, DataSource dataSource) throws SQLException {
    this.dataSource = dataSource;
    ACS.setStrictOrder(false);
    Users users = new Users(getDataSource());
    Identity id = new Identity(facilityInt, facilityVersionStr, users.getUnprotected(Users.USER_ADMIN));
    Syslog syslog = new Syslog(dataSource, id);
    this.dbi = new DBI(Integer.MAX_VALUE, dataSource, syslog);
  }
}
