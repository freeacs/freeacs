package com.owera.xaps.core.task;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.common.scheduler.ScheduleType;
import com.owera.xaps.core.Properties;
import com.owera.xaps.dbi.DynamicStatement;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.report.Key;
import com.owera.xaps.dbi.report.PeriodType;
import com.owera.xaps.dbi.report.RecordGroup;
import com.owera.xaps.dbi.report.RecordHardware;
import com.owera.xaps.dbi.report.RecordJob;
import com.owera.xaps.dbi.report.RecordProvisioning;
import com.owera.xaps.dbi.report.RecordSyslog;
import com.owera.xaps.dbi.report.RecordUnit;
import com.owera.xaps.dbi.report.RecordVoip;
import com.owera.xaps.dbi.report.Report;
import com.owera.xaps.dbi.report.ReportConverter;
import com.owera.xaps.dbi.report.ReportHardwareGenerator;
import com.owera.xaps.dbi.report.ReportProvisioningGenerator;
import com.owera.xaps.dbi.report.ReportSyslogGenerator;
import com.owera.xaps.dbi.report.ReportVoipGenerator;
import com.owera.xaps.dbi.report.TmsConverter;

public class ReportGenerator extends DBIOwner {

	private static long MINUTE_MS = 60 * 1000;
	private static long HOUR_MS = 60 * MINUTE_MS;
	private static long DAY_MS = 24 * HOUR_MS;
	private static long MONTH_MS = 31 * DAY_MS;

	//	private static Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();

	private Logger logger = new Logger();
	private XAPS xaps;
	private ScheduleType scheduleType;
	private TmsConverter converter = new TmsConverter();

	public ReportGenerator(String taskName, ScheduleType scheduleType) throws SQLException, NoAvailableConnectionException {
		super(taskName);
		this.scheduleType = scheduleType;

	}

	@Override
	public void runImpl() throws Exception {
		xaps = getLatestXAPS();
		if (scheduleType == ScheduleType.DAILY)
			dailyJobs();
		else if (scheduleType == ScheduleType.HOURLY) {
			hourlyJobs();
		} else {
			dailyJobs();
			hourlyJobs();
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	private int getGroupSize(Connection c, RecordJob record) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			DynamicStatement ds2 = new DynamicStatement();
			ds2.addSql("SELECT unit_count FROM report_group WHERE ");
			ds2.addSql("timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND group_name = ?");
			ds2.addArguments(record.getTms(), record.getPeriodType().getTypeInt(), record.getUnittypeName(), record.getGroupName());
			ps = ds2.makePreparedStatement(c);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("unit_count");
			}
			return 0;
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
	}

