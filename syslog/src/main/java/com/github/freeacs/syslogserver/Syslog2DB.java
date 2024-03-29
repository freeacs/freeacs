package com.github.freeacs.syslogserver;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ScriptExecutions;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogEvent;
import com.github.freeacs.dbi.SyslogEvent.StorePolicy;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.TriggerEvent;
import com.github.freeacs.dbi.Triggers;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.dbi.util.SystemParameters;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Syslog2DB implements Runnable {
  private final Properties properties;
  private final DataSource xapsCp;

  @Getter
  private static final Syslog2DBCounter counter = new Syslog2DBCounter();

  private static final Logger logger = LoggerFactory.getLogger(Syslog2DB.class);

  private static final Pattern priPattern = Pattern.compile("^<(\\d{1,3})>");

  private static final Pattern tmsPattern =
      Pattern.compile(
          "^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{1,2})\\s+(\\d{2}):(\\d{2}):(\\d{2})\\s+");

  private static final Pattern hostPattern = Pattern.compile("^\\S+\\s+");

  private static final Pattern tagPattern = Pattern.compile("^\\w{1,32}");

  private static final Pattern simpleIPPattern =
      Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");

  private static final List<Pattern> deviceIdPatterns = new ArrayList<>();

  static {
    deviceIdPatterns.add(Pattern.compile("\\[([a-fA-F0-9:-]{12,17})]:"));
  }

  private void poulateDeviceIdPatterns() {
    int index = 1;
    Pattern pattern = null;
    do {
      String patternStr = Properties.getDeviceIdPattern(index);
      if (patternStr != null) {
        try {
          pattern = Pattern.compile(patternStr);
          deviceIdPatterns.add(pattern);
          index++;
        } catch (Throwable t) {
          logger.error(
              "A mac-pattern (" + patternStr + ") was incorrect and will not be used." + t);
        }
      } else {
        pattern = null;
      }
    } while (pattern != null);
  }

  private static DBI dbi;

  @Getter
  private static Syslog syslog;

  private static boolean pause;

  @Getter
  private static boolean ok = true;

  @Getter
  private static Throwable throwable;

  private static final Cache unitCache = new Cache();

  private ScriptExecutions executions;

  private synchronized void init(DataSource xapsCp, Properties properties) {
    executions = new ScriptExecutions(xapsCp);
    if (dbi == null) {
      try {
        Users users = new Users(xapsCp);
        Identity id = new Identity(5, "latest", users.getUnprotected(Users.USER_ADMIN));
        syslog = new Syslog(xapsCp, id);
        if (properties.isSimulation()) {
          syslog.setSimulationMode(true);
          logger.warn(
              "Syslog server runs in simulation mode, collector timestamp will be populated with device timestamp *if* they differ from 1 Jan 00:00:00");
        }
        dbi = DBI.createAndInitialize(Integer.MAX_VALUE, xapsCp, syslog);
      } catch (Throwable t) {
        logger.error("Syslog2DB Thread could not start due to failure in initialization", t);
      }
    }
  }

  Syslog2DB(DataSource xapsDataSource, Properties properties) {
    this.xapsCp = xapsDataSource;
    poulateDeviceIdPatterns();
    init(xapsDataSource, properties);
    populateXAPS();
    this.properties = properties;
  }

  private static synchronized ACS populateXAPS() {
    if (dbi != null) {
      return dbi.getAcs();
    }
    logger.warn("Cannot popuplate ACS object because dbi instance is null");
    return null;
  }

  public void run() {
    while (dbi != null) {
      try {
        if (pause) {
          Thread.sleep(1000);
          if (Sleep.isTerminated()) {
            return;
          }
          continue;
        }
        SyslogPacket packet = SyslogPackets.get();
        if (Sleep.isTerminated()) {
          return;
        }
        if (packet == null) {
          continue;
        }
        try {
          SyslogEntry entry = prepareEntry(packet);
          ok = true;
          if (entry == null) {
            continue;
          }
          syslog.write(entry);
          counter.incOk();
          //					incWriteCount();
          if (logger.isDebugEnabled()) {
            logger.debug(
                "The syslog message is written to database (unitId: " + entry.getUnitId() + ")");
          }
        } catch (Throwable t) {
          throwable = t;
          ok = false;
          if (!Sleep.isTerminated()) {
            //						ff.write(packet.toString() + "An error occurred when inserting to database\n");
            counter.incFailed();
          }
        }
      } catch (Throwable t2) {
        logger.error("An error occurred in Syslog2DB.run()", t2);
      }
    }
    logger.warn(
        "Syslog thread "
            + Thread.currentThread().getName()
            + " exited due to missing dbi instance");
  }

  private SyslogEntry processSyslogEvent(SyslogEntry entry, SyslogEvent se, Unit unit) throws SQLException {
    if (logger.isDebugEnabled()) {
      logger.debug(
          "Unitid "
              + entry.getUnitId()
              + " will process syslog event "
              + se.getName()
              + " (policy: "
              + se.getStorePolicy()
              + ")");
    }
    if (se.getScript() != null) {
      String scriptArgs =
          "\"-uut:"
              + entry.getUnittypeName()
              + "/pr:"
              + entry.getProfileName()
              + "/un:"
              + entry.getUnitId()
              + "\"";
      executions.requestExecution(se.getScript(), scriptArgs, "SYSEVENT:" + se.getEventId());
      unitCache.remove(unit.getId());
    }

    if (se.getStorePolicy() == StorePolicy.STORE) {
      counter.incKnownEventAllowed();
      entry.setEventId(se.getEventId());
    } else if (se.getStorePolicy() == StorePolicy.DISCARD) {
      counter.incKnownEventDiscarded();
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Unitid "
                + entry.getUnitId()
                + " has a message which matches eventId "
                + se.getEventId()
                + ", which task is to discard - thus discarded.");
      }
      return null;
    } else if (se.getStorePolicy() == StorePolicy.DUPLICATE) {
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Unitid "
                + entry.getUnitId()
                + " has a message which matches eventId "
                + se.getEventId()
                + ", which task is duplicate check - may be discarded");
      }
      String key = entry.getUnitId() + entry.getContent();
      if (!DuplicateCheck.addMessage(key, entry, SyslogEvent.DUPLICATE_TIMEOUT, properties)) {
        counter.incKnownEventDuplicated();
        return null;
      } else {
        counter.incKnownEventAllowed();
        entry.setEventId(se.getEventId());
      }
    }

    return entry;
  }

  private SyslogEntry prepareEntryWithXAPSInfo(SyslogEntry entry, SyslogPacket packet)
      throws SQLException {
    CacheValue cv = unitCache.get(entry.getUnitId());
    Unit unit;
    ACS acs = populateXAPS();
    if (acs == null) {
      logger.warn(
          String.format(
              "ACS could not be loaded. Cannot continue to parse syslog entry [%s]",
                  entry),
          new Exception());
      return null;
    }
    if (cv == null) {
      ACSUnit acsUnit = new ACSUnit(xapsCp, acs, syslog);
      if ("UNITID".equals(entry.getTag())) {
        unit = acsUnit.getUnitById(entry.getUnitId());
      } else {
        unit = acsUnit.getLimitedUnitByValue(entry.getUnitId());
      }
      if (unit != null) {
        cv = new CacheValue(unit, Cache.ABSOLUTE, 15 * 1000 * 60);
      } else {
        cv = new CacheValue("Not found", Cache.ABSOLUTE, 1000 * 30);
      }
      unitCache.put(entry.getUnitId(), cv);
    }
    Object o = cv.getObject();
    if (o instanceof Unit) {
      unit = (Unit) cv.getObject();
      if (unit.getProfile() == null || unit.getUnittype() == null) {
        logger.warn(
            String.format(
                "We found the unit [%s], but it was not populated with Unittype and Profile. Syslog entry: %s",
                entry.getUnitId(), entry),
            new Exception());
        return null;
      }
      entry.setUnitId(unit.getId());
      entry.setProfileName(unit.getProfile().getName());
      entry.setUnittypeName(unit.getUnittype().getName());
      Unittype unittype = acs.getUnittype(entry.getUnittypeName());

      UnitParameter swUp = unit.getUnitParameters().get(SystemParameters.SOFTWARE_VERSION);
      if (swUp != null && entry.getFacilityVersion() == null) {
        entry.setFacilityVersion(swUp.getValue());
      }
      entry = populateSyslogEvent(unittype, entry, unit);
    } else {
      // unit was not found in xAPS
      String action = properties.getUnknownUnitsAction();
      if (action.startsWith("allow")) {
        entry.setEventId(SyslogConstants.EVENT_DEFAULT);
        counter.incUknownAllowed();
        if (action.endsWith("allow-create")) {
          counter.incUknownAllowedCreated();
          Unittype unittype = acs.getUnittype("MonitorDevice");
          if (unittype == null) {
            unittype =
                new Unittype(
                    "MonitorDevice",
                    "Unknown",
                    "A dummy unittype for monitoring purpose",
                    ProvisioningProtocol.TR069);
            acs.getUnittypes().addOrChangeUnittype(unittype, acs);
          }
          Profile profile = unittype.getProfiles().getByName("Default");
          ACSUnit acsUnit = new ACSUnit(xapsCp, acs, syslog);
          List<String> units = new ArrayList<>();
          String unitId = "000000-MD-" + entry.getUnitId();
          units.add(unitId);
          acsUnit.addUnits(units, profile);
          UnittypeParameter utp =
              unittype.getUnittypeParameters().getByName(SystemParameters.SERIAL_NUMBER);
          List<UnitParameter> unitParameters = new ArrayList<>();
          unitParameters.add(new UnitParameter(utp, unitId, entry.getUnitId(), profile));
          acsUnit.addOrChangeUnitParameters(unitParameters);
          unitCache.remove(entry.getUnitId());
          if (logger.isDebugEnabled()) {
            logger.debug(
                "Unitid "
                    + entry.getUnitId()
                    + " unknown, but unknown units are allowed and dummy unit is created");
          }
        } else if (logger.isDebugEnabled()) {
          logger.debug("Unitid " + entry.getUnitId() + " unknown, but unknown units are allowed");
        }
      } else {
        if ("discard".equals(action)) {
          if (logger.isDebugEnabled()) {
            logger.debug(
                "Unitid " + entry.getUnitId() + " unknown, and unknown units are discarded");
          }
          counter.incUknownDiscarded();
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug(
                "Unitid "
                    + entry.getUnitId()
                    + " unknown, and unknown units are redirected to "
                    + action);
          }
          SyslogClient.send(action, packet, properties.getPort());
          counter.incUknownRedirected();
        }
        return null;
      }
    }
    return entry;
  }

  private SyslogEntry populateSyslogEvent(Unittype unittype, SyslogEntry entry, Unit unit)
      throws SQLException {
    SyslogEvent[] syslogEvents = unittype.getSyslogEvents().getSyslogEvents();
    if (logger.isDebugEnabled()) {
      logger.debug("Found " + syslogEvents.length + " syslog events for unit-id " + unit.getId());
    }
    for (SyslogEvent se : syslogEvents) {
      if (se.getEventId() < 1000) {
        continue;
      }
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Syslog event " + se.getId() + " has expression pattern " + se.getExpressionPattern());
      }
      Matcher m = se.getExpressionPattern().matcher(entry.getContent());
      if (m.find()) {
        if (se.getGroup() != null) {
          if (se.getGroup().match(unit)) {
            if (logger.isDebugEnabled()) {
              logger.debug(
                  "Unit-id: "
                      + unit.getId()
                      + " in group "
                      + se.getGroup().getName()
                      + " with expression "
                      + se.getExpression()
                      + " matched "
                      + entry.getContent());
            }
          } else {
            continue;
          }
        } else if (logger.isDebugEnabled()) {
          logger.debug(
              "Unit-id: "
                  + unit.getId()
                  + " with expression "
                  + se.getExpression()
                  + " matched "
                  + entry.getContent());
        }
        Triggers triggers = unittype.getTriggers();
        for (Trigger trigger : triggers.getTriggers()) {
          if (!trigger.isActive()
              || trigger.getTriggerType() == Trigger.TRIGGER_TYPE_COMPOSITE
              || !trigger.getSyslogEvent().getId().equals(se.getId())) {
            continue;
          }
          counter.incTriggerEvent();
          triggers.addEvent(new TriggerEvent(trigger, new Date(), unit.getId()), populateXAPS());
        }
        if (processSyslogEvent(entry, se, unit) == null) {
          return null;
        }
        break;
      }
    }
    if (entry.getEventId() == null) {
      entry.setEventId(SyslogConstants.EVENT_DEFAULT);
    }
    return entry;
  }

  private SyslogEntry prepareEntry(SyslogPacket packet) throws SQLException {
    SyslogEntry entry = parse(packet);
    if (entry == null) {
      return null;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Syslog message has been parsed (unitId: " + entry.getUnitId() + ")");
    }
    if (entry.getHostname() == null) {
      entry.setHostname(packet.getAddress());
      if (logger.isDebugEnabled()) {
        logger.debug(
            "No hostname in syslog message, added IP-address (unitId: " + entry.getUnitId() + ")");
      }
    }
    if (entry.getUnitId() != null && entry.getUnittypeName() == null) {
      entry = prepareEntryWithXAPSInfo(entry, packet);
      if (entry == null) {
        return null;
      }
    }
    Matcher ipMatcher = simpleIPPattern.matcher(entry.getHostname());
    if (entry.getHostname() != null && ipMatcher.find()) {
      entry.setIpAddress(entry.getHostname());
    } else {
      entry.setIpAddress(packet.getAddress());
    }
    return entry;
  }

  /** No MAC-pattern were found, let's try using the IP-address. */
  private void handleMissingMAC(SyslogEntry entry, SyslogPacket packet, String parsedSyslogStr) {
    if (entry.getUnitId() == null) {
      entry.setUnitId(packet.getAddress());
      entry.setContent(parsedSyslogStr.trim());
    }
  }

  private SyslogEntry parse(SyslogPacket packet) {
    String syslogStr = packet.getSyslogStr().trim();
    Matcher m = priPattern.matcher(syslogStr);
    int severity = 7;
    int facility = 23;
    if (m.find()) {
      int priority = Integer.parseInt(m.group(1));
      severity = priority % 8;
      facility = (priority - severity) / 8;
      syslogStr = syslogStr.substring(m.end());
    }

    SyslogEntry entry = new SyslogEntry();
    if (packet.isFailoverPacket()) {
      entry.setCollectorTimestamp(new Date(packet.getTms()));
    }
    entry.setSeverity(severity);
    entry.setFacility(facility);

    m = tmsPattern.matcher(syslogStr);
    if (m.find()) {
      entry.setDeviceTimestamp(syslogStr.substring(0, m.end()));
      syslogStr = syslogStr.substring(m.end());

      m = hostPattern.matcher(syslogStr);
      if (m.find()) {
        entry.setHostname(syslogStr.substring(0, m.end()).trim());
        syslogStr = syslogStr.substring(m.end());
      }

      m = tagPattern.matcher(syslogStr);
      if (m.find()) {
        entry.setTag(syslogStr.substring(0, m.end()));
        syslogStr = syslogStr.substring(m.end());
        // Special hack to signal setting of UnitId directly, not using MAC/Serialnumber (as usual)
        if ("UNITID".equals(entry.getTag())) {
          int unitIdStartPos = syslogStr.indexOf('[') + 1;
          int unitIdEndPos = syslogStr.indexOf("]:");
          entry.setUnitId(syslogStr.substring(unitIdStartPos, unitIdEndPos));
          entry.setContent(syslogStr.substring(unitIdEndPos + 2).trim());
          String content = entry.getContent();
          int triplePipePos = content.lastIndexOf("|||");
          if (triplePipePos > -1) {
            // This message has been redirected from another server - extract original sender IP
            // See how original IP is inserted in SyslogClient.send()
            entry.setHostname(content.substring(triplePipePos + 3));
            content = content.substring(0, triplePipePos);
          }
          int userIdPos = content.indexOf("USER:");
          String userId = null;
          if (userIdPos > -1) {
            userId = content.substring(userIdPos + 5, content.indexOf(' ', userIdPos + 5));
            content =
                content.substring(content.indexOf(' ', userIdPos + 5)); // Content is rest of string
          }

          int fcvPos = content.indexOf("FCV:");
          String facilityVersion = null;
          if (fcvPos > -1) {
            facilityVersion = content.substring(fcvPos + 4, content.indexOf(' ', fcvPos + 4));
            content =
                content.substring(content.indexOf(' ', fcvPos + 4)); // Content is rest of string
          }

          entry.setFacilityVersion(facilityVersion);
          entry.setUserId(userId);
          entry.setContent(content.trim());
          return entry;
        }
      }
      for (Pattern macPattern : deviceIdPatterns) {
        m = macPattern.matcher(syslogStr);
        if (m.find()) {
          entry.setUnitId(m.group(m.groupCount()).toUpperCase());
          entry.setContent(syslogStr.substring(m.end()).trim());
          break;
        }
      }
    } else {
      for (Pattern macPattern : deviceIdPatterns) {
        m = macPattern.matcher(syslogStr.trim());
        if (m.find()) {
          entry.setUnitId(m.group(m.groupCount()).toUpperCase());
          entry.setContent(syslogStr.substring(m.end()).trim());
          break;
        }
      }
    }
    if (entry.getUnitId() == null) {
      handleMissingMAC(entry, packet, syslogStr);
    }
    if (entry.getContent() != null) {
      int triplePipePos = entry.getContent().lastIndexOf("|||");
      if (triplePipePos > -1) {
        // This message has been redirected from another server - extract original sender IP
        // See how original IP is inserted in SyslogClient.send()
        entry.setHostname(entry.getContent().substring(triplePipePos + 3));
        entry.setContent(entry.getContent().substring(0, triplePipePos).trim());
      }
      return entry;
    } else {
      return null;
    }
  }

  public static void pause(boolean newState) {
    pause = newState;
  }

  @Getter
  public static class Syslog2DBCounter {
    private int unknownRedirected;
    private int unknownDiscarded;
    private int unknownAllowed;
    private int unknownAllowedCreated;
    private int knownEventDiscarded;
    private int knownEventDuplicated;
    private int knownEventAllowed;
    private int ok;
    private int failed;
    private int triggerEvent;
    private int scriptExecuted;

    public Syslog2DBCounter() {}

    public Syslog2DBCounter(
        int unknownRedirected,
        int unknownDiscarded,
        int unknownAllowed,
        int unknownAllowedCreated,
        int knownEventDiscarded,
        int knownEventDuplicated,
        int knownEventAllowed,
        int ok,
        int failed,
        int triggerEvent,
        int scriptExecuted) {
      this.unknownRedirected = unknownRedirected;
      this.unknownDiscarded = unknownDiscarded;
      this.unknownAllowed = unknownAllowed;
      this.unknownAllowedCreated = unknownAllowedCreated;
      this.knownEventDiscarded = knownEventDiscarded;
      this.knownEventDuplicated = knownEventDuplicated;
      this.knownEventAllowed = knownEventAllowed;
      this.triggerEvent = triggerEvent;
      this.ok = ok;
      this.failed = failed;
      this.scriptExecuted = scriptExecuted;
    }

    public synchronized void incUknownRedirected() {
      unknownRedirected++;
    }

    public synchronized void incUknownDiscarded() {
      unknownDiscarded++;
    }

    public synchronized void incUknownAllowed() {
      unknownAllowed++;
    }

    public synchronized void incUknownAllowedCreated() {
      unknownAllowedCreated++;
    }

    public synchronized void incKnownEventDiscarded() {
      knownEventDiscarded++;
    }

    public synchronized void incKnownEventDuplicated() {
      knownEventDuplicated++;
    }

    public synchronized void incKnownEventAllowed() {
      knownEventAllowed++;
    }

    public synchronized void incOk() {
      ok++;
    }

    public synchronized void incFailed() {
      failed++;
    }

    public synchronized void incTriggerEvent() {
      triggerEvent++;
    }

    public synchronized Syslog2DBCounter resetCounters() {
      Syslog2DBCounter c =
          new Syslog2DBCounter(
              unknownRedirected,
              unknownDiscarded,
              unknownAllowed,
              unknownAllowedCreated,
              knownEventDiscarded,
              knownEventDuplicated,
              knownEventAllowed,
              ok,
              failed,
              triggerEvent,
              scriptExecuted);
      this.unknownRedirected = 0;
      this.unknownDiscarded = 0;
      this.unknownAllowed = 0;
      this.unknownAllowedCreated = 0;
      this.knownEventDiscarded = 0;
      this.knownEventDuplicated = 0;
      this.knownEventAllowed = 0;
      this.ok = 0;
      this.failed = 0;
      this.triggerEvent = 0;
      this.scriptExecuted = 0;
      return c;
    }

  }
}
