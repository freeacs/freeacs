package com.owera.xaps.dbi.report;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;


public class ReportGroupGenerator extends ReportGenerator {

	private static Logger logger = LoggerFactory.getLogger(ReportGroupGenerator.class);

	public ReportGroupGenerator(ConnectionProperties sysCp, ConnectionProperties xapsCp, XAPS xaps, String logPrefix, Identity id) {
		super(sysCp, xapsCp, xaps, logPrefix, id);
	}

	//	private List<SyslogEntry> readSyslog(Date start, Date end, List<Unittype> uts, String unitId, String msgIdentifier) throws SQLException, NoAvailableConnectionException {
	//		Syslog syslog = new Syslog(sysCp, id);
	//		SyslogFilter filter = new SyslogFilter();
	//		filter.setFacility(16); // Only messages from device
	//		filter.setMessage(msgIdentifier);
	//		filter.setUnittypes(uts);
	//		filter.setCollectorTmsStart(start);
	//		filter.setCollectorTmsEnd(end);
	//		filter.setUnitId(unitId);
	//		return syslog.read(filter, xaps);
	//	}

	//	private void addToReport(Report<RecordGroup> report, SyslogEntry entry, PeriodType periodType, String groupName) {
	//		if (entry.getUnittypeName() == null || entry.getProfileName() == null)
	//			return;
	//		if (entry.getFacilityVersion() == null)
	//			entry.setFacilityVersion("Unknown");
	//		RecordGroup recordTmp = new RecordGroup(entry.getCollectorTimestamp(), periodType, entry.getUnittypeName(), groupName);
	//		Key key = recordTmp.getKey();
	//		RecordGroup record = report.getRecord(key);
	//		if (record == null)
	//			record = recordTmp;
	//		record.getUnitCount().inc();
	//		report.setRecord(key, record);
	//	}

	//	private String getMessageIdentifier(Group group) {
	//		String msgIdentifier = null;
	//		for (SyslogEvent se : group.getUnittype().getSyslogEvents().getSyslogEvents()) {
	//			if (se.getTask().getTaskType() == SyslogEventTaskType.GROUPSYNC) {
	//				if (se.getTask().getSyncGroup().getName().equals(group.getName()))
	//					msgIdentifier = se.getExpression().toString();
	//			}
	//		}
	//		return msgIdentifier;
	//
	//	}

	//	public Report<RecordGroup> generateFromSyslog(PeriodType periodType, Date start, Date end, List<Unittype> uts, Group group, String unitId) throws NoAvailableConnectionException, SQLException,
	//			IOException {
	//		Report<RecordGroup> report = new Report<RecordGroup>(RecordGroup.class, periodType);
	//		logInfo("TimeGroupReport", unitId, uts, null, start, end);
	//		if (group.getTimeParameter() == null) {
	//			logger.info(logPrefix + "TimeGroupReport: The group was not a time (rolling) group - no report produced");
	//			return report;
	//		}
	//		String msgIdentifier = getMessageIdentifier(group);
	//		if (msgIdentifier == null) {
	//			logger.info(logPrefix + "TimeGroupReport: The group was a time rolling group, but no syslog event are synching with this group - no report produced");
	//			return report;
	//		}
	//		List<SyslogEntry> entries = readSyslog(start, end, uts, unitId, msgIdentifier);
	//		for (SyslogEntry entry : entries) {
	//			addToReport(report, entry, periodType, group.getName());
	//		}
	//		
	//		logger.info(logPrefix + "TimeGroupReport: Have read " + entries.size() + " rows from syslog, report is now " + report.getMap().size() + " entries");
	//		return report;
	//	}

	//	public Report<RecordGroup> generateFromSyslog(Date start, Date end, String unitId) throws NoAvailableConnectionException, SQLException, IOException {
	//		return generateFromSyslog(PeriodType.SECOND, start, end, null, null, unitId);
	//	}

	//	public Map<String, Report<RecordGroup>> generateFromSyslog(PeriodType periodType, Date start, Date end, List<Unittype> uts, Group group) throws NoAvailableConnectionException, SQLException,
	//			IOException {
	//		logInfo("TimeGroupReport", null, uts, null, start, end);
	//		Map<String, Report<RecordGroup>> unitReportMap = new HashMap<String, Report<RecordGroup>>();
	//		if (group.getTimeParameter() == null) {
	//			logger.info(logPrefix + "TimeGroupReport: The group was not a time (rolling) group - no report produced");
	//			return unitReportMap;
	//		}
	//		String msgIdentifier = getMessageIdentifier(group);
	//		if (msgIdentifier == null) {
	//			logger.info(logPrefix + "TimeGroupReport: The group was a time rolling group, but no syslog event are synching with this group - no report produced");
	//			return unitReportMap;
	//		}
	//		List<SyslogEntry> entries = readSyslog(start, end, uts, null, msgIdentifier);
	//		for (SyslogEntry entry : entries) {
	//			if (entry.getUnittypeName() == null || entry.getProfileName() == null)
	//				continue;
	//			String unitId = entry.getUnitId();
	//			Report<RecordGroup> report = unitReportMap.get(unitId);
	//			if (report == null) {
	//				report = new Report<RecordGroup>(RecordGroup.class, periodType);
	//				unitReportMap.put(unitId, report);
	//			}
	//			addToReport(report, entry, periodType, group.getName());
	//		}
	//		
	//		logger.info(logPrefix + "TimeGroupReport: Have read " + entries.size() + " rows from syslog, " + unitReportMap.size() + " units are mapped");
	//		return unitReportMap;
	//	}

	public Report<RecordGroup> generateGroupReport(PeriodType periodType, Date start, Date end, List<Unittype> uts, Group g) throws NoAvailableConnectionException, SQLException, IOException {
		Connection xapsConnection = null;
		Connection sysConnection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			Report<RecordGroup> report = new Report<RecordGroup>(RecordGroup.class, periodType);
			xapsConnection = ConnectionProvider.getConnection(xapsCp, true);
			
			logger.info(logPrefix + "Reads from report_group table from " + start + " to " + end);
			DynamicStatement ds = selectReportSQL("report_group", periodType, start, end, uts, null);
			if (g != null) {
				ds.addSqlAndArguments(" and group_name = ?", g.getName());
			}
			ps = ds.makePreparedStatement(xapsConnection);
			rs = ps.executeQuery();
			int counter = 0;
			while (rs.next()) {
				counter++;
				start = rs.getTimestamp("timestamp_");
				String unittypeName = rs.getString("unit_type_name");
				String groupName = rs.getString("group_name");
				RecordGroup recordTmp = new RecordGroup(start, periodType, unittypeName, groupName);
				Key key = recordTmp.getKey();
				RecordGroup record = report.getRecord(key);
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

}
