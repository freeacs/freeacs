package com.github.freeacs.web.app.page.syslog;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.SyslogEvent;
import com.github.freeacs.dbi.SyslogEvents;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.table.TableColor;
import com.github.freeacs.web.app.util.DateUtils;
import freemarker.template.SimpleObjectWrapper;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;

/** The Class SyslogUtil. */
@SuppressWarnings({"rawtypes"})
public class SyslogUtil {

  /**
   * Gets the background color.
   *
   * @param severityLevel the severity level
   * @return the background color
   */
  public static String getBackgroundColor(Integer severityLevel) {
    switch (severityLevel) {
      case SyslogConstants.SEVERITY_EMERGENCY:
        return TableColor.RED.toString();
      case SyslogConstants.SEVERITY_ALERT:
      case SyslogConstants.SEVERITY_CRITICAL:
      case SyslogConstants.SEVERITY_ERROR:
        return TableColor.ORANGE_DARK.toString();
      case SyslogConstants.SEVERITY_WARNING:
        return TableColor.ORANGE_LIGHT.toString();
      case SyslogConstants.SEVERITY_NOTICE:
      case SyslogConstants.SEVERITY_INFO:
        return TableColor.GREEN.toString();
      default:
        return TableColor.GRAY.toString();
    }
  }

  /**
   * Gets the date string.
   *
   * @return the date string
   */
  public static String getDateString() {
    Date toFormat = getDate();
    return DateUtils.formatDateDefault(toFormat);
  }

  /**
   * Gets the font color.
   *
   * @param severityLevel the severity level
   * @return the font color
   */
  public static String getFontColor(Integer severityLevel) {
    switch (severityLevel) {
      case SyslogConstants.SEVERITY_EMERGENCY:
      default:
        return TableColor.BLACK.toString();
    }
  }

  /**
   * Translate severity level.
   *
   * @param s the s
   * @return the object
   */
  public static Object translateSeverityLevel(String[] s) {
    List<Integer> arr = new ArrayList<>();
    for (Entry<Integer, String> entry : SyslogConstants.severityMap.entrySet()) {
      if (entry.getValue().equals(s.toString())) {
        return entry.getKey();
      }
      for (int i = 0; i < s.length; i++) {
        if (entry.getValue().equals(s[i])) {
          arr.add(entry.getKey());
        }
      }
    }
    if (arr.isEmpty()) {
      return null;
    }
    return arr.toArray(new Integer[] {});
  }

  /**
   * Convert to int.
   *
   * @param s the s
   * @return the integer
   */
  public static Integer convertToInt(String s) {
    if (s == null) {
      return null;
    }
    try {
      return Integer.parseInt(s);
    } catch (Throwable t) {
      throw new IllegalArgumentException("Argument " + s + " cannot be converted to a number.");
    }
  }

  /**
   * Convert to tms.
   *
   * @param string the string
   * @return the date
   */
  public static Date convertToTms(String string) {
    try {
      String s = string;
      if (s == null) {
        return null;
      }
      Calendar c = Calendar.getInstance();
      int year = c.get(Calendar.YEAR);
      if (s.startsWith("2")) {
        year = convertToInt(s.substring(0, 4));
        s = s.substring(4);
      }
      if (s.length() >= 4) {
        int month = convertToInt(s.substring(0, 2)) - 1;
        int date = convertToInt(s.substring(2, 4));
        c.set(year, month, date);
      }
      if (s.length() == 8) {
        int hour = convertToInt(s.substring(4, 6));
        int minute = convertToInt(s.substring(6, 8));
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
      } else {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
      }
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c.getTime();
    } catch (Throwable t) {
      throw new IllegalArgumentException(
          "The argument " + string + " cannot be converted to a date");
    }
  }

