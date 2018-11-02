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
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvStatus;
import java.io.IOException;
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

public class ReportProvisioningGenerator extends ReportGenerator {
  private static Logger logger = LoggerFactory.getLogger(ReportProvisioningGenerator.class);
  private static String provMsgId = "^ProvMsg: PP:";
  private static Pattern provPattern =
      Pattern.compile(provMsgId + ".*ST:(\\w+), PO:(\\w+), SL:(\\d+)");

  public ReportProvisioningGenerator(
      DataSource mainDataSource, ACS acs, String logPrefix, Identity id) {
    super(mainDataSource, acs, logPrefix, id);
  }

  public Report<RecordProvisioning> generateFromReport(
      PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs)
      throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      boolean foundDataInReportTable = false;
      Report<RecordProvisioning> report = new Report<>(RecordProvisioning.class, periodType);

      logger.debug(
          logPrefix
              + "ProvisioningReport: Reads from report_prov table from "
              + start
              + " to "
              + end);
      connection = mainDataSource.getConnection();
      DynamicStatement ds = selectReportSQL("report_prov", periodType, start, end, uts, prs);
      ps = ds.makePreparedStatement(connection);
      rs = ps.executeQuery();
      int counter = 0;
      while (rs.next()) {
        counter++;
        start = rs.getTimestamp("timestamp_");
        String unittypeName = rs.getString("unit_type_name");
        String profileName = rs.getString("profile_name");
        String softwareVersion = rs.getString("software_version");
        String output = rs.getString("prov_output");
        RecordProvisioning recordTmp =
            new RecordProvisioning(
                start, periodType, unittypeName, profileName, softwareVersion, output);
        Key key = recordTmp.getKey();
        RecordProvisioning record = report.getRecord(key);
        if (record == null) {
          record = recordTmp;
        }
        record.getProvisioningOkCount().add(rs.getInt("ok_count"));
        record.getProvisioningRescheduledCount().add(rs.getInt("rescheduled_count"));
        record.getProvisioningErrorCount().add(rs.getInt("error_count"));
        record.getProvisioningMissingCount().add(rs.getInt("missing_count"));
        record
            .getSessionLengthAvg()
            .add(
                rs.getInt("session_length_avg"),
                record.getProvisioningOkCount().get()
                    + record.getProvisioningRescheduledCount().get()
                    + record.getProvisioningErrorCount().get()
                    + record.getProvisioningMissingCount().get());
        report.setRecord(key, record);
        foundDataInReportTable = true;
      }
      if (foundDataInReportTable) {
        logger.debug(
            logPrefix
                + "ProvisioningReport: Have read "
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
   * Generate reports directly from syslog - retrieve data for a set of units, but keep them
   * separated in a map of reports.
   */
  public Map<String, Report<RecordProvisioning>> generateFromSyslog(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      Group group)
      throws SQLException {
    return generateFromSyslogImpl(periodType, start, end, uts, prs, null, group);
  }

  /**
   * Generate reports directly from syslog - retrieve data for one single unit and with periodtype =
   * SECOND.
   */
  public Report<RecordProvisioning> generateFromSyslog(Date start, Date end, String unitId)
      throws SQLException, IOException {
    return generateFromSyslog(PeriodType.SECOND, start, end, null, null, unitId, null);
  }

  /** Generate reports directly from syslog - retrieve data for a whole set of units. */
  public Report<RecordProvisioning> generateFromSyslog(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      String unitId,
      Group group)
      throws SQLException {
    Map<String, Report<RecordProvisioning>> unitReportMap =
        generateFromSyslogImpl(periodType, start, end, uts, prs, unitId, group);
    Report<RecordProvisioning> endReport = new Report<>(RecordProvisioning.class, periodType);

    for (Report<RecordProvisioning> report : unitReportMap.values()) {
      for (RecordProvisioning record : report.getMap().values()) {
        RecordProvisioning endRecord = endReport.getRecord(record.getKey());
        if (endRecord != null) {
          endRecord.add(record);
        } else {
          endReport.setRecord(record.getKey(), record);
        }
      }
    }
    return endReport;
  }

  private Map<String, Report<RecordProvisioning>> generateFromSyslogImpl(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      String unitId,
      Group group)
      throws SQLException {
    logInfo("ProvisioningReport", unitId, uts, prs, start, end);
    Syslog syslog = new Syslog(mainDataSource, id);
    SyslogFilter filter = new SyslogFilter();
    filter.setMessage(provMsgId);
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
    Map<String, Report<RecordProvisioning>> unitReportMap = new HashMap<>();
    for (SyslogEntry entry : entries) {
      if (group != null && unitsInGroup.get(entry.getUnitId()) == null) {
        continue;
      }
      if (entry.getUnittypeName() == null) {
        entry.setUnittypeName("Unknown");
      }
      if (entry.getProfileName() == null) {
        entry.setProfileName("Unknown");
      }
      if (entry.getFacilityVersion() == null) {
        entry.setFacilityVersion("Unknown");
      }
      String unitIdEntry = entry.getUnitId();
      Report<RecordProvisioning> report = unitReportMap.get(unitIdEntry);
      if (report == null) {
        report = new Report<>(RecordProvisioning.class, periodType);
        unitReportMap.put(unitIdEntry, report);
      }
      Matcher m = provPattern.matcher(entry.getContent());

      RecordProvisioning recordTmp;
      if (m.find()) {
        recordTmp =
            new RecordProvisioning(
                entry.getCollectorTimestamp(),
                periodType,
                entry.getUnittypeName(),
                entry.getProfileName(),
                entry.getFacilityVersion(),
                m.group(2));
        Key key = recordTmp.getKey();
        RecordProvisioning record =
            report.getRecord(key) == null ? recordTmp : report.getRecord(key);
        ProvStatus status = ProvStatus.valueOf(m.group(1));
        switch (status) {
          case OK:
            record.getProvisioningOkCount().add(1);
            break;
          case DELAYED:
            record.getProvisioningRescheduledCount().add(1);
            break;
          case ERROR:
            record.getProvisioningErrorCount().add(1);
            break;
        }
        record.getSessionLengthAvg().add(Integer.valueOf(m.group(3)));
        report.setRecord(key, record);
      } else if (entry.getContent().startsWith("ProvMsg: Expected provisioning")) {
        recordTmp =
            new RecordProvisioning(
                entry.getCollectorTimestamp(),
                periodType,
                entry.getUnittypeName(),
                entry.getProfileName(),
                entry.getFacilityVersion(),
                "N/A");
        Key key = recordTmp.getKey();
        RecordProvisioning record =
            report.getRecord(key) == null ? recordTmp : report.getRecord(key);
        record.getProvisioningMissingCount().add(1);
        record.getSessionLengthAvg().add(0);
        report.setRecord(key, record);
      }
    }
    logger.info(
        logPrefix
            + "ProvisioningReport: Have read "
            + entries.size()
            + " rows from syslog, "
            + unitReportMap.size()
            + " units are mapped");
    return unitReportMap;
  }
}
