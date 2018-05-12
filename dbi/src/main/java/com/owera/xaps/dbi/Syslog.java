package com.owera.xaps.dbi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.util.SQLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all write/read to the syslog-table. Now it also supports
 * the Syslog-server. The only source of writing to the syslog table not going
 * through here is from the OPP-server.
 * 
 * @author Morten
 * 
 */
public class Syslog {

  private ConnectionProperties connectionProperties = null;

  private Identity id;

  private static Logger logger = LoggerFactory.getLogger(Syslog.class);

  // Used to indicate if a read returns syslog entries from a device. Reset on
  // start of every read()
  // private boolean facilityDevice = false;

  // Holds all connections used and last commit timestamp, needed to commit
  // every 5 seconds
  // private static Cache connectionCache = new Cache();
  // Holds a number of inserts for the last 5 seconds
  private StringBuilder insertValues = new StringBuilder(1000);
  private int insertCount = 0;
  private long insertTms = System.currentTimeMillis();
  private int maxInsertCount; // The max number of insert commands in the commit
                              // queue, default below
  private int minTmsDelay; // The least number of milliseconds to pass between
                           // commit, default below
  public static final int defaultMaxInsertCount = 1000;
  public static final int defaultMinTmsDelay = 5000;

  // Only necessary in simulation-mode fields:
  private boolean simulationMode = false;
  private static SimpleDateFormat deviceTmsFormat = new SimpleDateFormat("yyyy MMM dd HH:mm:ss", new Locale("EN"));
  private static SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static Calendar yearCal = Calendar.getInstance();

  // private static Map<Connection, Long> connectionMap = new
  // HashMap<Connection, Long>();

  // The unit may have a unit-parameter which overrides the loglevel
  // suppression. Requires loggingEnabled=true
  // private Unit unit;

  public Syslog(ConnectionProperties cp, Identity id) {
    this(cp, id, defaultMaxInsertCount, defaultMinTmsDelay); // Default values.
  }

  /**
   * 
   * @param cp
   *          The {@link ConnectionProperties} for this syslog instance
   * @param id
   *          The {@link Identity} for this syslog instance
   * @param maxInsertCount
   *          The max number of insert messages (SQL) in the commit buffer
   * @param minTmsDelay
   *          The min milliseconds between inserts into the DB
   */
  public Syslog(ConnectionProperties cp, Identity id, int maxInsertCount, int minTmsDelay) {
    this.connectionProperties = cp;
    this.id = id;
    this.maxInsertCount = maxInsertCount; // The max number of insert commands
                                          // in the commit queue
    this.minTmsDelay = minTmsDelay; // The least number of milliseconds to pass
                                    // between commit
  }

  public Identity getIdentity() {
    return id;
  }

  private boolean allUnittypesSpecified(SyslogFilter filter, Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified) {
    Set<Integer> unittypesWithAllProfilesSpecified = new HashSet<Integer>();
    boolean allUnittypesSpecified = false;
    XAPS xaps = filter.getProfiles().get(0).getUnittype().getXaps();
    int noUnittypes = xaps.getUnittypes().getUnittypes().length; // the number
                                                                 // of unittypes
                                                                 // in xAPS
    for (Profile profile : filter.getProfiles()) {
      Integer unittypeId = profile.getUnittype().getId();
      Set<Profile> profilesInUnittype = unittypesWithSomeProfilesSpecified.get(unittypeId);
      if (profilesInUnittype == null)
        profilesInUnittype = new HashSet<Profile>();
      profilesInUnittype.add(profile);
      if (unittypesWithAllProfilesSpecified.contains(unittypeId))
        continue;
      unittypesWithSomeProfilesSpecified.put(unittypeId, profilesInUnittype);
      int noProfiles = profile.getUnittype().getProfiles().getProfiles().length;
      // populate and delete (logically: move from "someSpecified" to
      // "allSpecified")
      if (unittypesWithSomeProfilesSpecified.get(unittypeId).size() == noProfiles) {
        unittypesWithAllProfilesSpecified.add(unittypeId);
        unittypesWithSomeProfilesSpecified.remove(unittypeId);
      }
    }
    if (noUnittypes == unittypesWithAllProfilesSpecified.size())
      allUnittypesSpecified = true;
    return allUnittypesSpecified;
  }