	private void populateReportGroupTable(ConnectionProperties cp, Date now, PeriodType pt) throws NoAvailableConnectionException, SQLException {
		Connection c = null;
		PreparedStatement ps = null;
		SQLException sqle = null;
		int inserted = 0;
		int updated = 0;
		try {
			c = ConnectionProvider.getConnection(cp, true);
			XAPSUnit xapsUnit = new XAPSUnit(cp, xaps, getSyslog());
			for (Unittype unittype : xaps.getUnittypes().getUnittypes()) {
				for (Group group : unittype.getGroups().getGroups()) {

					int unitCount = xapsUnit.getUnitCount(group);
					DynamicStatement ds = new DynamicStatement();
					ds.addSql("INSERT INTO report_group VALUES(?,?,?,?,?)");
					ds.addArguments(now, pt.getTypeInt(), unittype.getName(), group.getName());
					ds.addArguments(unitCount);
					group.setCount(unitCount);
					ps = ds.makePreparedStatement(c);
					RecordGroup ru = new RecordGroup(now, pt, unittype.getName(), group.getName());
					try {
						ps.executeUpdate();
						unittype.getGroups().addOrChangeGroup(group, xaps);
						logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was inserted");
						inserted++;
					} catch (SQLException sqle2) {
						ds = new DynamicStatement();
						ds.addSqlAndArguments("UPDATE report_group set unit_count = ? ", unitCount);
						ds.addSql("WHERE timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND group_name = ? AND unit_count <> ?");
						ds.addArguments(now, pt.getTypeInt(), unittype.getName(), group.getName(), unitCount);
						ps = ds.makePreparedStatement(c);
						int rowsUpdated = ps.executeUpdate();
						if (rowsUpdated > 0)
							unittype.getGroups().addOrChangeGroup(group, xaps);
						logger.debug("ReportGenerator: ReportGenerator: - - The entry " + ru.getKey() + " was updated");
						updated++;
					}
					//					if (group.getTimeParameter() != null) {
					//						GroupParameter timeParameter = null;
					//						for (GroupParameter gp : group.getGroupParameters().getGroupParameters()) {
					//							if (gp.getParameter().getUnittypeParameter().getName().equals(group.getTimeParameter().getName())) {
					//								timeParameter = gp;
					//								break;
					//							}
					//						}
					//						String timeFormat = group.getTimeRollingFormat();
					//						if (timeFormat == null)
					//							timeFormat = "yyyyMMdd";
					//						Integer secOffset = group.getTimeRollingOffset();
					//						if (secOffset == null)
					//							secOffset = 0;
					//						SimpleDateFormat sdf = formatMap.get(timeFormat);
					//						if (sdf == null) {
					//							sdf = new SimpleDateFormat(timeFormat);
					//							formatMap.put(timeFormat, sdf);
					//						}
					//						String timeValue = null;
					//						if (secOffset != 0)
					//							timeValue = sdf.format(new Date(now.getTime() + (long) secOffset * 1000l));
					//						else
					//							timeValue = sdf.format(now);
					//
					//						if (timeParameter == null)
					//							timeParameter = new GroupParameter(new Parameter(group.getTimeParameter(), timeValue), group);
					//						else
					//							timeParameter.getParameter().setValue(timeValue);
					//						logger.debug("ReportGenerator: ReportGenerator: - - Time parameter " + timeParameter.getParameter().getUnittypeParameter().getName() + " is updated to " + timeValue);
					//						group.getGroupParameters().addOrChangeGroupParameter(timeParameter, xaps);
					//					}
				}
			}
			logger.info("ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated");
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps != null)
				ps.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqle);
		}
	}

	private void populateReportHWTable(ConnectionProperties cp, Report<RecordHardware> report) throws NoAvailableConnectionException, SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		Calendar c = Calendar.getInstance();
		int nowMonth = c.get(Calendar.MONTH);
		int nowDay = c.get(Calendar.DAY_OF_MONTH);
		int nowHour = c.get(Calendar.HOUR_OF_DAY);
		int nowMinute = c.get(Calendar.MINUTE);
		long nowTms = c.getTimeInMillis();
		int skipped = 0;
		int inserted = 0;
		int updated = 0;
		try {
			connection = ConnectionProvider.getConnection(cp, true);
			Map<Key, RecordHardware> recordMap = report.getMap();
			for (RecordHardware r : recordMap.values()) {
				long diff = nowTms - r.getTms().getTime();
				PeriodType pt = r.getPeriodType();
				if (pt == PeriodType.MONTH && diff < MONTH_MS && converter.month(r.getTms()) == nowMonth) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.DAY && diff < DAY_MS && converter.day(r.getTms()) == nowDay) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.HOUR && diff < HOUR_MS && converter.hour(r.getTms()) == nowHour) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.MINUTE && diff < MINUTE_MS && converter.minute(r.getTms()) == nowMinute) {
					skipped++;
					continue;
				}
				DynamicStatement ds = new DynamicStatement();
				Date keyTms = converter.convert(r.getTms(), r.getPeriodType());
				ds.addArguments(keyTms);
				ds.addArguments(pt.getTypeInt());
				ds.addArguments(r.getUnittypeName());
				ds.addArguments(r.getProfileName());
				ds.addArguments(r.getSoftwareVersion());
				//				ds.addArguments(r.getUnitCount().get());
				ds.addArguments(r.getBootCount().get());
				ds.addArguments(r.getBootWatchdogCount().get());
				ds.addArguments(r.getBootMiscCount().get());
				ds.addArguments(r.getBootPowerCount().get());
				ds.addArguments(r.getBootResetCount().get());
				ds.addArguments(r.getBootProvCount().get());
				ds.addArguments(r.getBootProvSwCount().get());
				ds.addArguments(r.getBootProvConfCount().get());
				ds.addArguments(r.getBootProvBootCount().get());
				ds.addArguments(r.getBootUserCount().get());
				ds.addArguments(r.getMemoryHeapDdrPoolAvg().get());
				ds.addArguments(r.getMemoryHeapDdrCurrentAvg().get());
				ds.addArguments(r.getMemoryHeapDdrLowAvg().get());
				ds.addArguments(r.getMemoryHeapOcmPoolAvg().get());
				ds.addArguments(r.getMemoryHeapOcmCurrentAvg().get());
				ds.addArguments(r.getMemoryHeapOcmLowAvg().get());
				ds.addArguments(r.getMemoryNpDdrPoolAvg().get());
				ds.addArguments(r.getMemoryNpDdrCurrentAvg().get());
				ds.addArguments(r.getMemoryNpDdrLowAvg().get());
				ds.addArguments(r.getMemoryNpOcmPoolAvg().get());
				ds.addArguments(r.getMemoryNpOcmCurrentAvg().get());
				ds.addArguments(r.getMemoryNpOcmLowAvg().get());
				ds.addArguments(r.getCpeUptimeAvg().get());
				ds.addSql("insert into report_hw VALUES(" + ds.getQuestionMarks() + ")");
				ps = ds.makePreparedStatement(connection);
				try {
					ps.executeUpdate();
					logger.debug("ReportGenerator: ReportGenerator: - - The entry " + r.getKey() + " was inserted");
					inserted++;
				} catch (SQLException sqlex2) {
					logger.error("ReportGenerator: ReportGenerator: - - The entry " + r.getKey() + " was not updated. Update is not implented yet.", sqlex2);
				}
			}
			if (recordMap.size() > 0) {
				logger.info("ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated, " + skipped + " entries skipped (too new)");
			}
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

	private void populateReportJobTable(ConnectionProperties cp, Date now, PeriodType pt) throws NoAvailableConnectionException, SQLException {
		Connection c = null;
		PreparedStatement ps = null;
		SQLException sqle = null;
		int inserted = 0;
		int updated = 0;
		try {
			c = ConnectionProvider.getConnection(cp, true);
			for (Unittype unittype : xaps.getUnittypes().getUnittypes()) {
				for (Job job : unittype.getJobs().getJobs()) {
					int completed = job.getCompletedHadFailures() + job.getCompletedNoFailures();
					int confirmedFailed = job.getConfirmedFailed();
					int unconfirmedFailed = job.getUnconfirmedFailed();
					Group group = job.getGroup();
					RecordJob ru = new RecordJob(now, pt, unittype.getName(), job.getName(), group.getName());
					int groupSize = getGroupSize(c, ru);
					DynamicStatement ds2 = new DynamicStatement();
					ds2.addSql("INSERT INTO report_job VALUES(?,?,?,?,?,?,?,?,?)");
					ds2.addArguments(now, pt.getTypeInt(), unittype.getName(), job.getName(), group.getName());
					ds2.addArguments(groupSize, completed, confirmedFailed, unconfirmedFailed);
					ps = ds2.makePreparedStatement(c);
					try {
						ps.executeUpdate();
						logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was inserted");
						inserted++;
					} catch (SQLException sqle2) {
						ds2 = new DynamicStatement();
						ds2.addSql("UPDATE report_job set group_name = ?, group_size = ?, completed = ?, confirmed_failed = ?, unconfirmed_failed = ? ");
						ds2.addArguments(group.getName(), groupSize, completed, confirmedFailed, unconfirmedFailed);
						ds2.addSql("WHERE timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND job_name = ?");
						ds2.addArguments(now, pt.getTypeInt(), unittype.getName(), job.getName());
						ps = ds2.makePreparedStatement(c);
						int rowsUpdated = ps.executeUpdate();
						if (rowsUpdated == 0)
							throw sqle2;
						else {
							logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was updated");
							updated++;
						}
					}
				}
			}
			logger.info("ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated");
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps != null)
				ps.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqle);
		}
	}

	private void populateReportSyslogTable(ConnectionProperties cp, Report<RecordSyslog> report) throws NoAvailableConnectionException, SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		Calendar c = Calendar.getInstance();
		int nowMonth = c.get(Calendar.MONTH);
		int nowDay = c.get(Calendar.DAY_OF_MONTH);
		int nowHour = c.get(Calendar.HOUR_OF_DAY);
		int nowMinute = c.get(Calendar.MINUTE);
		long nowTms = c.getTimeInMillis();
		int skipped = 0;
		int inserted = 0;
		int updated = 0;
		try {
			connection = ConnectionProvider.getConnection(cp, true);
			Map<Key, RecordSyslog> recordMap = report.getMap();
			for (RecordSyslog r : recordMap.values()) {
				long diff = nowTms - r.getTms().getTime();
				PeriodType pt = r.getPeriodType();
				if (pt == PeriodType.MONTH && diff < MONTH_MS && converter.month(r.getTms()) == nowMonth) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.DAY && diff < DAY_MS && converter.day(r.getTms()) == nowDay) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.HOUR && diff < HOUR_MS && converter.hour(r.getTms()) == nowHour) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.MINUTE && diff < MINUTE_MS && converter.minute(r.getTms()) == nowMinute) {
					skipped++;
					continue;
				}
				DynamicStatement ds = new DynamicStatement();
				Date keyTms = converter.convert(r.getTms(), r.getPeriodType());
				ds.addArguments(keyTms);
				ds.addArguments(pt.getTypeInt());
				ds.addArguments(r.getUnittypeName());
				ds.addArguments(r.getProfileName());
				ds.addArguments(r.getSeverity());
				ds.addArguments(new Integer(r.getEventId()));
				ds.addArguments(r.getFacility());
				ds.addArguments(r.getMessageCount().get());
				ds.addSql("insert into report_syslog VALUES(" + ds.getQuestionMarks() + ")");
				ps = ds.makePreparedStatement(connection);
				try {
					ps.executeUpdate();
					logger.debug("ReportGenerator: - - The entry " + r.getKey() + " was inserted");
					inserted++;
				} catch (SQLException sqlex2) {
					ds = new DynamicStatement();
					ds.addSql("UPDATE report_syslog set unit_count = ? ");
					ds.addSql("WHERE timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND profile_name = ? AND severity = ? AND syslog_event_id = ? AND facility = ?");
					ds.addArguments(r.getMessageCount().get(), keyTms, pt.getTypeInt(), r.getUnittypeName(), r.getProfileName(), r.getSeverity(), new Integer(r.getEventId()), r.getFacility());
					ps = ds.makePreparedStatement(connection);
					int rowsUpdated = ps.executeUpdate();
					if (rowsUpdated == 0)
						throw sqlex2;
					else {
						logger.debug("ReportGenerator: - - The entry " + r.getKey() + " was updated");
						updated++;
					}
				}
			}
			if (recordMap.size() > 0) {
				logger.info("ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated, " + skipped + " entries skipped (too new)");
			}
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

	private void populateReportUnitTable(ConnectionProperties cp, Date now, PeriodType pt) throws NoAvailableConnectionException, SQLException {
		Connection c1 = null;
		PreparedStatement ps1 = null;
		Connection c2 = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		Statement s = null;
		SQLException sqle = null;
		int inserted = 0;
		int updated = 0;
		try {
			c1 = ConnectionProvider.getConnection(cp, true);
			c2 = ConnectionProvider.getConnection(cp, true);
			s = c1.createStatement();
			rs = s.executeQuery("SELECT unit_id, profile_id, unit_type_id FROM unit");
			Map<String, UnitSWLCT> unitMap = new HashMap<String, UnitSWLCT>();
			while (rs.next()) {
				Unittype unittype = xaps.getUnittype(rs.getInt("unit_type_id"));
				Profile profile = unittype.getProfiles().getById(rs.getInt("profile_id"));
				Unit unit = new Unit(rs.getString("unit_id"), unittype, profile);
				unitMap.put(unit.getId(), new UnitSWLCT(unit));
			}
			rs.close();
			rs = s.executeQuery("SELECT up.unit_id, up.value FROM unit_type_param utp, unit_param up WHERE up.unit_type_param_id = utp.unit_type_param_id AND utp.name LIKE '%Device.DeviceInfo.SoftwareVersion'");
			while (rs.next()) {
				UnitSWLCT u = unitMap.get(rs.getString("unit_id"));
				if (u != null)
					u.setSoftwareVersion(rs.getString("value"));
			}
			rs.close();

			rs = s.executeQuery("SELECT up.unit_id, timestampdiff(DAY, up.value, sysdate()) FROM unit_type_param utp, unit_param up WHERE up.unit_type_param_id = utp.unit_type_param_id AND utp.name LIKE 'System.X_OWERA-COM.LastConnectTms'");
			while (rs.next()) {
				UnitSWLCT u = unitMap.get(rs.getString("unit_id"));
				if (u != null)
					u.setLastConnectTms(rs.getInt(2));
			}

			Map<String, Integer> unitReport = new HashMap<String, Integer>();
			for (UnitSWLCT unitSWLCT : unitMap.values()) {
				String swVersion = unitSWLCT.getSoftwareVersion();
				if (swVersion == null || swVersion.trim().equals(""))
					swVersion = "Unknown";
				String status = "Inactive";
				if (unitSWLCT.getLastConnectTms() != null) {
					if (unitSWLCT.getLastConnectTms() < 2)
						status = "Active last 48h";
					if (unitSWLCT.getLastConnectTms() >= 2 && unitSWLCT.getLastConnectTms() <= 7)
						status = "Active 2-7 days ago";
					if (unitSWLCT.getLastConnectTms() > 7)
						status = "Active 8 or more days ago";
				}
				String key = unitSWLCT.getUnit().getUnittype().getName() + "###" + unitSWLCT.getUnit().getProfile().getName() + "###" + swVersion + "###" + status;
				if (unitReport.get(key) == null)
					unitReport.put(key, 1);
				else
					unitReport.put(key, unitReport.get(key) + 1);
			}
			unitMap = null;

			for (Entry<String, Integer> entry : unitReport.entrySet()) {
				String[] keys = entry.getKey().split("###");
				DynamicStatement ds2 = new DynamicStatement();
				ds2.addSql("INSERT INTO report_unit (timestamp_, period_type, unit_type_name, profile_name, software_version, status, unit_count) VALUES(?,?,?,?,?,?,?)");
				ds2.addArguments(now, pt.getTypeInt(), keys[0], keys[1], keys[2], keys[3], entry.getValue());
				ps2 = ds2.makePreparedStatement(c2);
				RecordUnit ru = new RecordUnit(now, pt, keys[0], keys[1], keys[2], keys[3]);
				try {
					ps2.executeUpdate();
					logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was inserted");
					inserted++;
				} catch (SQLException sqle2) {
					ds2 = new DynamicStatement();
					ds2.addSql("UPDATE report_unit set unit_count = ? ");
					ds2.addSql("WHERE timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND profile_name = ? AND software_version = ? AND status = ? ");
					ds2.addArguments(entry.getValue(), now, pt.getTypeInt(), keys[0], keys[1], keys[2], keys[3]);
					ps2 = ds2.makePreparedStatement(c2);
					int rowsUpdated = ps2.executeUpdate();
					if (rowsUpdated == 0)
						throw sqle2;
					else {
						logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was updated");
						updated++;
					}
				}

			}
			logger.info("ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated");
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps1 != null)
				ps1.close();
			if (s != null)
				s.close();
			if (c1 != null)
				ConnectionProvider.returnConnection(c1, sqle);
			if (ps2 != null)
				ps2.close();
			if (c2 != null)
				ConnectionProvider.returnConnection(c2, sqle);
		}
	}

	public static class UnitSWLCT {
		private Unit unit;
		private Integer lastConnectTms;
		private String softwareVersion;

		public UnitSWLCT(Unit unit) {
			this.unit = unit;
		}

		public Integer getLastConnectTms() {
			return lastConnectTms;
		}

		public void setLastConnectTms(Integer lastConnectTms) {
			this.lastConnectTms = lastConnectTms;
		}

		public String getSoftwareVersion() {
			return softwareVersion;
		}

		public void setSoftwareVersion(String softwareVersion) {
			this.softwareVersion = softwareVersion;
		}

		public Unit getUnit() {
			return unit;
		}

		public void setUnit(Unit unit) {
			this.unit = unit;
		}

	}

	private void populateReportVoipTable(ConnectionProperties cp, Report<RecordVoip> report) throws NoAvailableConnectionException, SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		Calendar c = Calendar.getInstance();
		int nowMonth = c.get(Calendar.MONTH);
		int nowDay = c.get(Calendar.DAY_OF_MONTH);
		int nowHour = c.get(Calendar.HOUR_OF_DAY);
		int nowMinute = c.get(Calendar.MINUTE);
		long nowTms = c.getTimeInMillis();
		int skipped = 0;
		int inserted = 0;
		int updated = 0;
		try {
			connection = ConnectionProvider.getConnection(cp, true);
			Map<Key, RecordVoip> recordMap = report.getMap();
			for (RecordVoip r : recordMap.values()) {
				long diff = nowTms - r.getTms().getTime();
				PeriodType pt = r.getPeriodType();
				if (pt == PeriodType.MONTH && diff < MONTH_MS && converter.month(r.getTms()) == nowMonth) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.DAY && diff < DAY_MS && converter.day(r.getTms()) == nowDay) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.HOUR && diff < HOUR_MS && converter.hour(r.getTms()) == nowHour) {
					skipped++;
					continue;
				}
				if (pt == PeriodType.MINUTE && diff < MINUTE_MS && converter.minute(r.getTms()) == nowMinute) {
					skipped++;
					continue;
				}
				DynamicStatement ds = new DynamicStatement();
				Date keyTms = converter.convert(r.getTms(), r.getPeriodType());
				ds.addArguments(keyTms);
				ds.addArguments(pt.getTypeInt());
				ds.addArguments(r.getUnittypeName());
				ds.addArguments(r.getProfileName());
				ds.addArguments(r.getSoftwareVersion());
				ds.addArguments(r.getLine());
				ds.addArguments(r.getMosAvg().get());
				ds.addArguments(r.getJitterAvg().get());
				ds.addArguments(r.getJitterMax().get());
				ds.addArguments(r.getPercentLossAvg().get());
				ds.addArguments(r.getCallLengthAvg().get());
				ds.addArguments(r.getCallLengthTotal().get());
				ds.addArguments(r.getIncomingCallCount().get());
				ds.addArguments(r.getOutgoingCallCount().get());
				ds.addArguments(r.getOutgoingCallFailedCount().get());
				ds.addArguments(r.getAbortedCallCount().get());
				ds.addArguments(r.getNoSipServiceTime().get());
				ds.addSql("insert into report_voip VALUES(" + ds.getQuestionMarks() + ")");
				ps = ds.makePreparedStatement(connection);
				try {
					logger.debug("ReportGenerator: - - SQL to be run: " + ds.getSqlQuestionMarksSubstituted());
					ps.executeUpdate();
					logger.debug("ReportGenerator: - - The entry " + r.getKey() + " was inserted");
					inserted++;
				} catch (SQLException sqlex2) {
					logger.debug("ReportGenerator: - - Insert failed, will try with updated instead (exception: " + sqlex2 + ")");
					ds = new DynamicStatement();
					ds.addSql("update report_voip set mos_avg = ?, ");
					ds.addSql("jitter_avg = ?, jitter_max = ?, ");
					ds.addSql("percent_loss_avg = ?, call_length_avg = ?, ");
					ds.addSql("call_length_total = ?, incoming_call_count = ?, no_sip_service_time = ?, ");
					ds.addSql("outgoing_call_count = ?, outgoing_call_failed_count = ?, aborted_call_count = ? ");
					ds.addSql("where timestamp_ = ? and period_type = ? and unit_type_name = ? and profile_name = ? ");
					ds.addSql("and software_version = ? and line = ?");
					ds.addArguments(r.getMosAvg().get());
					ds.addArguments(r.getJitterAvg().get());
					ds.addArguments(r.getPercentLossAvg().get());
					ds.addArguments(r.getCallLengthAvg().get());
					ds.addArguments(r.getCallLengthTotal().get());
					ds.addArguments(r.getIncomingCallCount().get());
					ds.addArguments(r.getNoSipServiceTime().get());
					ds.addArguments(r.getOutgoingCallCount().get());
					ds.addArguments(r.getOutgoingCallFailedCount().get());
					ds.addArguments(r.getAbortedCallCount().get());
					ds.addArguments(keyTms);
					ds.addArguments(pt.getTypeInt());
					ds.addArguments(r.getUnittypeName());
					ds.addArguments(r.getProfileName());
					ds.addArguments(r.getSoftwareVersion());
					ds.addArguments(r.getLine());
					ps = ds.makePreparedStatement(connection);
					int rowsUpdated = ps.executeUpdate();
					if (rowsUpdated == 0) {
						throw sqlex2;
					}
					logger.debug("ReportGenerator: - - The entry " + r.getKey() + " was updated");
					updated++;
				}
			}
			if (recordMap.size() > 0) {
				logger.info("ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated, " + skipped + " entries skipped (too new)");
			}
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

	private boolean skip(PeriodType pt, Date recordTms, Calendar now) {
		long diff = now.getTimeInMillis() - recordTms.getTime();
		if (pt == PeriodType.MONTH && diff < MONTH_MS && converter.month(recordTms) == now.get(Calendar.MONTH))
			return true;
		if (pt == PeriodType.DAY && diff < DAY_MS && converter.day(recordTms) == now.get(Calendar.DAY_OF_MONTH))
			return true;
		if (pt == PeriodType.HOUR && diff < HOUR_MS && converter.hour(recordTms) == now.get(Calendar.HOUR_OF_DAY))
			return true;
		if (pt == PeriodType.MINUTE && diff < MINUTE_MS && converter.minute(recordTms) == now.get(Calendar.MINUTE))
			return true;
		return false;
	}

	private void populateReportProvTable(ConnectionProperties cp, Report<RecordProvisioning> report) throws NoAvailableConnectionException, SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			int skipped = 0;
			int inserted = 0;
			int updated = 0;
			connection = ConnectionProvider.getConnection(cp, true);
			Map<Key, RecordProvisioning> recordMap = report.getMap();
			Calendar now = Calendar.getInstance();
			for (RecordProvisioning r : recordMap.values()) {
				PeriodType pt = r.getPeriodType();
				if (skip(pt, r.getTms(), now)) {
					skipped++;
					continue;
				}
				DynamicStatement ds = new DynamicStatement();
				Date keyTms = converter.convert(r.getTms(), r.getPeriodType());
				ds.addArguments(keyTms, pt.getTypeInt(), r.getUnittypeName(), r.getProfileName(), r.getSoftwareVersion(), r.getOutput());
				ds.addArguments(r.getProvisioningOkCount().get(), r.getProvisioningRescheduledCount().get());
				ds.addArguments(r.getProvisioningErrorCount().get(), r.getProvisioningMissingCount().get(), r.getSessionLengthAvg().get());
				ds.addSql("insert into report_prov VALUES(" + ds.getQuestionMarks() + ")");
				ps = ds.makePreparedStatement(connection);
				try {
					logger.debug("ReportGenerator: - - SQL to be run: " + ds.getSqlQuestionMarksSubstituted());
					ps.executeUpdate();
					logger.debug("ReportGenerator: - - The entry " + r.getKey() + " was inserted");
					inserted++;
				} catch (SQLException sqlex2) {
					logger.debug("ReportGenerator: - - Insert failed, will try with updated instead (exception: " + sqlex2 + ")");
					ds = new DynamicStatement();
					ds.addSql("update report_prov set ");
					ds.addSql("ok_count = ?, rescheduled_count = ?, error_count =? , missing_count = ?, session_length_avg = ? ");
					ds.addArguments(r.getProvisioningOkCount().get(), r.getProvisioningRescheduledCount().get());
					ds.addArguments(r.getProvisioningErrorCount().get(), r.getProvisioningMissingCount().get(), r.getSessionLengthAvg().get());
					ds.addSql("where timestamp_ = ? and period_type = ? and unit_type_name = ? and profile_name = ? ");
					ds.addSql("and software_version = ? and line = ? and prov_output = ? and prov_status = ?");
					ds.addArguments(keyTms, pt.getTypeInt(), r.getUnittypeName(), r.getProfileName(), r.getSoftwareVersion(), r.getOutput());
					ps = ds.makePreparedStatement(connection);
					int rowsUpdated = ps.executeUpdate();
					if (rowsUpdated == 0) {
						throw sqlex2;
					}
					logger.debug("ReportGenerator: - - The entry " + r.getKey() + " was updated");
					updated++;
				}
			}
			if (recordMap.size() > 0) {
				logger.info("ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated, " + skipped + " entries skipped (too new)");
			}
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

	private void dailyJobs() throws NoAvailableConnectionException, SQLException, IOException, ParseException {
		logger.info("ReportGenerator: Daily report processing starts...");
		logger.info("ReportGenerator: Generate reports start");
		String reports = Properties.getReports();
		if (reports == null)
			reports = "Unit";
		else if (reports.indexOf("Unit") == -1)
			reports = "Unit, " + reports;

		runReports(reports, PeriodType.DAY);
		logger.info("ReportGenerator: Daily report processing ends");
	}

	private void hourlyJobs() throws NoAvailableConnectionException, SQLException, IOException, ParseException {
		logger.info("ReportGenerator: Hourly report processing starts...");
		logger.info("ReportGenerator: Generate reports start");
		String reports = Properties.getReports();
		if (reports == null)
			reports = "Unit";
		else if (reports.indexOf("Unit") == -1)
			reports = "Unit, " + reports;
		runReports(reports, PeriodType.HOUR);
		logger.info("ReportGenerator: Hourly report processing ends");
	}

	private void runReports(String reportProperty, PeriodType periodType) throws NoAvailableConnectionException, SQLException, IOException, ParseException {
		Date now = converter.convert(new Date(), periodType);
		int counter = 5;
		logger.info("ReportGenerator: Process reports start...");
		buildUnit(periodType, now);
		String skippingReports = "";

		if (reportProperty != null && reportProperty.indexOf("Basic") > -1) {
			buildSyslog(periodType);
			buildGroup(periodType, now);
			buildJob(periodType, now);
			buildProvisioning(periodType);
		} else {
			skippingReports += "Syslog, Group, Job, Provisioning, ";
		}

		if (reportProperty != null && reportProperty.indexOf("Pingcom") > -1) {
			counter += 2;
			buildHardwareSYS(periodType);
			buildVoipSYS(periodType);
		} else {
			skippingReports += "HardwareSYS, VoipSYS, ";
		}

		if (!skippingReports.equals("")) {
			skippingReports = skippingReports.substring(0, skippingReports.length() - 2);
			logger.info("ReportGenerator: Skipping reports: " + skippingReports);
		}
		logger.info("ReportGenerator: Process reports end, " + counter + " reports are made");
	}

	private void buildUnit(PeriodType periodType, Date now) throws NoAvailableConnectionException, SQLException {
		logger.info("ReportGenerator: - Generating and populating UnitReport (" + periodType.getTypeStr() + "-based)");
		populateReportUnitTable(getXapsCp(), now, periodType);
	}

	private void buildProvisioning(PeriodType periodType) throws NoAvailableConnectionException, SQLException, IOException, ParseException {
		ReportProvisioningGenerator rg = new ReportProvisioningGenerator(getSysCp(), getXapsCp(), xaps, "- - ", getIdentity());
		Date endTmsExc = new Date();
		Date startTmsInc = rg.startReportFromTms(periodType, "report_prov");
		logger.info("ReportGenerator: - Generating ProvSYSReport (" + periodType.getTypeStr() + "-based) from " + startTmsInc + " to " + endTmsExc);
		Report<RecordProvisioning> report = null;
		if (periodType == PeriodType.DAY) {
			report = rg.generateFromReport(PeriodType.HOUR, startTmsInc, endTmsExc, null, null);
			report = ReportConverter.convertProvReport(report, PeriodType.DAY);
		} else {
			report = rg.generateFromSyslog(periodType, startTmsInc, endTmsExc, null, null, null, null);
		}
		logger.info("ReportGenerator: - Generated ProvSYSReport (" + periodType.getTypeStr() + "-based), " + report.getMap().size() + " entries");
		populateReportProvTable(getXapsCp(), report);
		logger.info("ReportGenerator: - Populated ProvSYSReport  (" + periodType.getTypeStr() + "-based)");
	}

	private void buildSyslog(PeriodType periodType) throws NoAvailableConnectionException, SQLException, IOException, ParseException {
		ReportSyslogGenerator rg = new ReportSyslogGenerator(getSysCp(), getXapsCp(), xaps, "- - ", getIdentity());
		Date endTmsExc = new Date();
		Date startTmsInc = rg.startReportFromTms(periodType, "report_syslog");
		logger.info("ReportGenerator: - Generating SyslogReport (" + periodType.getTypeStr() + "-based) from " + startTmsInc + " to " + endTmsExc);
		Report<RecordSyslog> report = null;
		if (periodType == PeriodType.DAY) {
			report = rg.generateFromReport(PeriodType.HOUR, startTmsInc, endTmsExc, null, null);
			report = ReportConverter.convertSyslogReport(report, PeriodType.DAY);
		} else {
			report = rg.generateFromSyslog(periodType, startTmsInc, endTmsExc, null, null, null, null);
		}
		logger.info("ReportGenerator: - Generated SyslogReport (" + periodType.getTypeStr() + "-based), " + report.getMap().size() + " entries");
		populateReportSyslogTable(getXapsCp(), report);
		logger.info("ReportGenerator: - Populated SyslogReport  (" + periodType.getTypeStr() + "-based)");
	}

	private void buildHardwareSYS(PeriodType periodType) throws NoAvailableConnectionException, SQLException, IOException {
		ReportHardwareGenerator rg = new ReportHardwareGenerator(getSysCp(), getXapsCp(), xaps, "- - ", getIdentity());
		Date endTmsExc = new Date();
		Date startTmsInc = rg.startReportFromTms(periodType, "report_hw");
		logger.info("ReportGenerator: - Generating HardwareSYSReport (" + periodType.getTypeStr() + "-based) from " + startTmsInc + " to " + endTmsExc);
		Report<RecordHardware> report = null;
		if (periodType == PeriodType.DAY) {
			report = rg.generateFromReport(PeriodType.HOUR, startTmsInc, endTmsExc, null, null);
			report = ReportConverter.convertHardwareReport(report, PeriodType.DAY);
		} else {
			report = rg.generateFromSyslog(periodType, startTmsInc, endTmsExc, null, null, null, null);
		}
		logger.info("ReportGenerator: - Generated HardwareSYSReport (" + periodType.getTypeStr() + "-based), " + report.getMap().size() + " entries");
		populateReportHWTable(getXapsCp(), report);
		logger.info("ReportGenerator: - Populated HardwareSYSReport  (" + periodType.getTypeStr() + "-based)");
	}

	private void buildGroup(PeriodType periodType, Date now) throws NoAvailableConnectionException, SQLException {
		logger.info("ReportGenerator: - Generating and populating GroupReport (" + periodType.getTypeStr() + "-based)");
		populateReportGroupTable(getXapsCp(), now, periodType);

	}

	private void buildJob(PeriodType periodType, Date now) throws NoAvailableConnectionException, SQLException, IOException {
		logger.info("ReportGenerator: - Generating and populating JobReport (" + periodType.getTypeStr() + "-based)");
		populateReportJobTable(getXapsCp(), now, periodType);
	}

	private void buildVoipSYS(PeriodType periodType) throws NoAvailableConnectionException, SQLException, IOException {
		ReportVoipGenerator rg = new ReportVoipGenerator(getSysCp(), getXapsCp(), xaps, "- - ", getIdentity());
		Date endTmsExc = new Date();
		Date startTmsInc = rg.startReportFromTms(periodType, "report_voip");
		logger.info("ReportGenerator: - Generating VoipSYSReport (" + periodType.getTypeStr() + "-based) from " + startTmsInc + " to " + endTmsExc);
		Report<RecordVoip> report = null;
		if (periodType == PeriodType.DAY) {
			report = rg.generateFromReport(PeriodType.HOUR, startTmsInc, endTmsExc, null, null);
			report = ReportConverter.convertVoipReport(report, PeriodType.DAY);
		} else {
			report = rg.generateFromSyslog(periodType, startTmsInc, endTmsExc, null, null, null, null);
		}
		logger.info("ReportGenerator: - Generated VoipSYSReport (" + periodType.getTypeStr() + "-based), " + report.getMap().size() + " entries");
		populateReportVoipTable(getXapsCp(), report);
		logger.info("ReportGenerator: - Populated VoipSYSReport  (" + periodType.getTypeStr() + "-based)");
	}
}
