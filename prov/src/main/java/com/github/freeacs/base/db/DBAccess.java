package com.github.freeacs.base.db;

import com.github.freeacs.Properties;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.SessionDataI;
import com.github.freeacs.dbi.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

public class DBAccess {

	private static final Config config = ConfigFactory.load();
	private static Logger logger = LoggerFactory.getLogger(DBAccess.class);
	private final DataSource xapsDataSource;
	private final DataSource syslogDataSource;
	private final Properties.Module module;
	private final String facilityVersion;
	private final int facility;

	private DBI dbi;

	public DBAccess(Properties.Module mod, int facilityInt, String facilityVersionStr, DataSource xapsDataSource, DataSource syslogDataSource) {
		this.module = mod;
		this.facility = facilityInt;
		this.facilityVersion = facilityVersionStr;
		this.xapsDataSource = xapsDataSource;
		this.syslogDataSource = syslogDataSource;
	}

	public Properties.Module getModule() {
		return module;
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

	public static Job getJob(SessionDataI sessionData, String id) throws SQLException {
		return sessionData.getUnittype().getJobs().getById(new Integer(id));
	}

	static void handleError(String method, long start, Throwable t) throws SQLException {
		error(method + " failed", t);
		if (t instanceof SQLException) {
			throw (SQLException) t;
		}
		throw (RuntimeException) t;
	}

	public XAPSUnit getXAPSUnit(XAPS xaps) throws SQLException {
		return new XAPSUnit(getXapsDataSource(), xaps, xaps.getSyslog());
	}

    private static int getInteger(String propertyKey, int defaultValue) {
        if (!config.hasPath(propertyKey)) {
            logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
            return defaultValue;
        }
        try {
            return config.getInt(propertyKey);
        } catch (Throwable t) {
            logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
            return defaultValue;
        }
    }

    private static long getLong(String propertyKey, long defaultValue) {
        if (!config.hasPath(propertyKey)) {
            logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
            return defaultValue;
        }
        try {
            return config.getLong(propertyKey);
        } catch (Throwable t) {
            logger.warn("The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
            return defaultValue;
        }
    }


    private static String getString(String propertyKey, String defaultValue) {
        if (!config.hasPath(propertyKey)) {
            logger.warn("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
            return defaultValue;
        }
        return config.getString(propertyKey);
    }


    public static int getMaxConn(final String infix) {
        return getInteger("db." + infix + ".maxconn", 20);
    }

    public static long getMaxAge(final String infix) {
        return getLong("db." + infix + ".maxage", 60000);
    }

    public static String getUrl(final String infix) {
        return Optional.ofNullable(getString("db." + infix + ".url", null))
                .orElseGet(new Supplier<String>() {
                    @Override
                    public String get() {
                        return getString("db." +infix, null);
                    }
                });
    }

	public DataSource getXapsDataSource() {
		return xapsDataSource;
	}

	public DataSource getSyslogDataSource() {
		return syslogDataSource;
	}
}
