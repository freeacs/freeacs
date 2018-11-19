package com.github.freeacs.web;

import static com.github.freeacs.web.app.util.WebConstants.INDEX_URI;

import com.github.freeacs.web.app.page.SupportDashboardPage;
import com.github.freeacs.web.app.page.WebPage;
import com.github.freeacs.web.app.page.event.SyslogEventsPage;
import com.github.freeacs.web.app.page.file.FilePage;
import com.github.freeacs.web.app.page.file.SoftwarePage;
import com.github.freeacs.web.app.page.file.UpgradePage;
import com.github.freeacs.web.app.page.group.GroupPage;
import com.github.freeacs.web.app.page.group.GroupsPage;
import com.github.freeacs.web.app.page.heartbeat.HeartbeatsPage;
import com.github.freeacs.web.app.page.job.JobPage;
import com.github.freeacs.web.app.page.job.JobsPage;
import com.github.freeacs.web.app.page.job.UnitJobPage;
import com.github.freeacs.web.app.page.monitor.MonitorPage;
import com.github.freeacs.web.app.page.permissions.PermissionsPage;
import com.github.freeacs.web.app.page.profile.ProfileCreatePage;
import com.github.freeacs.web.app.page.profile.ProfileDetailsPage;
import com.github.freeacs.web.app.page.profile.ProfileOverviewPage;
import com.github.freeacs.web.app.page.report.ReportPage;
import com.github.freeacs.web.app.page.report.UnitListPage;
import com.github.freeacs.web.app.page.scriptexecution.ScriptExecutionsPage;
import com.github.freeacs.web.app.page.search.SearchPage;
import com.github.freeacs.web.app.page.syslog.SyslogPage;
import com.github.freeacs.web.app.page.trigger.TriggerOverviewPage;
import com.github.freeacs.web.app.page.trigger.TriggerReleaseHistoryPage;
import com.github.freeacs.web.app.page.trigger.TriggerReleasePage;
import com.github.freeacs.web.app.page.unit.InspectionPage;
import com.github.freeacs.web.app.page.unit.UnitPage;
import com.github.freeacs.web.app.page.unit.UnitStatusPage;
import com.github.freeacs.web.app.page.unit.UnitStatusRealTimeMosPage;
import com.github.freeacs.web.app.page.unittype.GetUnitTypeParameterFlagAndValuesPage;
import com.github.freeacs.web.app.page.unittype.UnittypeCreatePage;
import com.github.freeacs.web.app.page.unittype.UnittypeOverviewPage;
import com.github.freeacs.web.app.page.unittype.UnittypePage;
import com.github.freeacs.web.app.page.unittype.UnittypeParametersPage;
import com.github.freeacs.web.app.page.window.WindowPage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * An enumeration of all defined pages in xAPS Web with their request id, page class and icon url.
 *
 * <p>Tightly coupled with mostly all classes and FreeMarker templates.
 *
 * <p>Example:<br>
 * When calling the url <code>"web?page=search"</code> the front controller will execute <code>
 * Main.getWebPage("search")</code> that calls <code>Page.getById("search")</code>, and will get the
 * corresponding <code>WebPage</code> implementation class in return.
 *
 * <p>This enumeration also contains static helper methods.
 *
 * @author Jarl Andre Hubenthal
 */
public enum Page {
  /* The following pages are covered in ContextItem() - important to get the context-navigation to
   * work.
   */

  /** Unittype pages. */
  UNITTYPE("unit-type-configuration", UnittypePage.class, "icon_settings.png"),
  UNITTYPECREATE("unit-type-create", UnittypeCreatePage.class, "icon_settings.png"),
  UNITTYPEOVERVIEW("unit-type-overview", UnittypeOverviewPage.class, "icon_settings.png"),
  UNITTYPEPARAMETERS("parameters", UnittypeParametersPage.class),

  /** Profile pages. */
  PROFILE("profile-configuration", ProfileDetailsPage.class, "icon_profile.png"),
  PROFILECREATE("profile-create", ProfileCreatePage.class, "icon_profile.png"),
  PROFILEOVERVIEW("profile-overview", ProfileOverviewPage.class, "icon_profile.png"),
  WINDOWPROFILE("profilewindow", WindowPage.class),

