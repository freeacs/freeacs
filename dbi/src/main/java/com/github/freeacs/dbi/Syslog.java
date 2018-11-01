package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.SQLUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all write/read to the syslog-table. Now it also supports the Syslog-server.
 * The only source of writing to the syslog table not going through here is from the OPP-server.
 *
 * @author Morten
 */
public class Syslog {

  private DataSource dataSource;

  private Identity id;

  private static Logger logger = LoggerFactory.getLogger(Syslog.class);

  /** Only necessary in simulation-mode fields: */
  private boolean simulationMode;

  private static SimpleDateFormat deviceTmsFormat =
      new SimpleDateFormat("yyyy MMM dd HH:mm:ss", new Locale("EN"));
  private static Calendar yearCal = Calendar.getInstance();

  /**
   * @param dataSource
   * @param id The {@link Identity} for this syslog instance
   */
  public Syslog(DataSource dataSource, Identity id) {
    this.dataSource = dataSource;
    this.id = id;
  }

  public Identity getIdentity() {
    return id;
  }

  private boolean allUnittypesSpecified(
      SyslogFilter filter, Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified) {
    Set<Integer> unittypesWithAllProfilesSpecified = new HashSet<>();
    boolean allUnittypesSpecified = false;
    List<Profile> profiles = filter.getProfiles();
    ACS acs = profiles.get(0).getUnittype().getAcs();
    int noUnittypes = acs.getUnittypes().getUnittypes().length;
    doStuff(unittypesWithSomeProfilesSpecified, unittypesWithAllProfilesSpecified, profiles);
    if (noUnittypes == unittypesWithAllProfilesSpecified.size()) {
      allUnittypesSpecified = true;
    }
    return allUnittypesSpecified;
  }

  public static void doStuff(
      Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified,
      Set<Integer> unittypesWithAllProfilesSpecified,
      List<Profile> profiles) {
    for (Profile profile : profiles) {
      Integer unittypeId = profile.getUnittype().getId();
      Set<Profile> profilesInUnittype = unittypesWithSomeProfilesSpecified.get(unittypeId);
      if (profilesInUnittype == null) {
        profilesInUnittype = new HashSet<>();
      }
      profilesInUnittype.add(profile);
      if (unittypesWithAllProfilesSpecified.contains(unittypeId)) {
        continue;
      }
      unittypesWithSomeProfilesSpecified.put(unittypeId, profilesInUnittype);
      int noProfiles = profile.getUnittype().getProfiles().getProfiles().length;
      // populate and delete (logically: move from "someSpecified" to "allSpecified")
      if (unittypesWithSomeProfilesSpecified.get(unittypeId).size() == noProfiles) {
        unittypesWithAllProfilesSpecified.add(unittypeId);
        unittypesWithSomeProfilesSpecified.remove(unittypeId);
      }
    }
  }

