package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogFilter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.report.Chart;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.RecordHardware;
import com.github.freeacs.dbi.report.RecordSyslog;
import com.github.freeacs.dbi.report.RecordVoip;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.CheckBoxGroup;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.page.report.ReportPage;
import com.github.freeacs.web.app.page.report.ReportType;
import com.github.freeacs.web.app.page.report.UnitListData;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataHardware;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataHardwareFilter;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataSyslog;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataVoip;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.DateUtils;
import com.github.freeacs.web.app.util.DecimalUtils;
import com.github.freeacs.web.app.util.ReportConverter;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.WebProperties;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.chart.ui.GradientPaintTransformType;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;

/**
 *
 *
 * <h1>The Unit Dashboard and Unit History page.</h1>
 *
 * <p>This page is accessed in two ways:
 *
 * <p><b>Firstly</b>, it is used in the "normal" way by the Main servlet. Looked up in the Page
 * object, instantiated and processed in the usual way like mostly all pages in xAPS Web.
 *
 * <p><b>Secondly</b>, it is auto-configured and auto-wired by Spring IOC (injection of control)
 * Container by /WebContent/WEB-INF/app.xml. This means that this class will, in addition to being a
 * WebPage, act as a Controller in Spring, due its annotations (@Controller and @RequestMapping).
 * This means that because of how web.xml redirects all requests to the "app/" url pattern to the
 * app.xml (actully to a Spring Servlet that uses the app.xml), you can access this page through
 * Spring by calling the following url directly in the browsers navigation bar.
 *
 * <p>http://localhost:8080/xapsweb/app/unit-dashboard/[METHOD]
 *
 * <p>Note the part between app/ and /[METHOD], the "unit-dashboard" text. You might wonder how this
 * is linked to this page. The simple answer is that app.xml automatically searches all classes with
 * a Controller annotation and maps these classes on the "app/" url pattern based on the value of
 * their RequestMapping annotation.
 *
 * <p>One example is:
 *
 * <p>http://localhost:8080/xapsweb/app/unit-dashboard/charttable?start=...&end... etc
 *
 * <p>The above functionality is heavily leveraged by the module scripts
 * (xaps.module.unit.history.js and xaps.module.unit.dashboard.js), because some parts needs to be
 * loaded without a full refresh of the current page, and we want to avoid going the same way as
 * usual when the only thing we want is a generated image or a table. We want to get straight to
 * where we want to be and not be dependent on the process method.
 */
public class UnitStatusPage extends AbstractWebPage {
  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(UnitStatusPage.class);

  /** The current unit. */
  private Unit currentUnit;

  private DataSource mainDataSource;

