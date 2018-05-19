package com.github.freeacs.base.db;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.SessionDataI;
import com.github.freeacs.dbi.*;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DBAccess {

	private final DataSource xapsDataSource;
	private final DataSource syslogDataSource;
	private final String facilityVersion;
	private final int facility;

	private DBI dbi;

	public DBAccess(int facilityInt, String facilityVersionStr, DataSource xapsDataSource, DataSource syslogDataSource) {
		this.facility = facilityInt;
		this.facilityVersion = facilityVersionStr;
		this.xapsDataSource = xapsDataSource;
		this.syslogDataSource = syslogDataSource;
	}

	public int getFacility() {
		return facility;
	}

	private static void error(String message, Throwable t) {
		Log.error(DBAccess.class, message, t);
	}

	public Syslog getSyslog() throws SQLException {
		Users users = new Users(getXapsDataSource());
		Identity id = new Identity(facility, facilityVersion, users.getUnprotected(Users.USER_ADMIN));
		return new Syslog(getSyslogDataSource(), id);
	}

	public synchronized DBI getDBI() throws SQLException {
		XAPS.setStrictOrder(false);
		if (dbi == null) {
			Syslog syslog = getSyslog();
			dbi = new DBI(Integer.MAX_VALUE, getXapsDataSource(), syslog);
		}
		return dbi;
	}

	public static Job getJob(SessionDataI sessionData, String id) {
		return sessionData.getUnittype().getJobs().getById(new Integer(id));
	}

	static void handleError(String method, Throwable t) throws SQLException {
		error(method + " failed", t);
		if (t instanceof SQLException) {
			throw (SQLException) t;
		}
		throw (RuntimeException) t;
	}

	public static XAPSUnit getXAPSUnit(XAPS xaps) throws SQLException {
		return new XAPSUnit(xaps.getDataSource(), xaps, xaps.getSyslog());
	}


	public DataSource getXapsDataSource() {
		return xapsDataSource;
	}

	public DataSource getSyslogDataSource() {
		return syslogDataSource;
	}
}
