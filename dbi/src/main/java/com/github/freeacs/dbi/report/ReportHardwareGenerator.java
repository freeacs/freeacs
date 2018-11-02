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

public class ReportHardwareGenerator extends ReportGenerator {
  private static Logger logger = LoggerFactory.getLogger(ReportHardwareGenerator.class);
  /**
   * Memory-content: HW Memory Pool/Current/LowWater: Heap(DDR) 10863616/10558784/10558144,
   * Heap(OCM) 49152/34876/34876, NP(DDR) 4471/4094/4089, NP(OCM) 375/246/244
   */
  private static Pattern memPattern =
      Pattern.compile(
          "[^\\d]*(\\d+)\\/(\\d+)\\/(\\d+)[^\\d]*(\\d*)\\/(\\d+)\\/(\\d+)[^\\d]*(\\d*)\\/(\\d+)\\/(\\d+)[^\\d]*(\\d*)\\/(\\d+)\\/(\\d+).*");
  /** Uptime: [memPattern], Uptime 170:25:40 */
  private static Pattern uptimePattern = Pattern.compile("Uptime (\\d+):(\\d+):(\\d+)");
  /** Reboot-content: Reboot reason [0x0002] */
  private static Pattern rebootPattern = Pattern.compile(".*Reboot reason \\[.+\\](.+)");

  public ReportHardwareGenerator(
      DataSource mainDataSource, ACS acs, String logPrefix, Identity id) {
    super(mainDataSource, acs, logPrefix, id);
  }

  public Report<RecordHardware> generateFromReport(
      PeriodType periodType, Date start, Date end, List<Unittype> uts, List<Profile> prs)
      throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      boolean foundDataInReportTable = false;
      Report<RecordHardware> report = new Report<>(RecordHardware.class, periodType);

