package com.github.freeacs.web.app.page.report;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.Chart;
import com.github.freeacs.dbi.report.Key;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.Record;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.github.freeacs.dbi.report.ReportHardwareGenerator;
import com.github.freeacs.dbi.report.ReportProvisioningGenerator;
import com.github.freeacs.dbi.report.ReportSyslogGenerator;
import com.github.freeacs.dbi.report.ReportVoipCallGenerator;
import com.github.freeacs.dbi.report.ReportVoipGenerator;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.CheckBoxGroup;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.page.report.custom.DefaultRetriever;
import com.github.freeacs.web.app.page.report.custom.GroupRetriever;
import com.github.freeacs.web.app.page.report.custom.HardwareRetriever;
import com.github.freeacs.web.app.page.report.custom.JobRetriever;
import com.github.freeacs.web.app.page.report.custom.ProvRetriever;
import com.github.freeacs.web.app.page.report.custom.ReportRetriever;
import com.github.freeacs.web.app.page.report.custom.SyslogRetriever;
import com.github.freeacs.web.app.page.report.custom.UnitRetriever;
import com.github.freeacs.web.app.page.report.custom.VoipRetriever;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtils;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * One page (or controller if you like) for all types of reports.
 *
 * @author Jarl Andre Hubenthal
 */
public class ReportPage extends AbstractWebPage {
  private Chart<?> chartMaker;
  private ACS acs;
  private ReportData inputData;
  private JFreeChart chart;
  private Report<?> report;
  private ParameterParser req;
  private PeriodType toUseAsPeriodType;
  private ReportRetriever reportImplementation;
  private DropDownSingleSelect<String> swVersionList;
  private DropDownSingleSelect<Unittype> unittypes;
  private ReportType reportType;
  private DropDownSingleSelect<Profile> profiles;
  private DropDownSingleSelect<String> method;
  private DropDownSingleSelect<PeriodType> periodType;
  private DropDownSingleSelect<String> optionalmethod;
  private Unittype unittype;
  private DropDownSingleSelect<Group> groupList;
  private Date toUseAsStart;
  private Date toUseAsEnd;

  /** Translates ReportType to Class<? extends ReportRetriever> */
  private static Map<ReportType, Class<? extends ReportRetriever>> reportType2Implementation =
      new HashMap<>();

  static {
    reportType2Implementation.put(ReportType.VOIP, VoipRetriever.class);
    reportType2Implementation.put(ReportType.UNIT, UnitRetriever.class);
    reportType2Implementation.put(ReportType.SYS, SyslogRetriever.class);
    reportType2Implementation.put(ReportType.JOB, JobRetriever.class);
    reportType2Implementation.put(ReportType.HARDWARE, HardwareRetriever.class);
    reportType2Implementation.put(ReportType.GROUP, GroupRetriever.class);
    reportType2Implementation.put(ReportType.PROV, ProvRetriever.class);
  }