  /** Unit pages. */
  UNIT("unit-configuration", UnitPage.class, "icon_unit.png"),
  UNITSTATUS("unit-dashboard", UnitStatusPage.class, "icon_unit.png"),
  WINDOWUNIT("unitwindow", WindowPage.class),

  /** Group detail page. */
  GROUP("group", GroupPage.class, "icon_group.png"),

  /** Job detail page. */
  JOB("job", JobPage.class, "icon_job.png"),

  /** Trigger pages. */
  CREATETRIGGER("create-trigger", TriggerOverviewPage.class),
  TRIGGEROVERVIEW("trigger-overview", TriggerOverviewPage.class, "icon_settings.png"),
  TRIGGERRELEASE("trigger-release", TriggerReleasePage.class, "icon_settings.png"),
  TRIGGERRELEASEHISTORY(
      "trigger-release-history", TriggerReleaseHistoryPage.class, "icon_settings.png"),
  /** Misc pages - no profile "level" - or profile in dropdown on page. */
  FILES("files", FilePage.class, "icon_software.png"),
  SOFTWARE("software", SoftwarePage.class, "icon_software.png"),
  JOBSOVERVIEW("job-overview", JobsPage.class, "icon_job.png"),
  UNITJOB("unit-jobs", UnitJobPage.class, "icon_job.png"),
  SYSLOGEVENTS("events", SyslogEventsPage.class, "icon_syslog.png"),
  HEARTBEATS("heartbeats", HeartbeatsPage.class, "icon_syslog.png"),
  UPGRADE("upgrade", UpgradePage.class, "icon_software.png"),
  SCRIPTEXECUTIONS("scriptexecutions", ScriptExecutionsPage.class, "icon_syslog.png"),
  /** Misc pages - with profile "level". */
  GROUPSOVERVIEW("group-overview", GroupsPage.class, "icon_group.png"),
  REPORT("report", ReportPage.class, "icon_report.png"),
  SEARCH("search", SearchPage.class, "icon_search.png"),
  SYSLOG("syslog", SyslogPage.class, "icon_syslog.png"),
  UNITLIST("unit-list", UnitListPage.class, "icon_report.png"),

  /**
   * The following pages are small popup-pages or otherwise pages not part of the standard
   * context-navigation pages (unittype/profile-context is of no concern perhaps). Some of these
   * pages may not even be in use.
   */
  GETVALUE("getvalue", GetUnitTypeParameterFlagAndValuesPage.class),
  INSPECTION("inspection", InspectionPage.class),
  /** Top left menu pages - some are empty pages, not possible to view/click. */
  DASHBOARD_SUPPORT("support-dashboard", SupportDashboardPage.class),
  TOPMENU_EASY("topmenu-easy", null),
  TOPMENU_ADV("topmenu-adv", null),
  TOPMENU_FILESCRIPT("topmenu-filescript", null),
  TOPMENU_REPORT("topmenu-report", null),
  TOPMENU_TRIGEVENT("topmenu-trigevent", null),
  TOPMENU_WIZARDS("topmenu-wizard", null),
  /** Top top menu pages. */
  MONITOR("monitor", MonitorPage.class),
  PERMISSIONS("permissions", PermissionsPage.class),
  REALTIMEMOS("unit-status-realtime-mos", UnitStatusRealTimeMosPage.class),

  NONE(null, null);

  private static Map<String, Page> pageMap;
  private String id;
  private Class<? extends WebPage> clazz;
  private String iconURL;

  public String getIconURL() {
    return iconURL;
  }

  public String getTitle() {
    return getTitle(id);
  }

  public static String getTitle(String id) {
    String[] arr = id.split("-");
    int arrLength = arr.length;
    for (int i = 0; i < arrLength; i++) {
      arr[i] = convertToCamelCasedString(arr[i]);
    }
    return StringUtils.join(arr, " ");
  }

  public static Map<String, Page> getPageURLMap() {
    if (pageMap == null) {
      Map<String, Page> map = new HashMap<>();
      for (Page p : values()) {
        map.put(p.name(), p);
      }
      pageMap = map;
    }
    return pageMap;
  }

