package com.owera.xaps.dbi.report;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.DynamicStatement;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Identity;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.SyslogFilter;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;

public class ReportGenerator {

	private static Logger logger = new Logger();

	protected TmsConverter converter = new TmsConverter();
	protected ConnectionProperties sysCp;
	protected ConnectionProperties xapsCp;
	protected XAPS xaps;
	protected Identity id;
	protected String logPrefix = "";

	/* report filters - make report based on these parameters (if possible) */
	protected PeriodType periodType = null;
	protected Date start = null;
	protected Date end = null;
	protected List<Unittype> unittypes = null;
	protected List<Profile> profiles = null;
	protected Group group = null;
	protected String swVersion = null;
	protected SyslogFilter syslogFilter = null;

	public void resetFilters() {
		periodType = null;
		start = null;
		end = null;
		unittypes = null;
		profiles = null;
		group = null;
		swVersion = null;
	}

	public ReportGenerator(ConnectionProperties sysCp, ConnectionProperties xapsCp, XAPS xaps, String logPrefix, Identity id) {
		this.sysCp = sysCp;
		this.xapsCp = xapsCp;
		this.xaps = xaps;
		this.id = id;
		if (logPrefix != null)
			this.logPrefix = logPrefix;

	}

	protected DynamicStatement selectReportSQL(String tableName, PeriodType pt, Date start, Date end, List<Unittype> uts, List<Profile> prs) {
		DynamicStatement ds = new DynamicStatement();
		if (pt == PeriodType.MONTH)
			ds.addSqlAndArguments("select * from " + tableName + " where period_type = ? and ", PeriodType.DAY.getTypeInt());
		else
			ds.addSqlAndArguments("select * from " + tableName + " where period_type = ? and ", pt.getTypeInt());
		if (prs != null && prs.size() > 0) {
			ds.addSql("(");
			for (Profile p : prs) {
				ds.addSqlAndArguments("(unit_type_name = ? and profile_name = ?) or ", p.getUnittype().getName(), p.getName());
			}
			if (id.getUser().getUsername().equals(Users.USER_ADMIN)) {
				int numberOfProfiles = 0;
				for (Unittype ut : xaps.getUnittypes().getUnittypes())
					numberOfProfiles += ut.getProfiles().getProfiles().length;
				if (prs.size() >= numberOfProfiles)
					ds.addSqlAndArguments("(unit_type_name = ? and profile_name = ?) or ", "Unknown", "Unknown");
			}
			ds.cleanupSQLTail();
			ds.addSql(") and ");
		} else if (uts != null && uts.size() > 0) {
			ds.addSql("(");
			for (Unittype ut : uts)
				ds.addSqlAndArguments("unit_type_name = ? or ", ut.getName());
			if (id.getUser().getUsername().equals(Users.USER_ADMIN) && uts.size() >= xaps.getUnittypes().getUnittypes().length)
				ds.addSqlAndArguments("unit_type_name = ? or ", "Unknown");
			ds.cleanupSQLTail();
			ds.addSql(") and ");
		}
		if (start != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(start);
			if (pt == PeriodType.DAY) {
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
			}
			if (pt == PeriodType.MONTH) {
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.DAY_OF_MONTH, 1);
			}
			ds.addSqlAndArguments("timestamp_ >= ?  and ", cal.getTime());
		}
		if (end != null)
			ds.addSqlAndArguments("timestamp_ < ? and ", end);
		if (swVersion != null)
			ds.addSqlAndArguments("software_version = ?", swVersion);
		ds.cleanupSQLTail();
		return ds;
	}

	public List<String> getSoftwareVersions(Unittype unittype, Profile profile, Date start, Date end, String tablename) throws SQLException, NoAvailableConnectionException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		List<String> swVersionList = new ArrayList<String>();
		try {
			connection = ConnectionProvider.getConnection(xapsCp, true);
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments("select distinct(software_version) from " + tablename + " where ");
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(start);
			Calendar endCal = Calendar.getInstance();
			endCal.setTime(end);
			startCal.set(Calendar.MINUTE, 0);
			startCal.set(Calendar.SECOND, 0);
			startCal.set(Calendar.MILLISECOND, 0);
			endCal.set(Calendar.HOUR_OF_DAY, endCal.get(Calendar.HOUR_OF_DAY) + 1);
			ds.addSqlAndArguments("timestamp_ >= ? and timestamp_ < ? ", startCal, endCal);
			if (unittype != null)
				ds.addSqlAndArguments("and unit_type_name = ? ", unittype.getName());
			if (profile != null)
				ds.addSqlAndArguments("and profile_name = ? ", profile.getName());
			ds.addSql(" order by software_version asc");
			ps = ds.makePreparedStatement(connection);
			ps.setFetchSize(1);
			rs = ps.executeQuery();
			while (rs.next())
				swVersionList.add(rs.getString("software_version"));
			return swVersionList;
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, sqle);
		}
	}

	/**
	 * Find the previous timestamp where reports have been made, then roll forward to next timestamp to perform reporting.
	 * If the old timestamp is more than 2 days ago, then default to maximum 2 days ago.
	 * If the old timestamp is null, default to 2 days ago.
	 * @param periodType
	 * @param tablename
	 * @return
	 * @throws NoAvailableConnectionException
	 * @throws SQLException
	 */
	public Date startReportFromTms(PeriodType periodType, String tablename) throws NoAvailableConnectionException, SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			long now = System.currentTimeMillis();
			long twoDaysAgo = now - 2l * 86400l * 1000l;
			connection = ConnectionProvider.getConnection(xapsCp, true);
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments("select timestamp_ from " + tablename + " where period_type = " + periodType.getTypeInt() + " order by timestamp_ desc");
			ps = ds.makePreparedStatement(connection);
			ps.setFetchSize(1);
			rs = ps.executeQuery();
			if (rs.next()) {
				Timestamp tms = rs.getTimestamp("timestamp_");
				if (tms != null) {
					Date nextTms = converter.rollForward(tms, periodType);
					while (nextTms.getTime() < twoDaysAgo) {
						nextTms = converter.rollForward(nextTms, periodType);
					}
					return nextTms;
				}
			}
			// if no data exists, start from two days ago
			return converter.convert(new Date(twoDaysAgo), periodType);
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, sqle);
		}

	}

	public Map<String, Unit> getUnitsInGroup(Group group) throws NoAvailableConnectionException, SQLException {
		Map<String, Unit> unitsInGroup = new HashMap<String, Unit>();
		if (group != null) {
			XAPSUnit xapsUnit = new XAPSUnit(xapsCp, xaps, xaps.getSyslog());
			unitsInGroup = xapsUnit.getUnits(group);
		}
		return unitsInGroup;
	}

	/*
	 * Generate reports from report table
	 */

	//	public Report<RecordProvisioning> generateProvisioningReport(PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs) throws NoAvailableConnectionException,
	//			SQLException, IOException {
	//		Connection xapsConnection = null;
	//		Connection sysConnection = null;
	//		PreparedStatement ps = null;
	//		ResultSet rs = null;
	//		SQLException sqle = null;
	//		try {
	//			Report<RecordProvisioning> report = new Report<RecordProvisioning>(RecordProvisioning.class, periodType);
	//			xapsConnection = ConnectionProvider.getConnection(xapsCp, true);
	//			
	//			logger.info(logPrefix + "Reads from report_unit table from " + start + " to " + end);
	//			DynamicStatement ds = selectReportSQL("report_provisioning", periodType, start, end, uts, prs);
	//			ps = ds.makePreparedStatement(xapsConnection);
	//			rs = ps.executeQuery();
	//			int counter = 0;
	//			while (rs.next()) {
	//				counter++;
	//				start = rs.getTimestamp("timestamp_");
	//				String unittypeName = rs.getString("unit_type_name");
	//				String profileName = rs.getString("profile_name");
	//				RecordProvisioning recordTmp = new RecordProvisioning(start, periodType, unittypeName, profileName);
	//				Key key = recordTmp.getKey();
	//				RecordProvisioning record = report.getRecord(key);
	//				if (record == null)
	//					record = recordTmp;
	//				record.getNeverProvisionedCount().set(rs.getInt("never_provisioned_count"));
	//				record.getOkProvisionedCount().set(rs.getInt("ok_provisioned_count"));
	//				record.getOldProvisionedCount().set(rs.getInt("old_provisioned_count"));
	//				report.setRecord(key, record);
	//			}
	//			logger.info(logPrefix + "Have read " + counter + " rows, last tms was " + start + ", report is now " + report.getMap().size() + " entries");
	//			return report;
	//		} catch (SQLException sqlex) {
	//			sqle = sqlex;
	//			throw sqlex;
	//		} finally {
	//			if (rs != null)
	//				rs.close();
	//			if (ps != null)
	//				ps.close();
	//			if (xapsConnection != null)
	//				ConnectionProvider.returnConnection(xapsConnection, sqle);
	//			if (sysConnection != null)
	//				ConnectionProvider.returnConnection(sysConnection, sqle);
	//		}
	//	}

	public Report<RecordUnit> generateUnitReport(PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs) throws NoAvailableConnectionException, SQLException, IOException {
		Connection xapsConnection = null;
		Connection sysConnection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			Report<RecordUnit> report = new Report<RecordUnit>(RecordUnit.class, periodType);
			xapsConnection = ConnectionProvider.getConnection(xapsCp, true);

			logger.info(logPrefix + "Reads from report_unit table from " + start + " to " + end);
			DynamicStatement ds = selectReportSQL("report_unit", periodType, start, end, uts, prs);
			ps = ds.makePreparedStatement(xapsConnection);
			rs = ps.executeQuery();
			int counter = 0;
			while (rs.next()) {
				counter++;
				start = rs.getTimestamp("timestamp_");
				String unittypeName = rs.getString("unit_type_name");
				String profileName = rs.getString("profile_name");
				String softwareVersion = rs.getString("software_version");
				String status = rs.getString("status");
				RecordUnit recordTmp = new RecordUnit(start, periodType, unittypeName, profileName, softwareVersion, status);
				Key key = recordTmp.getKey();
				RecordUnit record = report.getRecord(key);
				if (record == null)
					record = recordTmp;
				record.getUnitCount().set(rs.getInt("unit_count"));
				report.setRecord(key, record);
			}
			logger.info(logPrefix + "Have read " + counter + " rows, last tms was " + start + ", report is now " + report.getMap().size() + " entries");
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

	public Report<RecordJob> generateJobReport(PeriodType periodType, Date start, Date end, List<Unittype> uts) throws NoAvailableConnectionException, SQLException, IOException {
		Connection xapsConnection = null;
		Connection sysConnection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			Report<RecordJob> report = new Report<RecordJob>(RecordJob.class, periodType);
			xapsConnection = ConnectionProvider.getConnection(xapsCp, true);

			logger.info(logPrefix + "Reads from report_job table from " + start + " to " + end);
			DynamicStatement ds = selectReportSQL("report_job", periodType, start, end, uts, null);
			ps = ds.makePreparedStatement(xapsConnection);
			rs = ps.executeQuery();
			int counter = 0;
			while (rs.next()) {
				counter++;
				start = rs.getTimestamp("timestamp_");
				String unittypeName = rs.getString("unit_type_name");
				String jobName = rs.getString("job_name");
				String groupName = rs.getString("group_name");
				RecordJob recordTmp = new RecordJob(start, periodType, unittypeName, jobName, groupName);
				Key key = recordTmp.getKey();
				RecordJob record = report.getRecord(key);
				if (record == null)
					record = recordTmp;
				record.getCompleted().set(rs.getInt("completed"));
				record.getGroupSize().set(rs.getInt("group_size"));
				record.getConfirmedFailed().set(rs.getInt("confirmed_failed"));
				record.getUnconfirmedFailed().set(rs.getInt("unconfirmed_failed"));
				report.setRecord(key, record);
			}
			logger.info(logPrefix + "Have read " + counter + " rows, last tms was " + start + ", report is now " + report.getMap().size() + " entries");
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

	protected void logInfo(String reportType, String unitId, List<Unittype> uts, List<Profile> prs, Date start, Date end) {
		String msg = logPrefix + reportType + ": Will generate from syslog (";
		if (unitId != null)
			msg += "unitId: " + unitId + ", ";
		if (uts != null)
			msg += "unittypes: " + uts.size() + ", ";
		if (prs != null)
			msg += "profile: " + prs.size() + ", ";
		msg += start + " - " + end + ")";

		logger.info(msg);
	}

	public PeriodType getPeriodType() {
		return periodType;
	}

	public void setPeriodType(PeriodType periodType) {
		this.periodType = periodType;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public List<Unittype> getUnittypes() {
		return unittypes;
	}

	public void setUnittypes(List<Unittype> unittypes) {
		this.unittypes = unittypes;
	}

	public List<Profile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public String getSwVersion() {
		return swVersion;
	}

	public void setSwVersion(String swVersion) {
		this.swVersion = swVersion;
	}

	public SyslogFilter getSyslogFilter() {
		return syslogFilter;
	}

	public void setSyslogFilter(SyslogFilter syslogFilter) {
		this.syslogFilter = syslogFilter;
	}

}