      logger.debug(
          logPrefix + "HardwareReport: Reads from report_hw table from " + start + " to " + end);
      connection = mainDataSource.getConnection();
      DynamicStatement ds = selectReportSQL("report_hw", periodType, start, end, uts, prs);
      ps = ds.makePreparedStatement(connection);
      rs = ps.executeQuery();
      int counter = 0;
      while (rs.next()) {
        counter++;
        start = rs.getTimestamp("timestamp_");
        String unittypeName = rs.getString("unit_type_name");
        String profileName = rs.getString("profile_name");
        String softwareVersion = rs.getString("software_version");
        RecordHardware recordTmp =
            new RecordHardware(start, periodType, unittypeName, profileName, softwareVersion);
        Key key = recordTmp.getKey();
        RecordHardware record = report.getRecord(key);
        if (record == null) {
          record = recordTmp;
        }
        record.getBootCount().add(rs.getInt("boot_count"));
        record.getBootMiscCount().add(rs.getInt("boot_misc_count"));
        record.getBootPowerCount().add(rs.getInt("boot_power_count"));
        record.getBootProvBootCount().add(rs.getInt("boot_prov_boot_count"));
        record.getBootProvConfCount().add(rs.getInt("boot_prov_conf_count"));
        record.getBootProvCount().add(rs.getInt("boot_prov_count"));
        record.getBootProvSwCount().add(rs.getInt("boot_prov_sw_count"));
        record.getBootResetCount().add(rs.getInt("boot_reset_count"));
        record.getBootUserCount().add(rs.getInt("boot_user_count"));
        record.getBootWatchdogCount().add(rs.getInt("boot_watchdog_count"));
        record.getMemoryHeapDdrPoolAvg().add(rs.getInt("mem_heap_ddr_pool_avg"));
        record.getMemoryHeapDdrCurrentAvg().add(rs.getInt("mem_heap_ddr_current_avg"));
        record.getMemoryHeapDdrLowAvg().add(rs.getInt("mem_heap_ddr_low_avg"));
        record.getMemoryHeapOcmPoolAvg().add(rs.getInt("mem_heap_ocm_pool_avg"));
        record.getMemoryHeapOcmCurrentAvg().add(rs.getInt("mem_heap_ocm_current_avg"));
        record.getMemoryHeapOcmLowAvg().add(rs.getInt("mem_heap_ocm_low_avg"));
        record.getMemoryNpDdrPoolAvg().add(rs.getInt("mem_np_ddr_pool_avg"));
        record.getMemoryNpDdrCurrentAvg().add(rs.getInt("mem_np_ddr_current_avg"));
        record.getMemoryNpDdrLowAvg().add(rs.getInt("mem_np_ddr_low_avg"));
        record.getMemoryNpOcmPoolAvg().add(rs.getInt("mem_np_ocm_pool_avg"));
        record.getMemoryNpOcmCurrentAvg().add(rs.getInt("mem_np_ocm_current_avg"));
        record.getMemoryNpOcmLowAvg().add(rs.getInt("mem_np_ocm_low_avg"));
        record.getCpeUptimeAvg().add(rs.getInt("cpe_uptime_avg"));
        report.setRecord(key, record);
        foundDataInReportTable = true;
      }
      if (foundDataInReportTable) {
        logger.debug(
            logPrefix
                + "HardwareReport: Have read "
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

  public Report<RecordHardware> generateFromSyslog(Date start, Date end, String unitId)
      throws SQLException {
    return generateFromSyslog(PeriodType.SECOND, start, end, null, null, unitId, null);
  }

  public Map<String, Report<RecordHardware>> generateFromSyslog(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      Group group)
      throws SQLException {
    logInfo("HardwareReport", null, uts, prs, start, end);
    Syslog syslog = new Syslog(mainDataSource, id);
    SyslogFilter filter = new SyslogFilter();
    filter.setFacility(16); // Only messages from device
    filter.setMessage("^Reboot reason|^HW Memory");
    filter.setProfiles(prs);
    filter.setUnittypes(uts);
    filter.setCollectorTmsStart(start);
    filter.setCollectorTmsEnd(end);
    filter.setFacilityVersion(swVersion);
    Map<String, Unit> unitsInGroup = getUnitsInGroup(group);
    List<SyslogEntry> entries = syslog.read(filter, acs);
    Map<String, Report<RecordHardware>> unitReportMap = new HashMap<>();
    for (SyslogEntry entry : entries) {
      String unitId = entry.getUnitId();
      if (group != null && unitsInGroup.get(entry.getUnitId()) == null) {
        continue;
      }
      if (unitId == null || "".equals(unitId.trim())) {
        unitId = "Unknown";
      }
      String unittypeName = entry.getUnittypeName();
      if (unittypeName == null || "".equals(unittypeName.trim())) {
        unittypeName = "Unknown";
      }
      String profileName = entry.getProfileName();
      if (profileName == null || "".equals(profileName.trim())) {
        profileName = "Unknown";
      }
      Report<RecordHardware> report = unitReportMap.get(unitId);
      if (report == null) {
        report = new Report<>(RecordHardware.class, periodType);
        unitReportMap.put(unitId, report);
      }
      if (entry.getFacilityVersion() == null || "".equals(entry.getFacilityVersion().trim())) {
        entry.setFacilityVersion("Unknown");
      }
      RecordHardware recordTmp =
          new RecordHardware(
              entry.getCollectorTimestamp(),
              periodType,
              unittypeName,
              profileName,
              entry.getFacilityVersion());
      Key key = recordTmp.getKey();
      RecordHardware record = report.getRecord(key);
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
            + "HardwareReport: Have read "
            + entries.size()
            + " rows from syslog, "
            + unitReportMap.size()
            + " units are mapped");
    return unitReportMap;
  }

  public Report<RecordHardware> generateFromSyslog(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> uts,
      List<Profile> prs,
      String unitId,
      Group group)
      throws SQLException {
    Report<RecordHardware> report = new Report<>(RecordHardware.class, periodType);
    logInfo("HardwareReport", unitId, uts, prs, start, end);
    Syslog syslog = new Syslog(mainDataSource, id);
    SyslogFilter filter = new SyslogFilter();
    filter.setFacility(16); // Only messages from device
    filter.setMessage("^Reboot reason|^HW Memory");
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
      if (group != null && unitsInGroup.get(entry.getUnitId()) == null) {
        continue;
      }
      String unittypeName = entry.getUnittypeName();
      if (unittypeName == null || "".equals(unittypeName.trim())) {
        unittypeName = "Unknown";
      }
      String profileName = entry.getProfileName();
      if (profileName == null || "".equals(profileName.trim())) {
        profileName = "Unknown";
      }
      if (entry.getFacilityVersion() == null || "".equals(entry.getFacilityVersion().trim())) {
        entry.setFacilityVersion("Unknown");
      }
      RecordHardware recordTmp =
          new RecordHardware(
              entry.getCollectorTimestamp(),
              periodType,
              unittypeName,
              profileName,
              entry.getFacilityVersion());
      Key key = recordTmp.getKey();
      RecordHardware record = report.getRecord(key);
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
            + "HardwareReport: Have read "
            + entries.size()
            + " rows from syslog, report is now "
            + report.getMap().size()
            + " entries");
    return report;
  }

  private void parseContentAndPopulateRecord(RecordHardware record, String content)
      throws SyslogParseException {
    try {
      Matcher m = memPattern.matcher(content);
      if (m.matches()) {
        record.getMemoryHeapDdrPoolAvg().add(Integer.parseInt(m.group(1)));
        record.getMemoryHeapDdrCurrentAvg().add(Integer.parseInt(m.group(2)));
        record.getMemoryHeapDdrLowAvg().add(Integer.parseInt(m.group(3)));
        record.getMemoryHeapOcmPoolAvg().add(Integer.parseInt(m.group(4)));
        record.getMemoryHeapOcmCurrentAvg().add(Integer.parseInt(m.group(5)));
        record.getMemoryHeapOcmLowAvg().add(Integer.parseInt(m.group(6)));
        record.getMemoryNpDdrPoolAvg().add(Integer.parseInt(m.group(7)));
        record.getMemoryNpDdrCurrentAvg().add(Integer.parseInt(m.group(8)));
        record.getMemoryNpOcmPoolAvg().add(Integer.parseInt(m.group(10)));
        record.getMemoryNpDdrLowAvg().add(Integer.parseInt(m.group(9)));
        record.getMemoryNpOcmCurrentAvg().add(Integer.parseInt(m.group(11)));
        record.getMemoryNpOcmLowAvg().add(Integer.parseInt(m.group(12)));
        Matcher m2 = uptimePattern.matcher(content);
        if (m2.find()) {
          int hours = Integer.parseInt(m2.group(1));
          int minutes = Integer.parseInt(m2.group(2));
          int totaluptimemin = hours * 60 + minutes;
          record.getCpeUptimeAvg().add(totaluptimemin);
        }
      }
      m = rebootPattern.matcher(content);
      if (m.matches()) {
        record.getBootCount().inc();
        if ("Power-on reset".equalsIgnoreCase(m.group(1).trim())) {
          record.getBootPowerCount().inc();
        } else if ("Software reset".equalsIgnoreCase(m.group(1).trim())) {
          record.getBootProvCount().inc();
        } else {
          record.getBootMiscCount().inc();
        }
      }
    } catch (Throwable t) {
      throw new SyslogParseException();
    }
  }
}
