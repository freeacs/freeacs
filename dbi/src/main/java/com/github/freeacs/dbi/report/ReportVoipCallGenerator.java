package com.github.freeacs.dbi.report;

import com.github.freeacs.dbi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReportVoipCallGenerator extends ReportGenerator {

	private static Logger logger = LoggerFactory.getLogger(ReportVoipCallGenerator.class);
	private static Pattern mosChannelPattern = Pattern.compile(".*Channel (\\d+).*");
	// MOS-report: MOS Report: Channel 0: MOS: 434
	private static Pattern mosPattern = Pattern.compile("MOS: (\\d+)");

	public ReportVoipCallGenerator(DataSource mainDataSource, DataSource syslogDataSource, ACS acs, String logPrefix, Identity id) {
		super(mainDataSource, syslogDataSource, acs, logPrefix, id);
	}

	public Report<RecordVoipCall> generateFromSyslog(Date start, Date end, String unitId, String line) throws SQLException, IOException {
		return generateFromSyslog(PeriodType.SECOND, start, end, null, null, unitId, line, null);
	}

	public Report<RecordVoipCall> generateFromSyslog(PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs, String unitId, String line, Group group)
			throws SQLException, IOException {
		Report<RecordVoipCall> report = new Report<RecordVoipCall>(RecordVoipCall.class, periodType);
		logInfo("VoipCallReport", null, uts, prs, start, end);
		if (unitId != null)
			unitId = "^" + unitId + "$";
		List<SyslogEntry> entries = readSyslog(start, end, uts, prs, unitId, line);
		Map<String, Unit> unitsInGroup = getUnitsInGroup(group);
		for (SyslogEntry entry : entries) {
			if (group != null && unitsInGroup.get(entry.getUnitId()) == null)
				continue;
			addToReport(report, entry, periodType);
		}
		
		logger.info(logPrefix + "HardwareReport: Have read " + entries.size() + " rows from syslog, report is now " + report.getMap().size() + " entries");
		return report;
	}

	public Map<String, Report<RecordVoipCall>> generateFromSyslog(PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs, Group group)
			throws SQLException, IOException {
		logInfo("VoipCallReport", null, uts, prs, start, end);
		Map<String, Unit> unitsInGroup = getUnitsInGroup(group);
		List<SyslogEntry> entries = readSyslog(start, end, uts, prs, null, null);
		Map<String, Report<RecordVoipCall>> unitReportMap = new HashMap<String, Report<RecordVoipCall>>();
		for (SyslogEntry entry : entries) {
			if (entry.getUnittypeName() == null || entry.getProfileName() == null)
				continue;
			if (group != null && unitsInGroup.get(entry.getUnitId()) == null)
				continue;
			String unitId = entry.getUnitId();
			Report<RecordVoipCall> report = unitReportMap.get(unitId);
			if (report == null) {
				report = new Report<RecordVoipCall>(RecordVoipCall.class, periodType);
				unitReportMap.put(unitId, report);
			}
			addToReport(report, entry, periodType);
		}
		
		logger.info(logPrefix + "VoipCallReport: Have read " + entries.size() + " rows from syslog, " + unitReportMap.size() + " units are mapped");
		return unitReportMap;
	}

	private void parseContentAndPopulateRecord(RecordVoipCall record, String content, Date tms) {
		Matcher m = mosPattern.matcher(content);
		if (m.find()) {
			record.getUnitCount().add(1);
			record.getMosAvg().add(Integer.parseInt(m.group(1)), 1);
		}
	}

	private List<SyslogEntry> readSyslog(Date start, Date end, List<Unittype> uts, List<Profile> prs, String unitId, String line) throws SQLException {
		Syslog syslog = new Syslog(syslogDataSource, id);
		SyslogFilter filter = new SyslogFilter();
		filter.setFacility(16); // Only messages from device
		if (line == null)
			filter.setMessage("MOS Report:");
		else
			filter.setMessage("MOS Report: Channel " + line);
		filter.setUnitId(unitId);
		filter.setProfiles(prs);
		filter.setUnittypes(uts);
		filter.setCollectorTmsStart(start);
		filter.setCollectorTmsEnd(end);
		filter.setFacilityVersion(swVersion);
		return syslog.read(filter, acs);
	}

	private void addToReport(Report<RecordVoipCall> report, SyslogEntry entry, PeriodType periodType) {
		if (entry.getUnittypeName() == null || entry.getProfileName() == null)
			return;
		Matcher m = mosChannelPattern.matcher(entry.getContent());
		String channel = "0";
		if (m.matches())
			channel = "" + m.group(1);
		RecordVoipCall recordTmp = new RecordVoipCall(entry.getCollectorTimestamp(), periodType, entry.getUnittypeName(), entry.getProfileName(), entry.getFacilityVersion(), channel);
		Key key = recordTmp.getKey();
		RecordVoipCall record = report.getRecord(key);
		if (record == null)
			record = recordTmp;
		parseContentAndPopulateRecord(record, entry.getContent(), entry.getCollectorTimestamp());
		report.setRecord(key, record);
	}

}
