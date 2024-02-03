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
import lombok.Getter;

import java.util.*;
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
      return switch (severityLevel) {
          case SyslogConstants.SEVERITY_EMERGENCY -> TableColor.RED.toString();
          case SyslogConstants.SEVERITY_ALERT, SyslogConstants.SEVERITY_CRITICAL, SyslogConstants.SEVERITY_ERROR ->
                  TableColor.ORANGE_DARK.toString();
          case SyslogConstants.SEVERITY_WARNING -> TableColor.ORANGE_LIGHT.toString();
          case SyslogConstants.SEVERITY_NOTICE, SyslogConstants.SEVERITY_INFO -> TableColor.GREEN.toString();
          default -> TableColor.GRAY.toString();
      };
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
   * @return the font color
   */
  public static String getFontColor() {
      return TableColor.BLACK.toString();
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
      if (entry.getValue().equals(Arrays.toString(s))) {
        return entry.getKey();
      }
      for (String string : s) {
          if (entry.getValue().equals(string)) {
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
  @Getter
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

  }

  /** The Class Facility. */
  @Getter
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
      color = getFontColor();
      colors.put(se, color);
      return new SimpleScalar(color);
    }
  }

  /** The Class GetEventMouseOver. */
  public static class GetEventMouseOver implements TemplateMethodModel {
    /** The texts. */
    Map<String, String> texts = new HashMap<>();

    /** The xaps. */
    private final ACS acs;

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
      SyslogEvent event = ut != null ? ut.getSyslogEvents().getByEventId(eventId) : null;
      if (ut != null && event != null) {
        texts.put(uts + ":" + es, event.toString());
        return new SimpleScalar(event.toString());
      }
      if (eventId == 0) {
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
    StringBuilder queryString = new StringBuilder("?");
    for (String kvp : keyValuePairs) {
      queryString.append(kvp).append("&");
    }
    return reqUrl + queryString.substring(0, queryString.length() - 1);
  }

  /** The Class GetUnittypeProfileByName. */
  public static class GetUnittypeProfileByName implements TemplateMethodModel {
    /** The xaps. */
    private final ACS acs;

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
      if (profileString != null && !profileString.isEmpty() && profileName == null) {
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
    private final ACS acs;

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

        map.put("unittype", Objects.requireNonNullElseGet(unittypeName, () -> "N/A (id: " + unittypeId + ")"));
      return new SimpleObjectWrapper().wrap(map);
    }
  }
}