  /**
   * Gets the date.
   *
   * @return the date
   */
  public static Date getDate() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -1);
    return cal.getTime();
  }

  /** The Class Event. */
  public static class Event {
    /** The key. */
    private final String key;

    /** The value. */
    private final String value;

    /**
     * Instantiates a new event.
     *
     * @param key the key
     * @param value the value
     */
    public Event(String key, String value) {
      this.key = key;
      this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
      return value;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
      return key;
    }
  }

  /** The Class Facility. */
  public static class Facility {
    /** The key. */
    private final String key;

    /** The value. */
    private final String value;

    /**
     * Instantiates a new facility.
     *
     * @param key the key
     * @param value the value
     */
    public Facility(String key, String value) {
      this.key = key;
      this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
      return value;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
      return key;
    }
  }

  /**
   * Gets the event.
   *
   * @param unittype the unittype
   * @param id the id
   * @return the event
   */
  public static Event getEvent(Unittype unittype, Integer id) {
    if (unittype == null && id != null) {
      return convertSyslogEvent(SyslogEvents.getById(id));
    }
    if (id == null) {
      return null;
    }
    if (id == 0) {
      return new Event("0", "Default");
    }
    return convertSyslogEvent(unittype.getSyslogEvents().getByEventId(id));
  }

  /**
   * Gets the events.
   *
   * @param unittype the unittype
   * @return the events
   */
  public static List<Event> getEvents(Unittype unittype) {
    List<Event> list = new ArrayList<>();
    if (unittype == null) {
      list.add(new Event("0", "Default"));
    } else {
      SyslogEvent[] entries = unittype.getSyslogEvents().getSyslogEvents();
      for (SyslogEvent event : entries) {
        list.add(convertSyslogEvent(event));
      }
    }
    return list;
  }

  /**
   * Convert syslog event.
   *
   * @param event the event
   * @return the event
   */
  private static Event convertSyslogEvent(SyslogEvent event) {
    return new Event(event.getEventId().toString(), event.getName());
  }

  /**
   * Gets the facility.
   *
   * @param id the id
   * @return the facility
   */
  public static Facility getFacility(Integer id) {
    if (id != null) {
      return new Facility(id.toString(), SyslogConstants.facilityMap.get(id));
    }
    return null;
  }

  /**
   * Gets the facilities.
   *
   * @return the facilities
   */
  public static List<Facility> getFacilities() {
    List<Facility> list = new ArrayList<>();
    for (Entry<Integer, String> facility : SyslogConstants.facilityMap.entrySet()) {
      list.add(new Facility(facility.getKey().toString(), facility.getValue()));
    }
    return list;
  }

  /**
   * Gets the severity.
   *
   * @param args the args
   * @return the severity
   */
  public static String getSeverity(String args) {
    Integer severityId = Integer.parseInt((String) args);
    Map severityMap = SyslogConstants.severityMap;
    return (String) severityMap.get(severityId);
  }

  /**
   * Gets the severity.
   *
   * @param args the args
   * @return the severity
   */
  public static String getSeverity(Integer args) {
    Map severityMap = SyslogConstants.severityMap;
    return (String) severityMap.get(args);
  }

  /** The Class GetSeverityText. */
  public static class GetSeverityText implements TemplateMethodModel {
    public String exec(List args) throws TemplateModelException {
      if (args.isEmpty()) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      return getSeverity((String) args.get(0));
    }
  }

  /**
   * Gets the facility.
   *
   * @param arg the arg
   * @return the facility
   */
  private static String getFacility(String arg) {
    Integer facilityId = Integer.parseInt((String) arg);
    Map facilityMap = SyslogConstants.facilityMap;
    return (String) facilityMap.get(facilityId);
  }

  /** The Class GetFacilityText. */
  public static class GetFacilityText implements TemplateMethodModel {
    public String exec(List args) throws TemplateModelException {
      if (args.isEmpty()) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      return getFacility((String) args.get(0));
    }
  }

  /** The Class GetBackgroundColor. */
  public static class GetBackgroundColor implements TemplateMethodModel {
    /** The colors. */
    Map<String, String> colors = new HashMap<>();

    public TemplateModel exec(List args) throws TemplateModelException {
      if (args.isEmpty()) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      String color = colors.get(args.get(0));
      if (color != null) {
        return new SimpleScalar(color);
      }
      String se = (String) args.get(0);
      Integer severity = Integer.parseInt(se);
      color = getBackgroundColor(severity);
      colors.put(se, color);
      return new SimpleScalar(color);
    }
  }

  /** The Class GetFontColor. */
  public static class GetFontColor implements TemplateMethodModel {
    /** The colors. */
    Map<String, String> colors = new HashMap<>();

    public TemplateModel exec(List args) throws TemplateModelException {
      if (args.isEmpty()) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      String color = colors.get(args.get(0));
      if (color != null) {
        return new SimpleScalar(color);
      }
      String se = (String) args.get(0);
      Integer severity = Integer.parseInt(se);
      color = getFontColor(severity);
      colors.put(se, color);
      return new SimpleScalar(color);
    }
  }

  /** The Class GetEventMouseOver. */
  public static class GetEventMouseOver implements TemplateMethodModel {
    /** The texts. */
    Map<String, String> texts = new HashMap<>();

    /** The xaps. */
    private ACS acs;

    /**
     * Instantiates a new gets the event mouse over.
     *
     * @param acs the xaps
     */
    public GetEventMouseOver(ACS acs) {
      this.acs = acs;
    }

    public TemplateModel exec(List args) throws TemplateModelException {
      if (args.isEmpty()) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      String uts = (String) args.get(0);
      String es = (String) args.get(1);
      String text = texts.get(uts + ":" + es);
      if (text != null) {
        return new SimpleScalar(text);
      }
      Integer unittypeId = !"".equals(uts) ? Integer.parseInt(uts) : null;
      Integer eventId = Integer.parseInt(es);
      Unittype ut = unittypeId != null ? acs.getUnittype(unittypeId) : null;
      SyslogEvent event =
          ut != null && eventId != null ? ut.getSyslogEvents().getByEventId(eventId) : null;
      if (ut != null && event != null) {
        texts.put(uts + ":" + es, event.toString());
        return new SimpleScalar(event.toString());
      }
      if (eventId != null && eventId == 0) {
        return new SimpleScalar(SyslogEvents.getById(eventId).getName());
      }
      return new SimpleScalar("n/a");
    }
  }

  /**
   * Gets the url.
   *
   * @param req the req
   * @param keyValuePairs the key value pairs
   * @return the url
   */
  public static String getUrl(HttpServletRequest req, String... keyValuePairs) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = "?";
    for (String kvp : keyValuePairs) {
      queryString += kvp + "&";
    }
    return reqUrl + queryString.substring(0, queryString.length() - 1);
  }

  /** The Class GetUnittypeProfileByName. */
  public static class GetUnittypeProfileByName implements TemplateMethodModel {
    /** The xaps. */
    private ACS acs;

    /**
     * Instantiates a new gets the unittype profile by name.
     *
     * @param acs the xaps
     */
    public GetUnittypeProfileByName(ACS acs) {
      this.acs = acs;
    }

    public TemplateModel exec(List args) throws TemplateModelException {
      if (args.isEmpty()) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      Map<String, String> map = new HashMap<>();
      String unittypeString = (String) args.get(0);
      String profileString = (String) args.get(1);
      Unittype entryUnittype = acs.getUnittype(unittypeString);
      Profile entryProfile =
          entryUnittype != null && profileString != null
              ? entryUnittype.getProfiles().getByName(profileString)
              : null;
      String unittypeName = entryUnittype != null ? entryUnittype.getName() : null;
      String profileName = entryProfile != null ? entryProfile.getName() : null;
      if (profileString != null && !"".equals(profileString) && profileName == null) {
        map.put("profile", "N/A (id: " + profileString + ")");
      } else {
        map.put("profile", profileName);
      }
      if (unittypeString != null && unittypeName == null) {
        map.put("unittype", "N/A (id: " + unittypeString + ")");
      } else {
        map.put("unittype", unittypeName);
      }
      return new SimpleObjectWrapper().wrap(map);
    }
  }

  /** The Class GetUnittypeProfileById. */
  public static class GetUnittypeProfileById implements TemplateMethodModel {
    /** The xaps. */
    private ACS acs;

    /**
     * Instantiates a new gets the unittype profile by id.
     *
     * @param acs the xaps
     */
    public GetUnittypeProfileById(ACS acs) {
      this.acs = acs;
    }

    public TemplateModel exec(List args) throws TemplateModelException {
      if (args.size() < 2) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      Map<String, String> map = new HashMap<>();
      Integer unittypeId = Integer.parseInt((String) args.get(0));
      Integer profileId =
          !"".equals((String) args.get(1)) ? Integer.parseInt((String) args.get(1)) : null;
      Unittype entryUnittype = acs.getUnittype(unittypeId);
      Profile entryProfile =
          entryUnittype != null && profileId != null
              ? entryUnittype.getProfiles().getById(profileId)
              : null;
      String unittypeName = entryUnittype != null ? entryUnittype.getName() : null;
      String profileName = entryProfile != null ? entryProfile.getName() : null;
      if (profileId != null && !"".equals((String) args.get(1)) && profileName == null) {
        map.put("profile", "N/A (id: " + profileId + ")");
      } else {
        map.put("profile", profileName);
      }

      if (unittypeId != null && unittypeName == null) {
        map.put("unittype", "N/A (id: " + unittypeId + ")");
      } else {
        map.put("unittype", unittypeName);
      }
      return new SimpleObjectWrapper().wrap(map);
    }
  }
}
