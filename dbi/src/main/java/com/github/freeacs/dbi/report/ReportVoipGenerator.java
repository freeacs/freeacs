package com.github.freeacs.dbi.report;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DynamicStatement;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogFilter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportVoipGenerator extends ReportGenerator {
  private static Logger logger = LoggerFactory.getLogger(ReportVoipGenerator.class);
  private static Pattern qosPattern =
      Pattern.compile(
          ".*MOS: (\\d+)[^\\d]+(\\d+)[^\\d]+(\\d+)[^\\d]+(\\d+):(\\d+):(\\d+)[^\\d]+(\\d+)[^\\d]+(\\d+)[^\\d]+");
  private static Pattern qosChannelPattern = Pattern.compile(".*channel (\\d+).*");
  /** Reg failed: ua0: reg failed 613883@nettala.fo: 903 DNS Error (0 bindings) */
  private static Pattern regfailedPattern = Pattern.compile(".*reg failed.*");

  public ReportVoipGenerator(DataSource mainDataSource, ACS acs, String logPrefix, Identity id) {
    super(mainDataSource, acs, logPrefix, id);
  }

  public Report<RecordVoip> generateFromReport(
      PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs)
      throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      boolean foundDataInReportTable = false;
      Report<RecordVoip> report = new Report<>(RecordVoip.class, periodType);

      logger.debug(
          logPrefix + "VoipReport: Reads from report_voip table from " + start + " to " + end);
      connection = mainDataSource.getConnection();
      DynamicStatement ds = selectReportSQL("report_voip", periodType, start, end, uts, prs);
      ps = ds.makePreparedStatement(connection);
      rs = ps.executeQuery();
      int counter = 0;
      while (rs.next()) {
        counter++;
        start = rs.getTimestamp("timestamp_");
        String unittypeName = rs.getString("unit_type_name");
        String profileName = rs.getString("profile_name");
        String softwareVersion = rs.getString("software_version");
        String line = rs.getString("line");
        RecordVoip recordTmp =
            new RecordVoip(start, periodType, unittypeName, profileName, softwareVersion, line);
        Key key = recordTmp.getKey();
        RecordVoip record = report.getRecord(key);
        if (record == null) {
          record = recordTmp;
        }
        record.getIncomingCallCount().add(rs.getInt("incoming_call_count"));
        record.getCallLengthTotal().add(rs.getInt("call_length_total"));
        record.getOutgoingCallCount().add(rs.getInt("outgoing_call_count"));
        record.getAbortedCallCount().add(rs.getInt("aborted_call_count"));
        record
            .getCallLengthAvg()
            .add(
                rs.getInt("call_length_avg"),
                record.getIncomingCallCount().get() + record.getOutgoingCallCount().get());
        record.getJitterAvg().add(rs.getInt("jitter_avg"), record.getCallLengthTotal().get());
        record.getJitterMax().add(rs.getInt("jitter_max"), record.getCallLengthTotal().get());
        record.getMosAvg().add(rs.getInt("mos_avg"), record.getCallLengthTotal().get());
        record.getOutgoingCallFailedCount().add(rs.getInt("outgoing_call_failed_count"));
        record
            .getPercentLossAvg()
            .add(rs.getInt("percent_loss_avg"), record.getCallLengthTotal().get());
        record.getNoSipServiceTime().add(rs.getInt("no_sip_service_time"));
        report.setRecord(key, record);
        foundDataInReportTable = true;
      }
      if (foundDataInReportTable) {
        logger.debug(
            logPrefix
                + "VoipReport: Have read "
                + counter
                + " rows, last tms was "
                + start
                + ", report is now "
                + report.getMap().size()
                + " entries");
      }
      return report;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
  }

  /**
   * Generate reports directly from syslog - retrieve data for one single unit and with periodtype =
   * SECOND.
   */
  public Report<RecordVoip> generateFromSyslog(Date start, Date end, String unitId)
      throws SQLException {
    return generateFromSyslog(PeriodType.SECOND, start, end, null, null, unitId, null);
  }

  /**
   * Generate reports directly from syslog - retrieve data for a set of units, but keep them
   * separated in a map of reports.
   */
  public Map<String, Report<RecordVoip>> generateFromSyslog(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      Group group)
      throws SQLException {
    logInfo("VoipReport", null, uts, prs, start, end);
    Syslog syslog = new Syslog(mainDataSource, id);
    SyslogFilter filter = new SyslogFilter();
    filter.setFacility(16); // Only messages from device
    filter.setMessage("^QoS|^ua_: reg failed");
    filter.setProfiles(prs);
    filter.setUnittypes(uts);
    filter.setCollectorTmsStart(start);
    filter.setCollectorTmsEnd(end);
    filter.setFacilityVersion(swVersion);
    Map<String, Unit> unitsInGroup = getUnitsInGroup(group);
    List<SyslogEntry> entries = syslog.read(filter, acs);
    Map<String, Report<RecordVoip>> unitReportMap = new HashMap<>();
    for (SyslogEntry entry : entries) {
      if (entry.getUnittypeName() == null
          || entry.getProfileName() == null
          || (group != null && unitsInGroup.get(entry.getUnitId()) == null)) {
        continue;
      }
      String unitId = entry.getUnitId();
      Report<RecordVoip> report = unitReportMap.get(unitId);
      if (report == null) {
        report = new Report<>(RecordVoip.class, periodType);
        unitReportMap.put(unitId, report);
      }
      Matcher m = qosChannelPattern.matcher(entry.getContent());
      String channel = "0";
      if (m.matches()) {
        channel = m.group(1);
      }
      if (entry.getFacilityVersion() == null || "".equals(entry.getFacilityVersion().trim())) {
        entry.setFacilityVersion("Unknown");
      }
      RecordVoip recordTmp =
          new RecordVoip(
              entry.getCollectorTimestamp(),
              periodType,
              entry.getUnittypeName(),
              entry.getProfileName(),
              entry.getFacilityVersion(),
              channel);
      Key key = recordTmp.getKey();
      RecordVoip record = report.getRecord(key);
      if (record == null) {
        record = recordTmp;
      }
      try {
        parseContentAndPopulateRecord(record, entry.getContent());
        report.setRecord(key, record);
      } catch (SyslogParseException spe) {
        // ignore this record
      }
    }

    logger.info(
        logPrefix
            + "VoipReport: Have read "
            + entries.size()
            + " rows from syslog, "
            + unitReportMap.size()
            + " units are mapped");
    return unitReportMap;
  }

  /** Generate reports directly from syslog - retrieve data for a whole set of units. */
  public Report<RecordVoip> generateFromSyslog(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      String unitId,
      Group group)
      throws SQLException {
    Report<RecordVoip> report = new Report<>(RecordVoip.class, periodType);
    logInfo("VoipReport", unitId, uts, prs, start, end);
    Syslog syslog = new Syslog(mainDataSource, id);
    SyslogFilter filter = new SyslogFilter();
    filter.setFacility(16); // Only messages from device
    filter.setMessage("^QoS|^ua_: reg failed");
    if (unitId != null) {
      filter.setUnitId("^" + unitId + "$");
    }
    filter.setProfiles(prs);
    filter.setUnittypes(uts);
    filter.setCollectorTmsStart(start);
    filter.setCollectorTmsEnd(end);
    filter.setFacilityVersion(swVersion);
    Map<String, Unit> unitsInGroup = getUnitsInGroup(group);
    List<SyslogEntry> entries = syslog.read(filter, acs);
    for (SyslogEntry entry : entries) {
      if (entry.getUnittypeName() == null
          || entry.getProfileName() == null
          || (group != null && unitsInGroup.get(entry.getUnitId()) == null)) {
        continue;
      }
      Matcher m = qosChannelPattern.matcher(entry.getContent());
      String channel = "0";
      if (m.matches()) {
        channel = m.group(1);
      }
      if (entry.getFacilityVersion() == null || "".equals(entry.getFacilityVersion().trim())) {
        entry.setFacilityVersion("Unknown");
      }
      RecordVoip recordTmp =
          new RecordVoip(
              entry.getCollectorTimestamp(),
              periodType,
              entry.getUnittypeName(),
              entry.getProfileName(),
              entry.getFacilityVersion(),
              channel);
      Key key = recordTmp.getKey();
      RecordVoip record = report.getRecord(key);
      if (record == null) {
        record = recordTmp;
      }
      try {
        parseContentAndPopulateRecord(record, entry.getContent());
        report.setRecord(key, record);
      } catch (SyslogParseException spe) {
        // ignore this record
      }
    }

    logger.info(
        logPrefix
            + "VoipReport: Have read "
            + entries.size()
            + " rows from syslog, report is now "
            + report.getMap().size()
            + " entries");
    return report;
  }

  private void parseContentAndPopulateRecord(RecordVoip record, String content)
      throws SyslogParseException {
    try {
      Matcher m = qosPattern.matcher(content);
      if (m.matches()) {
        long mosAvg = Long.parseLong(m.group(1));
        if (mosAvg < 100) {
          mosAvg = 100;
        }
        long jitterAvg = Long.parseLong(m.group(2));
        long jitterMax = Long.parseLong(m.group(3));
        long okCallHour = Long.parseLong(m.group(4));
        long okCallMin = Long.parseLong(m.group(5));
        long okCallSec = Long.parseLong(m.group(6));
        long concealedCallLength = Long.parseLong(m.group(7));
        long callSecTotal =
            ((okCallHour * 3600 + okCallMin * 60 + okCallSec) * 1000 + concealedCallLength) / 1000;
        if (callSecTotal == 0) {
          callSecTotal = 1;
        }
        long lossPercent = Long.parseLong(m.group(8));
        record.getCallLengthTotal().add(callSecTotal);
        record.getCallLengthAvg().add(callSecTotal);
        record.getJitterMax().add(jitterMax, callSecTotal);
        record.getJitterAvg().add(jitterAvg, callSecTotal);
        record.getMosAvg().add(mosAvg, callSecTotal);
        record.getPercentLossAvg().add(lossPercent, callSecTotal);
        record.getIncomingCallCount().inc();
      }
      m = regfailedPattern.matcher(content);
      if (m.matches()) {
        record.getNoSipServiceTime().add(1);
      }
    } catch (Throwable t) {
      throw new SyslogParseException();
    }
  }
}