  /**
   * NOTE ABOUT 5.1 toUseAsStart and toUseAsEnd is two variables that exists to make it possible to
   * set different timestamps for realtime while preserving the original timestamps. If you go out
   * of realtime you should see the same timestamps you had before you entered realtime.
   *
   * @param req the req
   * @param outputHandler the outputHandler
   * @param xapsDataSource
   * @param syslogDataSource
   * @throws Exception the exception
   */
  public void process(
      ParameterParser req,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    this.req = req;
    // 1
    inputData = (ReportData) InputDataRetriever.parseInto(new ReportData(), req);

    String pageType = inputData.getType().getString();

    // 2
    if (req.getBoolean("image")) {
      processImageRequest(pageType, outputHandler, req.getSession());
      return;
    }

    InputDataIntegrity.loadAndStoreSession(
        req, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile());

    // 3
    acs = ACSLoader.getXAPS(req.getSession().getId(), xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    Map<String, Object> templateMap = outputHandler.getTemplateMap();

    /* Morten jan 2014 - Certificate checks disable due to open source
    // Display exception page if certificate for report is not present
    if (!CertificateVerification.isCertificateValid(Certificate.CERT_TYPE_REPORT, req.getSession().getId())
    		|| !SessionCache.getSessionData(req.getHttpServletRequest().getSession().getId()).getUser().isReportsAllowed()) {
    	templateMap.put("message", "No valid certificate found for Reports page. Please contact your systems administrator.");
    	outputHandler.setTemplatePath("/exception.ftl");
    	return;
    }
    */

    // 4
    reportType = ReportType.getEnum(pageType);
    unittypes = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    profiles =
        InputSelectionFactory.getProfileSelection(
            inputData.getProfile(), inputData.getUnittype(), acs);
    method =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getMethod(), inputData.getMethod().getString(), getMethodOptions(pageType));
    optionalmethod =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getOptionalMethod(),
            inputData.getOptionalMethod().getString(),
            getOptionalMethodOptions(method.getSelectedOrFirstItem(), method.getItems()));
    periodType =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getPeriod(),
            getPeriodType(inputData.getPeriod().getString()),
            Arrays.asList(PeriodType.getTypes()));
    unittype = unittypes.getSelected();
    groupList = null;
    Group groupSelect = null;
    if (unittype != null) {
      groupSelect = unittype.getGroups().getByName(inputData.getGroupSelect().getString());
      groupList =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getGroupSelect(), groupSelect, getGroups(unittypes, profiles));
    }
    Input advancedView = inputData.getAdvancedView();

    Date start = getStartDate(inputData.getStart());
    toUseAsStart = start;
    Date end = getEndDate(inputData.getEnd());
    toUseAsEnd = end;

    Boolean realtime = inputData.getRealtime().getBoolean();

    toUseAsPeriodType = periodType.getSelected();

    if (reportType == ReportType.VOIP && inputData.getMethod().getString() == null) {
      method.setSelected("VoIPQuality");
    }

    reportImplementation = DefaultRetriever.class.newInstance();
    Class<? extends ReportRetriever> reportImplementationClass =
        reportType2Implementation.get(reportType);
    if (reportImplementationClass != null) {
      reportImplementation =
          (ReportRetriever)
              reportImplementationClass.getConstructors()[0].newInstance(inputData, req, acs);
    }

    /*
     * We need to get a reference to the ReportRetriever before we can produce a list of software
     * versions.
     */
    swVersionList = null;
    String swVersion = null;
    if (unittype != null && reportType2TableNameMap.containsKey(reportType)) {
      swVersion = inputData.getSwVersion().getString();
      List<String> swVersions =
          getSwVersion(
              reportType,
              start,
              end,
              unittypes.getSelected(),
              profiles.getSelected(),
              reportImplementation.getReportGenerator());
      swVersionList =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getSwVersion(), swVersion, swVersions);
    }

    setPeriodTypeToSecondsIfRealtime(realtime);

    if (isZoomingRequestAndShouldReturn(req, outputHandler)) {
      return;
    }

    makeReport(groupSelect, swVersion);

    rememberPeriodTypeIfNotSeconds();

    CheckBoxGroup<String> aggregation = getAggregationCheckbox(getKeyNames());

    Integer legendIndex = getLegendIndex(req.getSession().getId());

    makeChart(legendIndex, aggregation);

    rememberChartInSession(req);

    displayReportChart(req, templateMap, aggregation);

    // apply report specific objects to the template map
    reportImplementation.applyObjects(templateMap);

    // 7
    templateMap.put("aggregation", aggregation);
    templateMap.put("start", inputData.getStart().getDateFormat().format(start));
    templateMap.put("end", inputData.getEnd().getDateFormat().format(end));
    templateMap.put("method", method);
    templateMap.put("pageType", pageType);
    templateMap.put("unittype", unittypes);
    templateMap.put("profile", profiles);
    templateMap.put("optionalMethod", optionalmethod);
    templateMap.put("advancedView", advancedView);
    templateMap.put("legend", legendIndex);
    templateMap.put("periodtype", periodType);
    templateMap.put("groupList", groupList);
    templateMap.put("swList", swVersionList);
    templateMap.put("realtime", realtime);

    // 8 and 9
    outputHandler.setTemplatePathWithIndex("report");
  }

  private boolean isZoomingRequestAndShouldReturn(ParameterParser req, Output outputHandler) {
    boolean isZoomingRequest =
        isNumber(req.getParameter("series"))
            && isNumber(req.getParameter("item"))
            && req.getSession().getAttribute("JFreeChart") != null;
    if (isZoomingRequest) {
      processZoomingRequest(outputHandler);
      return true;
    } else {
      return false;
    }
  }

  private void setPeriodTypeToSecondsIfRealtime(Boolean realtime) {
    if (realtime) {
      // 5.1.1
      toUseAsStart = getStartDateForRealtime();
      // 5.1.2
      toUseAsEnd = getEndDateForRealtime();
      // 5.1.3
      toUseAsPeriodType = PeriodType.SECOND;
    }
  }

  private void rememberPeriodTypeIfNotSeconds() {
    if (toUseAsPeriodType != PeriodType.SECOND) {
      periodType.setSelected(toUseAsPeriodType);
    }
  }

  private void makeReport(Group groupSelect, String swVersion) throws Exception {
    reportImplementation.getReportGenerator().setSwVersion(swVersion);
    report =
        reportImplementation.generateReport(
            toUseAsPeriodType,
            toUseAsStart,
            toUseAsEnd,
            unittypes.getSelectedOrAllItemsAsList(),
            profiles.getSelectedOrAllItemsAsList(),
            groupSelect);
  }

  private void displayReportChart(
      ParameterParser req, Map<String, Object> templateMap, CheckBoxGroup<String> aggregation)
      throws Exception {
    ChartLegendsDimensions legendDimensions =
        new ChartLegendsDimensions(aggregation.getSelected().toArray(new String[] {}), report);
    displayReportChartWithImageMap(
        templateMap,
        legendDimensions.numberOfColumns,
        legendDimensions.averageLengthPrLegend,
        aggregation.getSelected(),
        req.getSession());
  }

  private void rememberChartInSession(ParameterParser req) {
    req.getSession().setAttribute("JFreeChart", chart);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void makeChart(Integer legendIndex, CheckBoxGroup<String> aggregation) throws Exception {
    chartMaker =
        new Chart<Record>(
            (Report<Record>) report,
            method.getSelectedOrFirstItem(),
            false,
            null,
            aggregation.getSelected().toArray(new String[] {}));
    chart = chartMaker.makeTimeChart(null, null, optionalmethod.getSelected(), legendIndex);
  }

  private List<String> getKeyNames() {
    List<String> keyNames = new ArrayList<>(Arrays.asList(report.getKeyFactory().getKeyNames()));
    removeUnrelevantKeyNames(keyNames);
    return keyNames;
  }

  private CheckBoxGroup<String> getAggregationCheckbox(List<String> keyNames) {
    Input aggregationInput = getSelectedAggregation(inputData.getAggregate(), keyNames);
    return InputSelectionFactory.getCheckBoxGroup(
        aggregationInput, aggregationInput.getStringList(), keyNames);
  }

  private void removeUnrelevantKeyNames(List<String> keyNames) {
    if (keyNames.contains("Profile") && isProfileListSelected()) {
      keyNames.remove("Profile");
    }
    if (keyNames.contains("SoftwareVersion") && isSoftwareListSelected()) {
      keyNames.remove("SoftwareVersion");
    }
    if (keyNames.contains("Unittype") && isUnittypeListSelected()) {
      keyNames.remove("Unittype");
    }
    if (keyNames.contains("Group") && isGroupListSelected()) {
      keyNames.remove("Group");
    }
  }

  private boolean isUnittypeListSelected() {
    return unittypes.getSelected() != null;
  }

  private boolean isProfileListSelected() {
    return unittypes.getSelected() == null || profiles.getSelected() != null;
  }

  private boolean isSoftwareListSelected() {
    return swVersionList != null && swVersionList.getSelected() != null;
  }

  private boolean isGroupListSelected() {
    return reportImplementation instanceof GroupRetriever && isGroupRetrieverListSelected();
  }

  private boolean isGroupRetrieverListSelected() {
    return ((GroupRetriever) reportImplementation).getGroups() != null
        && ((GroupRetriever) reportImplementation).getGroups().getSelected() != null;
  }

  private void processZoomingRequest(Output outputHandler) {
    JFreeChart _chart = (JFreeChart) req.getSession().getAttribute("JFreeChart");

    int series = Integer.parseInt(req.getParameter("series"));
    int item = Integer.parseInt(req.getParameter("item"));

    // 5.2.1
    toUseAsStart = getItemStartDate(_chart, series, item);
    toUseAsEnd = getItemEndDate(_chart, series, item);

    toUseAsEnd = fixDateMillis(toUseAsEnd);

    // 5.2.2
    if (!toUseAsStart.before(toUseAsEnd) && !toUseAsEnd.after(toUseAsStart)) {
      // 5.2.2.1
      Calendar endCal = Calendar.getInstance();
      endCal.setTime(toUseAsEnd);
      endCal.add(Calendar.MINUTE, 1);
      toUseAsEnd = endCal.getTime();
    }

    String startStr = inputData.getStart().format(toUseAsStart);
    String endStr = inputData.getEnd().format(toUseAsEnd);

    toUseAsPeriodType = getNextLowerPeriodType(periodType.getSelected());

    // 5.2.3
    if (toUseAsPeriodType == PeriodType.SECOND) {
      String url = "start=" + startStr + "&end=" + endStr + "&type=" + reportType.getName();
      String[] aggregation = inputData.getAggregate().getStringArray();
      if (aggregation != null && aggregation.length > 0) {
        for (String aggr : aggregation) {
          url += "&" + aggr.toLowerCase() + "=" + req.getParameter(aggr.toLowerCase());
        }
      }
      if (!url.contains("&unittype=") && isUnittypeListSelected()) {
        url += "&unittype=" + unittypes.getSelected().getName();
      }
      if (!url.contains("&profile=") && profiles.getSelected() != null) {
        url += "&profile=" + profiles.getSelected().getName();
      }
      if (!url.contains("&groupselect=") && groupList != null && groupList.getSelected() != null) {
        url += "&groupselect=" + groupList.getSelected().getName();
      }
      if (!url.contains("&group=") && inputData.getGroup().getString() != null) {
        url += "&group=" + inputData.getGroup().getString();
      }
      if (!url.contains("&swversion=")
          && swVersionList != null
          && swVersionList.getSelected() != null) {
        url += "&swversion=" + swVersionList.getSelected();
      }
      // 5.2.3.1
      outputHandler.setDirectToPage(Page.UNITLIST, url);
      // 5.2.3.2
    } else { // 5.2.4
      // 5.2.4.1
      String url = "type=" + reportType.getName() + "&start=" + startStr + "&end=" + endStr;
      url += "&" + inputData.getPeriod().getKey() + "=" + toUseAsPeriodType.getTypeStr();
      if (method.getSelected() != null) {
        url += "&" + inputData.getMethod().getKey() + "=" + method.getSelected();
      }
      if (optionalmethod.getSelected() != null) {
        url += "&" + inputData.getOptionalMethod().getKey() + "=" + optionalmethod.getSelected();
      }
      if (inputData.getAdvancedView().getBoolean() != null) {
        url +=
            "&"
                + inputData.getAdvancedView().getKey()
                + "="
                + inputData.getAdvancedView().getBoolean();
      }
      if (!url.contains("&groupselect=") && groupList != null && groupList.getSelected() != null) {
        url += "&groupselect=" + groupList.getSelected().getName();
      }
      if (!url.contains("&group=") && inputData.getGroup().getString() != null) {
        url += "&group=" + inputData.getGroup().getString();
      }
      if (!url.contains("&swversion=")
          && swVersionList != null
          && swVersionList.getSelected() != null) {
        url += "&swversion=" + swVersionList.getSelected();
      }
      url += "&" + inputData.getRealtime().getKey() + "=" + inputData.getRealtime().getBoolean();
      String[] aggregation = inputData.getAggregate().getStringArray();
      if (aggregation != null && aggregation.length > 0) {
        for (String aggr : aggregation) {
          url += "&aggregate=" + aggr;
        }
      }

      outputHandler.setDirectToPage(Page.REPORT, url);
    }
  }

  /**
   * Process image request.
   *
   * @param pageType the page type
   * @param res the res
   * @param session the session
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void processImageRequest(String pageType, Output res, HttpSession session)
      throws IOException {
    byte[] image = (byte[]) session.getAttribute("JFreeChartPNG" + pageType);
    if (image != null) {
      res.addNoCacheToResponse();
      res.writeImageBytesToResponse(image);
    }
  }

  /** The Class ChartLegendsDimensions. */
  class ChartLegendsDimensions {
    /** The MA x_ legend s_ p r_ column. */
    private int MAX_LEGENDS_PR_COLUMN = 21;

    /** The average length pr legend. */
    private int averageLengthPrLegend;

    /** The number of columns. */
    private int numberOfColumns;

    /**
     * Instantiates a new chart legends dimensions.
     *
     * @param aggregation the aggregation
     * @param report the report
     */
    ChartLegendsDimensions(String[] aggregation, Report<?> report) {
      Set<String> uniqueLegendsSet = new HashSet<>();
      for (Key key : report.getMap().keySet()) {
        uniqueLegendsSet.add(key.getKeyString(false, aggregation));
      }
      int totalLegendSize = 0;
      for (String uniqueLegend : uniqueLegendsSet) {
        totalLegendSize += uniqueLegend.length();
      }
      int uniqueLegendsSetSize = uniqueLegendsSet.size();
      if (uniqueLegendsSetSize == 0) {
        uniqueLegendsSetSize = 1;
      }
      averageLengthPrLegend = totalLegendSize / uniqueLegendsSetSize;
      numberOfColumns = uniqueLegendsSet.size() / MAX_LEGENDS_PR_COLUMN + 1;
    }
  }

  /**
   * Fix date millis.
   *
   * @param date the date
   * @return the date
   */
  private static Date fixDateMillis(Date date) {
    Calendar fix = Calendar.getInstance();
    fix.setTime(date);
    fix.add(Calendar.MILLISECOND, 1);
    return fix.getTime();
  }

  /**
   * Fix date minutes.
   *
   * @param date the date
   * @return the date
   */
  private static Date fixDateMinutes(Date date) {
    Calendar fix = Calendar.getInstance();
    fix.setTime(date);
    if (fix.get(Calendar.MINUTE) == 59) {
      fix.add(Calendar.MINUTE, 1);
    }
    return fix.getTime();
  }

  /**
   * Gets the types.
   *
   * @return the types
   */
  private PeriodType[] getTypes() {
    return new PeriodType[] {PeriodType.MONTH, PeriodType.DAY, PeriodType.HOUR, PeriodType.SECOND};
  }

  /**
   * Gets the next lower period type.
   *
   * @param t the t
   * @return the next lower period type
   */
  private PeriodType getNextLowerPeriodType(PeriodType t) {
    PeriodType[] types = getTypes();
    for (PeriodType type : types) {
      if (t.isLongerThan(type)) {
        return type;
      }
    }
    return types[types.length - 1];
  }

  /**
   * Gets the item start date.
   *
   * @param chart the chart
   * @param series the series
   * @param item the item
   * @return the item start date
   */
  private Date getItemStartDate(JFreeChart chart, int series, int item) {
    TimeSeriesCollection collection = (TimeSeriesCollection) chart.getXYPlot().getDataset();
    return collection.getSeries(series).getDataItem(item).getPeriod().getStart();
  }

  private Date getItemEndDate(JFreeChart chart, int series, int item) {
    TimeSeriesCollection collection = (TimeSeriesCollection) chart.getXYPlot().getDataset();
    return collection.getSeries(series).getDataItem(item).getPeriod().getEnd();
  }

  private boolean isUnittypeChanged(String sessionId) {
    String oldUnittype = SessionCache.getSessionData(sessionId).getUnittypeName();
    return inputData.getUnittype().notValue(oldUnittype);
  }

  public static List<String> getMethodOptions(String pageType) {
    ReportType type = ReportType.getEnum(pageType);
    if (type != null) {
      return type.getMethods();
    }
    return ReportType.UNIT.getMethods();
  }

  public static List<String> getMethodOptionsByType(ReportType type) {
    if (type != null) {
      return type.getMethods();
    }
    return ReportType.UNIT.getMethods();
  }

  private Integer getLegendIndex(String sessionId) {
    return isUnittypeChanged(sessionId) ? null : inputData.getLegendIndex().getInteger();
  }

  public Date getStartDate(Input input) throws ParseException {
    Calendar start = Calendar.getInstance();
    if (input.notNullNorValue("")) {
      start.setTime(input.getDate());
      start.set(Calendar.SECOND, 0);
      start.set(Calendar.MILLISECOND, 0);
    } else {
      start.setTime(new Date());
      start.set(Calendar.HOUR_OF_DAY, 0);
      start.set(Calendar.MINUTE, 0);
      start.set(Calendar.SECOND, 0);
      start.set(Calendar.MILLISECOND, 0);
      start.add(Calendar.WEEK_OF_YEAR, -1);
    }
    return start.getTime();
  }

  public Date getStartDateForRealtime() {
    Calendar start = Calendar.getInstance();
    if (inputData.getRealtime().getBoolean()) {
      start.setTime(new Date());
      start.add(Calendar.MINUTE, -5);
    }
    return start.getTime();
  }

  /**
   * Gets the end date.
   *
   * @param input the input
   * @return the end date
   */
  public Date getEndDate(Input input) {
    Calendar end = Calendar.getInstance();

    if (input.notNullNorValue("")) {
      end.setTime(input.getDate());
    } else {
      end.setTime(new Date());
    }

    if (end.get(Calendar.MINUTE) == 59) {
      end.add(Calendar.MINUTE, 1);
    }
    end.set(Calendar.SECOND, 0);
    end.set(Calendar.MILLISECOND, 0);

    return end.getTime();
  }

  /**
   * Gets the end date for realtime.
   *
   * @return the end date for realtime
   */
  public Date getEndDateForRealtime() {
    Calendar end = Calendar.getInstance();
    if (inputData.getRealtime().getBoolean()) {
      end.setTime(new Date());
    }
    return end.getTime();
  }

  /**
   * Gets the report voip generator.
   *
   * @param sessionId the session id
   * @param acs the xaps
   * @return the report voip generator
   * @throws SQLException the sQL exception the no available connection exception
   */
  public static ReportVoipGenerator getReportVoipGenerator(String sessionId, ACS acs)
      throws SQLException {
    return new ReportVoipGenerator(
        acs.getDataSource(), acs, null, ACSLoader.getIdentity(sessionId, acs.getDataSource()));
  }

  public static ReportProvisioningGenerator getReportProvGenerator(String sessionId, ACS acs)
      throws SQLException {
    return new ReportProvisioningGenerator(
        acs.getDataSource(), acs, null, ACSLoader.getIdentity(sessionId, acs.getDataSource()));
  }

  /**
   * Gets the report hardware generator.
   *
   * @param sessionId the session id
   * @param acs the xaps
   * @return the report hardware generator
   * @throws SQLException the sQL exception the no available connection exception
   */
  public static ReportHardwareGenerator getReportHardwareGenerator(String sessionId, ACS acs)
      throws SQLException {
    return new ReportHardwareGenerator(
        acs.getDataSource(), acs, null, ACSLoader.getIdentity(sessionId, acs.getDataSource()));
  }

  /**
   * Gets the report syslog generator.
   *
   * @param sessionId the session id
   * @param acs the xaps
   * @return the report syslog generator
   * @throws SQLException the sQL exception the no available connection exception
   */
  public static ReportSyslogGenerator getReportSyslogGenerator(String sessionId, ACS acs)
      throws SQLException {
    return new ReportSyslogGenerator(
        acs.getDataSource(), acs, null, ACSLoader.getIdentity(sessionId, acs.getDataSource()));
  }

  /**
   * Gets the report voip call generator.
   *
   * @param sessionId the session id
   * @param acs the xaps
   * @return the report voip call generator
   * @throws SQLException the sQL exception the no available connection exception
   */
  public static ReportVoipCallGenerator getReportVoipCallGenerator(String sessionId, ACS acs)
      throws SQLException {
    return new ReportVoipCallGenerator(
        acs.getDataSource(), acs, null, ACSLoader.getIdentity(sessionId, acs.getDataSource()));
  }

  /**
   * Gets the type.
   *
   * @param name the name
   * @return the type
   */
  private PeriodType getType(String name) {
    for (PeriodType type : getTypes()) {
      if (type.getTypeStr().equals(name)) {
        return type;
      }
    }
    return null;
  }

  /**
   * Gets the period type.
   *
   * @param period the period
   * @return the period type
   */
  private PeriodType getPeriodType(String period) {
    PeriodType periodType = getType(period);

    if (periodType == null) {
      periodType = PeriodType.DAY;
    }

    return periodType;
  }

  private static Map<String, String> reportType2TableNameMap = new HashMap<>();

  static {
    reportType2TableNameMap.put(ReportType.HARDWARE.getName(), "report_hw");
    reportType2TableNameMap.put(ReportType.HARDWARETR.getName(), "report_hw_tr");
    reportType2TableNameMap.put(ReportType.SYS.getName(), "report_syslog");
    reportType2TableNameMap.put(ReportType.GATEWAYTR.getName(), "report_gateway_tr");
    reportType2TableNameMap.put(ReportType.VOIP.getName(), "report_voip");
    reportType2TableNameMap.put(ReportType.PROV.getName(), "report_prov");
  }

  public static List<String> getSwVersion(
      ReportType type,
      Date start,
      Date end,
      Unittype unittype,
      Profile profile,
      ReportGenerator rgHardware)
      throws SQLException {
    List<String> swVersionList = new ArrayList<>();
    String tableName = reportType2TableNameMap.get(type.getName());
    if (unittype != null) {
      return rgHardware.getSoftwareVersions(unittype, profile, start, end, tableName);
    }
    return swVersionList;
  }

  private List<Group> getGroups(
      DropDownSingleSelect<Unittype> unittypes, DropDownSingleSelect<Profile> profiles) {
    Unittype unittype = unittypes.getSelected();
    Profile profile = profiles.getSelected();
    List<Group> groupList = new ArrayList<>();
    if (unittype == null) {
      return groupList;
    }
    Group[] groupArr = unittype.getGroups().getGroups();
    for (Group g : groupArr) {
      if (profile == null) {
        groupList.add(g);
        continue;
      }
      if (g.getTopParent().getProfile() != null
          && g.getTopParent().getProfile().getId().intValue() == profile.getId()) {
        groupList.add(g);
      }
    }
    return groupList;
  }

  private List<String> getOptionalMethodOptions(String method, List<String> list) {
    List<String> optionalmethods = new ArrayList<>();
    if (method == null) {
      return optionalmethods;
    }
    for (String m : list) {
      if (!m.equals(method)) {
        optionalmethods.add(m);
      }
    }
    return !optionalmethods.isEmpty() ? optionalmethods : null;
  }

  private String generateClickablePointUrl(
      PeriodType periodType, String pageType, String method, String optionalmethod) {
    String messageToDisplayWhileWaiting = "Loading";
    String url =
        "javascript:goToUrlAndWait('"
            + Page.REPORT.getUrl()
            + "&type="
            + pageType
            + "&series=%s&item=%s%AGGREGATION%";
    if (method != null) {
      url += "&method=" + method;
    }
    if (inputData.getAggregate().getStringArray() != null) {
      for (String aggregation : inputData.getAggregate().getStringArray()) {
        url += "&" + inputData.getAggregate().getKey() + "=" + aggregation;
      }
    }
    if (periodType.isLongerThan(PeriodType.SECOND)) {
      messageToDisplayWhileWaiting += " graph ...";
      url += "&" + inputData.getPeriod().getKey() + "=" + periodType.getTypeStr();
      if (inputData.getGroupSelect().notNullNorValue("")) {
        url +=
            "&"
                + inputData.getGroupSelect().getKey()
                + "="
                + inputData.getGroupSelect().getString();
      }
      if (inputData.getGroup().notNullNorValue("")) {
        url += "&" + inputData.getGroup().getKey() + "=" + inputData.getGroup().getString();
      }
      if (optionalmethod != null) {
        url += "&" + inputData.getOptionalMethod().getKey() + "=" + optionalmethod;
      }
      if (inputData.getAdvancedView().getBoolean() != null) {
        url += "&" + inputData.getAdvancedView().getKey() + "=" + Boolean.TRUE;
      }
      url += "&" + inputData.getRealtime().getKey() + "=" + inputData.getRealtime().getBoolean();
    } else {
      messageToDisplayWhileWaiting += " units ...";
    }
    url += "','" + messageToDisplayWhileWaiting + "')";
    return url;
  }

  private void displayReportChartWithImageMap(
      Map<String, Object> root,
      int numberOfColumns,
      int averageLengthPrLegend,
      List<String> aggregation,
      HttpSession session) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);

    boolean shouldZoom = true;

    switch (reportType) {
      case JOB:
      case UNIT:
        shouldZoom = false;
        break;
      default:
        break;
    }

    String clickablePointUrl = "";
    if (shouldZoom || periodType.getSelected().isLongerThan(PeriodType.HOUR)) {
      clickablePointUrl =
          generateClickablePointUrl(
              periodType.getSelected(),
              reportType.getName(),
              method.getSelected(),
              optionalmethod.getSelected());
    }

    XYPlot plot = (XYPlot) chart.getPlot();
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    XYURLGenerator urls = new ReportURLGenerator(clickablePointUrl, chart, aggregation);
    renderer.setURLGenerator(urls);
    XYSeriesLabelGenerator slg =
        new CustomXYSeriesLabelGenerator("javascript:xAPS.report.updateReport(%d);");
    renderer.setLegendItemURLGenerator(slg);
    renderer.setDefaultShapesVisible(true);
    renderer.setDrawOutlines(true);
    renderer.setUseFillPaint(true);
    renderer.setDefaultFillPaint(Color.white);

    try {
      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      ByteArrayOutputStream image = new ByteArrayOutputStream();

      int chartWidth = 700 + 10 * averageLengthPrLegend * numberOfColumns + 35 * numberOfColumns;

      ChartUtils.writeChartAsPNG(image, chart, chartWidth, 400, info);

      session.setAttribute("JFreeChartPNG" + reportType.getName(), image.toByteArray());

      ImageMapUtils.writeImageMap(
          writer,
          "chart" + reportType.getName(),
          info,
          arg0 -> " title=\"" + arg0 + "\" alt=\"" + arg0 + "\"",
          arg0 -> " href=\"" + arg0 + "\"");

      writer.println(
          "<img src=\""
              + Page.REPORT.getUrl()
              + "&type="
              + reportType.getName()
              + "&image=true&d="
              + new Date().getTime()
              + "\" border=\"0\" usemap=\"#chart"
              + reportType.getName()
              + "\" id=\"ImageMapImg\" alt=\"ReportImage\"/>");
      writer.close();

      root.put("imagemap", stringWriter.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String[] getSelectedAggregation(String[] aggregation, List<String> keyNames) {
    List<String> selectedAggregation = new ArrayList<>();

    if (aggregation == null) {
      return new String[] {};
    }

    if (aggregation.length == 0) {
      return aggregation;
    }

    for (String k : keyNames) {
      for (String a : aggregation) {
        if (a.equals(k)) {
          selectedAggregation.add(a);
          break;
        }
      }
    }

    return selectedAggregation.toArray(new String[] {});
  }

  public static Input getSelectedAggregation(Input aggregation, List<String> keyNames) {
    List<String> selectedAggregation = new ArrayList<>();

    if (aggregation.getStringArray() == null) {
      aggregation.setValue(new String[] {});
      return aggregation;
    }

    for (String k : keyNames) {
      for (String a : aggregation.getStringArray()) {
        if (a.equals(k)) {
          selectedAggregation.add(a);
          break;
        }
      }
    }

    aggregation.setValue(selectedAggregation.toArray(new String[] {}));

    return aggregation;
  }

  @Override
  public boolean requiresNoCache() {
    return true;
  }

  @Override
  public String getTitle(String page) {
    return super.getTitle(page)
        + (inputData.getType().notNullNorValue("") ? " | " + inputData.getType().getString() : "");
  }
}