  public UnitStatusPage() {
    mainDataSource = null;
  }

  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    if (currentUnit != null) {
      list.add(
          new MenuItem("Last 100 syslog entries", Page.SYSLOG)
              .addCommand("auto") // automatically hit the Search button
              .addParameter("unittype", currentUnit.getUnittype().getName())
              .addParameter("profile", currentUnit.getProfile().getName())
              .addParameter("unit", currentUnit.getId()));
      list.add(
          new MenuItem("Unit configuration", Page.UNIT)
              .addParameter("unittype", currentUnit.getUnittype().getName())
              .addParameter("profile", currentUnit.getProfile().getName())
              .addParameter("unit", currentUnit.getId()));
      list.add(
          new MenuItem("Unit dashboard", Page.UNITSTATUS)
              .addParameter("current", "true")
              .addParameter("history", "false")
              .addParameter("unittype", currentUnit.getUnittype().getName())
              .addParameter("profile", currentUnit.getProfile().getName())
              .addParameter("unit", currentUnit.getId()));
      list.add(
          new MenuItem("Unit history", Page.UNITSTATUS)
              .addParameter("current", "false")
              .addParameter("history", "true")
              .addParameter("unittype", currentUnit.getUnittype().getName())
              .addParameter("profile", currentUnit.getProfile().getName())
              .addParameter("unit", currentUnit.getId()));
    }
    return list;
  }

  public String getTitle(String page) {
    return super.getTitle(page)
        + (currentUnit != null
            ? " | "
                + currentUnit.getId()
                + " | "
                + currentUnit.getProfile().getName()
                + " | "
                + currentUnit.getProfile().getUnittype().getName()
            : "");
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    UnitStatusData inputData =
        (UnitStatusData) InputDataRetriever.parseInto(new UnitStatusData(), params);

    InputDataIntegrity.loadAndStoreSession(
        params,
        outputHandler,
        inputData,
        inputData.getUnittype(),
        inputData.getProfile(),
        inputData.getUnit());

    String sessionId = params.getSession().getId();

    if (ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource) == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    Map<String, Object> templateMap = outputHandler.getTemplateMap();

    ACSUnit acsUnit = ACSLoader.getACSUnit(sessionId, xapsDataSource, syslogDataSource);

    Unit unit = null;

    if (inputData.getUnit().notNullNorValue("")) {
      long getUnitStatusInfo = System.nanoTime();
      unit = acsUnit.getUnitById(inputData.getUnit().getString());
      logTimeElapsed(getUnitStatusInfo, "Retrieved Unit from database!", logger);
    }

    if (unit == null && inputData.getUnit().notNullNorValue("")) {
      templateMap.put("unitId", inputData.getUnit().getString());
      outputHandler.setTemplatePath("/unit-status/notfound.ftl");
      return;
    } else if (unit == null) {
      outputHandler.setDirectToPage(Page.SEARCH);
      return;
    }

    currentUnit = unit;

    Long currentTime = System.currentTimeMillis();

    Date fromDate = getFromDate(inputData, currentTime);
    Date toDate = getToDate(inputData, currentTime);

    templateMap.put("start", inputData.getStart().getDateFormat().format(fromDate));
    templateMap.put("end", inputData.getEnd().getDateFormat().format(toDate));

    String pageType = inputData.getGraphType().getString();

    UnitStatusInfo info =
        UnitStatusInfo.getUnitStatusInfo(
            unit, fromDate, toDate, sessionId, xapsDataSource, syslogDataSource);

    templateMap.put("info", info);

    // The following two timestamps is used to display a friendly version of the current date
    templateMap.put("startSimple", new SimpleDateFormat("MMM dd yyyy HH:mm").format(fromDate));
    templateMap.put("endSimple", new SimpleDateFormat("MMM dd yyyy HH:mm").format(toDate));

    boolean currentEnabled = params.getBoolean("current", true);
    templateMap.put("currentEnabled", currentEnabled);
    boolean historyEnabled = params.getBoolean("history");

    templateMap.put("showVoip", WebProperties.getInstance().isShowVoip());
    templateMap.put("showHardware", WebProperties.getInstance().isShowHardware());

    // Custom set properties that are to be displayed
    String unittypeName = unit.getUnittype().getName();
    Map<String, String> shortCutParams = new LinkedHashMap<>();
    for (Entry<String, String> property : WebProperties.getCustomDash(unittypeName).entrySet()) {
      // Call to resolve any parameter referencing other parameters
      String propValue = resolveParameters(property.getKey());
      if (property.getValue() != null) {
        shortCutParams.put(property.getValue(), propValue);
      } else {
        shortCutParams.put(property.getKey(), propValue);
      }
    }
    templateMap.put("shortCutParams", shortCutParams);

    // If this is the history view, we add the required objects to the template map
    if (historyEnabled) {
      DropDownSingleSelect<PeriodType> periodType =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getPeriod(),
              getPeriodType(inputData),
              Arrays.asList(PeriodType.MONTH, PeriodType.DAY, PeriodType.HOUR, PeriodType.MINUTE));
      String selectedTabKey = (pageType != null ? pageType : "") + "selectedTab";
      Integer selectedTab = params.getIntegerParameter(selectedTabKey);
      templateMap.put("pageType", pageType);
      DropDownSingleSelect<String> methodsForVoip =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getMethod(),
              inputData.getMethod().getStringOrDefault("VoIPQuality"),
              ReportPage.getMethodOptionsByType(ReportType.VOIP));
      templateMap.put("methodsForVoip", methodsForVoip);
      DropDownSingleSelect<String> methodsForHardware =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getMethod(),
              inputData.getMethod().getString(),
              ReportPage.getMethodOptionsByType(ReportType.HARDWARE));
      templateMap.put("methodsForHardware", methodsForHardware);
      templateMap.put("Sysaggregation", getSyslogAggregation(inputData, info));
      DropDownSingleSelect<String> methodsForSyslog =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getMethod(),
              inputData.getMethod().getString(),
              ReportPage.getMethodOptionsByType(ReportType.SYS));
      templateMap.put("methodsForSyslog", methodsForSyslog);
      templateMap.put("periodType", periodType);
      templateMap.put("selectedTab", selectedTab);
      templateMap.put("isrecordacall", new IsRecordACall());
      templateMap.put("divideby", new DivideBy());
      templateMap.put("backgroundcolor", new RowBackgroundColorMethod());
      templateMap.put("friendlytime", new FriendlyTimeRepresentationMethod());
      templateMap.put("syslogFilter", inputData.getSyslogFilter().getString());
    }

    outputHandler.setTemplatePathWithIndex("unit-status");
  }

  /**
   * Gets the to date.
   *
   * @param inputData the input data
   * @param currentTime the current time
   * @return the to date
   */
  private Date getToDate(UnitStatusData inputData, Long currentTime) {
    Date toDate = inputData.getEnd().getDate();
    if (toDate == null) {
      Calendar end = Calendar.getInstance();
      end.setTimeInMillis(currentTime);
      end.add(Calendar.HOUR, 1);
      end.set(Calendar.SECOND, 0);
      end.set(Calendar.MILLISECOND, 0);
      toDate = end.getTime();
    }
    return toDate;
  }

  /**
   * Gets the from date.
   *
   * @param inputData the input data
   * @param currentTime the current time
   * @return the from date
   */
  private Date getFromDate(UnitStatusData inputData, Long currentTime) {
    Date fromDate = inputData.getStart().getDate();
    if (fromDate == null) {
      Calendar start = Calendar.getInstance();
      start.setTimeInMillis(currentTime);
      start.set(Calendar.SECOND, 0);
      start.set(Calendar.MILLISECOND, 0);
      start.add(Calendar.DAY_OF_MONTH, -1);
      fromDate = start.getTime();
    }
    return fromDate;
  }

  /**
   * Gets the line status.
   *
   * @param unitId the unit id
   * @param session the session
   * @return the line status
   * @throws Exception the exception
   */
  public Map<String, Boolean> getLineStatus(String unitId, HttpSession session) throws Exception {
    Map<String, Boolean> status = new HashMap<>();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.SECOND, -30);
    Date start = cal.getTime();
    ACSUnit acsUnit = ACSLoader.getACSUnit(session.getId(), mainDataSource, mainDataSource);
    if (acsUnit == null) {
      throw new NotLoggedInException();
    }
    Unit unit = acsUnit.getUnitById(unitId);
    if (unit == null) {
      throw new UnitNotFoundException();
    }
    boolean isline1up =
        isCallOngoing(
            session.getId(), UnitStatusInfo.VoipLine.LINE_0, unit, start, null, mainDataSource);
    boolean isline2up =
        isCallOngoing(
            session.getId(), UnitStatusInfo.VoipLine.LINE_1, unit, start, null, mainDataSource);
    status.put("line1", isline1up);
    status.put("line2", isline2up);
    return status;
  }

  /**
   * Gets the chart image.
   *
   * @param pageType the page type
   * @param periodType the period type
   * @param requestMethod the request method
   * @param startTms the start tms
   * @param endTms the end tms
   * @param unitId the unit id
   * @param servletResponseChannel the servlet outputHandler channel
   * @param session the session
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public void getChartImage(
      String pageType,
      String periodType,
      String requestMethod,
      String startTms,
      String endTms,
      String unitId,
      String syslogFilter,
      String[] selectedAggregation,
      HttpServletResponse servletResponseChannel,
      HttpSession session)
      throws Exception {
    Date fromDate = DateUtils.parseDateDefault(startTms);
    Date toDate = DateUtils.parseDateDefault(endTms);
    Unit unit =
        ACSLoader.getACSUnit(session.getId(), mainDataSource, mainDataSource).getUnitById(unitId);
    UnitStatusInfo info =
        UnitStatusInfo.getUnitStatusInfo(
            unit, fromDate, toDate, session.getId(), mainDataSource, mainDataSource);
    PeriodType type = getPeriodType(periodType);
    ReportType reportType = ReportType.getEnum(pageType);
    String method = getReportMethod(reportType, requestMethod);

    byte[] image;

    switch (reportType) {
      case VOIP:
        long getVoipChart = System.nanoTime();
        Report<?> report = ReportConverter.convertVoipReport(info.getVoipReport(), type);
        Chart<?> chartMaker = new Chart<>((Report<RecordVoip>) report, method, false, null);
        image = getReportChartImageBytes(chartMaker, null, 900, 250);
        logTimeElapsed(getVoipChart, "Retrieved Voip chart", logger);
        break;
      case HARDWARE:
        long getHwChart = System.nanoTime();
        report = ReportConverter.convertHardwareReport(info.getHardwareReport(), type);
        chartMaker = new Chart<>((Report<RecordHardware>) report, method, false, null);
        image = getReportChartImageBytes(chartMaker, null, 900, 250);
        logTimeElapsed(getHwChart, "Retrieved Hardware chart", logger);
        break;
      case SYS:
        long getSyslogChart = System.nanoTime();
        report = ReportConverter.convertSyslogReport(info.getSyslogReport(syslogFilter), type);
        List<String> keyNames =
            new ArrayList<>(
                Arrays.asList(info.getSyslogReport(syslogFilter).getKeyFactory().getKeyNames()));
        String[] syslogAggregation =
            ReportPage.getSelectedAggregation(selectedAggregation, keyNames);
        chartMaker =
            new Chart<>((Report<RecordSyslog>) report, method, false, null, syslogAggregation);
        image = getReportChartImageBytes(chartMaker, null, 900, 250);
        logTimeElapsed(getSyslogChart, "Retrieved Syslog chart", logger);
        break;
      default:
        return;
    }

    Output.writeImageBytesToResponse(image, servletResponseChannel);
  }

  /**
   * Gets the chart table.
   *
   * @param pageType the page type
   * @param startTms the start tms
   * @param endTms the end tms
   * @param unitId the unit id
   * @param servletRequest the servlet request
   * @param session the session
   * @return the chart table
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public ModelAndView getChartTable(
      String pageType,
      String startTms,
      String endTms,
      String unitId,
      String syslogFilter,
      HttpServletRequest servletRequest,
      HttpSession session)
      throws Exception {
    Date fromDate = DateUtils.parseDateDefault(startTms);
    Date toDate = DateUtils.parseDateDefault(endTms);
    ACSUnit acsUnit = ACSLoader.getACSUnit(session.getId(), mainDataSource, mainDataSource);
    Unit unit = acsUnit.getUnitById(unitId);
    UnitStatusInfo info =
        UnitStatusInfo.getUnitStatusInfo(
            unit, fromDate, toDate, session.getId(), mainDataSource, mainDataSource);
    ReportType reportType = ReportType.getEnum(pageType);
    String page;
    List<?> records;

    Map<String, Object> root = new HashMap<>();

    switch (reportType) {
      case VOIP:
        long getVoipChart = System.nanoTime();
        records = new ArrayList<>(info.getVoipReport().getMap().values());
        ((List<RecordVoip>) records).sort(new RecordVoipComparator());
        records = RecordUIDataVoip.convertRecords((List<RecordVoip>) records);
        page = "calls.ftl";
        logTimeElapsed(getVoipChart, "Retrieved Voip table", logger);
        break;
      case HARDWARE:
        long getHwChart = System.nanoTime();
        records = new ArrayList<>(info.getHardwareReport().getMap().values());
        ((List<RecordHardware>) records).sort(new RecordHardwareComparator());
        records =
            RecordUIDataHardware.convertRecords(
                unit,
                (List<RecordHardware>) records,
                new RecordUIDataHardwareFilter(
                    (UnitListData)
                        InputDataRetriever.parseInto(
                            new UnitListData(), new ParameterParser(servletRequest)),
                    new HashMap<>()));
        if (records.size() > 100) {
          records = records.subList(0, 100);
        }
        page = "memory.ftl";
        logTimeElapsed(getHwChart, "Retrieved Hardware table", logger);
        break;
      case SYS:
        long getSyslogChart = System.nanoTime();
        records = RecordUIDataSyslog.convertRecords(info.getSyslogEntries(), acsUnit.getAcs());
        page = "syslog.ftl";
        logTimeElapsed(getSyslogChart, "Retrieved Syslog table", logger);
        break;
      default:
        return null;
    }

    root.put("start", DateUtils.formatDateDefault(fromDate));
    root.put("end", DateUtils.formatDateDefault(toDate));
    root.put("syslogFilter", syslogFilter);
    root.put("records", records);
    root.put("info", info);
    root.put("URL_MAP", Page.getPageURLMap());

    return new ModelAndView(root, "unit-status/" + page);
  }

  /**
   * Gets the total score effect.
   *
   * @param startTms the start tms
   * @param endTms the end tms
   * @param unitId the unit id
   * @param session the session
   * @return the total score effect
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException the security exception
   * @throws ParseException the parse exception the no available connection exception
   * @throws SQLException the sQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   * @throws NoSuchMethodException the no such method exception
   */
  public Map<String, Object> getTotalScoreEffect(
      String startTms, String endTms, String unitId, HttpSession session)
      throws ParseException, SQLException, IOException, IllegalAccessException,
          InvocationTargetException, NoSuchMethodException {
    Date fromDate = DateUtils.parseDateDefault(startTms);
    Date toDate = DateUtils.parseDateDefault(endTms);
    Unit unit =
        ACSLoader.getACSUnit(session.getId(), mainDataSource, mainDataSource).getUnitById(unitId);
    UnitStatusInfo info =
        UnitStatusInfo.getUnitStatusInfo(
            unit, fromDate, toDate, session.getId(), mainDataSource, mainDataSource);
    Double effect = info.getOverallStatus().getTotalScoreEffect();
    DecimalUtils.Format df = DecimalUtils.Format.ONE_DECIMAL;
    Map<String, Object> map = new HashMap<>();
    if (effect > 0.09) {
      map.put("score", df.format(effect));
      map.put("color", "red");
    }
    return map;
  }

  /**
   * Gets the total score number.
   *
   * @param startTms the start tms
   * @param endTms the end tms
   * @param unitId the unit id
   * @param session the session
   * @return the total score number the no available connection exception
   * @throws SQLException the sQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateModelException the template model exception
   * @throws ParseException the parse exception
   */
  public String getTotalScoreNumber(
      String startTms, String endTms, String unitId, HttpSession session)
      throws SQLException, IOException, TemplateModelException, ParseException {
    Date fromDate = DateUtils.parseDateDefault(startTms);
    Date toDate = DateUtils.parseDateDefault(endTms);
    Unit unit =
        ACSLoader.getACSUnit(session.getId(), mainDataSource, mainDataSource).getUnitById(unitId);
    UnitStatusInfo info =
        UnitStatusInfo.getUnitStatusInfo(
            unit, fromDate, toDate, session.getId(), mainDataSource, mainDataSource);
    Double totalScore = info.getTotalScore();
    DecimalUtils.Format df = DecimalUtils.Format.ONE_DECIMAL;
    String totalScoreString =
        totalScore != null ? df.format(totalScore) : "No calls have been made";
    boolean totalScoreIsNA = "No calls have been made".equals(totalScoreString);
    String html = "";
    html +=
        "<span style='"
            + (totalScore != null && totalScore > 0
                ? new RowBackgroundColorMethod().getFontColor(totalScore.floatValue())
                : "")
            + (!totalScoreIsNA ? "font-weight:bold;" : "")
            + "'>"
            + totalScoreString
            + "</span>";
    if (!totalScoreIsNA) {
      html += " of 100";
    }
    return html;
  }

  /**
   * Gets the overall status speedometer.
   *
   * @param startTms the start tms
   * @param endTms the end tms
   * @param unitId the unit id
   * @param res the res
   * @param session the session
   * @throws Exception the exception
   */
  public void getOverallStatusSpeedometer(
      String startTms, String endTms, String unitId, HttpServletResponse res, HttpSession session)
      throws Exception {
    Date fromDate = DateUtils.parseDateDefault(startTms);
    Date toDate = DateUtils.parseDateDefault(endTms);
    Unit unit =
        ACSLoader.getACSUnit(session.getId(), mainDataSource, mainDataSource).getUnitById(unitId);
    UnitStatusInfo info =
        UnitStatusInfo.getUnitStatusInfo(
            unit, fromDate, toDate, session.getId(), mainDataSource, mainDataSource);
    JFreeChart chart =
        createStatusDialChart(
            null,
            "Overall status",
            new DefaultValueDataset(info.getOverallStatus().getStatus()),
            UnitStatusInfo.OVERALL_STATUS_MIN,
            UnitStatusInfo.OVERALL_STATUS_MAX);
    byte[] image = getReportChartImageBytes(null, chart, 380, 380);
    Output.writeImageBytesToResponse(image, res);
  }

  /**
   * Gets the syslog aggregation.
   *
   * @param inputData the input data
   * @param info the info
   * @return the syslog aggregation the no available connection exception
   */
  private CheckBoxGroup<String> getSyslogAggregation(
      UnitStatusData inputData, UnitStatusInfo info) {
    List<String> keyNames = new ArrayList<>(Arrays.asList("Severity", "Facility", "EventId"));
    Input input = ReportPage.getSelectedAggregation(inputData.getAggregate(), keyNames);
    return InputSelectionFactory.getCheckBoxGroup(input, input.getStringList(), keyNames);
  }

  public void setMainDataSource(DataSource mainDataSource) {
    this.mainDataSource = mainDataSource;
  }

  /** The Class RecordHardwareComparator. */
  class RecordHardwareComparator implements Comparator<RecordHardware> {
    /** Instantiates a new record hardware comparator. */
    public RecordHardwareComparator() {}

    public int compare(RecordHardware o1, RecordHardware o2) {
      if (o1.getKey().getTms().before(o2.getKey().getTms())) {
        return 1;
      }
      if (o1.getKey().getTms().after(o2.getKey().getTms())) {
        return -1;
      }
      return 0;
    }
  }

  /** The Class RecordVoipComparator. */
  class RecordVoipComparator implements Comparator<RecordVoip> {
    /** Instantiates a new record voip comparator. */
    public RecordVoipComparator() {}

    public int compare(RecordVoip o1, RecordVoip o2) {
      if (o1.getKey().getTms().before(o2.getKey().getTms())) {
        return 1;
      }
      if (o1.getKey().getTms().after(o2.getKey().getTms())) {
        return -1;
      }
      return 0;
    }
  }

  /**
   * Gets the report chart image bytes.
   *
   * @param chartMaker the chart maker
   * @param chart the chart
   * @param width the width
   * @param height the height
   * @return the report chart image bytes
   * @throws Exception the exception
   */
  public static byte[] getReportChartImageBytes(
      Chart<?> chartMaker, JFreeChart chart, int width, int height) throws Exception {
    if (chart == null && chartMaker != null) {
      chart = chartMaker.makeTimeChart(null, null, null, null);
    } else if (chart == null) {
      throw new IllegalArgumentException("Chart<?> and JFreeChart is NULL.");
    }
    if (chart.getPlot() instanceof XYPlot) {
      XYPlot plot = (XYPlot) chart.getPlot();
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
      renderer.setDefaultShapesVisible(true);
      renderer.setDrawOutlines(true);
      renderer.setUseFillPaint(true);
      renderer.setDefaultFillPaint(Color.white);
    }
    ByteArrayOutputStream image = new ByteArrayOutputStream();
    ChartUtils.writeChartAsPNG(image, chart, width, height);
    return image.toByteArray();
  }

  /**
   * Checks if is call ongoing.
   *
   * @param sessionId the session id
   * @param line the line
   * @param unit the unit
   * @param start the start
   * @param end the end
   * @param xapsDataSource
   * @return true, if is call ongoing
   * @throws SQLException the sQL exception the no available connection exception
   */
  public boolean isCallOngoing(
      String sessionId,
      UnitStatusInfo.VoipLine line,
      Unit unit,
      Date start,
      Date end,
      DataSource xapsDataSource)
      throws SQLException {
    Syslog syslog = new Syslog(mainDataSource, ACSLoader.getIdentity(sessionId, xapsDataSource));
    SyslogFilter filter = new SyslogFilter();
    filter.setMaxRows(1);
    String keyToFind =
        "MOS Report: Channel "
            + (line != null ? line.toString() : "")
            + "|ua"
            + (line != null ? line.toString() : "")
            + ": session connected";
    filter.setMessage("^" + keyToFind);
    filter.setCollectorTmsStart(start);
    filter.setCollectorTmsEnd(end);
    filter.setUnitId("^" + unit.getId() + "$");
    long isCallOngoingMs = System.nanoTime();
    List<SyslogEntry> mosEntry =
        syslog.read(filter, ACSLoader.getXAPS(sessionId, this.mainDataSource, mainDataSource));
    logTimeElapsed(
        isCallOngoingMs,
        "Retrieved last MOS report from Syslog. Result size: " + mosEntry.size(),
        logger);
    long getQoS = System.nanoTime();
    Date lastQoS =
        UnitStatusRealTimeMosPage.getLastQoSTimestamp(
            sessionId,
            unit,
            start,
            line.toString(),
            ACSLoader.getXAPS(sessionId, this.mainDataSource, mainDataSource));
    logTimeElapsed(
        getQoS,
        "Retrieved last QoS report from Syslog. QoS timestamp: "
            + (lastQoS != null ? lastQoS.toString() : "n/a"),
        logger);
    if (!mosEntry.isEmpty() && lastQoS != null) {
      SyslogEntry mosSyslogEntry = mosEntry.get(0);
      Date mosSyslogEntryTs = mosSyslogEntry.getCollectorTimestamp();
      boolean ongoing = mosSyslogEntryTs.after(lastQoS);
      logger.info(
          (line != null ? ("Line " + line + " is ") : "One of the lines are ")
              + (ongoing ? "active" : "inactive"));
      return ongoing;
    } else if (!mosEntry.isEmpty()) {
      logger.info("Line " + line + " is active");
      return true;
    }
    return false;
  }

  /**
   * Creates a chart displaying a circular dial.
   *
   * @param chartTitle the chart title.
   * @param dialLabel the dial label.
   * @param dataset the dataset.
   * @param lowerBound the lower bound.
   * @param upperBound the upper bound.
   * @return A chart that displays a value as a dial.
   */
  private JFreeChart createStatusDialChart(
      String chartTitle,
      String dialLabel,
      ValueDataset dataset,
      double lowerBound,
      double upperBound) {
    DialPlot plot = new DialPlot();
    plot.setDataset(dataset);
    plot.setDialFrame(new StandardDialFrame());
    DialTextAnnotation annotation1 = new DialTextAnnotation(dialLabel);
    annotation1.setFont(new Font("Dialog", Font.BOLD, 14));
    annotation1.setRadius(0.7);

    plot.addLayer(annotation1);

    DialValueIndicator dvi = new DialValueIndicator(0);
    plot.addLayer(dvi);

    StandardDialScale scale = new StandardDialScale(lowerBound, upperBound, -120, -300, 1, 4);
    scale.setTickRadius(0.88);
    scale.setTickLabelOffset(0.15);
    scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));
    plot.addScale(0, scale);

    plot.addPointer(new DialPointer.Pin());

    DialCap cap = new DialCap();
    plot.setCap(cap);

    JFreeChart chart = new JFreeChart(chartTitle, plot);
    chart.setBackgroundPaint(Color.WHITE);

    StandardDialRange range =
        new StandardDialRange(UnitStatusInfo.DIAL_CHART_YELLOW, upperBound, Color.green);
    range.setInnerRadius(0.52);
    range.setOuterRadius(0.55);
    plot.addLayer(range);

    StandardDialRange range2 =
        new StandardDialRange(
            UnitStatusInfo.DIAL_CHART_RED, UnitStatusInfo.DIAL_CHART_YELLOW, Color.orange);
    range2.setInnerRadius(0.52);
    range2.setOuterRadius(0.55);
    plot.addLayer(range2);

    StandardDialRange range3 =
        new StandardDialRange(lowerBound, UnitStatusInfo.DIAL_CHART_RED, Color.red);
    range3.setInnerRadius(0.52);
    range3.setOuterRadius(0.55);
    plot.addLayer(range3);

    GradientPaint gp =
        new GradientPaint(
            new Point(), new Color(255, 255, 255), new Point(), new Color(170, 170, 220));
    DialBackground db = new DialBackground(gp);
    db.setGradientPaintTransformer(
        new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
    plot.setBackground(db);

    plot.removePointer(0);
    DialPointer.Pointer p = new DialPointer.Pointer();
    plot.addPointer(p);

    return chart;
  }

  /** The Class IsRecordACall. */
  public class IsRecordACall implements TemplateMethodModel {
    @SuppressWarnings({"rawtypes"})
    public Boolean exec(List arg0) throws TemplateModelException {
      if (arg0.size() < 2) {
        throw new TemplateModelException("Specify total score and sip reg failed");
      }
      String sipregfailed = (String) arg0.get(0);
      String totalscore = (String) arg0.get(1);
      if (isNumber(sipregfailed) && isNumber(totalscore)) {
        return Integer.parseInt(sipregfailed) == 0 && Integer.parseInt(totalscore) > 0;
      }
      return false;
    }
  }

  /**
   * Gets the period type.
   *
   * @param inputData the input data
   * @return the period type
   */
  private PeriodType getPeriodType(UnitStatusData inputData) {
    return getPeriodType(inputData.getPeriod().getString());
  }

  /**
   * Gets the period type.
   *
   * @param periodTypeStr the period type str
   * @return the period type
   */
  private PeriodType getPeriodType(String periodTypeStr) {
    PeriodType periodType = PeriodType.MINUTE;
    if (periodTypeStr != null) {
      if (periodTypeStr.equals(PeriodType.MONTH.getTypeStr())) {
        periodType = PeriodType.MONTH;
      } else if (periodTypeStr.equals(PeriodType.DAY.getTypeStr())) {
        periodType = PeriodType.DAY;
      } else if (periodTypeStr.equals(PeriodType.HOUR.getTypeStr())) {
        periodType = PeriodType.HOUR;
      } else if (periodTypeStr.equals(PeriodType.MINUTE.getTypeStr())) {
        periodType = PeriodType.MINUTE;
      }
    }
    return periodType;
  }

  /**
   * Gets the report method.
   *
   * @param pageType the page type
   * @param suppliedMethod the supplied method
   * @return the report method
   */
  private String getReportMethod(ReportType pageType, String suppliedMethod) {
    List<String> methods = pageType.getMethods();
    if (methods.contains(suppliedMethod)) {
      return suppliedMethod;
    }
    return methods.get(0);
  }

  private static Pattern paramRefPattern = Pattern.compile("(\\$\\{([^\\}]+)\\})");
  /**
   * This method attempts to resolve any references to other parameters by looking for references
   * with the syntax ${<other_parameter>} in the parameter from the unit.
   *
   * <p>If this pattern is not detected the value is returned as it is.
   *
   * <p>If this pattern is detected the first match will be considered a reference and will be
   * attempted to be resolved by a call to the current unit for the new parameter. Anything before
   * and after this match will be appended to the new value meaning that you can add other
   * information to the referenced parameter. For instance: "https://${IP_parameter_reference}" if
   * you want an IP-address to be appended with a https:// input.
   *
   * @return The parameter with any one step references resolved
   */
  private String resolveParameters(String key) {
    String value = currentUnit.getParameterValue(key);
    if (value != null) {
      Matcher matcher = paramRefPattern.matcher(value);
      if (matcher.find()) {
        StringBuilder param = new StringBuilder();
        if (matcher.start() > 0) {
          param.append(value, 0, matcher.start());
        }

        String newValue = currentUnit.getParameterValue(matcher.group(2));
        if (newValue != null && !newValue.isEmpty()) {
          param.append(newValue);
        }
        if (matcher.end() < value.length()) {
          param.append(value.substring(matcher.end()));
        }
        value = param.toString();
      }
    }
    if (value == null || value.isEmpty()) {
      value = "NO VALUE";
    }

    return value;
  }
}