  private DynamicStatement addUnittypeOrProfileCriteria(DynamicStatement ds, SyslogFilter filter) {
    User user = id.getUser();
    // Permissions permissionsObj = user.getPermissions();
    // if (filter.getProfile() != null) {
    // ds.addSqlAndArguments("profile_name = ? AND unit_type_name = ? AND ",
    // filter.getProfile().getName(),
    // filter.getProfile().getUnittype().getName());
    // } else
    if (filter.getProfiles() != null && filter.getProfiles().size() > 0) {
      Map<Integer, Set<Profile>> unittypesWithSomeProfilesSpecified = new HashMap<Integer, Set<Profile>>();
      boolean allUnittypesSpecified = allUnittypesSpecified(filter, unittypesWithSomeProfilesSpecified);
      if (user.isAdmin() && allUnittypesSpecified)
        return ds; // no criteria added -> quicker search, will search for all
                   // unittypes/profiles
      ds.addSql("(");
      for (int i = 0; i < filter.getProfiles().size(); i++) {
        Profile profile = filter.getProfiles().get(i);
        boolean allProfilesSpecified = (unittypesWithSomeProfilesSpecified.get(profile.getUnittype().getId()) == null ? true : false);
        // all profiles in unittype are specified, we can skip profiles criteria
        if (allProfilesSpecified && user.isUnittypeAdmin(profile.getUnittype().getId())) {
          boolean alreadyTreated = false;
          for (int j = 0; j < i; j++) {
            Profile p = filter.getProfiles().get(j);
            if (p.getId().equals(profile.getId()))
              alreadyTreated = true;
          }
          if (!alreadyTreated) // To avoid repeating "unit_type_name = ?" with
                               // the same arguments many times - the SQL
                               // becomes ugly
            ds.addSqlAndArguments("unit_type_name = ? OR ", profile.getUnittype().getName());
        } else
          // have to specify profiles since not all are specified or we do not
          // know of all profile (not UnittypeAdmin)
          ds.addSqlAndArguments("(profile_name = ? AND unit_type_name = ?) OR ", profile.getName(), profile.getUnittype().getName());
      }
      ds.cleanupSQLTail();
      ds.addSql(") AND ");
      // } else if (filter.getUnittype() != null) {
      // XAPS xaps = filter.getUnittype().getXaps();
      // int noUnittypes = xaps.getUnittypes().getUnittypes().length;
      // boolean isAdmin = true;
      // if (permissionsObj != null)
      // isAdmin = permissionsObj.isAdmin();
      // if ((isAdmin && noUnittypes > 1) || !isAdmin)
      // ds.addSqlAndArguments("unit_type_name = ? AND ",
      // filter.getUnittype().getName());
    } else if (filter.getUnittypes() != null && filter.getUnittypes().size() > 0) {
      XAPS xaps = filter.getUnittypes().get(0).getXaps();
      int noUnittypes = xaps.getUnittypes().getUnittypes().length;
      boolean isAdmin = user.isAdmin();
      if (noUnittypes > filter.getUnittypes().size() || !isAdmin) {
        ds.addSql("(");
        for (Unittype unittype : filter.getUnittypes()) {
          ds.addSqlAndArguments("unit_type_name = ? OR ", unittype.getName());
        }
        ds.cleanupSQLTail();
        ds.addSql(") AND ");
      } else {
        // no criteria added, all unittypes are specified and user isAdmin
      }
    }
    return ds;
  }

