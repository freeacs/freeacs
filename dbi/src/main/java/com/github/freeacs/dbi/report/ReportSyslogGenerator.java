package com.github.freeacs.dbi.report;

import com.github.freeacs.dbi.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportSyslogGenerator extends ReportGenerator {
  private static Logger logger = LoggerFactory.getLogger(ReportSyslogGenerator.class);

  public ReportSyslogGenerator(DataSource mainDataSource, ACS acs, String logPrefix, Identity id) {
    super(mainDataSource, acs, logPrefix, id);
  }

  public Report<RecordSyslog> generateFromReport(
      PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs)
      throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      boolean foundDataInReportTable = false;
      Report<RecordSyslog> report = new Report<RecordSyslog>(RecordSyslog.class, periodType);

      logger.debug(
          logPrefix + "SyslogReport: Reads from report_syslog table from " + start + " to " + end);
      connection = mainDataSource.getConnection();
      DynamicStatement ds = selectReportSQL("report_syslog", periodType, start, end, uts, prs);
      ps = ds.makePreparedStatement(connection);
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
        RecordSyslog recordTmp =
            new RecordSyslog(
                start, periodType, unittypeName, profileName, severity, eventId, facility);
        Key key = recordTmp.getKey();
        RecordSyslog record = report.getRecord(key);
        if (record == null) {
          record = recordTmp;
        }
        record.getMessageCount().add(rs.getInt("unit_count"));
        report.setRecord(key, record);
        foundDataInReportTable = true;
      }
      if (foundDataInReportTable) {
        logger.debug(
            logPrefix
                + "SyslogReport: Have read "
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

  public Report<RecordSyslog> generateFromSyslog(Date start, Date end, String unitId)
      throws SQLException, ParseException {
    return generateFromSyslog(PeriodType.SECOND, start, end, null, null, unitId, null);
  }

  public Map<String, Report<RecordSyslog>> generateFromSyslog(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      Group group)
      throws SQLException, ParseException {
    Connection c = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
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
      ds.addSql(
          "SELECT date_format(collector_timestamp, '"
              + sqlFormat
              + "'), unit_type_name, profile_name, unit_id, severity, syslog_event_id, facility, count(*) ");
      ds.addSqlAndArguments("FROM syslog WHERE collector_timestamp >= ? AND ", start);
      if (swVersion != null) {
        ds.addSqlAndArguments("facility_version = ?  AND ", swVersion);
      } else if (syslogFilter != null && syslogFilter.getFacilityVersion() != null) {
        ds.addSqlAndArguments("facility_version = ?  AND ", syslogFilter.getFacilityVersion());
      }
      if (syslogFilter != null && syslogFilter.getMessage() != null) {
        ds.addSqlAndArguments("content LIKE ? AND ", syslogFilter.getMessage());
      }
      addUnittypeOrProfileCriteria(ds, uts, prs);
      ds.addSqlAndArguments("collector_timestamp < ? ", end);
      ds.addSql(
          "GROUP BY date_format(collector_timestamp, '"
              + sqlFormat
              + "'), unit_type_name, profile_name, unit_id, severity, syslog_event_id, facility");
      c = mainDataSource.getConnection();
      ps = ds.makePreparedStatement(c);
      rs = ps.executeQuery();
      Map<String, Report<RecordSyslog>> unitReportMap = new HashMap<>();
      int entries = 0;
      while (rs.next()) {
        Date tms = tmsFormatter.parse(rs.getString(1));
        String unitId = rs.getString("unit_id");
        if (group != null && unitsInGroup.get(unitId) == null) {
          continue;
        }
        entries++;
        if (unitId == null || "".equals(unitId.trim())) {
          unitId = "Unknown";
        }
        String unittypeName = rs.getString("unit_type_name");
        if (unittypeName == null || "".equals(unittypeName.trim())) {
          unittypeName = "Unknown";
        }
        String profileName = rs.getString("profile_name");
        if (profileName == null || "".equals(profileName.trim())) {
          profileName = "Unknown";
        }
        String severity = SyslogConstants.getSeverityName(rs.getInt("severity"));
        String eventId = rs.getString("syslog_event_id");
        String facility = SyslogConstants.getFacilityName(rs.getInt("facility"));
        Report<RecordSyslog> report = unitReportMap.get(unitId);
        if (report == null) {
          report = new Report<>(RecordSyslog.class, periodType);
          unitReportMap.put(unitId, report);
        }
        RecordSyslog recordTmp =
            new RecordSyslog(
                tms, periodType, unittypeName, profileName, severity, eventId, facility);
        Key key = recordTmp.getKey();
        RecordSyslog record = report.getRecord(key);
        if (record == null) {
          record = recordTmp;
        }
        record.setMessageCount(new Counter());
        record.getMessageCount().add(rs.getInt("count(*)"));
        report.setRecord(key, record);
      }

      logger.info(
          logPrefix
              + "SyslogReport: Have read "
              + entries
              + " rows from syslog, "
              + unitReportMap.size()
              + " units are mapped");
      return unitReportMap;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  public Report<RecordSyslog> generateFromSyslog(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      String unitId,
      Group group)
      throws SQLException, ParseException {
    Connection c = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
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
        ds.addSql(
            "SELECT date_format(collector_timestamp, '"
                + sqlFormat
                + "'), unit_type_name, profile_name, severity, syslog_event_id, facility, unit_id ");
        ds.addSqlAndArguments("FROM syslog WHERE collector_timestamp >= ? AND ", start);
        if (unitId != null) {
          ds.addSqlAndArguments("unit_id = ? AND ", unitId);
        } else {
          addUnittypeOrProfileCriteria(ds, uts, prs);
        }
        if (swVersion != null) {
          ds.addSqlAndArguments("facility_version = ?  AND ", swVersion);
        } else if (syslogFilter != null && syslogFilter.getFacilityVersion() != null) {
          ds.addSqlAndArguments("facility_version = ?  AND ", syslogFilter.getFacilityVersion());
        }
        if (syslogFilter != null && syslogFilter.getMessage() != null) {
          ds.addSqlAndArguments("content LIKE ? AND ", syslogFilter.getMessage());
        }
        ds.addSqlAndArguments("collector_timestamp < ? ", end);
        c = mainDataSource.getConnection();
        ps = ds.makePreparedStatement(c);
        rs = ps.executeQuery();
        while (rs.next()) {
          String unitIdTmp = rs.getString("unit_id");
          if (unitsInGroup.get(unitIdTmp) == null) {
            continue;
          }
          entries++;
          Date tms = tmsFormatter.parse(rs.getString(1));
          String unittypeName = rs.getString("unit_type_name");
          if (unittypeName == null || "".equals(unittypeName.trim())) {
            unittypeName = "Unknown";
          }
          String profileName = rs.getString("profile_name");
          if (profileName == null || "".equals(profileName.trim())) {
            profileName = "Unknown";
          }
          String severity = SyslogConstants.getSeverityName(rs.getInt("severity"));
          String eventId = rs.getString("syslog_event_id");
          String facility = SyslogConstants.getFacilityName(rs.getInt("facility"));
          RecordSyslog recordTmp =
              new RecordSyslog(
                  tms, periodType, unittypeName, profileName, severity, eventId, facility);
          Key key = recordTmp.getKey();
          RecordSyslog record = report.getRecord(key);
          if (record == null) {
            record = recordTmp;
          }
          record.getMessageCount().add(1);
          report.setRecord(key, record);
        }
      } else {
        DynamicStatement ds = new DynamicStatement();
        ds.addSql(
            "SELECT date_format(collector_timestamp, '"
                + sqlFormat
                + "'), unit_type_name, profile_name, severity, syslog_event_id, facility, count(*) ");
        ds.addSqlAndArguments("FROM syslog WHERE collector_timestamp >= ? AND ", start);
        if (unitId != null) {
          ds.addSqlAndArguments("unit_id = ? AND ", unitId);
        } else {
          addUnittypeOrProfileCriteria(ds, uts, prs);
        }
        if (swVersion != null) {
          ds.addSqlAndArguments("facility_version = ?  AND ", swVersion);
        } else if (syslogFilter != null && syslogFilter.getFacilityVersion() != null) {
          ds.addSqlAndArguments("facility_version = ?  AND ", syslogFilter.getFacilityVersion());
        }
        if (syslogFilter != null && syslogFilter.getMessage() != null) {
          ds.addSqlAndArguments("content LIKE ? AND ", syslogFilter.getMessage());
        }
        ds.addSqlAndArguments("collector_timestamp < ? ", end);
        ds.addSql(
            "GROUP BY date_format(collector_timestamp, '"
                + sqlFormat
                + "'), unit_type_name, profile_name, severity, syslog_event_id, facility");
        c = mainDataSource.getConnection();
        ps = ds.makePreparedStatement(c);
        rs = ps.executeQuery();
        while (rs.next()) {
          entries++;
          Date tms = tmsFormatter.parse(rs.getString(1));
          String unittypeName = rs.getString("unit_type_name");
          if (unittypeName == null || "".equals(unittypeName.trim())) {
            unittypeName = "Unknown";
          }
          String profileName = rs.getString("profile_name");
          if (profileName == null || "".equals(profileName.trim())) {
            profileName = "Unknown";
          }
          String severity = SyslogConstants.getSeverityName(rs.getInt("severity"));
          String eventId = rs.getString("syslog_event_id");
          String facility = SyslogConstants.getFacilityName(rs.getInt("facility"));
          RecordSyslog recordTmp =
              new RecordSyslog(
                  tms, periodType, unittypeName, profileName, severity, eventId, facility);
          Key key = recordTmp.getKey();
          RecordSyslog record = report.getRecord(key);
          if (record == null) {
            record = recordTmp;
          }
          record.setMessageCount(new Counter());
          record.getMessageCount().add(rs.getInt("count(*)"));
          report.setRecord(key, record);
        }
        logger.info(
            logPrefix + "SyslogReport: Using [" + ds.getSqlQuestionMarksSubstituted() + "]");
      }

      logger.info(logPrefix + "SyslogReport: Have read " + entries + " rows from syslog");

      return report;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  private boolean allUnittypesSpecified(
      List<Profile> profiles, Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified) {
    Set<Integer> unittypesWithAllProfilesSpecified = new HashSet<>();
    boolean allUnittypesSpecified = false;
    ACS acs = profiles.get(0).getUnittype().getAcs();
    int noUnittypes = acs.getUnittypes().getUnittypes().length; // the number of unittypes in ACS
    Syslog.doStuff(unittypesWithSomeProfilesSpecified, unittypesWithAllProfilesSpecified, profiles);
    if (noUnittypes == unittypesWithAllProfilesSpecified.size()) {
      allUnittypesSpecified = true;
    }
    return allUnittypesSpecified;
  }

  private DynamicStatement addUnittypeOrProfileCriteria(
      DynamicStatement ds, List<Unittype> unittypes, List<Profile> profiles) {
    User user = id.getUser();
    if (profiles != null && !profiles.isEmpty()) {
      Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified = new HashMap<>();
      boolean allUnittypesSpecified =
          allUnittypesSpecified(profiles, unittypesWithSomeProfilesSpecified);
      if (user.isAdmin() && allUnittypesSpecified) {
        return ds;
      } // no criteria added -> quicker search,  will search for all unittypes/profiles
      ds.addSql("(");
      for (int i = 0; i < profiles.size(); i++) {
        Profile profile = profiles.get(i);
        boolean allProfilesSpecified =
            unittypesWithSomeProfilesSpecified.get(profile.getUnittype().getId()) == null;
        // all profiles in unittype are specified, we can skip profiles criteria
        if (allProfilesSpecified && user.isUnittypeAdmin(profile.getUnittype().getId())) {
          boolean alreadyTreated = false;
          for (int j = 0; j < i; j++) {
            Profile p = profiles.get(j);
            if (p.getId().equals(profile.getId())) {
              alreadyTreated = true;
            }
          }
          if (!alreadyTreated) {
            ds.addSqlAndArguments("unit_type_name = ? OR ", profile.getUnittype().getName());
          }
        } else {
          ds.addSqlAndArguments(
              "(profile_name = ? AND unit_type_name = ?) OR ",
              profile.getName(),
              profile.getUnittype().getName());
        }
      }
      ds.cleanupSQLTail();
      ds.addSql(") AND ");
    } else if (unittypes != null && !unittypes.isEmpty()) {
      ACS acs = unittypes.get(0).getAcs();
      int noUnittypes = acs.getUnittypes().getUnittypes().length;
      boolean isAdmin = user.isAdmin();
      if (noUnittypes > unittypes.size() || !isAdmin) {
        ds.addSql("(");
        for (Unittype unittype : unittypes) {
          ds.addSqlAndArguments("unit_type_name = ? OR ", unittype.getName());
        }
        ds.cleanupSQLTail();
        ds.addSql(") AND ");
      }
    }
    return ds;
  }
}
