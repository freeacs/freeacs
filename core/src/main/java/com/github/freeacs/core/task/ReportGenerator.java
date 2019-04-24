package com.github.freeacs.core.task;

import com.github.freeacs.common.scheduler.ScheduleType;
import com.github.freeacs.core.Properties;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.report.Key;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.RecordGroup;
import com.github.freeacs.dbi.report.RecordHardware;
import com.github.freeacs.dbi.report.RecordJob;
import com.github.freeacs.dbi.report.RecordProvisioning;
import com.github.freeacs.dbi.report.RecordSyslog;
import com.github.freeacs.dbi.report.RecordUnit;
import com.github.freeacs.dbi.report.RecordVoip;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportConverter;
import com.github.freeacs.dbi.report.ReportHardwareGenerator;
import com.github.freeacs.dbi.report.ReportProvisioningGenerator;
import com.github.freeacs.dbi.report.ReportSyslogGenerator;
import com.github.freeacs.dbi.report.ReportVoipGenerator;
import com.github.freeacs.dbi.report.TmsConverter;
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
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportGenerator extends DBIOwner {
  private static final String LOG_PREFIX = "- - ";
  private static long MINUTE_MS = 60 * 1000;
  private static long HOUR_MS = 60 * MINUTE_MS;
  private static long DAY_MS = 24 * HOUR_MS;
  private static long MONTH_MS = 31 * DAY_MS;

  private static Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
  private final Properties properties;
  private ACS acs;
  private ScheduleType scheduleType;
  private TmsConverter converter;

  public ReportGenerator(
      String taskName, ScheduleType scheduleType, DBI dbi, Properties properties) {
    super(taskName, dbi);
    this.scheduleType = scheduleType;
    this.properties = properties;
    this.converter = new TmsConverter();
  }

  @Override
  public void runImpl() throws Exception {
    acs = getLatestACS();
    if (scheduleType == ScheduleType.DAILY) {
      dailyJobs();
    } else if (scheduleType == ScheduleType.HOURLY) {
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
      ds2.addArguments(
          record.getTms(),
          record.getPeriodType().getTypeInt(),
          record.getUnittypeName(),
          record.getGroupName());
      ps = ds2.makePreparedStatement(c);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt("unit_count");
      }
      return 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (rs != null) {
        rs.close();
      }
    }
  }

  private void populateReportGroupTable(DataSource cp, Date now, PeriodType pt)
      throws SQLException {
    Connection c = null;
    PreparedStatement ps = null;
    int inserted = 0;
    int updated = 0;
    try {
      c = cp.getConnection();
      ACSUnit acsUnit = new ACSUnit(cp, acs, getSyslog());
      for (Unittype unittype : acs.getUnittypes().getUnittypes()) {
        for (Group group : unittype.getGroups().getGroups()) {
          int unitCount = acsUnit.getUnitCount(group);
          DynamicStatement ds = new DynamicStatement();
          ds.addSql("INSERT INTO report_group VALUES(?,?,?,?,?)");
          ds.addArguments(now, pt.getTypeInt(), unittype.getName(), group.getName());
          ds.addArguments(unitCount);
          group.setCount(unitCount);
          ps = ds.makePreparedStatement(c);
          RecordGroup ru = new RecordGroup(now, pt, unittype.getName(), group.getName());
          try {
            ps.executeUpdate();
            unittype.getGroups().addOrChangeGroup(group, acs);
            logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was inserted");
            inserted++;
          } catch (SQLException sqle2) {
            ds = new DynamicStatement();
            ds.addSqlAndArguments("UPDATE report_group set unit_count = ? ", unitCount);
            ds.addSql(
                "WHERE timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND group_name = ? AND unit_count <> ?");
            ds.addArguments(now, pt.getTypeInt(), unittype.getName(), group.getName(), unitCount);
            ps = ds.makePreparedStatement(c);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
              unittype.getGroups().addOrChangeGroup(group, acs);
            }
            logger.debug(
                "ReportGenerator: ReportGenerator: - - The entry " + ru.getKey() + " was updated");
            updated++;
          }
        }
      }
      logger.info(
          "ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated");
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  protected void populateReportHWTable(DataSource cp, Report<RecordHardware> report)
      throws SQLException {
    populateReportHWTable(cp, report, Calendar.getInstance());
  }

  protected void populateReportHWTable(
      DataSource cp, Report<RecordHardware> report, Calendar calendar) throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    int skipped = 0;
    int inserted = 0;
    int updated = 0;
    try {
      connection = cp.getConnection();
      final Map<Key, RecordHardware> recordMap = report.getMap();
      for (RecordHardware record : recordMap.values()) {
        final PeriodType periodType = record.getPeriodType();
        if (skipPeriod(calendar, record.getTms(), periodType)) {
          skipped++;
          continue;
        }
        DynamicStatement ds = new DynamicStatement();
        Date keyTms = converter.convert(record.getTms(), record.getPeriodType());
        ds.addArguments(keyTms);
        ds.addArguments(periodType.getTypeInt());
        ds.addArguments(record.getUnittypeName());
        ds.addArguments(record.getProfileName());
        ds.addArguments(record.getSoftwareVersion());
        ds.addArguments(record.getBootCount().get());
        ds.addArguments(record.getBootWatchdogCount().get());
        ds.addArguments(record.getBootMiscCount().get());
        ds.addArguments(record.getBootPowerCount().get());
        ds.addArguments(record.getBootResetCount().get());
        ds.addArguments(record.getBootProvCount().get());
        ds.addArguments(record.getBootProvSwCount().get());
        ds.addArguments(record.getBootProvConfCount().get());
        ds.addArguments(record.getBootProvBootCount().get());
        ds.addArguments(record.getBootUserCount().get());
        ds.addArguments(record.getMemoryHeapDdrPoolAvg().get());
        ds.addArguments(record.getMemoryHeapDdrCurrentAvg().get());
        ds.addArguments(record.getMemoryHeapDdrLowAvg().get());
        ds.addArguments(record.getMemoryHeapOcmPoolAvg().get());
        ds.addArguments(record.getMemoryHeapOcmCurrentAvg().get());
        ds.addArguments(record.getMemoryHeapOcmLowAvg().get());
        ds.addArguments(record.getMemoryNpDdrPoolAvg().get());
        ds.addArguments(record.getMemoryNpDdrCurrentAvg().get());
        ds.addArguments(record.getMemoryNpDdrLowAvg().get());
        ds.addArguments(record.getMemoryNpOcmPoolAvg().get());
        ds.addArguments(record.getMemoryNpOcmCurrentAvg().get());
        ds.addArguments(record.getMemoryNpOcmLowAvg().get());
        ds.addArguments(record.getCpeUptimeAvg().get());
        ds.addSql("insert into report_hw VALUES(" + ds.getQuestionMarks() + ")");
        ps = ds.makePreparedStatement(connection);
        try {
          ps.executeUpdate();
          logger.debug(
              "ReportGenerator: ReportGenerator: - - The entry "
                  + record.getKey()
                  + " was inserted");
          inserted++;
        } catch (SQLException sqlex2) {
          logger.error(
              "ReportGenerator: ReportGenerator: - - The entry "
                  + record.getKey()
                  + " was not updated. Update is not implented yet.",
              sqlex2);
        }
      }
      if (!recordMap.isEmpty()) {
        logger.info(
            "ReportGenerator: - "
                + inserted
                + " entries inserted, "
                + updated
                + " entries updated, "
                + skipped
                + " entries skipped (too new)");
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
  }

  private void populateReportJobTable(DataSource cp, Date now, PeriodType pt) throws SQLException {
    Connection c = null;
    PreparedStatement ps = null;
    int inserted = 0;
    int updated = 0;
    try {
      c = cp.getConnection();
      for (Unittype unittype : acs.getUnittypes().getUnittypes()) {
        for (Job job : unittype.getJobs().getJobs()) {
          int completed = job.getCompletedHadFailures() + job.getCompletedNoFailures();
          int confirmedFailed = job.getConfirmedFailed();
          int unconfirmedFailed = job.getUnconfirmedFailed();
          Group group = job.getGroup();
          RecordJob ru = new RecordJob(now, pt, unittype.getName(), job.getName(), group.getName());
          int groupSize = getGroupSize(c, ru);
          DynamicStatement ds2 = new DynamicStatement();
          ds2.addSql("INSERT INTO report_job VALUES(?,?,?,?,?,?,?,?,?)");
          ds2.addArguments(
              now, pt.getTypeInt(), unittype.getName(), job.getName(), group.getName());
          ds2.addArguments(groupSize, completed, confirmedFailed, unconfirmedFailed);
          ps = ds2.makePreparedStatement(c);
          try {
            ps.executeUpdate();
            logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was inserted");
            inserted++;
          } catch (SQLException sqle2) {
            ds2 = new DynamicStatement();
            ds2.addSql(
                "UPDATE report_job set group_name = ?, group_size = ?, completed = ?, confirmed_failed = ?, unconfirmed_failed = ? ");
            ds2.addArguments(
                group.getName(), groupSize, completed, confirmedFailed, unconfirmedFailed);
            ds2.addSql(
                "WHERE timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND job_name = ?");
            ds2.addArguments(now, pt.getTypeInt(), unittype.getName(), job.getName());
            ps = ds2.makePreparedStatement(c);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
              throw sqle2;
            } else {
              logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was updated");
              updated++;
            }
          }
        }
      }
      logger.info(
          "ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated");
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  private void populateReportSyslogTable(DataSource cp, Report<RecordSyslog> report)
      throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    Calendar calendar = Calendar.getInstance();
    int skipped = 0;
    int inserted = 0;
    int updated = 0;
    try {
      connection = cp.getConnection();
      Map<Key, RecordSyslog> recordMap = report.getMap();
      for (RecordSyslog record : recordMap.values()) {
        PeriodType periodType = record.getPeriodType();
        if (skipPeriod(calendar, record.getTms(), periodType)) {
          skipped++;
          continue;
        }
        DynamicStatement ds = new DynamicStatement();
        Date keyTms = converter.convert(record.getTms(), record.getPeriodType());
        ds.addArguments(keyTms);
        ds.addArguments(periodType.getTypeInt());
        ds.addArguments(record.getUnittypeName());
        ds.addArguments(record.getProfileName());
        ds.addArguments(record.getSeverity());
        ds.addArguments(Integer.valueOf(record.getEventId()));
        ds.addArguments(record.getFacility());
        ds.addArguments(record.getMessageCount().get());
        ds.addSql("insert into report_syslog VALUES(" + ds.getQuestionMarks() + ")");
        ps = ds.makePreparedStatement(connection);
        try {
          ps.executeUpdate();
          logger.debug("ReportGenerator: - - The entry " + record.getKey() + " was inserted");
          inserted++;
        } catch (SQLException sqlex2) {
          ds = new DynamicStatement();
          ds.addSql("UPDATE report_syslog set unit_count = ? ");
          ds.addSql(
              "WHERE timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND profile_name = ? AND severity = ? AND syslog_event_id = ? AND facility = ?");
          ds.addArguments(
              record.getMessageCount().get(),
              keyTms,
              periodType.getTypeInt(),
              record.getUnittypeName(),
              record.getProfileName(),
              record.getSeverity(),
              Integer.valueOf(record.getEventId()),
              record.getFacility());
          ps = ds.makePreparedStatement(connection);
          int rowsUpdated = ps.executeUpdate();
          if (rowsUpdated == 0) {
            throw sqlex2;
          } else {
            logger.debug("ReportGenerator: - - The entry " + record.getKey() + " was updated");
            updated++;
          }
        }
      }
      if (!recordMap.isEmpty()) {
        logger.info(
            "ReportGenerator: - "
                + inserted
                + " entries inserted, "
                + updated
                + " entries updated, "
                + skipped
                + " entries skipped (too new)");
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
  }

  private boolean skipPeriod(
      final Calendar calendar, final Date syslogTms, final PeriodType periodType) {
    final int nowMonth = calendar.get(Calendar.MONTH);
    final int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
    final int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
    final int nowMinute = calendar.get(Calendar.MINUTE);
    final long nowTms = calendar.getTimeInMillis();
    final long diff = nowTms - syslogTms.getTime();
    final boolean skipMonth =
        periodType == PeriodType.MONTH && diff < MONTH_MS && converter.month(syslogTms) == nowMonth;
    final boolean skipDay =
        periodType == PeriodType.DAY && diff < DAY_MS && converter.day(syslogTms) == nowDay;
    final boolean skipHour =
        periodType == PeriodType.HOUR && diff < HOUR_MS && converter.hour(syslogTms) == nowHour;
    final boolean skipMinute =
        periodType == PeriodType.MINUTE
            && diff < MINUTE_MS
            && converter.minute(syslogTms) == nowMinute;
    return skipMonth || skipDay || skipHour || skipMinute;
  }

  private void populateReportUnitTable(DataSource cp, Date now, PeriodType pt) throws SQLException {
    Connection c1 = null;
    PreparedStatement ps1 = null;
    Connection c2 = null;
    PreparedStatement ps2 = null;
    ResultSet rs = null;
    Statement s = null;
    int inserted = 0;
    int updated = 0;
    try {
      c1 = cp.getConnection();
      c2 = cp.getConnection();
      s = c1.createStatement();
      rs = s.executeQuery("SELECT unit_id, profile_id, unit_type_id FROM unit");
      Map<String, UnitSWLCT> unitMap = new HashMap<>();
      while (rs.next()) {
        Unittype unittype = acs.getUnittype(rs.getInt("unit_type_id"));
        Profile profile = unittype.getProfiles().getById(rs.getInt("profile_id"));
        Unit unit = new Unit(rs.getString("unit_id"), unittype, profile);
        unitMap.put(unit.getId(), new UnitSWLCT(unit));
      }
      rs.close();
      rs =
          s.executeQuery(
              "SELECT up.unit_id, up.value FROM unit_type_param utp, unit_param up "
                  + "WHERE up.unit_type_param_id = utp.unit_type_param_id "
                  + "AND utp.name LIKE '%Device.DeviceInfo.SoftwareVersion'");
      while (rs.next()) {
        UnitSWLCT u = unitMap.get(rs.getString("unit_id"));
        if (u != null) {
          u.setSoftwareVersion(rs.getString("value"));
        }
      }
      rs.close();

      rs =
          s.executeQuery(
              "SELECT up.unit_id, timestampdiff(DAY, up.value, sysdate()) FROM unit_type_param utp, unit_param up "
                  + "WHERE up.unit_type_param_id = utp.unit_type_param_id "
                  + "AND utp.name LIKE 'System.X_FREEACS-COM.LastConnectTms'");
      while (rs.next()) {
        UnitSWLCT u = unitMap.get(rs.getString("unit_id"));
        if (u != null) {
          u.setLastConnectTms(rs.getInt(2));
        }
      }

      Map<String, Integer> unitReport = new HashMap<>();
      for (UnitSWLCT unitSWLCT : unitMap.values()) {
        String swVersion = unitSWLCT.getSoftwareVersion();
        if (swVersion == null || "".equals(swVersion.trim())) {
          swVersion = "Unknown";
        }
        String status = "Inactive";
        if (unitSWLCT.getLastConnectTms() != null) {
          if (unitSWLCT.getLastConnectTms() < 2) {
            status = "Active last 48h";
          }
          if (unitSWLCT.getLastConnectTms() >= 2 && unitSWLCT.getLastConnectTms() <= 7) {
            status = "Active 2-7 days ago";
          }
          if (unitSWLCT.getLastConnectTms() > 7) {
            status = "Active 8 or more days ago";
          }
        }
        String key =
            unitSWLCT.getUnit().getUnittype().getName()
                + "###"
                + unitSWLCT.getUnit().getProfile().getName()
                + "###"
                + swVersion
                + "###"
                + status;
        unitReport.merge(key, 1, (a, b) -> a + b);
      }

      for (Entry<String, Integer> entry : unitReport.entrySet()) {
        String[] keys = entry.getKey().split("###");
        DynamicStatement ds2 = new DynamicStatement();
        ds2.addSql(
            "INSERT INTO report_unit (timestamp_, period_type, unit_type_name, profile_name, software_version, status, unit_count) VALUES(?,?,?,?,?,?,?)");
        ds2.addArguments(
            now, pt.getTypeInt(), keys[0], keys[1], keys[2], keys[3], entry.getValue());
        ps2 = ds2.makePreparedStatement(c2);
        RecordUnit ru = new RecordUnit(now, pt, keys[0], keys[1], keys[2], keys[3]);
        try {
          ps2.executeUpdate();
          logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was inserted");
          inserted++;
        } catch (SQLException sqle2) {
          ds2 = new DynamicStatement();
          ds2.addSql("UPDATE report_unit set unit_count = ? ");
          ds2.addSql(
              "WHERE timestamp_ = ? AND period_type = ? AND unit_type_name = ? AND profile_name = ? AND software_version = ? AND status = ? ");
          ds2.addArguments(
              entry.getValue(), now, pt.getTypeInt(), keys[0], keys[1], keys[2], keys[3]);
          ps2 = ds2.makePreparedStatement(c2);
          int rowsUpdated = ps2.executeUpdate();
          if (rowsUpdated == 0) {
            throw sqle2;
          } else {
            logger.debug("ReportGenerator: - - The entry " + ru.getKey() + " was updated");
            updated++;
          }
        }
      }
      logger.info(
          "ReportGenerator: - " + inserted + " entries inserted, " + updated + " entries updated");
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps1 != null) {
        ps1.close();
      }
      if (s != null) {
        s.close();
      }
      if (ps2 != null) {
        ps2.close();
      }
      if (c1 != null) {
        c1.close();
      }
      if (c2 != null) {
        c2.close();
      }
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
  }

  private void populateReportVoipTable(DataSource cp, Report<RecordVoip> report)
      throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    Calendar calendar = Calendar.getInstance();
    int skipped = 0;
    int inserted = 0;
    int updated = 0;
    try {
      connection = cp.getConnection();
      Map<Key, RecordVoip> recordMap = report.getMap();
      for (RecordVoip record : recordMap.values()) {
        PeriodType periodType = record.getPeriodType();
        if (skipPeriod(calendar, record.getTms(), periodType)) {
          skipped++;
          continue;
        }
        DynamicStatement ds = new DynamicStatement();
        Date keyTms = converter.convert(record.getTms(), record.getPeriodType());
        ds.addArguments(keyTms);
        ds.addArguments(periodType.getTypeInt());
        ds.addArguments(record.getUnittypeName());
        ds.addArguments(record.getProfileName());
        ds.addArguments(record.getSoftwareVersion());
        ds.addArguments(record.getLine());
        ds.addArguments(record.getMosAvg().get());
        ds.addArguments(record.getJitterAvg().get());
        ds.addArguments(record.getJitterMax().get());
        ds.addArguments(record.getPercentLossAvg().get());
        ds.addArguments(record.getCallLengthAvg().get());
        ds.addArguments(record.getCallLengthTotal().get());
        ds.addArguments(record.getIncomingCallCount().get());
        ds.addArguments(record.getOutgoingCallCount().get());
        ds.addArguments(record.getOutgoingCallFailedCount().get());
        ds.addArguments(record.getAbortedCallCount().get());
        ds.addArguments(record.getNoSipServiceTime().get());
        ds.addSql("insert into report_voip VALUES(" + ds.getQuestionMarks() + ")");
        ps = ds.makePreparedStatement(connection);
        try {
          logger.debug(
              "ReportGenerator: - - SQL to be run: " + ds.getSqlQuestionMarksSubstituted());
          ps.executeUpdate();
          logger.debug("ReportGenerator: - - The entry " + record.getKey() + " was inserted");
          inserted++;
        } catch (SQLException sqlex2) {
          logger.debug(
              "ReportGenerator: - - Insert failed, will try with updated instead (exception: "
                  + sqlex2
                  + ")");
          ds = new DynamicStatement();
          ds.addSql("update report_voip set mos_avg = ?, ");
          ds.addSql("jitter_avg = ?, jitter_max = ?, ");
          ds.addSql("percent_loss_avg = ?, call_length_avg = ?, ");
          ds.addSql("call_length_total = ?, incoming_call_count = ?, no_sip_service_time = ?, ");
          ds.addSql(
              "outgoing_call_count = ?, outgoing_call_failed_count = ?, aborted_call_count = ? ");
          ds.addSql(
              "where timestamp_ = ? and period_type = ? and unit_type_name = ? and profile_name = ? ");
          ds.addSql("and software_version = ? and line = ?");
          ds.addArguments(record.getMosAvg().get());
          ds.addArguments(record.getJitterAvg().get());
          ds.addArguments(record.getPercentLossAvg().get());
          ds.addArguments(record.getCallLengthAvg().get());
          ds.addArguments(record.getCallLengthTotal().get());
          ds.addArguments(record.getIncomingCallCount().get());
          ds.addArguments(record.getNoSipServiceTime().get());
          ds.addArguments(record.getOutgoingCallCount().get());
          ds.addArguments(record.getOutgoingCallFailedCount().get());
          ds.addArguments(record.getAbortedCallCount().get());
          ds.addArguments(keyTms);
          ds.addArguments(periodType.getTypeInt());
          ds.addArguments(record.getUnittypeName());
          ds.addArguments(record.getProfileName());
          ds.addArguments(record.getSoftwareVersion());
          ds.addArguments(record.getLine());
          ps = ds.makePreparedStatement(connection);
          int rowsUpdated = ps.executeUpdate();
          if (rowsUpdated == 0) {
            throw sqlex2;
          }
          logger.debug("ReportGenerator: - - The entry " + record.getKey() + " was updated");
          updated++;
        }
      }
      if (!recordMap.isEmpty()) {
        logger.info(
            "ReportGenerator: - "
                + inserted
                + " entries inserted, "
                + updated
                + " entries updated, "
                + skipped
                + " entries skipped (too new)");
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
  }

  private void populateReportProvTable(DataSource cp, Report<RecordProvisioning> report)
      throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    try {
      int skipped = 0;
      int inserted = 0;
      int updated = 0;
      connection = cp.getConnection();
      Map<Key, RecordProvisioning> recordMap = report.getMap();
      Calendar calendar = Calendar.getInstance();
      for (RecordProvisioning record : recordMap.values()) {
        PeriodType periodType = record.getPeriodType();
        if (skipPeriod(calendar, record.getTms(), periodType)) {
          skipped++;
          continue;
        }
        DynamicStatement ds = new DynamicStatement();
        Date keyTms = converter.convert(record.getTms(), record.getPeriodType());
        ds.addArguments(
            keyTms,
            periodType.getTypeInt(),
            record.getUnittypeName(),
            record.getProfileName(),
            record.getSoftwareVersion(),
            record.getOutput());
        ds.addArguments(
            record.getProvisioningOkCount().get(), record.getProvisioningRescheduledCount().get());
        ds.addArguments(
            record.getProvisioningErrorCount().get(),
            record.getProvisioningMissingCount().get(),
            record.getSessionLengthAvg().get());
        ds.addSql("insert into report_prov VALUES(" + ds.getQuestionMarks() + ")");
        ps = ds.makePreparedStatement(connection);
        try {
          logger.debug(
              "ReportGenerator: - - SQL to be run: " + ds.getSqlQuestionMarksSubstituted());
          ps.executeUpdate();
          logger.debug("ReportGenerator: - - The entry " + record.getKey() + " was inserted");
          inserted++;
        } catch (SQLException sqlex2) {
          logger.debug(
              "ReportGenerator: - - Insert failed, will try with updated instead (exception: "
                  + sqlex2
                  + ")");
          ds = new DynamicStatement();
          ds.addSql("update report_prov set ");
          ds.addSql(
              "ok_count = ?, rescheduled_count = ?, error_count =? , missing_count = ?, session_length_avg = ? ");
          ds.addArguments(
              record.getProvisioningOkCount().get(),
              record.getProvisioningRescheduledCount().get());
          ds.addArguments(
              record.getProvisioningErrorCount().get(),
              record.getProvisioningMissingCount().get(),
              record.getSessionLengthAvg().get());
          ds.addSql(
              "where timestamp_ = ? and period_type = ? and unit_type_name = ? and profile_name = ? ");
          ds.addSql(
              "and software_version = ? and line = ? and prov_output = ? and prov_status = ?");
          ds.addArguments(
              keyTms,
              periodType.getTypeInt(),
              record.getUnittypeName(),
              record.getProfileName(),
              record.getSoftwareVersion(),
              record.getOutput());
          ps = ds.makePreparedStatement(connection);
          int rowsUpdated = ps.executeUpdate();
          if (rowsUpdated == 0) {
            throw sqlex2;
          }
          logger.debug("ReportGenerator: - - The entry " + record.getKey() + " was updated");
          updated++;
        }
      }
      if (!recordMap.isEmpty()) {
        logger.info(
            "ReportGenerator: - "
                + inserted
                + " entries inserted, "
                + updated
                + " entries updated, "
                + skipped
                + " entries skipped (too new)");
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
  }

  private void dailyJobs() throws SQLException, IOException, ParseException {
    logger.info("ReportGenerator: Daily report processing starts...");
    logger.info("ReportGenerator: Generate reports start");
    String reports = properties.getReports();
    if (reports == null) {
      reports = "Unit";
    } else if (!reports.contains("Unit")) {
      reports = "Unit, " + reports;
    }

    runReports(reports, PeriodType.DAY);
    logger.info("ReportGenerator: Daily report processing ends");
  }

  private void hourlyJobs() throws SQLException, IOException, ParseException {
    logger.info("ReportGenerator: Hourly report processing starts...");
    logger.info("ReportGenerator: Generate reports start");
    String reports = properties.getReports();
    if (reports == null) {
      reports = "Unit";
    } else if (!reports.contains("Unit")) {
      reports = "Unit, " + reports;
    }
    runReports(reports, PeriodType.HOUR);
    logger.info("ReportGenerator: Hourly report processing ends");
  }

  private void runReports(String reportProperty, PeriodType periodType)
      throws SQLException, IOException, ParseException {
    Date now = converter.convert(new Date(), periodType);
    int counter = 5;
    logger.info("ReportGenerator: Process reports start...");
    buildUnit(periodType, now);
    String skippingReports = "";

    if (reportProperty != null && reportProperty.contains("Basic")) {
      buildSyslog(periodType);
      buildGroup(periodType, now);
      buildJob(periodType, now);
      buildProvisioning(periodType);
    } else {
      skippingReports += "Syslog, Group, Job, Provisioning, ";
    }

    if (reportProperty != null && reportProperty.contains("Pingcom")) {
      counter += 2;
      buildHardwareSYS(periodType);
      buildVoipSYS(periodType);
    } else {
      skippingReports += "HardwareSYS, VoipSYS, ";
    }

    if (!"".equals(skippingReports)) {
      skippingReports = skippingReports.substring(0, skippingReports.length() - 2);
      logger.info("ReportGenerator: Skipping reports: " + skippingReports);
    }
    logger.info("ReportGenerator: Process reports end, " + counter + " reports are made");
  }

  private void buildUnit(PeriodType periodType, Date now) throws SQLException {
    logger.info(
        "ReportGenerator: - Generating and populating UnitReport ("
            + periodType.getTypeStr()
            + "-based)");
    populateReportUnitTable(getDataSource(), now, periodType);
  }

  private void buildProvisioning(PeriodType periodType) throws SQLException {
    ReportProvisioningGenerator rg =
        new ReportProvisioningGenerator(getDataSource(), acs, LOG_PREFIX, getIdentity());
    Date endTmsExc = new Date();
    Date startTmsInc = rg.startReportFromTms(periodType, "report_prov");
    logger.info(
        "ReportGenerator: - Generating ProvSYSReport ("
            + periodType.getTypeStr()
            + "-based) from "
            + startTmsInc
            + " to "
            + endTmsExc);
    Report<RecordProvisioning> report;
    if (periodType == PeriodType.DAY) {
      report = rg.generateFromReport(PeriodType.HOUR, startTmsInc, endTmsExc, null, null);
      report = ReportConverter.convertProvReport(report, PeriodType.DAY);
    } else {
      report = rg.generateFromSyslog(periodType, startTmsInc, endTmsExc, null, null, null, null);
    }
    logger.info(
        "ReportGenerator: - Generated ProvSYSReport ("
            + periodType.getTypeStr()
            + "-based), "
            + report.getMap().size()
            + " entries");
    populateReportProvTable(getDataSource(), report);
    logger.info(
        "ReportGenerator: - Populated ProvSYSReport  (" + periodType.getTypeStr() + "-based)");
  }

  private void buildSyslog(PeriodType periodType) throws SQLException, ParseException {
    ReportSyslogGenerator rg =
        new ReportSyslogGenerator(getDataSource(), acs, LOG_PREFIX, getIdentity());
    Date endTmsExc = new Date();
    Date startTmsInc = rg.startReportFromTms(periodType, "report_syslog");
    logger.info(
        "ReportGenerator: - Generating SyslogReport ("
            + periodType.getTypeStr()
            + "-based) from "
            + startTmsInc
            + " to "
            + endTmsExc);
    Report<RecordSyslog> report;
    if (periodType == PeriodType.DAY) {
      report = rg.generateFromReport(PeriodType.HOUR, startTmsInc, endTmsExc, null, null);
      report = ReportConverter.convertSyslogReport(report, PeriodType.DAY);
    } else {
      report = rg.generateFromSyslog(periodType, startTmsInc, endTmsExc, null, null, null, null);
    }
    logger.info(
        "ReportGenerator: - Generated SyslogReport ("
            + periodType.getTypeStr()
            + "-based), "
            + report.getMap().size()
            + " entries");
    populateReportSyslogTable(getDataSource(), report);
    logger.info(
        "ReportGenerator: - Populated SyslogReport  (" + periodType.getTypeStr() + "-based)");
  }

  private void buildHardwareSYS(PeriodType periodType) throws SQLException {
    ReportHardwareGenerator rg =
        new ReportHardwareGenerator(getDataSource(), acs, LOG_PREFIX, getIdentity());
    Date endTmsExc = new Date();
    Date startTmsInc = rg.startReportFromTms(periodType, "report_hw");
    logger.info(
        "ReportGenerator: - Generating HardwareSYSReport ("
            + periodType.getTypeStr()
            + "-based) from "
            + startTmsInc
            + " to "
            + endTmsExc);
    Report<RecordHardware> report;
    if (periodType == PeriodType.DAY) {
      report = rg.generateFromReport(PeriodType.HOUR, startTmsInc, endTmsExc, null, null);
      report = ReportConverter.convertHardwareReport(report, PeriodType.DAY);
    } else {
      report = rg.generateFromSyslog(periodType, startTmsInc, endTmsExc, null, null, null, null);
    }
    logger.info(
        "ReportGenerator: - Generated HardwareSYSReport ("
            + periodType.getTypeStr()
            + "-based), "
            + report.getMap().size()
            + " entries");
    populateReportHWTable(getDataSource(), report);
    logger.info(
        "ReportGenerator: - Populated HardwareSYSReport  (" + periodType.getTypeStr() + "-based)");
  }

  private void buildGroup(PeriodType periodType, Date now) throws SQLException {
    logger.info(
        "ReportGenerator: - Generating and populating GroupReport ("
            + periodType.getTypeStr()
            + "-based)");
    populateReportGroupTable(getDataSource(), now, periodType);
  }

  private void buildJob(PeriodType periodType, Date now) throws SQLException {
    logger.info(
        "ReportGenerator: - Generating and populating JobReport ("
            + periodType.getTypeStr()
            + "-based)");
    populateReportJobTable(getDataSource(), now, periodType);
  }

  private void buildVoipSYS(PeriodType periodType) throws SQLException {
    ReportVoipGenerator rg =
        new ReportVoipGenerator(getDataSource(), acs, LOG_PREFIX, getIdentity());
    Date endTmsExc = new Date();
    Date startTmsInc = rg.startReportFromTms(periodType, "report_voip");
    logger.info(
        "ReportGenerator: - Generating VoipSYSReport ("
            + periodType.getTypeStr()
            + "-based) from "
            + startTmsInc
            + " to "
            + endTmsExc);
    Report<RecordVoip> report;
    if (periodType == PeriodType.DAY) {
      report = rg.generateFromReport(PeriodType.HOUR, startTmsInc, endTmsExc, null, null);
      report = ReportConverter.convertVoipReport(report, PeriodType.DAY);
    } else {
      report = rg.generateFromSyslog(periodType, startTmsInc, endTmsExc, null, null, null, null);
    }
    logger.info(
        "ReportGenerator: - Generated VoipSYSReport ("
            + periodType.getTypeStr()
            + "-based), "
            + report.getMap().size()
            + " entries");
    populateReportVoipTable(getDataSource(), report);
    logger.info(
        "ReportGenerator: - Populated VoipSYSReport  (" + periodType.getTypeStr() + "-based)");
  }
}
