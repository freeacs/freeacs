package com.github.freeacs.web.app.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.page.report.ReportType;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebProperties;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A menu servlet that generates different types of response based a type parameter.
 *
 * @author Jarl Andre Hubenthal
 */
public class MenuServlet extends HttpServlet {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1057185634997595190L;

  /** The template config. */
  private Configuration templateConfig;

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(MenuServlet.class);

  public MenuServlet(Configuration templateConfig) {
    this.templateConfig = templateConfig;
  }

  @Override
  @Deprecated
  protected void doGet(HttpServletRequest req, HttpServletResponse res) {
    String type = req.getParameter("type");
    if (type == null) {
      type = "html";
    }
    try {
      if ("json".equals(type)) {
        List<MenuItem> mainMenu = getMainMenu(req);
        String json = OBJECT_MAPPER.writeValueAsString(mainMenu);
        res.setContentType("application/json");
        res.getWriter().println(json);
        res.getWriter().close();
      } else if ("html".equals(type)) {
        String html = getMenuHtml(req);
        res.setContentType("text/html");
        res.getWriter().println(html);
        res.getWriter().close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("An error occured", e);
    }
  }

  /**
   * Gets the menu html.
   *
   * @param req the req
   * @return the menu html
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String getMenuHtml(HttpServletRequest req) throws TemplateException, IOException {
    List<MenuItem> mainMenu = getMainMenu(req);
    Template template = templateConfig.getTemplate("MenuTemplate.ftl");
    Map<String, Object> map = new HashMap<>();
    map.put("list", mainMenu);
    return Freemarker.parseTemplate(map, template);
  }

  /**
   * Gets the main menu.
   *
   * @param req the req
   * @return the main menu
   */
  private List<MenuItem> getMainMenu(HttpServletRequest req) {
    String page = req.getParameter("page");

    Page selectedMenuPage = Page.getById(Page.getParentPage(page));

    return getMenuItems(req.getSession().getId(), page, selectedMenuPage);
  }

  /**
   * Gets the pages allowed.
   *
   * @param sessionId the session id
   * @return the pages allowed
   */
  private List<String> getPagesAllowed(String sessionId) {
    SessionData sessionData = SessionCache.getSessionData(sessionId);
    if (sessionData.getUser() != null
        && !sessionData.getUser().getAccess().equals(Users.ACCESS_ADMIN)) {
      return sessionData.getUser().getAllowedPages(sessionId);
    } else if (sessionData.getUser() == null
        || (sessionData.getUser() != null
            && sessionData.getUser().getAccess().equals(Users.ACCESS_ADMIN))) {
      return Page.getAllPagesAsString();
    }
    return null;
  }

  /**
   * Gets the menu items.
   *
   * @param sessionId the session id
   * @param currentPageId the current page id
   * @param selectedPage the selected page
   * @return the menu items
   */
  public List<MenuItem> getMenuItems(String sessionId, String currentPageId, Page selectedPage) {
    List<Page> allowedPages = Page.getPageValuesFromList(getPagesAllowed(sessionId));
    return createMenuItems(allowedPages, selectedPage, sessionId);
  }

  /**
   * Gets the tools menu.
   *
   * <p>Morten: Is there a need to know "currentPage"?
   *
   * @param sessionId the session id
   * @param currentPage the current page
   * @return the tools menu
   */
  @SuppressWarnings("serial")
  public List<MenuItem> getToolsMenu(final String sessionId, final String currentPage) {
    final List<Page> _pages = Page.getPageValuesFromList(getPagesAllowed(sessionId));
    return new ArrayList<MenuItem>() {
      {
        if (_pages.contains(Page.PERMISSIONS)) {
          add(
              new MenuItem("Permissions", Page.PERMISSIONS)
                  .setSelected(Page.PERMISSIONS.equals(currentPage)));
        }
        if (_pages.contains(Page.MONITOR)) {
          add(new MenuItem("Monitor", Page.MONITOR).setSelected(Page.MONITOR.equals(currentPage)));
        }
      }
    };
  }

  /**
   * Creates the menu items new standard.
   *
   * @param allowedPages the pages
   * @param selectedPage the selected page
   * @param sessionId the session id
   * @return the list
   */
  private List<MenuItem> createMenuItems(
      List<Page> allowedPages, Page selectedPage, String sessionId) {
    List<MenuItem> menu = new ArrayList<>();
    if (allowedPages.contains(Page.DASHBOARD_SUPPORT)) {
      MenuItem support =
          new MenuItem("Support", Page.DASHBOARD_SUPPORT)
              .setSelected(
                  selectedPage.equalsAny(Page.SEARCH, Page.DASHBOARD_SUPPORT, Page.SYSLOG));
      if (allowedPages.contains(Page.SEARCH)) {
        support.addSubMenuItem(new MenuItem("Search", Page.SEARCH));
      }
      if (allowedPages.contains(Page.SYSLOG)) {
        support.addSubMenuItem(new MenuItem("Syslog", Page.SYSLOG));
      }
      support.addSubMenuItem(new MenuItem("Forum support", "http://freeacsforum.freeforums.org/"));
      menu.add(support);
    }
    if (allowedPages.contains(Page.TOPMENU_EASY)) {
      MenuItem simpleProv =
          new MenuItem("Easy Provisioning", Page.TOPMENU_EASY)
              .setSelected(selectedPage.equalsAny(Page.UNITTYPE, Page.PROFILE, Page.UNIT))
              .setDisableOnClickWithJavaScript();
      if (allowedPages.contains(Page.UNITTYPE)) {
        simpleProv.addSubMenuItem(
            new MenuItem("Unit Type", Page.UNITTYPEOVERVIEW)
                .addSubMenuItems(
                    new MenuItem("Unit Type Overview", Page.UNITTYPEOVERVIEW),
                    new MenuItem("Create Unit Type", Page.UNITTYPECREATE)));
      }
      if (allowedPages.contains(Page.PROFILE)) {
        simpleProv.addSubMenuItem(
            new MenuItem("Profile", Page.PROFILEOVERVIEW)
                .addSubMenuItems(
                    new MenuItem("Profile Overview", Page.PROFILEOVERVIEW),
                    new MenuItem("Create Profile", Page.PROFILECREATE)));
      }
      if (allowedPages.contains(Page.UNIT)) {
        simpleProv.addSubMenuItem(
            new MenuItem("Unit", Page.UNIT)
                .addSubMenuItems(new MenuItem("Create Unit", Page.UNIT).addCommand("create")));
      }
      menu.add(simpleProv);
    }
    if (allowedPages.contains(Page.TOPMENU_ADV)) {
      MenuItem advProv =
          new MenuItem("Advanced Provisioning", Page.TOPMENU_ADV)
              .setSelected(selectedPage.equalsAny(Page.GROUP, Page.JOB))
              .setDisableOnClickWithJavaScript();
      if (allowedPages.contains(Page.GROUP)) {
        advProv.addSubMenuItem(
            new MenuItem("Group", Page.GROUPSOVERVIEW)
                .addSubMenuItems(
                    new MenuItem("Group Overview", Page.GROUPSOVERVIEW),
                    new MenuItem("Create Group", Page.GROUP).addCommand("create")));
      }
      if (allowedPages.contains(Page.JOB)) {
        advProv.addSubMenuItem(
            new MenuItem("Job", Page.JOBSOVERVIEW)
                .addSubMenuItems(
                    new MenuItem("Job Overview", Page.JOBSOVERVIEW),
                    new MenuItem("Create Job", Page.JOB).addCommand("create")));
      }
      menu.add(advProv);
    }
    if (allowedPages.contains(Page.TOPMENU_FILESCRIPT)) {
      MenuItem filescript =
          new MenuItem("Files & Scripts", Page.TOPMENU_FILESCRIPT)
              .setSelected(selectedPage.equalsAny(Page.FILES, Page.SCRIPTEXECUTIONS))
              .setDisableOnClickWithJavaScript();
      if (allowedPages.contains(Page.FILES)) {
        filescript.addSubMenuItem(new MenuItem("Files", Page.FILES));
      }
      filescript.addSubMenuItem(new MenuItem("Script Executions", Page.SCRIPTEXECUTIONS));

      menu.add(filescript);
    }
    if (allowedPages.contains(Page.TOPMENU_TRIGEVENT)) {
      MenuItem triggerAndEvents =
          new MenuItem("Triggers & Events", Page.TOPMENU_TRIGEVENT)
              .setSelected(
                  selectedPage.equalsAny(
                      Page.TRIGGEROVERVIEW,
                      Page.CREATETRIGGER,
                      Page.TRIGGERRELEASE,
                      Page.TRIGGERRELEASEHISTORY,
                      Page.SYSLOGEVENTS,
                      Page.HEARTBEATS))
              .setDisableOnClickWithJavaScript();
      triggerAndEvents.addSubMenuItem(new MenuItem("Triggers", Page.TRIGGEROVERVIEW));
      //			triggerAndEvents.addSubMenuItem(new MenuItem("Create Trigger", Page.CREATETRIGGER));
      triggerAndEvents.addSubMenuItem(new MenuItem("Releases", Page.TRIGGERRELEASE));
      triggerAndEvents.addSubMenuItem(new MenuItem("Release History", Page.TRIGGERRELEASEHISTORY));
      triggerAndEvents.addSubMenuItem(new MenuItem("Syslog Events", Page.SYSLOGEVENTS));
      triggerAndEvents.addSubMenuItem(new MenuItem("Heartbeats", Page.HEARTBEATS));
      menu.add(triggerAndEvents);
    }

    if (allowedPages.contains(Page.TOPMENU_REPORT)) {
      MenuItem reporting =
          new MenuItem("Reports", Page.TOPMENU_REPORT)
              .setSelected(selectedPage.equalsAny(Page.REPORT))
              .setDisableOnClickWithJavaScript();
      MenuItem fusionReports =
          new MenuItem("All devices", Page.REPORT).setDisableOnClickWithJavaScript();
      reporting.addSubMenuItem(fusionReports);
      fusionReports.addSubMenuItem(
          new MenuItem("Unit", Page.REPORT).addParameter("type", ReportType.UNIT.getName()));
      fusionReports.addSubMenuItem(
          new MenuItem("Group", Page.REPORT).addParameter("type", ReportType.GROUP.getName()));
      fusionReports.addSubMenuItem(
          new MenuItem("Job", Page.REPORT).addParameter("type", ReportType.JOB.getName()));
      fusionReports.addSubMenuItem(
          new MenuItem("Provisioning", Page.REPORT)
              .addParameter("type", ReportType.PROV.getName()));
      MenuItem syslogReport =
          new MenuItem("Syslog", Page.REPORT.getUrl("type=" + ReportType.SYS.getName()));
      fusionReports.addSubMenuItem(syslogReport);
      syslogReport.addSubMenuItem(
          new MenuItem(
              "Units",
              Page.UNITLIST.getUrl("type=" + ReportType.SYS.getName()),
              new ArrayList<MenuItem>()));

      // If both hardware and voip are to be hidden this menu item is redundant.
      boolean showHardware = WebProperties.getInstance().isShowHardware();
      boolean showVoip = WebProperties.getInstance().isShowVoip();
      if (showHardware || showVoip) {
        MenuItem syslogReports = new MenuItem("Pingcom Devices", Page.REPORT);
        if (showVoip) {
          MenuItem voipReport =
              new MenuItem("Voip", Page.REPORT.getUrl("type=" + ReportType.VOIP.getName()));
          syslogReports.addSubMenuItem(voipReport);
          voipReport.addSubMenuItem(
              new MenuItem(
                  "Units",
                  Page.UNITLIST.getUrl("type=" + ReportType.VOIP.getName()),
                  new ArrayList<MenuItem>()));
        }
        if (showHardware) {
          MenuItem hardwareReport =
              new MenuItem("Hardware", Page.REPORT.getUrl("type=" + ReportType.HARDWARE.getName()));
          syslogReports.addSubMenuItem(hardwareReport);
          hardwareReport.addSubMenuItem(
              new MenuItem(
                  "Units",
                  Page.UNITLIST.getUrl("type=" + ReportType.HARDWARE.getName()),
                  new ArrayList<MenuItem>()));
        }
        syslogReports.setDisableOnClickWithJavaScript();
        reporting.addSubMenuItem(syslogReports);
      }
      menu.add(reporting);
    }
    if (allowedPages.contains(Page.TOPMENU_WIZARDS)) {
      MenuItem wizards =
          new MenuItem("Wizards", Page.TOPMENU_WIZARDS)
              .setSelected(selectedPage.equalsAny(Page.UPGRADE))
              .setDisableOnClickWithJavaScript();
      wizards.addSubMenuItem(new MenuItem("Upgrade wizard", Page.UPGRADE));
      menu.add(wizards);
    }
    return menu;
  }
}