  /**
   * Criteria will be changed according to these rules: contains | -> split
   * criteria-string, use AND or OR depending on negation (see next test)
   * startswith ! -> negation of search, ! is removed before next test contains
   * * -> replaced by % startswith ^ -> no leading % and ^ is removed endswith $
   * -> no trailing % and $ is removed
   * 
   * @param criteriaName
   * @param ds
   * @param criteria
   * @return
   */
  private void addCriteria(String criteriaName, DynamicStatement ds, String criteria) {
    int dsLength = ds.getSql().length();
    SQLUtil.input2SQLCriteria(ds, criteriaName, criteria);
    if (ds.getSql().length() > dsLength)
      ds.addSql(" AND ");
    // if (criteria == null)
    // return ds;
    // boolean equality = true;
    // if (criteria.startsWith("!")) {
    // criteria = criteria.substring(1);
    // equality = false;
    // }
    // String[] contentArr = criteria.split("\\|");
    // ds.addSql("(");
    // for (String c : contentArr) {
    // c = c.replace('*', '%');
    // String searchStr = "%" + c + "%";
    // boolean exact = false;
    // if (c.startsWith("^") && c.endsWith("$") && c.indexOf("%") == -1 &&
    // c.indexOf("_") == -1)
    // exact = true;
    // if (c.startsWith("^"))
    // searchStr = searchStr.substring(2); // remove %^
    // if (c.endsWith("$"))
    // searchStr = searchStr.substring(0, searchStr.length() - 2); // remove $%
    // if (exact) {
    // if (equality)
    // ds.addSqlAndArguments(criteriaName + " = ? OR ", searchStr);
    // else
    // ds.addSqlAndArguments(criteriaName + " <> ? AND ", searchStr);
    // } else {
    // if (equality)
    // ds.addSqlAndArguments(criteriaName + " LIKE ? OR ", searchStr);
    // else
    // ds.addSqlAndArguments(criteriaName + " NOT LIKE ? AND ", searchStr);
    // }
    // }
    // ds.cleanupSQLTail();
    // ds.addSql(") AND ");
    // return ds;
  }

  /**
   * Content will be split on "|" to make it possible to search for more than
   * one type of content. Apart from that it will also handle the same as
   * addCriteria(): !, ^, $, *, _
   * 
   * @param ds
   * @param content
   * @return
   */
  /*
   * private DynamicStatement addContentCriteria(DynamicStatement ds, String
   * content) { if (content == null) return ds; boolean equality = true; if
   * (content.startsWith("!")) { content = content.substring(1); equality =
   * false; } String[] contentArr = content.split("\\|"); ds.addSql("("); for
   * (String c : contentArr) { c = c.replace('*', '%'); String searchStr = "%" +
   * c + "%"; if (c.startsWith("^")) searchStr = searchStr.substring(2); if
   * (c.endsWith("$")) searchStr = searchStr.substring(0, searchStr.length() -
   * 2); if (equality) ds.addSqlAndArguments("content LIKE ? OR ", searchStr);
   * else ds.addSqlAndArguments("content NOT LIKE ? AND ", searchStr); }
   * ds.cleanupSQLTail(); ds.addSql(") AND "); return ds; }
   */

  private DynamicStatement addSeverityCriteria(DynamicStatement ds, Integer[] severities) {
    if (severities != null && severities.length > 0) {
      ds.addSql("(");
      for (int i = 0; i < severities.length; i++)
        ds.addSqlAndArguments("severity = ? OR ", severities[i]);
      ds.cleanupSQLTail();
      ds.addSql(") AND ");
    }
    return ds;
  }

  private PreparedStatement makeReadSQL(Connection c, DynamicStatement ds, SyslogFilter filter) throws SQLException {
    ds.addSql("SELECT * FROM syslog WHERE ");
    addCriteria("content", ds, filter.getMessage());
    addCriteria("facility_version", ds, filter.getFacilityVersion());
    addCriteria("user_id", ds, filter.getUserId());
    addCriteria("ipaddress", ds, filter.getIpAddress());
    addCriteria("unit_id", ds, filter.getUnitId());
    ds = addUnittypeOrProfileCriteria(ds, filter);
    ds = addSeverityCriteria(ds, filter.getSeverity());
    if (filter.getEventId() != null)
      ds.addSqlAndArguments("syslog_event_id = ? AND ", filter.getEventId());
    if (filter.getFacility() != null)
      ds.addSqlAndArguments("facility = ? AND ", filter.getFacility());
    if (filter.getCollectorTmsStart() != null)
      ds.addSqlAndArguments("collector_timestamp >= ? AND ", filter.getCollectorTmsStart());
    if (filter.getCollectorTmsEnd() != null)
      ds.addSqlAndArguments("collector_timestamp < ? AND ", filter.getCollectorTmsEnd());
    if (filter.getFacility() != null)
      ds.addSqlAndArguments("facility = ? AND ", filter.getFacility());
    if (filter.getFlags() != null)
      ds.addSqlAndArguments("flags = ? AND ", filter.getFlags());
    ds.cleanupSQLTail();
    ds.addSql("ORDER BY collector_timestamp DESC ");
    if (filter.getMaxRows() != null && filter.getMaxRows() < Integer.MAX_VALUE) {
      String sql = ds.getSql();
      if (connectionProperties.getDriver().indexOf("mysql") > -1)
        sql += " LIMIT " + filter.getMaxRows();
      else if (connectionProperties.getDriver().indexOf("oracle") > -1)
        sql = "SELECT * FROM (" + sql + ") WHERE ROWNUM <= " + filter.getMaxRows();
      ds.setSql(sql);
    }
    return ds.makePreparedStatement(c);
  }

