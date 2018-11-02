package com.github.freeacs.dbi.report;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DynamicStatement;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.SyslogFilter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Users;
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
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportGenerator {
  private static Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

  protected TmsConverter converter;
  protected DataSource syslogDataSource;
  protected DataSource mainDataSource;
  protected ACS acs;
  protected Identity id;
  protected String logPrefix = "";

  /** Report filters - make report based on these parameters (if possible). */
  protected PeriodType periodType;

  protected Date start;
  protected Date end;
  protected List<Unittype> unittypes;
  protected List<Profile> profiles;
  protected Group group;
  protected String swVersion;
  protected SyslogFilter syslogFilter;

  public ReportGenerator(DataSource mainDataSource, ACS acs, String logPrefix, Identity id) {
    this.mainDataSource = mainDataSource;
    this.acs = acs;
    this.id = id;
    if (logPrefix != null) {
      this.logPrefix = logPrefix;
    }
    this.converter = new TmsConverter();
  }

  protected DynamicStatement selectReportSQL(
      String tableName,
      PeriodType pt,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs) {
    DynamicStatement ds = new DynamicStatement();
    if (pt == PeriodType.MONTH) {
      ds.addSqlAndArguments(
          "select * from " + tableName + " where period_type = ? and ",
          PeriodType.DAY.getTypeInt());
    } else {
      ds.addSqlAndArguments(
          "select * from " + tableName + " where period_type = ? and ", pt.getTypeInt());
    }
    if (prs != null && !prs.isEmpty()) {
      ds.addSql("(");
      for (Profile p : prs) {
        ds.addSqlAndArguments(
            "(unit_type_name = ? and profile_name = ?) or ",
            p.getUnittype().getName(),
            p.getName());
      }
      if (id.getUser().getUsername().equals(Users.USER_ADMIN)) {
        int numberOfProfiles = 0;
        for (Unittype ut : acs.getUnittypes().getUnittypes()) {
          numberOfProfiles += ut.getProfiles().getProfiles().length;
        }
        if (prs.size() >= numberOfProfiles) {
          ds.addSqlAndArguments(
              "(unit_type_name = ? and profile_name = ?) or ", "Unknown", "Unknown");
        }
      }
      ds.cleanupSQLTail();
      ds.addSql(") and ");
    } else if (uts != null && !uts.isEmpty()) {
      ds.addSql("(");
      for (Unittype ut : uts) {
        ds.addSqlAndArguments("unit_type_name = ? or ", ut.getName());
      }
      if (id.getUser().getUsername().equals(Users.USER_ADMIN)
          && uts.size() >= acs.getUnittypes().getUnittypes().length) {
        ds.addSqlAndArguments("unit_type_name = ? or ", "Unknown");
      }
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
    if (end != null) {
      ds.addSqlAndArguments("timestamp_ < ? and ", end);
    }
    if (swVersion != null) {
      ds.addSqlAndArguments("software_version = ?", swVersion);
    }
    ds.cleanupSQLTail();
    return ds;
  }

  public List<String> getSoftwareVersions(
      Unittype unittype, Profile profile, Date start, Date end, String tablename)
      throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    SQLException sqle = null;
    List<String> swVersionList = new ArrayList<>();
    try {
      connection = mainDataSource.getConnection();
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
      if (unittype != null) {
        ds.addSqlAndArguments("and unit_type_name = ? ", unittype.getName());
      }
      if (profile != null) {
        ds.addSqlAndArguments("and profile_name = ? ", profile.getName());
      }
      ds.addSql(" order by software_version asc");
      ps = ds.makePreparedStatement(connection);
      ps.setFetchSize(1);
      rs = ps.executeQuery();
      while (rs.next()) {
        swVersionList.add(rs.getString("software_version"));
      }
      return swVersionList;
    } catch (SQLException sqlex) {
      sqle = sqlex;
      throw sqlex;
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
   * Find the previous timestamp where reports have been made, then roll forward to next timestamp
   * to perform reporting. If the old timestamp is more than 2 days ago, then default to maximum 2
   * days ago. If the old timestamp is null, default to 2 days ago.
   *
   * @param periodType
   * @param tablename
   * @return
   * @throws SQLException
   */
  public Date startReportFromTms(PeriodType periodType, String tablename) throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    SQLException sqle = null;
    try {
      long now = System.currentTimeMillis();
      long twoDaysAgo = now - 2L * 86400L * 1000L;
      connection = mainDataSource.getConnection();
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments(
          "select timestamp_ from "
              + tablename
              + " where period_type = "
              + periodType.getTypeInt()
              + " order by timestamp_ desc");
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

  public Map<String, Unit> getUnitsInGroup(Group group) throws SQLException {
    Map<String, Unit> unitsInGroup = new HashMap<>();
    if (group != null) {
      ACSUnit acsUnit = new ACSUnit(mainDataSource, acs, acs.getSyslog());
      unitsInGroup = acsUnit.getUnits(group);
    }
    return unitsInGroup;
  }

  public Report<RecordUnit> generateUnitReport(
      PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs)
      throws SQLException, IOException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    SQLException sqle = null;
    try {
      Report<RecordUnit> report = new Report<RecordUnit>(RecordUnit.class, periodType);
      connection = mainDataSource.getConnection();

      logger.info(logPrefix + "Reads from report_unit table from " + start + " to " + end);
      DynamicStatement ds = selectReportSQL("report_unit", periodType, start, end, uts, prs);
      ps = ds.makePreparedStatement(connection);
      rs = ps.executeQuery();
      int counter = 0;
      while (rs.next()) {
        counter++;
        start = rs.getTimestamp("timestamp_");
        String unittypeName = rs.getString("unit_type_name");
        String profileName = rs.getString("profile_name");
        String softwareVersion = rs.getString("software_version");
        String status = rs.getString("status");
        RecordUnit recordTmp =
            new RecordUnit(start, periodType, unittypeName, profileName, softwareVersion, status);
        Key key = recordTmp.getKey();
        RecordUnit record = report.getRecord(key);
        if (record == null) {
          record = recordTmp;
        }
        record.getUnitCount().set(rs.getInt("unit_count"));
        report.setRecord(key, record);
      }
      logger.info(
          logPrefix
              + "Have read "
              + counter
              + " rows, last tms was "
              + start
              + ", report is now "
              + report.getMap().size()
              + " entries");
      return report;
    } catch (SQLException sqlex) {
      sqle = sqlex;
      throw sqlex;
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

  public Report<RecordJob> generateJobReport(
      PeriodType periodType, Date start, Date end, List<Unittype> uts)
      throws SQLException, IOException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    SQLException sqle = null;
    try {
      Report<RecordJob> report = new Report<RecordJob>(RecordJob.class, periodType);
      connection = mainDataSource.getConnection();

      logger.info(logPrefix + "Reads from report_job table from " + start + " to " + end);
      DynamicStatement ds = selectReportSQL("report_job", periodType, start, end, uts, null);
      ps = ds.makePreparedStatement(connection);
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
        if (record == null) {
          record = recordTmp;
        }
        record.getCompleted().set(rs.getInt("completed"));
        record.getGroupSize().set(rs.getInt("group_size"));
        record.getConfirmedFailed().set(rs.getInt("confirmed_failed"));
        record.getUnconfirmedFailed().set(rs.getInt("unconfirmed_failed"));
        report.setRecord(key, record);
      }
      logger.info(
          logPrefix
              + "Have read "
              + counter
              + " rows, last tms was "
              + start
              + ", report is now "
              + report.getMap().size()
              + " entries");
      return report;
    } catch (SQLException sqlex) {
      sqle = sqlex;
      throw sqlex;
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

  protected void logInfo(
      String reportType,
      String unitId,
      List<Unittype> uts,
      List<Profile> prs,
      Date start,
      Date end) {
    String msg = logPrefix + reportType + ": Will generate from syslog (";
    if (unitId != null) {
      msg += "unitId: " + unitId + ", ";
    }
    if (uts != null) {
      msg += "unittypes: " + uts.size() + ", ";
    }
    if (prs != null) {
      msg += "profile: " + prs.size() + ", ";
    }
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
