package com.owera.xaps.base.db;

import java.sql.SQLException;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.xaps.Properties.Module;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.SessionDataI;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Identity;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.Syslog;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;

public class DBAccess {

	private static DBI dbi;

	private static Module module;
	private static String facilityVersion;
	private static int facility;

	public static void init(Module mod, int facilityInt, String facilityVersionStr) {
		module = mod;
		facility = facilityInt;
		facilityVersion = facilityVersionStr;
	}
	
	public static Module getModule() {
		return module;
	}
	
	public static String getFacilityVersion() {
		return facilityVersion;
	}
	
	public static int getFacility() {
		return facility;
	}

	public static ConnectionProperties getXAPSProperties() {
		return ConnectionProvider.getConnectionProperties("xaps-" + module.getModuleName() + ".properties", "db.xaps");
	}

	public static ConnectionProperties getSyslogProperties() {
		return ConnectionProvider.getConnectionProperties("xaps-" + module.getModuleName() + ".properties", "db.syslog");
	}

	@SuppressWarnings("unused")
	private static void warn(String message) {
		Log.warn(DBAccess.class, message);
	}

	private static void error(String message, Throwable t) {
		Log.error(DBAccess.class, message, t);
	}

	public static Syslog getSyslog() throws SQLException {
		Users users = new Users(getXAPSProperties());
		Identity id = new Identity(facility, facilityVersion, users.getUnprotected(Users.USER_ADMIN));
		Syslog syslog = new Syslog(getSyslogProperties(), id);
		return syslog;
	}

	public synchronized static DBI getDBI() throws SQLException {
		XAPS.setStrictOrder(false);
		if (dbi == null) {
			Syslog syslog = getSyslog();
			dbi = new DBI(Integer.MAX_VALUE, getXAPSProperties(), syslog);
		}
		return dbi;
	}

	public static Job getJob(SessionDataI sessionData, String id) throws SQLException {
		return sessionData.getUnittype().getJobs().getById(new Integer(id));
	}

	public static void handleError(String method, long start, Throwable t) throws SQLException {
		error(method + " failed", t);
		if (t instanceof SQLException) {
			throw (SQLException) t;
		}
		throw (RuntimeException) t;
	}

	public static XAPSUnit getXAPSUnit(XAPS xaps) throws SQLException {
		return new XAPSUnit(getXAPSProperties(), xaps, xaps.getSyslog());
	}
}