  private DynamicStatement addUnittypeOrProfileCriteria(DynamicStatement ds, SyslogFilter filter) {
    User user = id.getUser();
    if (filter.getProfiles() != null && !filter.getProfiles().isEmpty()) {
      Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified = new HashMap<>();
      boolean allUnittypesSpecified =
          allUnittypesSpecified(filter, unittypesWithSomeProfilesSpecified);
      if (user.isAdmin() && allUnittypesSpecified) {
        return ds;
      }
      ds.addSql("(");
      for (int i = 0; i < filter.getProfiles().size(); i++) {
        Profile profile = filter.getProfiles().get(i);
        boolean allProfilesSpecified =
            unittypesWithSomeProfilesSpecified.get(profile.getUnittype().getId()) == null;
        // all profiles in unittype are specified, we can skip profiles criteria
        if (allProfilesSpecified && user.isUnittypeAdmin(profile.getUnittype().getId())) {
          boolean alreadyTreated = false;
          for (int j = 0; j < i; j++) {
            Profile p = filter.getProfiles().get(j);
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
    } else if (filter.getUnittypes() != null && !filter.getUnittypes().isEmpty()) {
      ACS acs = filter.getUnittypes().get(0).getAcs();
      int noUnittypes = acs.getUnittypes().getUnittypes().length;
      boolean isAdmin = user.isAdmin();
      if (noUnittypes > filter.getUnittypes().size() || !isAdmin) {
        ds.addSql("(");
        for (Unittype unittype : filter.getUnittypes()) {
          ds.addSqlAndArguments("unit_type_name = ? OR ", unittype.getName());
        }
        ds.cleanupSQLTail();
        ds.addSql(") AND ");
      } // no criteria added, all unittypes are specified and user isAdmin
    }
    return ds;
  }

  /**
   * Criteria will be changed according to these rules: contains | -> split criteria-string, use AND
   * or OR depending on negation (see next test) startswith ! -> negation of search, ! is removed
   * before next test contains * -> replaced by % startswith ^ -> no leading % and ^ is removed
   * endswith $ -> no trailing % and $ is removed
   *
   * @param criteriaName
   * @param ds
   * @param criteria
   * @return
   */
  private void addCriteria(String criteriaName, DynamicStatement ds, String criteria) {
    int dsLength = ds.getSql().length();
    SQLUtil.input2SQLCriteria(ds, criteriaName, criteria);
    if (ds.getSql().length() > dsLength) {
      ds.addSql(" AND ");
    }
  }

  private DynamicStatement addSeverityCriteria(DynamicStatement ds, Integer[] severities) {
    if (severities != null && severities.length > 0) {
      ds.addSql("(");
      for (Integer severity : severities) {
        ds.addSqlAndArguments("severity = ? OR ", severity);
      }
      ds.cleanupSQLTail();
      ds.addSql(") AND ");
    }
    return ds;
  }

  private PreparedStatement makeReadSQL(Connection c, DynamicStatement ds, SyslogFilter filter)
      throws SQLException {
    ds.addSql("SELECT * FROM syslog WHERE ");
    addCriteria("content", ds, filter.getMessage());
    addCriteria("facility_version", ds, filter.getFacilityVersion());
    addCriteria("user_id", ds, filter.getUserId());
    addCriteria("ipaddress", ds, filter.getIpAddress());
    addCriteria("unit_id", ds, filter.getUnitId());
    ds = addUnittypeOrProfileCriteria(ds, filter);
    ds = addSeverityCriteria(ds, filter.getSeverity());
    if (filter.getEventId() != null) {
      ds.addSqlAndArguments("syslog_event_id = ? AND ", filter.getEventId());
    }
    if (filter.getFacility() != null) {
      ds.addSqlAndArguments("facility = ? AND ", filter.getFacility());
    }
    if (filter.getCollectorTmsStart() != null) {
      ds.addSqlAndArguments("collector_timestamp >= ? AND ", filter.getCollectorTmsStart());
    }
    if (filter.getCollectorTmsEnd() != null) {
      ds.addSqlAndArguments("collector_timestamp < ? AND ", filter.getCollectorTmsEnd());
    }
    if (filter.getFacility() != null) {
      ds.addSqlAndArguments("facility = ? AND ", filter.getFacility());
    }
    if (filter.getFlags() != null) {
      ds.addSqlAndArguments("flags = ? AND ", filter.getFlags());
    }
    ds.cleanupSQLTail();
    ds.addSql("ORDER BY collector_timestamp DESC ");
    if (filter.getMaxRows() != null && filter.getMaxRows() < Integer.MAX_VALUE) {
      String sql = ds.getSql();
      sql += " LIMIT " + filter.getMaxRows();
      ds.setSql(sql);
    }
    return ds.makePreparedStatement(c);
  }

  private String getContent(String content) {
    if (content.length() > 1024) {
      return content.substring(0, 1020) + "....";
    }
    if (content.contains("'")) {
      return content.replace("'", "\\'");
    }
    return content;
  }

  private List<SyslogEntry> makeSyslogEntries(ResultSet rs) throws SQLException {
    List<SyslogEntry> result = new ArrayList<>();
    while (rs.next()) {
      SyslogEntry se = new SyslogEntry();
      se.setId(rs.getInt("syslog_id"));
      se.setCollectorTimestamp(rs.getTimestamp("collector_timestamp"));
      se.setEventId(rs.getInt("syslog_event_id"));
      se.setFacility(rs.getInt("facility"));
      se.setFacilityVersion(rs.getString("facility_version"));
      se.setSeverity(rs.getInt("severity"));
      se.setDeviceTimestamp(rs.getString("device_timestamp"));
      se.setHostname(rs.getString("hostname"));
      se.setTag(rs.getString("tag"));
      se.setContent(rs.getString("content"));
      se.setFlags(rs.getString("flags"));
      se.setIpAddress(rs.getString("ipaddress"));
      se.setUnitId(rs.getString("unit_id"));
      se.setProfileName(rs.getString("profile_name"));
      se.setUnittypeName(rs.getString("unit_type_name"));
      se.setUserId(rs.getString("user_id"));
      result.add(se);
    }
    return result;
  }

  public void updateContent(long tms, String oldContent, String newContent, String unitId)
      throws SQLException {
    Connection c = getDataSource().getConnection();
    PreparedStatement pp = null;
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("UPDATE syslog SET content = ? WHERE ", newContent);
      ds.addSqlAndArguments("collector_timestamp >= ? AND ", new Timestamp(tms));
      ds.addSqlAndArguments("collector_timestamp <= ? AND ", new Timestamp(tms + 10000));
      ds.addSqlAndArguments("content = ? AND ", oldContent);
      if (unitId != null) {
        ds.addSqlAndArguments("unit_id = ?", unitId);
      } else {
        ds.cleanupSQLTail();
      }
      pp = ds.makePreparedStatement(c);
      pp.executeUpdate();
      logger.debug(ds.getDebugMessage());
    } finally {
      if (pp != null) {
        pp.close();
      }
      c.close();
    }
  }

  /** Delete entries with a specific event, unless it has some a severity level. */
  public int deleteOldEventsEntries(Calendar fromCal, Calendar toCal, SyslogEvent event, int limit)
      throws SQLException {
    Connection c = getDataSource().getConnection();
    PreparedStatement pp = null;
    DynamicStatement ds = new DynamicStatement();
    try {
      ds.addSql("DELETE FROM syslog WHERE ");
      ds.addSqlAndArguments("syslog_event_id = ? and ", event.getId());
      if (fromCal != null) {
        ds.addSqlAndArguments("collector_timestamp >= ? and ", fromCal);
      }
      ds.addSqlAndArguments("collector_timestamp < ? ", toCal);
      ds.addSql(" LIMIT " + limit);
      pp = ds.makePreparedStatement(c);

      logger.debug("The SQL which will be executed: " + ds.getSqlQuestionMarksSubstituted());
      return pp.executeUpdate();
    } catch (SQLException sqle) {
      // This happens if MYSQL database is busy, perhaps running a backup
      if (sqle.getMessage().contains("Lock wait timeout exceeded")) {
        logger.warn(
            "The database is busy (perhaps running a backup?), we'll wait 10 minutes and try again. SQLError: "
                + sqle.getErrorCode()
                + ", "
                + sqle.getMessage());
        try {
          Thread.sleep(1000 * 600);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        // relatively dirty programming - can cause stack overflow. However I
        // think
        // the loop is lazy enough to avoid that situation (can happen if the
        // database is busy in days...)
        return deleteOldEventsEntries(fromCal, toCal, event, limit);
      } else {
        throw sqle;
      }
    } finally {
      if (pp != null) {
        pp.close();
      }
      c.close();
    }
  }

  /** Delete entries with a specific severity, unless it has a specified event id. */
  public int deleteOldSeverityEntries(
      Calendar fromCal, Calendar toCal, int severity, List<SyslogEvent> events, int limit)
      throws SQLException {
    Connection c = getDataSource().getConnection();
    PreparedStatement pp = null;
    DynamicStatement ds = new DynamicStatement();
    try {
      ds.addSql("DELETE FROM syslog WHERE ");
      ds.addSqlAndArguments("severity = ? and ", severity);
      for (SyslogEvent event : events) {
        ds.addSqlAndArguments("syslog_event_id <> ? and ", event.getId());
      }
      if (fromCal != null) {
        ds.addSqlAndArguments("collector_timestamp >= ? and ", fromCal);
      }
      ds.addSqlAndArguments("collector_timestamp < ? ", toCal);
      ds.addSql(" LIMIT " + limit);
      pp = ds.makePreparedStatement(c);

      logger.debug("The SQL which will be executed: " + ds.getSqlQuestionMarksSubstituted());
      return pp.executeUpdate();
    } catch (SQLException sqle) {
      // This happens if MYSQL database is busy, perhaps running a backup
      if (sqle.getMessage().contains("Lock wait timeout exceeded")) {
        logger.warn(
            "The database is busy (perhaps running a backup?), we'll wait 10 minutes and try again. SQLError: "
                + sqle.getErrorCode()
                + ", "
                + sqle.getMessage());
        try {
          Thread.sleep(1000 * 600);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        // relatively dirty programming - can cause stack overflow. However I
        // think
        // the loop is lazy enough to avoid that situation.
        return deleteOldSeverityEntries(fromCal, toCal, severity, events, limit);
      } else {
        throw sqle;
      }
    } finally {
      if (pp != null) {
        pp.close();
      }
      c.close();
    }
  }

  public List<SyslogEntry> read(SyslogFilter filter, ACS acs) throws SQLException {
    Connection c = getDataSource().getConnection();
    PreparedStatement pp = null;
    ResultSet rs = null;
    try {
      DynamicStatement ds = new DynamicStatement();
      pp = makeReadSQL(c, ds, filter);
      long start = System.currentTimeMillis();
      rs = pp.executeQuery();
      if (logger.isDebugEnabled()) {
        float milli = System.currentTimeMillis() - start;
        if (logger.isDebugEnabled()) {
          String msg = "Syslog query: " + ds.getSqlQuestionMarksSubstituted();
          logger.debug("[" + String.format("%10.2f", milli) + " ms] " + msg);
        }
      }
      List<SyslogEntry> syslogEntryList = makeSyslogEntries(rs);
      syslogEntryList.sort(new SyslogEntryComparator());
      return syslogEntryList;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (pp != null) {
        pp.close();
      }
      c.close();
    }
  }

  private Date getTmsArg(SyslogEntry entry) {
    if (simulationMode || (entry.getFacility() != null && entry.getFacility() >= 50)) {
      if (entry.getDeviceTimestamp() != null
          && !"Jan 1 00:00:00".equals(entry.getDeviceTimestamp().trim())) {
        try {
          Date deviceTms =
              deviceTmsFormat.parse(yearCal.get(Calendar.YEAR) + " " + entry.getDeviceTimestamp());
          // Around midnight on new year's eve, one might have date-tms combined
          // with the wrong year, since we set the year on server side. To avoid
          // this, we must check if the device-tms is too far off from the now-tms.
          // If it is, we adjust the yearCal and force deviceTms to now-tms
          if (Math.abs(System.currentTimeMillis() - deviceTms.getTime()) > 3600 * 1000 * 24 * 30L) {
            deviceTms = new Date();
            yearCal.setTime(deviceTms);
          }
          return deviceTms;
        } catch (ParseException e) {
          logger.error(
              "Device timestamp ("
                  + entry.getDeviceTimestamp()
                  + ") could not be parsed into a date, using NOW tms instead",
              e);
          return new Date();
        }
      }
      return new Date();
    } else if (entry.getCollectorTimestamp() != null) {
      return entry.getCollectorTimestamp();
    } else {
      return new Date();
    }
  }

  /** Returns syslog id. */
  public void write(SyslogEntry entry) throws SQLException {
    try (Connection c = getDataSource().getConnection();
        PreparedStatement s =
            c.prepareStatement(
                "INSERT INTO syslog ("
                    + "collector_timestamp, syslog_event_id, facility, facility_version, severity, "
                    + "device_timestamp, hostname, tag, unit_id, profile_name, unit_type_name, user_id, content, "
                    + "flags, ipaddress) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
      s.setTimestamp(1, new Timestamp(getTmsArg(entry).getTime()));
      s.setInt(2, Optional.ofNullable(entry.getEventId()).orElse(0));
      s.setInt(3, Optional.ofNullable(entry.getFacility()).orElse(id.getFacility()));
      s.setString(4, entry.getFacilityVersion());
      s.setInt(5, entry.getSeverity());
      s.setString(6, entry.getDeviceTimestamp());
      s.setString(7, entry.getHostname());
      s.setString(8, entry.getTag());
      s.setString(9, entry.getUnitId());
      s.setString(10, entry.getProfileName());
      s.setString(11, entry.getUnittypeName());
      s.setString(12, entry.getUserId());
      s.setString(13, Optional.ofNullable(entry.getContent()).map(this::getContent).orElse(null));
      s.setString(14, entry.getFlags());
      s.setString(15, entry.getIpAddress());
      s.executeUpdate();
    }
  }

  public void setIdentity(Identity id) {
    this.id = id;
  }

  public void setSimulationMode(boolean simulationMode) {
    this.simulationMode = simulationMode;
  }

  public DataSource getDataSource() {
    return dataSource;
  }
}