  private static String convertToCamelCasedString(String string) {
    String result = "";
    for (int i = 0; i < string.length(); i++) {
      String next = string.substring(i, i + 1);
      if (i == 0) {
        result += next.toUpperCase();
      } else {
        result += next.toLowerCase();
      }
    }
    return result;
  }

  Page(String id, Class<? extends WebPage> clazz) {
    this.id = id != null ? id : "";
    this.clazz = clazz;
  }

  Page(String id, Class<? extends WebPage> clazz, String icon) {
    this(id, clazz);
    this.iconURL = icon;
  }

  public static Page getById(String id) {
    if (id == null) {
      return Page.NONE;
    }
    for (Page p : Page.values()) {
      if (p.getId() != null && p.getId().equals(id)) {
        return p;
      }
    }
    return Page.NONE;
  }

  public String getId() {
    return id;
  }

  public String getUrl() {
    return getUrl(null);
  }

  /**
   * A helper method for generating a url for the current page id.<br>
   * Accepts a String that can contain the extra arguments to be appended to the url.
   *
   * @param params an amp (<code>&</code>) separated list of key=value pairs
   * @return the generated url string
   */
  public String getUrl(String params) {
    return INDEX_URI.substring(1)
        + "?page="
        + getId()
        + (params != null && !"".equals(params) ? "&amp;" + params : "");
  }

  public Class<? extends WebPage> getClazz() {
    return clazz;
  }

  /**
   * A static helper method for retrieving a list of pages that can be assigned to a users access
   * list.
   *
   * @return a list of page ids
   */
  public static List<String> getPermissiblePagesAsString() {
    List<Page> toConvert =
        Arrays.asList(
            SEARCH, UNIT, PROFILE, UNITTYPE, GROUP, JOB, SOFTWARE, SYSLOG, REPORT, MONITOR);
    List<String> pages = new ArrayList<>();
    for (Page p : toConvert) {
      pages.add(p.getId());
    }
    return pages;
  }

  private static Map<String, Page> permissiblePages = new LinkedHashMap<>();

  static {
    permissiblePages.put("support", DASHBOARD_SUPPORT);
    permissiblePages.put("limited-provisioning", TOPMENU_EASY);
    permissiblePages.put("full-provisioning", TOPMENU_ADV);
    permissiblePages.put("report", TOPMENU_REPORT);
    permissiblePages.put("monitor", MONITOR);
  }

  public static Map<String, Page> getPermissiblePageMap() {
    return permissiblePages;
  }

  /**
   * A static helper method for retrieving all pages.
   *
   * @return a list of page ids
   */
  public static List<String> getAllPagesAsString() {
    List<String> pages = new ArrayList<>();
    for (Page p : Page.values()) {
      pages.add(p.getId());
    }
    return pages;
  }

  /**
   * A static helper method for retrieving the parent of a given page. This makes it easier to find
   * the correct selected pages in the menu. Used to highlight the correct menu item.
   */
  public static String getParentPage(String pageStr) {
    Page page = Page.getById(pageStr);
    switch (page) {
      case UNITTYPEOVERVIEW:
      case UNITTYPEPARAMETERS:
      case UNITTYPECREATE:
        return UNITTYPE.getId();

      case PROFILEOVERVIEW:
      case PROFILECREATE:
      case WINDOWPROFILE:
        return PROFILE.getId();

      case WINDOWUNIT:
      case UNITSTATUS:
        return UNIT.getId();

      case GROUPSOVERVIEW:
        return GROUP.getId();
      case JOBSOVERVIEW:
        return JOB.getId();

      case UNITLIST:
        return REPORT.getId();

      default:
        return pageStr;
    }
  }

  public static String getHelpPage(String pageStr) {
    Page page = Page.getById(pageStr);
    switch (page) {
      case UNITTYPEOVERVIEW:
      case UNITTYPEPARAMETERS:
      case UNITTYPECREATE:
        return UNITTYPE.getId();

      case PROFILEOVERVIEW:
      case PROFILECREATE:
      case WINDOWPROFILE:
        return PROFILE.getId();

      case WINDOWUNIT:
      case UNITSTATUS:
        return UNIT.getId();

      case GROUPSOVERVIEW:
        return GROUP.getId();
      case JOBSOVERVIEW:
        return JOB.getId();

      case UPGRADE:
        return SOFTWARE.getId();

      case CREATETRIGGER:
      case TRIGGERRELEASE:
      case TRIGGERRELEASEHISTORY:
        return TRIGGEROVERVIEW.getId();

      case UNITLIST:
        return REPORT.getId();

      default:
        return pageStr;
    }
  }

