package com.owera.xaps.dbi.report;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.DynamicStatement;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Identity;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.SyslogConstants;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.User;
import com.owera.xaps.dbi.XAPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReportSyslogGenerator extends ReportGenerator {

	private static Logger logger = LoggerFactory.getLogger(ReportProvisioningGenerator.class);

	public ReportSyslogGenerator(ConnectionProperties sysCp, ConnectionProperties xapsCp, XAPS xaps, String logPrefix, Identity id) {
		super(sysCp, xapsCp, xaps, logPrefix, id);
	}

	public Report<RecordSyslog> generateFromReport(PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs) throws NoAvailableConnectionException, SQLException, IOException {
		Connection xapsConnection = null;
		Connection sysConnection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			boolean foundDataInReportTable = false;
			Report<RecordSyslog> report = new Report<RecordSyslog>(RecordSyslog.class, periodType);
			
			logger.debug(logPrefix + "SyslogReport: Reads from report_syslog table from " + start + " to " + end);
			xapsConnection = ConnectionProvider.getConnection(xapsCp, true);
			DynamicStatement ds = selectReportSQL("report_syslog", periodType, start, end, uts, prs);
			ps = ds.makePreparedStatement(xapsConnection);
			rs = ps.executeQuery();
			int counter = 0;
			while (rs.next()) {
				counter++;
				start = rs.getTimestamp("timestamp_");
				String unittypeName = rs.getString("unit_type_name");
				String profileName = rs.getString("profile_name");
				String severity = rs.getString("severity");
				String eventId = rs.getString("syslog_event_id");
				String facility = rs.getString("facility");
				RecordSyslog recordTmp = new RecordSyslog(start, periodType, unittypeName, profileName, severity, eventId, facility);
				Key key = recordTmp.getKey();
				RecordSyslog record = report.getRecord(key);
				if (record == null)
					record = recordTmp;
				record.getMessageCount().add(rs.getInt("unit_count"));
				report.setRecord(key, record);
				foundDataInReportTable = true;
			}
			if (foundDataInReportTable)
				logger.debug(logPrefix + "SyslogReport: Have read " + counter + " rows, last tms was " + start + ", report is now " + report.getMap().size() + " entries");
			return report;
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (xapsConnection != null)
				ConnectionProvider.returnConnection(xapsConnection, sqle);
			if (sysConnection != null)
				ConnectionProvider.returnConnection(sysConnection, sqle);
		}

	}

	public Report<RecordSyslog> generateFromSyslog(Date start, Date end, String unitId) throws NoAvailableConnectionException, SQLException, IOException, ParseException {
		return generateFromSyslog(PeriodType.SECOND, start, end, null, null, unitId, null);
	}

	public Map<String, Report<RecordSyslog>> generateFromSyslog(PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs, Group group) throws NoAvailableConnectionException,
			SQLException, IOException, ParseException {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			logInfo("SyslogReport", null, uts, prs, start, end);

			String sqlFormat = "%Y%m"; // default PeriodType = MONTH
			String javaFormat = "yyyyMM"; // default PeriodType = MONTH
			if (periodType == PeriodType.SECOND) {
				sqlFormat += "%d%H%i%s";
				javaFormat += "ddHHmmss";
			}
			if (periodType == PeriodType.MINUTE) {
				sqlFormat += "%d%H%i";
				javaFormat += "ddHHmm";
			}
			if (periodType == PeriodType.HOUR) {
				sqlFormat += "%d%H";
				javaFormat += "ddHH";
			}
			if (periodType == PeriodType.DAY) {
				sqlFormat += "%d";
				javaFormat += "dd";
			}
			SimpleDateFormat tmsFormatter = new SimpleDateFormat(javaFormat);
			Map<String, Unit> unitsInGroup = getUnitsInGroup(group);
			DynamicStatement ds = new DynamicStatement();
			ds.addSql("SELECT date_format(collector_timestamp, '" + sqlFormat + "'), unit_type_name, profile_name, unit_id, severity, syslog_event_id, facility, count(*) ");
			ds.addSqlAndArguments("FROM syslog WHERE collector_timestamp >= ? AND ", start);
			if (swVersion != null)
				ds.addSqlAndArguments("facility_version = ?  AND ", swVersion);
			else if (syslogFilter != null && syslogFilter.getFacilityVersion() != null)
				ds.addSqlAndArguments("facility_version = ?  AND ", syslogFilter.getFacilityVersion());
			if (syslogFilter != null && syslogFilter.getMessage() != null)
				ds.addSqlAndArguments("content LIKE ? AND ", syslogFilter.getMessage());
			addUnittypeOrProfileCriteria(ds, uts, prs);
			//			ds.addSql(" AND unit_type_name <> '' AND profile_name <> '' ");
			ds.addSqlAndArguments("collector_timestamp < ? ", end);
			ds.addSql("GROUP BY date_format(collector_timestamp, '" + sqlFormat + "'), unit_type_name, profile_name, unit_id, severity, syslog_event_id, facility");
			c = ConnectionProvider.getConnection(sysCp);
			ps = ds.makePreparedStatement(c);
			rs = ps.executeQuery();
			Map<String, Report<RecordSyslog>> unitReportMap = new HashMap<String, Report<RecordSyslog>>();
			int entries = 0;
			while (rs.next()) {
				Date tms = tmsFormatter.parse(rs.getString(1));
				String unitId = rs.getString("unit_id");
				if (group != null && unitsInGroup.get(unitId) == null)
					continue;
				entries++;
				if (unitId == null || unitId.trim().equals(""))
					unitId = "Unknown";
				String unittypeName = rs.getString("unit_type_name");
				if (unittypeName == null || unittypeName.trim().equals(""))
					unittypeName = "Unknown";
				String profileName = rs.getString("profile_name");
				if (profileName == null || profileName.trim().equals(""))
					profileName = "Unknown";
				String severity = SyslogConstants.getSeverityName(rs.getInt("severity"));
				String eventId = rs.getString("syslog_event_id");
				String facility = SyslogConstants.getFacilityName(rs.getInt("facility"));
				Report<RecordSyslog> report = unitReportMap.get(unitId);
				if (report == null) {
					report = new Report<RecordSyslog>(RecordSyslog.class, periodType);
					unitReportMap.put(unitId, report);
				}
				RecordSyslog recordTmp = new RecordSyslog(tms, periodType, unittypeName, profileName, severity, eventId, facility);
				Key key = recordTmp.getKey();
				RecordSyslog record = report.getRecord(key);
				if (record == null)
					record = recordTmp;
				record.setMessageCount(new Counter());
				record.getMessageCount().add(rs.getInt("count(*)"));
				report.setRecord(key, record);
			}
			
			logger.info(logPrefix + "SyslogReport: Have read " + entries + " rows from syslog, " + unitReportMap.size() + " units are mapped");
			return unitReportMap;
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqle);
		}
	}

	public Report<RecordSyslog> generateFromSyslog(PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs, String unitId, Group group) throws SQLException,
			NoAvailableConnectionException, IOException, ParseException {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			Report<RecordSyslog> report = new Report<RecordSyslog>(RecordSyslog.class, periodType);
			logInfo("SyslogReport", unitId, uts, prs, start, end);

			String sqlFormat = "%Y%m"; // default PeriodType = MONTH
			String javaFormat = "yyyyMM"; // default PeriodType = MONTH
			if (periodType == PeriodType.SECOND) {
				sqlFormat += "%d%H%i%s";
				javaFormat += "ddHHmmss";
			}
			if (periodType == PeriodType.MINUTE) {
				sqlFormat += "%d%H%i";
				javaFormat += "ddHHmm";
			}
			if (periodType == PeriodType.HOUR) {
				sqlFormat += "%d%H";
				javaFormat += "ddHH";
			}
			if (periodType == PeriodType.DAY) {
				sqlFormat += "%d";
				javaFormat += "dd";
			}
			SimpleDateFormat tmsFormatter = new SimpleDateFormat(javaFormat);

			int entries = 0;
			if (group != null) {
				Map<String, Unit> unitsInGroup = getUnitsInGroup(group);
				DynamicStatement ds = new DynamicStatement();
				ds.addSql("SELECT date_format(collector_timestamp, '" + sqlFormat + "'), unit_type_name, profile_name, severity, syslog_event_id, facility, unit_id ");
				ds.addSqlAndArguments("FROM syslog WHERE collector_timestamp >= ? AND ", start);
				if (unitId != null)
					ds.addSqlAndArguments("unit_id = ? AND ", unitId);
				else
					addUnittypeOrProfileCriteria(ds, uts, prs);
				if (swVersion != null)
					ds.addSqlAndArguments("facility_version = ?  AND ", swVersion);
				else if (syslogFilter != null && syslogFilter.getFacilityVersion() != null)
					ds.addSqlAndArguments("facility_version = ?  AND ", syslogFilter.getFacilityVersion());
				if (syslogFilter != null && syslogFilter.getMessage() != null)
					ds.addSqlAndArguments("content LIKE ? AND ", syslogFilter.getMessage());
				ds.addSqlAndArguments("collector_timestamp < ? ", end);
				c = ConnectionProvider.getConnection(sysCp);
				ps = ds.makePreparedStatement(c);
				rs = ps.executeQuery();
				while (rs.next()) {
					String unitIdTmp = rs.getString("unit_id");
					if (unitsInGroup.get(unitIdTmp) == null)
						continue;
					entries++;
					Date tms = tmsFormatter.parse(rs.getString(1));
					String unittypeName = rs.getString("unit_type_name");
					if (unittypeName == null || unittypeName.trim().equals(""))
						unittypeName = "Unknown";
					String profileName = rs.getString("profile_name");
					if (profileName == null || profileName.trim().equals(""))
						profileName = "Unknown";
					String severity = SyslogConstants.getSeverityName(rs.getInt("severity"));
					String eventId = rs.getString("syslog_event_id");
					String facility = SyslogConstants.getFacilityName(rs.getInt("facility"));
					RecordSyslog recordTmp = new RecordSyslog(tms, periodType, unittypeName, profileName, severity, eventId, facility);
					Key key = recordTmp.getKey();
					RecordSyslog record = report.getRecord(key);
					if (record == null)
						record = recordTmp;
					record.getMessageCount().add(1);
					report.setRecord(key, record);
				}
			} else {
				DynamicStatement ds = new DynamicStatement();
				ds.addSql("SELECT date_format(collector_timestamp, '" + sqlFormat + "'), unit_type_name, profile_name, severity, syslog_event_id, facility, count(*) ");
				ds.addSqlAndArguments("FROM syslog WHERE collector_timestamp >= ? AND ", start);
				if (unitId != null)
					ds.addSqlAndArguments("unit_id = ? AND ", unitId);
				else
					addUnittypeOrProfileCriteria(ds, uts, prs);
				if (swVersion != null)
					ds.addSqlAndArguments("facility_version = ?  AND ", swVersion);
				else if (syslogFilter != null && syslogFilter.getFacilityVersion() != null)
					ds.addSqlAndArguments("facility_version = ?  AND ", syslogFilter.getFacilityVersion());
				if (syslogFilter != null && syslogFilter.getMessage() != null)
					ds.addSqlAndArguments("content LIKE ? AND ", syslogFilter.getMessage());
				ds.addSqlAndArguments("collector_timestamp < ? ", end);
				ds.addSql("GROUP BY date_format(collector_timestamp, '" + sqlFormat + "'), unit_type_name, profile_name, severity, syslog_event_id, facility");
				c = ConnectionProvider.getConnection(sysCp);
				ps = ds.makePreparedStatement(c);
				rs = ps.executeQuery();
				while (rs.next()) {
					entries++;
					Date tms = tmsFormatter.parse(rs.getString(1));
					String unittypeName = rs.getString("unit_type_name");
					if (unittypeName == null || unittypeName.trim().equals(""))
						unittypeName = "Unknown";
					String profileName = rs.getString("profile_name");
					if (profileName == null || profileName.trim().equals(""))
						profileName = "Unknown";
					String severity = SyslogConstants.getSeverityName(rs.getInt("severity"));
					String eventId = rs.getString("syslog_event_id");
					String facility = SyslogConstants.getFacilityName(rs.getInt("facility"));
					RecordSyslog recordTmp = new RecordSyslog(tms, periodType, unittypeName, profileName, severity, eventId, facility);
					Key key = recordTmp.getKey();
					RecordSyslog record = report.getRecord(key);
					if (record == null)
						record = recordTmp;
					record.setMessageCount(new Counter());
					record.getMessageCount().add(rs.getInt("count(*)"));
					report.setRecord(key, record);
				}
				logger.info(logPrefix + "SyslogReport: Using [" + ds.getSqlQuestionMarksSubstituted() + "]");
			}
			
			logger.info(logPrefix + "SyslogReport: Have read " + entries + " rows from syslog");

			return report;
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqle);
		}

	}

	private boolean allUnittypesSpecified(List<Profile> profiles, Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified) {
		Set<Integer> unittypesWithAllProfilesSpecified = new HashSet<Integer>();
		boolean allUnittypesSpecified = false;
		XAPS xaps = profiles.get(0).getUnittype().getXaps();
		int noUnittypes = xaps.getUnittypes().getUnittypes().length; // the number of unittypes in xAPS
		for (Profile profile : profiles) {
			Integer unittypeId = profile.getUnittype().getId();
			Set<Profile> profilesInUnittype = unittypesWithSomeProfilesSpecified.get(unittypeId);
			if (profilesInUnittype == null)
				profilesInUnittype = new HashSet<Profile>();
			profilesInUnittype.add(profile);
			if (unittypesWithAllProfilesSpecified.contains(unittypeId))
				continue;
			unittypesWithSomeProfilesSpecified.put(unittypeId, profilesInUnittype);
			int noProfiles = profile.getUnittype().getProfiles().getProfiles().length;
			// populate and delete (logically: move from "someSpecified" to "allSpecified")
			if (unittypesWithSomeProfilesSpecified.get(unittypeId).size() == noProfiles) {
				unittypesWithAllProfilesSpecified.add(unittypeId);
				unittypesWithSomeProfilesSpecified.remove(unittypeId);
			}
		}
		if (noUnittypes == unittypesWithAllProfilesSpecified.size())
			allUnittypesSpecified = true;
		return allUnittypesSpecified;
	}

	private DynamicStatement addUnittypeOrProfileCriteria(DynamicStatement ds, List<Unittype> unittypes, List<Profile> profiles) {
		User user = id.getUser();
		//		Permissions permissionsObj = user.getPermissions();
		//		if (filter.getProfile() != null) {
		//			ds.addSqlAndArguments("profile_name = ? AND unit_type_name = ? AND ", filter.getProfile().getName(), filter.getProfile().getUnittype().getName());
		//		} else 
		if (profiles != null && profiles.size() > 0) {
			Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified = new HashMap<Integer, Set<Profile>>();
			boolean allUnittypesSpecified = allUnittypesSpecified(profiles, unittypesWithSomeProfilesSpecified);
			if (user.isAdmin() && allUnittypesSpecified)
				return ds; // no criteria added -> quicker search,  will search for all unittypes/profiles
			ds.addSql("(");
			for (int i = 0; i < profiles.size(); i++) {
				Profile profile = profiles.get(i);
				boolean allProfilesSpecified = (unittypesWithSomeProfilesSpecified.get(profile.getUnittype().getId()) == null ? true : false);
				// all profiles in unittype are specified, we can skip profiles criteria
				if (allProfilesSpecified && user.isUnittypeAdmin(profile.getUnittype().getId())) {
					boolean alreadyTreated = false;
					for (int j = 0; j < i; j++) {
						Profile p = profiles.get(j);
						if (p.getId().equals(profile.getId()))
							alreadyTreated = true;
					}
					if (!alreadyTreated) // To avoid repeating "unit_type_name = ?" with the same arguments many times - the SQL becomes ugly
						ds.addSqlAndArguments("unit_type_name = ? OR ", profile.getUnittype().getName());
				} else
					// have to specify profiles since not all are specified or we do not know of all profile (not UnittypeAdmin)
					ds.addSqlAndArguments("(profile_name = ? AND unit_type_name = ?) OR ", profile.getName(), profile.getUnittype().getName());
			}
			ds.cleanupSQLTail();
			ds.addSql(") AND ");
			//		} else if (filter.getUnittype() != null) {
			//			XAPS xaps = filter.getUnittype().getXaps();
			//			int noUnittypes = xaps.getUnittypes().getUnittypes().length;
			//			boolean isAdmin = true;
			//			if (permissionsObj != null)
			//				isAdmin = permissionsObj.isAdmin();
			//			if ((isAdmin && noUnittypes > 1) || !isAdmin)
			//				ds.addSqlAndArguments("unit_type_name = ? AND ", filter.getUnittype().getName());
		} else if (unittypes != null && unittypes.size() > 0) {
			XAPS xaps = unittypes.get(0).getXaps();
			int noUnittypes = xaps.getUnittypes().getUnittypes().length;
			boolean isAdmin = user.isAdmin();
			if (noUnittypes > unittypes.size() || !isAdmin) {
				ds.addSql("(");
				for (Unittype unittype : unittypes) {
					ds.addSqlAndArguments("unit_type_name = ? OR ", unittype.getName());
				}
				ds.cleanupSQLTail();
				ds.addSql(") AND ");
			} else {
				// no criteria added, all unittypes are specified and user isAdmin
			}
		}
		return ds;
	}

	//	private DynamicStatement addUnittypeOrProfileCriteria(DynamicStatement ds, List<Unittype> unittypes, List<Profile> profiles) {
	//		Permissions permissionsObj = id.getUser().getPermissions();
	//		if (profiles != null && profiles.size() > 0) {
	//			Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified = new HashMap<Integer, Set<Profile>>();
	//			Set<Integer> unittypesWithAllProfilesSpecified = new HashSet<Integer>();
	//			boolean allUnittypesSpecified = false;
	//			XAPS xaps = profiles.get(0).getUnittype().getXaps();
	//			int noUnittypes = xaps.getUnittypes().getUnittypes().length; // the number of unittypes in xAPS
	//			boolean isAdmin = true; // backward comp. before users where supported
	//			if (permissionsObj != null)
	//				isAdmin = permissionsObj.isAdmin();
	//			for (Profile profile : profiles) {
	//				Integer unittypeId = profile.getUnittype().getId();
	//				Set<Profile> profilesInUnittype = unittypesWithSomeProfilesSpecified.get(unittypeId);
	//				if (profilesInUnittype == null)
	//					profilesInUnittype = new HashSet<Profile>();
	//				profilesInUnittype.add(profile);
	//				if (unittypesWithAllProfilesSpecified.contains(unittypeId))
	//					continue;
	//				unittypesWithSomeProfilesSpecified.put(unittypeId, profilesInUnittype);
	//				int noProfiles = profile.getUnittype().getProfiles().getProfiles().length;
	//				// populate and delete (logically: move from "someSpecified" to "allSpecified")
	//				if (unittypesWithSomeProfilesSpecified.get(unittypeId).size() == noProfiles) {
	//					unittypesWithAllProfilesSpecified.add(unittypeId);
	//					unittypesWithSomeProfilesSpecified.remove(unittypeId);
	//				}
	//			}
	//			if (noUnittypes == unittypesWithAllProfilesSpecified.size())
	//				allUnittypesSpecified = true;
	//			if (allUnittypesSpecified && isAdmin)
	//				return ds; // no criteria added - will search for all unittypes/profiles
	//			ds.addSql("(");
	//			for (Profile profile : profiles) {
	//				Set<Profile> someProfilesSpecified = unittypesWithSomeProfilesSpecified.get(profile.getUnittype().getId());
	//				if (someProfilesSpecified != null) {
	//					ds.addSqlAndArguments("(profile_name = ? AND unit_type_name = ?) OR ", profile.getName(), profile.getUnittype().getName());
	//				} else { // all profiles in unittype are specified
	//					boolean isUnittypeAdmin = true;
	//					if (permissionsObj != null)
	//						isUnittypeAdmin = permissionsObj.allowed(profile.getUnittype());
	//					if (isUnittypeAdmin)
	//						ds.addSqlAndArguments("unit_type_name = ? OR ", profile.getUnittype().getName());
	//					else
	//						ds.addSqlAndArguments("(profile_name = ? AND unit_type_name = ?) OR ", profile.getName(), profile.getUnittype().getName());
	//				}
	//			}
	//			ds.cleanupSQLTail();
	//			ds.addSql(") AND ");
	//		} else if (unittypes != null && unittypes.size() > 0) {
	//			XAPS xaps = unittypes.get(0).getXaps();
	//			int noUnittypes = xaps.getUnittypes().getUnittypes().length;
	//			boolean isAdmin = true;
	//			if (permissionsObj != null)
	//				isAdmin = permissionsObj.isAdmin();
	//			if ((isAdmin && noUnittypes > unittypes.size()) || !isAdmin) {
	//				ds.addSql("(");
	//				for (Unittype unittype : unittypes) {
	//					ds.addSqlAndArguments("unit_type_name = ? OR ", unittype.getName());
	//				}
	//				ds.cleanupSQLTail();
	//				ds.addSql(") AND ");
	//			}
	//		}
	//		return ds;
	//	}

}
