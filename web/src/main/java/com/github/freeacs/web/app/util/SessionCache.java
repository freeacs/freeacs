package com.github.freeacs.web.app.util;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogFilter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.report.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * The Session Store for xAPS Web.
 * Uses session id as unique identifier.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public class SessionCache {
	
	/** The cache. */
	private static Cache cache = new Cache();

	/** The Constant UNIT_SESSION_TIMEOUT. */
	private static final long UNIT_SESSION_TIMEOUT = 15 * 1000; // 15 seconds
	
	/** The Constant SYSLOG_RESULT_TIMEOUT. */
	private static final long SYSLOG_RESULT_TIMEOUT = 60 * 1000; // 60 seconds
	
	/** The Constant SYSLOG_EXPORT_TIMEOUT. */
	private static final long SYSLOG_EXPORT_TIMEOUT = 60 * 60 * 1000; // 60 seconds
	
	/** The Constant SYSLOG_RESULT_CACHE_KEY_FORMAT. */
	private static final SimpleDateFormat SYSLOG_RESULT_CACHE_KEY_FORMAT = new SimpleDateFormat("yyyyMMddHH");

	/** The CONTEX t_ path. */
	public static String CONTEXT_PATH = "";

	/**
	 * Reset.
	 */
	public static void reset() {
		cache = new Cache();
	}

	/**
	 * Key.
	 *
	 * @param sessionId the session id
	 * @param keypart the keypart
	 * @return the string
	 */
	private static String key(String sessionId, String keypart) {
		return sessionId + keypart;
	}

	/**
	 * Put dbi.
	 *
	 * @param sessionId the session id
	 * @param xapsCache the xaps cache
	 * @param lifeTimeSec the life time sec
	 */
	public static void putDBI(String sessionId, DBI xapsCache, int lifeTimeSec) {
		if (xapsCache == null) {
			cache.remove(key(sessionId, "dbi"));
		} else {
			String key = key(sessionId, "dbi");
			cache.put(key, new CacheValue(xapsCache, Cache.SESSION, (lifeTimeSec * 1000)));
		}
	}

	/**
	 * Gets the dBI.
	 *
	 * @param sessionId the session id
	 * @return the dBI
	 */
	public static DBI getDBI(String sessionId) {
		if (cache.get(key(sessionId, "dbi")) != null)
			return (DBI) cache.get(key(sessionId, "dbi")).getObject();
		return null;
	}

	/**
	 * Put unit.
	 *
	 * @param sessionId the session id
	 * @param unit the unit
	 */
	public static void putUnit(String sessionId, Unit unit) {
		if (unit != null) {
			String key = key(sessionId, unit.getId());
			cache.put(key, new CacheValue(unit, Cache.SESSION, UNIT_SESSION_TIMEOUT));
		}
	}

	/**
	 * Gets the unit.
	 *
	 * @param sessionId the session id
	 * @param unitId the unit id
	 * @return the unit
	 */
	public static Unit getUnit(String sessionId, String unitId) {
		if (cache.get(key(sessionId, unitId)) != null)
			return (Unit) cache.get(key(sessionId, unitId)).getObject();
		return null;
	}

	/**
	 * Gets the session data.
	 *
	 * @param sessionId the session id
	 * @return the session data
	 */
	public static SessionData getSessionData(String sessionId) {
		CacheValue cv = cache.get(key(sessionId, "sessionData"));
		if (cv == null) {
			SessionData sessionData = new SessionData();
			cache.put(key(sessionId, "sessionData"), new CacheValue(sessionData, Cache.SESSION, Long.MAX_VALUE));
			return sessionData;
		}
		return (SessionData) cv.getObject();
	}

	/**
	 * Removes the session data.
	 *
	 * @param sessionId the session id
	 */
	public static void removeSessionData(String sessionId) {
		cache.remove(key(sessionId, "sessionData"));
	}

	/**
	 * Put syslog connection properties.
	 *
	 * @param sessionId the session id
	 * @param props the props
	 */
	public static void putSyslogConnectionProperties(String sessionId, DataSource props) {
		if (props == null)
			cache.remove(key(sessionId, "syslogprops"));
		else
			cache.put(key(sessionId, "syslogprops"), new CacheValue(props, Cache.SESSION, Long.MAX_VALUE));
	}

	/**
	 * Put connection properties.
	 *
	 * @param sessionId the session id
	 * @param props the props
	 */
	public static void putXAPSConnectionProperties(String sessionId, DataSource props) {
		if (props == null)
			cache.remove(key(sessionId, "xapsprops"));
		else
			cache.put(key(sessionId, "xapsprops"), new CacheValue(props, Cache.SESSION, Long.MAX_VALUE));
	}

	/**
	 * Gets the connection properties.
	 *
	 * @param sessionId the session id
	 * @return the connection properties
	 */
	public static DataSource getXAPSConnectionProperties(String sessionId) {
		CacheValue cv = cache.get(key(sessionId, "xapsprops"));
		if (cv == null)
			return null;
		return (DataSource) cv.getObject();
	}

	/**
	 * Put syslog entries.
	 *
	 * @param sessionId the session id
	 * @param entries the entries
	 */
	public static void putSyslogEntries(String sessionId, List<SyslogEntry> entries) {
		if (entries == null)
			cache.remove(key(sessionId, "syslogresults"));
		else
			cache.put(key(sessionId, "syslogresults"), new CacheValue(entries, Cache.SESSION, SYSLOG_EXPORT_TIMEOUT));
	}

	/**
	 * Gets the syslog entries.
	 *
	 * @param sessionId the session id
	 * @return the syslog entries
	 */
	@SuppressWarnings("unchecked")
	public static List<SyslogEntry> getSyslogEntries(String sessionId) {
		CacheValue cv = cache.get(key(sessionId, "syslogresults"));
		if (cv == null)
			return null;
		List<SyslogEntry> object = (List<SyslogEntry>) cv.getObject();
		return object;
	}


	/**
	 * Get voip report for a given unit.
	 *
	 * @param sessionId the session id
	 * @param unitId the unit id
	 * @param fromDate the from date
	 * @param toDate the to date
	 * @param xapsDataSource
	 * @param syslogDataSource
	 * @return The report. Is null when report functionality is not supported by the database.
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	public static Report<RecordVoip> getVoipReport(String sessionId, String unitId, Date fromDate, Date toDate, DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException, IOException {
		String key = getRangeKey(unitId,"voipcalls",fromDate,toDate);
		
		CacheValue cv = cache.get(key);
		if(cv == null){
			ReportVoipGenerator rg = new ReportVoipGenerator(xapsDataSource, syslogDataSource, XAPSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource),null,XAPSLoader.getIdentity(sessionId, xapsDataSource));
			Report<RecordVoip> value = rg.generateFromSyslog(fromDate, toDate, unitId);
			cv = new CacheValue(value,Cache.ABSOLUTE,SYSLOG_RESULT_TIMEOUT);
			cache.put(key, cv);
		}

		return (Report<RecordVoip>) cv.getObject();
	}
	
	/**
	 * Gets the range key.
	 *
	 * @param unit the unit
	 * @param descriptor the descriptor
	 * @param from the from
	 * @param to the to
	 * @return the range key
	 */
	private static String getRangeKey(String unit,String descriptor,Date from,Date to){
		return key(unit,descriptor+SYSLOG_RESULT_CACHE_KEY_FORMAT.format(from)+SYSLOG_RESULT_CACHE_KEY_FORMAT.format(to));
	}
	
	private static String getSyslogRangeKey(String unit,String descriptor,Date from,Date to,String syslogFilter){
		return key(unit,descriptor+SYSLOG_RESULT_CACHE_KEY_FORMAT.format(from)+SYSLOG_RESULT_CACHE_KEY_FORMAT.format(to)+syslogFilter);
	}
	
	/**
	 * Converts a voip report to a given period type.
	 *
	 * @param report the report
	 * @param type the type
	 * @return The changed report if period type is different, or the same report unchanged.
	 */
	public static Report<RecordVoip> convertVoipReport(Report<RecordVoip> report,PeriodType type){
		if(type!=null && report.getPeriodType().getTypeInt()!=type.getTypeInt())
			report = ReportConverter.convertVoipReport(report, type);
		return report;
	}
	
	/**
	 * Gets the hardware report.
	 *
	 * @param sessionId the session id
	 * @param unitId the unit id
	 * @param fromDate the from date
	 * @param toDate the to date
	 * @param xapsDataSource
	 * @param syslogDataSource
	 * @return the hardware report
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	public static Report<RecordHardware> getHardwareReport(String sessionId, String unitId, Date fromDate, Date toDate, DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException, IOException {
		String key = getRangeKey(unitId,"hardwarereport",fromDate,toDate);
		
		CacheValue cv = cache.get(key);
		if(cv == null){
			ReportHardwareGenerator rg = new ReportHardwareGenerator(xapsDataSource, syslogDataSource, XAPSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource), null,XAPSLoader.getIdentity(sessionId, xapsDataSource));
			Report<RecordHardware> value = rg.generateFromSyslog(fromDate, toDate, unitId);
			cv = new CacheValue(value,Cache.ABSOLUTE,SYSLOG_RESULT_TIMEOUT);
			cache.put(key, cv);
		}

		return (Report<RecordHardware>) cv.getObject();
	}
	
	/**
	 * Convert hardware report.
	 *
	 * @param report the report
	 * @param type the type
	 * @return the report
	 */
	public static Report<RecordHardware> convertHardwareReport(Report<RecordHardware> report,PeriodType type){
		if(type!=null && report.getPeriodType().getTypeInt()!=type.getTypeInt())
			report = ReportConverter.convertHardwareReport(report, type);
		return report;
	}
	
	/**
	 * Gets the syslog report.
	 *
	 * @param sessionId the session id
	 * @param unitId the unit id
	 * @param fromDate the from date
	 * @param toDate the to date
	 * @param syslogFilter
	 * @param xapsDataSource
	 * @param syslogDataSource
	 * @return the syslog report
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 */
	@SuppressWarnings("unchecked")
	public static Report<RecordSyslog> getSyslogReport(String sessionId, String unitId, Date fromDate, Date toDate, String syslogFilter, DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException, IOException, ParseException {
		String key = getSyslogRangeKey(unitId,"syslogreport",fromDate,toDate,syslogFilter);
		
		CacheValue cv = cache.get(key);
		if(cv == null){
			SyslogFilter filter = new SyslogFilter();
			filter.setMessage(syslogFilter);
			ReportSyslogGenerator rg = new ReportSyslogGenerator(xapsDataSource, syslogDataSource, XAPSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource), null,XAPSLoader.getIdentity(sessionId, xapsDataSource));
			rg.setSyslogFilter(filter);
			Report<RecordSyslog> value = rg.generateFromSyslog(fromDate, toDate, unitId);
			cv = new CacheValue(value,Cache.ABSOLUTE,SYSLOG_RESULT_TIMEOUT);
			cache.put(key, cv);
		}

		return (Report<RecordSyslog>) cv.getObject();
	}

	/**
	 * Convert syslog report.
	 *
	 * @param report the report
	 * @param type the type
	 * @return the report
	 */
	public static Report<RecordSyslog> convertSyslogReport(Report<RecordSyslog> report,PeriodType type){
		if(type!=null && report.getPeriodType().getTypeInt()!=type.getTypeInt())
			report = ReportConverter.convertSyslogReport(report, type);
		return report;
	}

	public static Cache getCache() {
		return cache;
	}
}