  // private String defaultTmsArg() {
  // if (connectionProperties.getDriver().indexOf("oracle") > -1) {
  // return "SYSTIMESTAMP, ";
  // } else {
  // return "NOW(), ";
  // }
  // }

  
  
  private String getTmsArg(SyslogEntry entry) {
    if (simulationMode || (entry.getFacility() != null && entry.getFacility() >= 50)) {
      if (entry.getDeviceTimestamp() != null && !entry.getDeviceTimestamp().trim().equals("Jan 1 00:00:00")) {
        try {
          Date deviceTms = deviceTmsFormat.parse(yearCal.get(Calendar.YEAR) + " " + entry.getDeviceTimestamp());
          // Around midnight on new year's eve, one might have date-tms combined
          // with the wrong year, since we set the year on server side. To avoid
          // this, we must check if the device-tms is too far off from the now-tms.
          // If it is, we adjust the yearCal and force deviceTms to now-tms
          if (Math.abs(System.currentTimeMillis() - deviceTms.getTime()) > 3600 * 1000 * 24 * 30) {
            deviceTms = new Date();
            yearCal.setTime(deviceTms);
          }
          return "'" + dbDateFormat.format(deviceTms) + "'";
        } catch (ParseException e) {
          logger.error("Device timestamp (" + entry.getDeviceTimestamp() + ") could not be parsed into a date, using NOW tms instead", e);
          return "'" + dbDateFormat.format(new Date()) + "'";
        }
      }
      return "'" + dbDateFormat.format(new Date()) + "'";
    } else if (entry.getCollectorTimestamp() != null) {
      return "'" + dbDateFormat.format(entry.getCollectorTimestamp()) + "'";
    } else {
      return "'" + dbDateFormat.format(new Date()) + "'";
    }
  }

  // private String getTmsArg(DynamicStatement ds, SyslogEntry entry) {
  // if (simulationMode || (entry.getFacility() != null && entry.getFacility()
  // >= 50)) {
  // if (entry.getDeviceTimestamp() != null &&
  // !entry.getDeviceTimestamp().trim().equals("Jan 1 00:00:00")) {
  // try {
  // Date deviceTms = simulationTmsFormat.parse(simulationYear + " " +
  // entry.getDeviceTimestamp());
  // ds.addArguments(deviceTms); // results in "return null" from this method
  // return null;
  // } catch (ParseException e) {
  // logger.error("Device timestamp (" + entry.getDeviceTimestamp() +
  // ") could not be parsed into a date, using NOW tms instead", e);
  // return defaultTmsArg();
  // }
  // } else {
  // return defaultTmsArg();
  // }
  // } else if (entry.getCollectorTimestamp() != null) {
  // ds.addArguments(entry.getCollectorTimestamp());
  // return null;
  // } else {
  // return defaultTmsArg();
  // }
  // }

  private String makeInsertColumnSQL() {
    return "INSERT INTO syslog (collector_timestamp, syslog_event_id, facility, facility_version, severity, device_timestamp, hostname, tag, unit_id, profile_name, unit_type_name, user_id, content, flags, ipaddress) VALUES ";
  }

