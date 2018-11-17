package com.github.freeacs.web.app.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.table.TableColor;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.ResourceHandler;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.TimeFormatter;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.security.AllowedUnittype;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * The simple {@link WebPage} implementation.<br>
 * The only required method to implement when extending this class is the process method in {@link
 * WebPage}<br>
 * Contains convenient methods that is used by (mostly) all pages.
 *
 * @author Jarl Andre Hubenthal
 */
public abstract class AbstractWebPage implements WebPage {
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected class ValueHolder {
    public String value;

    public ValueHolder(String value) {
      this.value = value;
    }
  }

  /** The page processed. */
  private boolean pageProcessed;

  @Override
  public boolean isPageProcessed() {
    return pageProcessed;
  }

  @Override
  public String getTitle(String page) {
    return ResourceHandler.getProperties().getString("TITLE_DESCRIPTION")
        + (page != null ? " | " + Page.getTitle(page) : "");
  }

  @Override
  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>();
    if (StringUtils.isNotEmpty(sessionData.getUnittypeName())) {
      list.add(
          new MenuItem("Go to Unit Type", Page.UNITTYPE)
              .addParameter("unittype", sessionData.getUnittypeName()));
      if (StringUtils.isNotEmpty(sessionData.getProfileName())) {
        list.add(
            new MenuItem("Go to Profile", Page.PROFILE)
                .addParameter("unittype", sessionData.getUnittypeName())
                .addParameter("profile", sessionData.getProfileName()));
        if (StringUtils.isNotEmpty(sessionData.getUnitId())) {
          list.add(
              new MenuItem("Go to Unit", Page.UNITSTATUS)
                  .addParameter("unittype", sessionData.getUnittypeName())
                  .addParameter("profile", sessionData.getProfileName())
                  .addParameter("unit", sessionData.getUnitId()));
        }
      }
    }
    return list;
  }

  @Override
  public boolean useWrapping() {
    return false;
  }

  @Override
  public boolean requiresNoCache() {
    return false;
  }

  /** The Class NotLoggedInException. */
  @SuppressWarnings("serial")
  public static class NotLoggedInException extends IllegalAccessException {}

  /** The Class UnitNotFoundException. */
  @SuppressWarnings("serial")
  public static class UnitNotFoundException extends IllegalAccessException {}

  /**
   * Checks if is profiles limited.
   *
   * @param unittype the unittype
   * @param sessionId the session id
   * @param xapsDataSource
   * @param syslogDataSource
   * @return true, if is profiles limited the no available connection exception
   * @throws SQLException the sQL exception
   */
  public boolean isProfilesLimited(
      Unittype unittype, String sessionId, DataSource xapsDataSource, DataSource syslogDataSource)
      throws SQLException {
    if (unittype == null) {
      return false;
    }
    List<Profile> list = getAllowedProfiles(sessionId, unittype, xapsDataSource, syslogDataSource);
    return list.size() != unittype.getProfiles().getProfiles().length;
  }

  /**
   * Removes the from start.
   *
   * @param string the string
   * @param match the match
   * @return the string
   */
  public String removeFromStart(String string, char match) {
    char[] charArr = string.toCharArray();
    int matchCount = 0;
    for (char c : charArr) {
      if (c == match) {
        matchCount++;
      } else {
        break;
      }
    }
    return string.substring(matchCount);
  }

  /**
   * Gets the time elapsed.
   *
   * @param start the start
   * @param msg the msg
   * @return the time elapsed
   */
  public static String getTimeElapsed(long start, String msg) {
    if (msg == null) {
      return null;
    }
    long estimatedTime = System.nanoTime() - start;
    long ms = estimatedTime / 1000000L;
    StringBuilder sb = new StringBuilder();
    Formatter formatter = new Formatter(sb, Locale.US);
    return formatter.format("%s in %d ms", msg, ms).toString();
  }

  /**
   * Log time elapsed.
   *
   * @param start the start
   * @param message the message
   * @param logger the logger
   */
  public static void logTimeElapsed(long start, String message, Logger logger) {
    String toLog = getTimeElapsed(start, message);
    if (toLog != null) {
      logger.debug(toLog);
    }
  }

  /**
   * This class is used by some pages. It is used like this:
   *
   * <p>rootMap.put("indexof",new LastIndefOfMethod());
   *
   * <p>And is called from the template like this:
   *
   * <p>${indexof(text,tofind)}
   *
   * @author Jarl Andre Hubenthal
   */
  public static class LastIndexOfMethod implements TemplateMethodModel {
    @SuppressWarnings("rawtypes")
    public TemplateModel exec(List args) throws TemplateModelException {
      if (args.size() != 2) {
        throw new TemplateModelException("Wrong arguments");
      }

      String text = (String) args.get(0);
      String toFind = (String) args.get(1);

      if (".".equals(toFind)) {
        toFind = "\\.";
      }

      String[] arr = text.split(toFind);

      String result = arr.length > 1 ? arr[arr.length - 1] : arr[0];

      return new SimpleScalar(result);
    }
  }

  /** The Class GetParameterValue. */
  public static class GetParameterValue implements TemplateMethodModel {
    /** The xaps unit. */
    private ACSUnit acsUnit;

    /** Instantiates a new gets the parameter value. */
    public GetParameterValue(ACSUnit acsUnit) {
      this.acsUnit = acsUnit;
    }

    /** The units. */
    Map<String, Unit> units = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public TemplateModel exec(List args) throws TemplateModelException {
      if (args.size() < 2) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      String id = (String) args.get(0);
      String name = (String) args.get(1);
      Unit unit = units.get(id);
      if (unit == null) {
        try {
          unit = acsUnit.getUnitById(id);
          units.put(id, unit);
        } catch (SQLException e) {
          throw new TemplateModelException("Error: " + e.getLocalizedMessage());
        }
      }
      String up = unit.getParameters().get(name);
      if (up != null && up.trim().isEmpty() && unit.getUnitParameters().get(name) != null) {
        return new SimpleScalar(
            "<span class=\"requiresTitlePopup\" title=\"The unit has overridden the profile value with a blank string\">[blank&nbsp;unit&nbsp;parameter]</span>");
      }
      return new SimpleScalar(up);
    }
  }

  /** The Class RowBackgroundColorMethod. */
  public static class RowBackgroundColorMethod implements TemplateMethodModel {
    /** The GOOD. */
    private String GOOD = TableColor.GREEN.toString();

    /** The MEDIUM. */
    private String MEDIUM = TableColor.ORANGE_LIGHT.toString();

    /** The CRITICAL. */
    private String CRITICAL = TableColor.RED.toString();

    @SuppressWarnings("rawtypes")
    public String exec(List arg0) throws TemplateModelException {
      return getStyle(arg0, "background-color:#%s;");
    }

    /**
     * Gets the font color.
     *
     * @param totalScore the total score
     * @return the font color
     * @throws TemplateModelException the template model exception
     */
    public String getFontColor(Float totalScore) throws TemplateModelException {
      GOOD = "green";
      MEDIUM = "orange";
      CRITICAL = "red";
      return getStyle(Arrays.asList(totalScore != null ? totalScore.toString() : ""), "color:%s;");
    }

    /**
     * Gets the style.
     *
     * @param arg0 the arg0
     * @param toFormat the to format
     * @return the style
     * @throws TemplateModelException the template model exception
     */
    private String getStyle(List<?> arg0, String toFormat) throws TemplateModelException {
      if (arg0.isEmpty()) {
        throw new TemplateModelException("Specify total");
      }
      String totalString = (String) arg0.get(0);
      Float total = Float.parseFloat(totalString);
      if (total > 80) {
        toFormat = String.format(toFormat, GOOD);
      } else if (total >= 70) {
        toFormat = String.format(toFormat, MEDIUM);
      } else if (total < 70) {
        toFormat = String.format(toFormat, CRITICAL);
      }
      return toFormat;
    }
  }

  /** The Class DivideBy. */
  public static class DivideBy implements TemplateMethodModel {
    @SuppressWarnings("rawtypes")
    public Float exec(List arg) throws TemplateModelException {
      if (arg.size() < 2) {
        throw new TemplateModelException("Specify the number and the dividend");
      }
      String number = (String) arg.get(0);
      String dividend = (String) arg.get(1);
      if (isNumber(number) && isNumber(dividend)) {
        return Float.parseFloat(number) / Float.parseFloat(dividend);
      }
      return (float) -1;
    }
  }

  /** The Class FriendlyTimeRepresentationMethod. */
  public static class FriendlyTimeRepresentationMethod implements TemplateMethodModel {
    @SuppressWarnings("rawtypes")
    public String exec(List arg) throws TemplateModelException {
      if (arg.isEmpty()) {
        throw new TemplateModelException("Wrong number of arguments given. Seconds needed.");
      }
      if (isNumber((String) arg.get(0))) {
        int milliseconds = Integer.parseInt((String) arg.get(0)) * 1000;
        return TimeFormatter.convertMs2HourMinSecString(milliseconds);
      } else {
        throw new TemplateModelException(arg.get(0) + " is not a number");
      }
    }
  }

  /**
   * Checks if is valid string.
   *
   * @param s the s
   * @return true, if is valid string
   */
  public boolean isValidString(String s) {
    return s != null && !(s = s.trim()).isEmpty() && !WebConstants.ALL_ITEMS_OR_DEFAULT.equals(s);
  }

  /**
   * Checks if is number.
   *
   * @param string the string
   * @return true, if is number
   */
  public static boolean isNumber(String string) {
    return string != null && Pattern.matches("[0-9]+", string);
  }

  /**
   * Strip spaces replace with under score.
   *
   * @param name the name
   * @return the string
   */
  public String stripSpacesReplaceWithUnderScore(String name) {
    return name.replaceAll(" ", "_");
  }

  /**
   * Checks if is unittypes limited.
   *
   * @param sessionId the session id
   * @param xapsDataSource
   * @param syslogDataSource
   * @return true, if is unittypes limited the no available connection exception
   * @throws SQLException the sQL exception
   */
  public static boolean isUnittypesLimited(
      String sessionId, DataSource xapsDataSource, DataSource syslogDataSource)
      throws SQLException {
    List<Unittype> list = getAllowedUnittypes(sessionId, xapsDataSource, syslogDataSource);
    ACS acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    return list.size() != acs.getUnittypes().getUnittypes().length;
  }

  /**
   * Gets the allowed unittypes.
   *
   * @param sessionId the session id
   * @param xapsDataSource
   * @param syslogDataSource
   * @return the allowed unittypes the no available connection exception
   * @throws SQLException the sQL exception
   */
  public static List<Unittype> getAllowedUnittypes(
      String sessionId, DataSource xapsDataSource, DataSource syslogDataSource)
      throws SQLException {
    ACS acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    SessionData sessionData = SessionCache.getSessionData(sessionId);
    List<Unittype> unittypesList = null;
    if (sessionData.getFilteredUnittypes() != null) {
      List<Unittype> unittypes = new ArrayList<>();
      Unittype[] xAPSUnittypes = acs.getUnittypes().getUnittypes();

      if (sessionData.getFilteredUnittypes().length == 1
          && sessionData.getFilteredUnittypes()[0].getName() != null
          && "*".equals(sessionData.getFilteredUnittypes()[0].getName())) {
        return unittypesList = Arrays.asList(xAPSUnittypes);
      }

      for (Unittype xAPSUnittype : xAPSUnittypes) {
        for (AllowedUnittype ut : sessionData.getFilteredUnittypes()) {
          if (ut.getName() != null && ut.getName().trim().equals(xAPSUnittype.getName())) {
            unittypes.add(xAPSUnittype);
          } else if (ut.getId() != null) {
            Unittype unittype = acs.getUnittype(ut.getId());
            if (unittype != null
                && unittype.getName().equals(xAPSUnittype.getName())
                && !unittypes.contains(xAPSUnittype)) {
              unittypes.add(xAPSUnittype);
            }
          }
        }
      }
      unittypesList = unittypes;
    }
    return unittypesList;
  }

  /**
   * Gets the allowed profiles.
   *
   * @param sessionId the session id
   * @param unittype the unittype
   * @param xapsDataSource
   * @param syslogDataSource
   * @return the allowed profiles the no available connection exception
   * @throws SQLException the sQL exception
   */
  public static List<Profile> getAllowedProfiles(
      String sessionId, Unittype unittype, DataSource xapsDataSource, DataSource syslogDataSource)
      throws SQLException {
    if (unittype == null) {
      return getAllAllowedProfiles(sessionId, xapsDataSource, syslogDataSource);
    }

    SessionData sessionData = SessionCache.getSessionData(sessionId);

    Profile[] allProfiles = unittype.getProfiles().getProfiles();

    List<Profile> profilesList = null;
    if (sessionData.getFilteredUnittypes() != null) {
      List<Profile> profiles = new ArrayList<>();

      for (AllowedUnittype ut : sessionData.getFilteredUnittypes()) {
        if ((ut.getId() != null && ut.getId().intValue() != unittype.getId().intValue())
            || (ut.getName() != null && !ut.getName().trim().equals(unittype.getName()))) {
          continue;
        } else {
          if (ut.getProfile() == null) {
            continue;
          }
          for (Profile profile : allProfiles) {
            if ((ut.getProfile().getId() != null
                    && ut.getProfile().getId().intValue() == profile.getId().intValue()
                    && !profiles.contains(profile))
                || (ut.getProfile().getName() != null
                    && ut.getProfile().getName().trim().equals(profile.getName())
                    && !profiles.contains(profile))) {
              profiles.add(profile);
              break;
            }
          }
        }
      }
      profilesList = profiles;
    } else {
      profilesList = Arrays.asList(unittype.getProfiles().getProfiles());
    }

    if (profilesList.isEmpty()) {
      return Arrays.asList(allProfiles);
    }
    return profilesList;
  }

  /**
   * Gets the all allowed profiles.
   *
   * @param sessionId the session id
   * @param xapsDataSource
   * @param syslogDataSource
   * @return the all allowed profiles the no available connection exception
   * @throws SQLException the sQL exception
   */
  protected static List<Profile> getAllAllowedProfiles(
      String sessionId, DataSource xapsDataSource, DataSource syslogDataSource)
      throws SQLException {
    List<Unittype> unittypes = getAllowedUnittypes(sessionId, xapsDataSource, syslogDataSource);
    List<Profile> filteredProfiles = new ArrayList<>();
    for (Unittype unittype : unittypes) {
      List<Profile> profiles =
          getAllowedProfiles(sessionId, unittype, xapsDataSource, syslogDataSource);
      if (profiles != null) {
        filteredProfiles.addAll(profiles);
      }
    }
    return filteredProfiles;
  }
}