  /**
   * Take a list of Page enums and convert it to a list of Strings.
   *
   * @param pages a list of Page enums
   * @return a list of strings
   */
  public static List<String> getStringValuesFromList(List<Page> pages) {
    List<String> ids = new ArrayList<>();
    for (Page p : pages) {
      ids.add(p.getId());
    }
    return ids;
  }

  /**
   * Take a list of strings and convert it to a list of Page enums.
   *
   * @param pages A list of Strings
   * @return a list of Page enums
   */
  public static List<Page> getPageValuesFromList(List<String> pages) {
    List<Page> pageList = new ArrayList<>();
    for (String p : pages) {
      Page _p = getPermissiblePageMap().get(p);
      if (_p != null || ((_p = Page.getById(p)) != null)) {
        pageList.add(_p);
      }
    }
    return pageList;
  }

  /**
   * This method is crucial for indirect use in templates.
   *
   * <p>Should not be removed or changed without explicitly changing all templates.
   *
   * @return the page url
   */
  public String toString() {
    return getUrl();
  }

  /**
   * What this method does is to add the required pages needed by the pages in the provided list.
   *
   * @param list the liist of Page enums
   */
  public static void addRequiredPages(List<Page> list) {
    if (list.contains(TOPMENU_ADV)) {
      list.add(CREATETRIGGER);
      list.add(TRIGGEROVERVIEW);
      list.add(TRIGGERRELEASE);
      list.add(TRIGGERRELEASEHISTORY);
      list.add(HEARTBEATS);
      list.add(SCRIPTEXECUTIONS);
      list.add(UNITTYPE);
      list.add(UNITTYPEPARAMETERS);
      list.add(UNITTYPEOVERVIEW);
      list.add(UNITTYPECREATE);
      list.add(SYSLOGEVENTS);
      list.add(GETVALUE);
      list.add(UPGRADE);
      list.add(FILES);
      list.add(SOFTWARE);
      list.add(GROUP);
      list.add(GROUPSOVERVIEW);
      list.add(JOB);
      list.add(UNITJOB);
      list.add(JOBSOVERVIEW);
      list.add(WINDOWPROFILE);
      list.add(PROFILEOVERVIEW);
      list.add(PROFILECREATE);
      list.add(PROFILE);
      list.add(SYSLOG);
      list.add(SEARCH);
      list.add(UNIT);
      list.add(REALTIMEMOS);
      list.add(INSPECTION);
      list.add(UNITSTATUS);
      list.add(WINDOWUNIT);
      list.add(TOPMENU_WIZARDS);
      list.add(TOPMENU_EASY);
      list.add(TOPMENU_FILESCRIPT);
      list.add(TOPMENU_TRIGEVENT);
    } else if (list.contains(TOPMENU_EASY)) {
      list.add(WINDOWPROFILE);
      list.add(PROFILEOVERVIEW);
      list.add(PROFILECREATE);
      list.add(PROFILE);
      list.add(SYSLOG);
      list.add(SEARCH);
      list.add(UNIT);
      list.add(REALTIMEMOS);
      list.add(INSPECTION);
      list.add(UNITSTATUS);
      list.add(WINDOWUNIT);
      list.add(TOPMENU_WIZARDS);
    } else if (list.contains(DASHBOARD_SUPPORT)) {
      list.add(SYSLOG);
      list.add(SEARCH);
      list.add(UNIT);
      list.add(REALTIMEMOS);
      list.add(INSPECTION);
      list.add(UNITSTATUS);
      list.add(WINDOWUNIT);
      list.add(UPGRADE);
      list.add(TOPMENU_WIZARDS);
    }

    if (list.contains(TOPMENU_REPORT)) {
      list.add(REPORT);
      list.add(UNITLIST);
    }
  }

  /**
   * Equals any.
   *
   * @param pages the pages
   * @return true, if successful
   */
  public boolean equalsAny(Page... pages) {
    for (Page p : pages) {
      if (equals(p)) {
        return true;
      }
    }
    return false;
  }
}