  private String makeInsertValueSQL(SyslogEntry entry) {
    StringBuilder sb = new StringBuilder();
    sb.append("(");

    sb.append(getTmsArg(entry)); // collector_timestamp, can be NOW() or a
                                 // 'timestamp'
    sb.append(",");

    if (entry.getEventId() != null)
      sb.append(entry.getEventId()); // syslog_event_id
    else
      sb.append(0); // syslog_event_id - default
    sb.append(",");

    if (entry.getFacility() != null)
      sb.append(entry.getFacility()); // facility - set explicitely
    else
      sb.append(id.getFacility()); // facility - retrieve via Identify
    sb.append(",");

    if (entry.getFacilityVersion() != null)
      sb.append("'" + entry.getFacilityVersion() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    sb.append(entry.getSeverity());
    sb.append(",");

    if (entry.getDeviceTimestamp() != null)
      sb.append("'" + entry.getDeviceTimestamp() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    if (entry.getHostname() != null)
      sb.append("'" + entry.getHostname() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    if (entry.getTag() != null)
      sb.append("'" + entry.getTag() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    if (entry.getUnitId() != null)
      sb.append("'" + entry.getUnitId() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    if (entry.getProfileName() != null)
      sb.append("'" + entry.getProfileName() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    if (entry.getUnittypeName() != null)
      sb.append("'" + entry.getUnittypeName() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    if (id.getUser() != null)
      sb.append("'" + id.getUser().getUsername() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    if (entry.getContent() != null) {
      if (entry.getContent().length() > 1024)
        entry.setContent(entry.getContent().substring(0, 1020) + "....");
      if (entry.getContent().contains("'"))
        entry.setContent(entry.getContent().replace("'", "\\'"));
      sb.append("'" + entry.getContent() + "'");
    } else
      sb.append("NULL");
    sb.append(",");

    if (entry.getFlags() != null)
      sb.append("'" + entry.getFlags() + "'");
    else
      sb.append("NULL");
    sb.append(",");

    if (entry.getIpAddress() != null)
      sb.append("'" + entry.getIpAddress() + "'");
    else
      sb.append("NULL");

    sb.append(")");
    return sb.toString();
  }

  // private PreparedStatement makeWriteSQL(SyslogEntry entry, Connection c)
  // throws SQLException {
  // DynamicStatement ds = new DynamicStatement();
  // ds.addSql("INSERT INTO syslog (");
  // ds.addSql("collector_timestamp, ");
  // String tmsArg = getTmsArg(ds, entry);
  // if (entry.getEventId() != null)
  // ds.addSqlAndArguments("syslog_event_id, ", entry.getEventId());
  // else if (entry.getId() != null)
  // ds.addSqlAndArguments("syslog_event_id, ", entry.getId());
  // else
  // ds.addSqlAndArguments("syslog_event_id, ", 0);
  // if (entry.getFacility() != null)
  // ds.addSqlAndArguments("facility, ", entry.getFacility());
  // else
  // ds.addSqlAndArguments("facility, ", id.getFacility());
  // if (entry.getFacilityVersion() != null /*&&
  // XAPSVersionCheck.syslogFacilityVersionSupported*/)
  // ds.addSqlAndArguments("facility_version, ", entry.getFacilityVersion());
  // ds.addSqlAndArguments("severity, ", entry.getSeverity());
  // if (entry.getDeviceTimestamp() != null)
  // ds.addSqlAndArguments("device_timestamp, ", entry.getDeviceTimestamp());
  // if (entry.getHostname() != null)
  // ds.addSqlAndArguments("hostname, ", entry.getHostname());
  // if (entry.getTag() != null)
  // ds.addSqlAndArguments("tag, ", entry.getTag());
  // if (entry.getUnitId() != null)
  // ds.addSqlAndArguments("unit_id, ", entry.getUnitId());
  // if (entry.getProfileName() != null/* && XAPSVersionCheck.syslogSupported*/)
  // ds.addSqlAndArguments("profile_name, ", entry.getProfileName());
  // if (entry.getProfileId() != null/* && !XAPSVersionCheck.syslogSupported*/)
  // ds.addSqlAndArguments("profile_id, ", entry.getProfileId());
  // if (entry.getUnittypeName() != null/* &&
  // XAPSVersionCheck.syslogSupported*/)
  // ds.addSqlAndArguments("unit_type_name, ", entry.getUnittypeName());
  // if (entry.getUnittypeId() != null/* && !XAPSVersionCheck.syslogSupported*/)
  // ds.addSqlAndArguments("unit_type_id, ", entry.getUnittypeId());
  // if (id.getUser() != null)
  // ds.addSqlAndArguments("user_id, ", id.getUser().getUsername());
  // if (entry.getContent() != null) {
  // if (entry.getContent().length() > 1024) {
  // entry.setContent(entry.getContent().substring(0, 1020) + "....");
  // }
  // ds.addSqlAndArguments("content, ", entry.getContent());
  // }
  // if (entry.getFlags() != null)
  // ds.addSqlAndArguments("flags, ", entry.getFlags());
  // if (entry.getIpAddress() != null)
  // ds.addSqlAndArguments("ipaddress", entry.getIpAddress());
  // ds.cleanupSQLTail();
  // ds.addSql(") VALUES (");
  // if (tmsArg != null)
  // ds.addSql(tmsArg);
  // ds.addSql(ds.getQuestionMarks() + ")");
  // return ds.makePreparedStatement(c, "syslog_id");
  // }

  private List<SyslogEntry> makeSyslogEntries(ResultSet rs) throws SQLException {
    List<SyslogEntry> result = new ArrayList<SyslogEntry>();
    while (rs.next()) {
      SyslogEntry se = new SyslogEntry();
      se.setId(rs.getInt("syslog_id"));
      se.setCollectorTimestamp(rs.getTimestamp("collector_timestamp"));
      se.setEventId(rs.getInt("syslog_event_id"));
      se.setFacility(rs.getInt("facility"));
      // if (se.getFacility() <= 16) // Local devices
      // facilityDevice = true;
      // if (XAPSVersionCheck.syslogFacilityVersionSupported)
      se.setFacilityVersion(rs.getString("facility_version"));
      se.setSeverity(rs.getInt("severity"));
      se.setDeviceTimestamp(rs.getString("device_timestamp"));
      se.setHostname(rs.getString("hostname"));
      se.setTag(rs.getString("tag"));
      se.setContent(rs.getString("content"));
      se.setFlags(rs.getString("flags"));
      se.setIpAddress(rs.getString("ipaddress"));
      se.setUnitId(rs.getString("unit_id"));
      // if (XAPSVersionCheck.syslogSupported)
      se.setProfileName(rs.getString("profile_name"));
      // else {
      // String profileIdStr = rs.getString("profile_id");
      // if (profileIdStr != null)
      // se.setProfileId(rs.getInt("profile_id"));
      // }
      // if (XAPSVersionCheck.syslogSupported)
      se.setUnittypeName(rs.getString("unit_type_name"));
      // else {
      // String unittypeIdStr = rs.getString("unit_type_id");
      // if (unittypeIdStr != null)
      // se.setUnittypeId(rs.getInt("unit_type_id"));
      // }
      se.setUserId(rs.getString("user_id"));
      result.add(se);
    }
    return result;
  }

  public void updateContent(long tms, String oldContent, String newContent, String unitId) throws SQLException, NoAvailableConnectionException {
    Connection c = ConnectionProvider.getConnection(connectionProperties, true);
    PreparedStatement pp = null;
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("UPDATE syslog SET content = ? WHERE ", newContent);
      ds.addSqlAndArguments("collector_timestamp >= ? AND ", new Timestamp(tms));
      ds.addSqlAndArguments("collector_timestamp <= ? AND ", new Timestamp(tms + 10000));
      ds.addSqlAndArguments("content = ? AND ", oldContent);
      if (unitId != null)
        ds.addSqlAndArguments("unit_id = ?", unitId);
      else
        ds.cleanupSQLTail();
      pp = ds.makePreparedStatement(c);
      pp.executeUpdate();
      logger.debug(ds.getDebugMessage());
    } finally {
      if (pp != null)
        pp.close();
      if (c != null)
        ConnectionProvider.returnConnection(c, null);
    }
  }

  // Delete entries with a specific event, unless it has some a severity level
  public int deleteOldEventsEntries(Calendar fromCal, Calendar toCal, SyslogEvent event, int limit) throws SQLException, NoAvailableConnectionException {
    Connection c = ConnectionProvider.getConnection(connectionProperties, true);
    PreparedStatement pp = null;
    DynamicStatement ds = new DynamicStatement();
    try {
      ds.addSql("DELETE FROM syslog WHERE ");
      ds.addSqlAndArguments("syslog_event_id = ? and ", event.getId());
      if (fromCal != null)
        ds.addSqlAndArguments("collector_timestamp >= ? and ", fromCal);
      ds.addSqlAndArguments("collector_timestamp < ? ", toCal);
      if (connectionProperties.getDriver().indexOf("mysql") > -1)
        ds.addSql(" LIMIT " + limit);
      else if (connectionProperties.getDriver().indexOf("oracle") > -1)
        ds.addSql(" ROWNUM <= " + limit);
      pp = ds.makePreparedStatement(c);

      logger.debug("The SQL which will be executed: " + ds.getSqlQuestionMarksSubstituted());
      return pp.executeUpdate();
    } catch (SQLException sqle) {
      // This happens if MYSQL database is busy, perhaps running a backup
      if (sqle.getMessage().indexOf("Lock wait timeout exceeded") > -1) {

        logger.warn("The database is busy (perhaps running a backup?), we'll wait 10 minutes and try again. SQLError: " + sqle.getErrorCode() + ", " + sqle.getMessage());
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
      if (pp != null)
        pp.close();
      if (c != null)
        ConnectionProvider.returnConnection(c, null);
    }
  }

  // Delete entries with a specific severity, unless it has a specified event id
  public int deleteOldSeverityEntries(Calendar fromCal, Calendar toCal, int severity, List<SyslogEvent> events, int limit) throws SQLException, NoAvailableConnectionException {
    Connection c = ConnectionProvider.getConnection(connectionProperties, true);
    PreparedStatement pp = null;
    DynamicStatement ds = new DynamicStatement();
    try {
      ds.addSql("DELETE FROM syslog WHERE ");
      ds.addSqlAndArguments("severity = ? and ", severity);
      for (SyslogEvent event : events)
        ds.addSqlAndArguments("syslog_event_id <> ? and ", event.getId());
      if (fromCal != null)
        ds.addSqlAndArguments("collector_timestamp >= ? and ", fromCal);
      ds.addSqlAndArguments("collector_timestamp < ? ", toCal);
      if (connectionProperties.getDriver().indexOf("mysql") > -1)
        ds.addSql(" LIMIT " + limit);
      else if (connectionProperties.getDriver().indexOf("oracle") > -1)
        ds.addSql(" ROWNUM <= " + limit);
      pp = ds.makePreparedStatement(c);

      logger.debug("The SQL which will be executed: " + ds.getSqlQuestionMarksSubstituted());
      return pp.executeUpdate();
    } catch (SQLException sqle) {
      // This happens if MYSQL database is busy, perhaps running a backup
      if (sqle.getMessage().indexOf("Lock wait timeout exceeded") > -1) {

        logger.warn("The database is busy (perhaps running a backup?), we'll wait 10 minutes and try again. SQLError: " + sqle.getErrorCode() + ", " + sqle.getMessage());
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
      if (pp != null)
        pp.close();
      if (c != null)
        ConnectionProvider.returnConnection(c, null);
    }
  }

  public List<SyslogEntry> read(SyslogFilter filter, XAPS xaps) throws SQLException, NoAvailableConnectionException {
    Connection c = ConnectionProvider.getConnection(connectionProperties, true);
    PreparedStatement pp = null;
    ResultSet rs = null;
    // facilityDevice = false;
    try {
      DynamicStatement ds = new DynamicStatement();
      pp = makeReadSQL(c, ds, filter);
      long start = System.currentTimeMillis();
      rs = pp.executeQuery();
      if (logger.isDebugEnabled()) {
        float milli = (float) ((System.currentTimeMillis() - start));
        if (logger.isDebugEnabled()) {

          String msg = "Syslog query: " + ds.getSqlQuestionMarksSubstituted();
          logger.debug("[" + String.format("%10.2f", milli) + " ms] " + msg);
        }
      }
      List<SyslogEntry> syslogEntryList = makeSyslogEntries(rs);
      Collections.sort(syslogEntryList, new SyslogEntryComparator());
      // if (!facilityDevice && filter.getUnitId() != null && xaps != null) {
      // logger.info("No device syslog entries found for " + filter.getUnitId()
      // + ", will search among other units with same MAC");
      // String orgUnitId = filter.getUnitId();
      // XAPSUnit xapsUnit = new XAPSUnit(xaps.connectionProperties, xaps,
      // this);
      // Unit u = xapsUnit.getUnitById(filter.getUnitId());
      // if (u != null) {
      // UnitParameter up = u.getUnitParameters().get(SystemParameters.MAC);
      // if (up != null && up.getValue() != null) {
      // Map<String, Unit> unitMap = xapsUnit.getUnits(up.getValue(), null,
      // null, null);
      // logger.info("Found " + unitMap.size() + " units with MAC " +
      // up.getValue());
      // for (Unit unit : unitMap.values()) {
      // if (unit.getId().equals(orgUnitId))
      // continue;
      //
      // logger.info("UnitId " + orgUnitId +
      // " had no device syslog entries, using (" + unit.getId() + "," +
      // unit.getUnittype().getName() + ") (sharing same MAC) instead.");
      // filter.setUnittype(unit.getUnittype());
      // filter.setUnittypes(new ArrayList<Unittype>());
      // filter.setProfile(unit.getProfile());
      // filter.setProfiles(new ArrayList<Profile>());
      // filter.setUnitId(unit.getId());
      // syslogEntryList.addAll(read(filter, null));
      // }
      // if (unitMap.size() > 1) { // Actually found another unit with the same
      // MAC
      // Collections.sort(syslogEntryList, new SyslogEntryComparator());
      // // Run limit according to filter
      // if (filter.getMaxRows() != null && filter.getMaxRows() <
      // Integer.MAX_VALUE && syslogEntryList.size() > filter.getMaxRows()) {
      // int entriesToRemove = syslogEntryList.size() - filter.getMaxRows();
      // for (int i = 0; i < entriesToRemove; i++) {
      // syslogEntryList.remove(filter.getMaxRows());
      // }
      // }
      // }
      // }
      // }
      // }
      return syslogEntryList;
    } finally {
      if (rs != null)
        rs.close();
      if (pp != null)
        pp.close();
      if (c != null)
        ConnectionProvider.returnConnection(c, null);
    }
  }

  // Returns syslog id
  public void write(SyslogEntry entry) throws SQLException, NoAvailableConnectionException {
    Connection c = null;
    Statement s = null;
    SQLException sqlex = null;
    try {
      insertValues.append(makeInsertValueSQL(entry));
      insertCount++;
      if (insertCount > maxInsertCount || System.currentTimeMillis() > insertTms + minTmsDelay) {
        String sql = makeInsertColumnSQL() + insertValues.toString();
        c = ConnectionProvider.getConnection(connectionProperties, true);
        s = c.createStatement();
        s.executeUpdate(sql);
        insertCount = 0;
        insertTms = System.currentTimeMillis();
        insertValues = new StringBuilder();
      } else
        insertValues.append(", ");

    } catch (SQLException sqle) {
      insertValues = new StringBuilder();
      sqlex = sqle;
      throw sqle;
    } finally {
      if (s != null)
        s.close();
      if (c != null)
        ConnectionProvider.returnConnection(c, sqlex);
    }
  }

  public void setIdentity(Identity id) {
    this.id = id;
  }

  public void setConnectionProperties(ConnectionProperties connectionProperties) {
    this.connectionProperties = connectionProperties;
  }

  public boolean isSimulationMode() {
    return simulationMode;
  }

  public void setSimulationMode(boolean simulationMode) {
    this.simulationMode = simulationMode;
  }
}
