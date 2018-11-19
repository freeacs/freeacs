package com.github.freeacs.web.app.page.report;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.RecordHardware;
import com.github.freeacs.dbi.report.RecordProvisioning;
import com.github.freeacs.dbi.report.RecordSyslog;
import com.github.freeacs.dbi.report.RecordVoip;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportHardwareGenerator;
import com.github.freeacs.dbi.report.ReportProvisioningGenerator;
import com.github.freeacs.dbi.report.ReportSyslogGenerator;
import com.github.freeacs.dbi.report.ReportVoipGenerator;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataHardware;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataHardwareFilter;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataHwSum;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataProv;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataSyslogFilter;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataSyslogFromReport;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataSyslogSumFromReport;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.DateUtils;
import com.github.freeacs.web.app.util.WebConstants;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;

/**
 * This page represents the unit list page that you will land on after zooming down into a syslog
 * report.
 *
 * @author Jarl Andre Hubenthal
 */
public class UnitListPage extends AbstractWebPage {
  private DropDownSingleSelect<Unittype> unittype;
  private DropDownSingleSelect<Profile> profile;
  private Date fromDate;
  private Date toDate;
  private ACS acs;
  private ACSUnit acsUnit;
  private ParameterParser req;
  private UnitListData inputData;

  public void process(
      ParameterParser req,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    this.req = req;

    inputData = (UnitListData) InputDataRetriever.parseInto(new UnitListData(), req);

    acs = ACSLoader.getXAPS(req.getSession().getId(), xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    InputDataIntegrity.loadAndStoreSession(
        req, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile());

    Map<String, Object> root = outputHandler.getTemplateMap();

    acsUnit = ACSLoader.getACSUnit(req.getSession().getId(), xapsDataSource, syslogDataSource);

    unittype = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    profile =
        InputSelectionFactory.getProfileSelection(
            inputData.getProfile(), inputData.getUnittype(), acs);
    fromDate = getStartDate(inputData.getStart());
    toDate = getEndDate(inputData.getEnd());

    ReportType reportType = ReportType.getEnum(inputData.getType().getString());

    /*
     * For use by mostly all types of reports. The Group report does NOT use on this variable, since
     * it uses the default group Input from inputData.
     */
    Group groupSelect = null;
    if (unittype.getSelected() != null) {
      groupSelect =
          unittype.getSelected().getGroups().getByName(inputData.getGroupSelect().getString());
    }

    if (reportType == ReportType.VOIP) {
      displayVoipReport(root, groupSelect);
    } else if (reportType == ReportType.HARDWARE) {
      ReportHardwareGenerator rgHardware =
          ReportPage.getReportHardwareGenerator(req.getSession().getId(), acs);
      List<String> swVersions =
          ReportPage.getSwVersion(
              ReportType.HARDWARE,
              fromDate,
              toDate,
              unittype.getSelected(),
              profile.getSelected(),
              rgHardware);
      displayHardwareReport(root, rgHardware, groupSelect, swVersions);
    } else if (reportType == ReportType.SYS) {
      displaySyslogReport(root, groupSelect);
      //		} else if (reportType == ReportType.GROUP) {
      //			displayGroupReport(root);
    } else if (reportType == ReportType.PROV) {
      displayProvReport(root, groupSelect);
    } else {
      throw new UnsupportedOperationException(
          "ReportType not valid: " + inputData.getType().getString());
    }

    root.put("backgroundcolor", new RowBackgroundColorMethod());
    root.put("divideby", new DivideBy());
    root.put("friendlytime", new FriendlyTimeRepresentationMethod());
    root.put("start", DateUtils.formatDateDefault(fromDate));
    root.put("end", DateUtils.formatDateDefault(toDate));
    root.put("unittype", unittype);
    root.put("profile", profile);
    root.put("type", reportType.getName());

    outputHandler.setTemplatePathWithIndex("unit-list");
  }

  /**
   * Private void displayGroupReport(Map<String, Object> root) throws SQLException, IOException {
   * Group group = null; if (unittype.getSelected() != null) group =
   * unittype.getSelected().getGroups().getByName(inputData.getGroup().getString()); if (group ==
   * null || group.getTimeParameter() == null) throw new UnsupportedOperationException("Group for
   * ReportType Group is not a time rolling group"); ReportGroupGenerator rgGroup =
   * ReportPage.getReportGroupGenerator(req.getSession().getId(), xaps); Map<String,
   * Report<RecordGroup>> reports = rgGroup.generateFromSyslog(PeriodType.DAY, fromDate, toDate,
   * unittype.getSelectedOrAllItemsAsList(), group); Map<Unit, RecordUIDataGroupSumFromReport>
   * records = new HashMap<Unit, RecordUIDataGroupSumFromReport>(); for (Entry<String,
   * Report<RecordGroup>> entry : reports.entrySet()) { Unit unit =
   * xapsUnit.getUnitById(entry.getKey()); if (unit == null) unit = new Unit(entry.getKey(), null,
   * null); RecordUIDataGroupSumFromReport unitRecordSum = null; if (records.get(unit) == null) {
   * unitRecordSum = new RecordUIDataGroupSumFromReport(unit); records.put(unit, unitRecordSum); }
   * Collection<? extends RecordUIDataGroupFromReport> convertedRecords =
   * RecordUIDataGroupFromReport.convertRecords(unit, entry.getValue().getMap().values()); for
   * (RecordUIDataGroupFromReport rec : convertedRecords) unitRecordSum.addRecord(rec); }
   * DropDownSingleSelect<Group> groups =
   * InputSelectionFactory.getGroupSelection(inputData.getGroup(), unittype.getSelected(), xaps);
   * root.put("groups", groups); root.put("reports", records.values()); }
   */
  private void displaySyslogReport(Map<String, Object> root, Group groupSelect)
      throws SQLException, IOException, ParseException {
    ReportSyslogGenerator rgSyslog =
        ReportPage.getReportSyslogGenerator(req.getSession().getId(), acs);
    Map<String, Report<RecordSyslog>> reports =
        rgSyslog.generateFromSyslog(
            PeriodType.DAY,
            fromDate,
            toDate,
            unittype.getSelectedOrAllItemsAsList(),
            profile.getSelectedOrAllItemsAsList(),
            groupSelect);
    Map<Unit, RecordUIDataSyslogSumFromReport> records = new HashMap<>();
    RecordUIDataSyslogFilter filter = new RecordUIDataSyslogFilter(inputData, root);
    for (Entry<String, Report<RecordSyslog>> entry : reports.entrySet()) {
      Unit unit = acsUnit.getUnitById(entry.getKey());

      if (unit == null) {
        unit = new Unit(entry.getKey(), null, null);
      }

      RecordUIDataSyslogSumFromReport unitRecordSum = null;

      if (records.get(unit) == null) {
        unitRecordSum = new RecordUIDataSyslogSumFromReport(unit);
        records.put(unit, unitRecordSum);
      }

      List<RecordUIDataSyslogFromReport> convertedRecords =
          RecordUIDataSyslogFromReport.convertRecords(unit, entry.getValue().getMap().values());

      /*
       * Iterate over each record and filter it according to parameter input. This is done with a
       * logical AND test.
       */
      for (RecordUIDataSyslogFromReport rec : convertedRecords) {
        boolean isRelevant =
            filter.eventid == null
                || rec.getEntry().getEventId() == null
                || rec.getEntry().getEventId().equals(filter.eventid);

        if (isRelevant && filter.facility != null && rec.getEntry().getFacility() != null) {
          isRelevant = rec.getEntry().getFacility().equals(filter.facility);
        }
        if (isRelevant && filter.severity != null && rec.getEntry().getSeverity() != null) {
          isRelevant = rec.getEntry().getSeverity().equals(filter.severity);
        }

        if (isRelevant) {
          unitRecordSum.addRecord(rec);
        }
      }

      if (unitRecordSum.getRecords().isEmpty() || !filter.isRecordSumRelevant(unitRecordSum)) {
        records.remove(unit);
      }
    }
    List<RecordUIDataSyslogSumFromReport> list = new ArrayList<>(records.values());
    if (filter.max_rows != null && list.size() > filter.max_rows) {
      Collections.sort(list);
      list = list.subList(0, filter.max_rows);
    }
    root.put("reports", list);
  }

  private void displayHardwareReport(
      Map<String, Object> root,
      ReportHardwareGenerator rgHardware,
      Group groupSelect,
      List<String> swVersions)
      throws SQLException, IOException {
    Map<String, Report<RecordHardware>> reports =
        rgHardware.generateFromSyslog(
            PeriodType.DAY,
            fromDate,
            toDate,
            unittype.getSelectedOrAllItemsAsList(),
            profile.getSelectedOrAllItemsAsList(),
            groupSelect);
    Map<Unit, List<RecordUIDataHardware>> records = new HashMap<>();
    RecordUIDataHardwareFilter limits = new RecordUIDataHardwareFilter(inputData, root);
    String swVersionFromReport = req.getParameter("softwareversion");
    String selectedSoftwareVersion =
        swVersionFromReport != null ? swVersionFromReport : inputData.getSwVersion().getString();
    DropDownSingleSelect<String> swVersionList =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getSwVersion(), selectedSoftwareVersion, swVersions);
    for (Entry<String, Report<RecordHardware>> entry : reports.entrySet()) {
      Unit unit = acsUnit.getUnitById(entry.getKey());

      if (unit == null) {
        unit = new Unit(entry.getKey(), null, null);
      }

      if (swVersionList.getSelected() != null) {
        String cpSW = unit.getParameters().get(SystemParameters.SOFTWARE_VERSION);
        if (cpSW == null || !cpSW.equals(swVersionList.getSelected())) {
          continue;
        }
      }

      if (records.get(unit) == null) {
        records.put(unit, new ArrayList<RecordUIDataHardware>());
      }

      records
          .get(unit)
          .addAll(
              RecordUIDataHardware.convertRecords(
                  unit, new ArrayList<RecordHardware>(entry.getValue().getMap().values()), limits));

      List<RecordUIDataHardware> recs = new ArrayList<>();

      List<RecordUIDataHardware> _recs = records.get(unit);

      /*
       * Iterate over each record and filter it according to parameter input. This is done with a
       * logical AND test.
       */
      for (RecordUIDataHardware uiDataRecord : _recs) {
        boolean include =
            swVersionFromReport == null
                || (uiDataRecord.getSoftwareVersion() != null
                    && uiDataRecord.getSoftwareVersion().equals(swVersionFromReport));

        if (include) {
          recs.add(uiDataRecord);
        }
      }

      if (recs.isEmpty()) {
        records.remove(unit);
      } else {
        records.put(unit, recs);
      }
    }
    List<RecordUIDataHwSum> hwSums = processUnitHardwareRecords(records);
    root.put("reports", hwSums);
    root.put("swVersionList", swVersionList);
  }

  private void displayProvReport(Map<String, Object> root, Group groupSelect)
      throws SQLException, IOException {
    ReportProvisioningGenerator rgProv =
        ReportPage.getReportProvGenerator(req.getSession().getId(), acs);
    Map<String, Report<RecordProvisioning>> reports =
        rgProv.generateFromSyslog(
            PeriodType.DAY,
            fromDate,
            toDate,
            unittype.getSelectedOrAllItemsAsList(),
            profile.getSelectedOrAllItemsAsList(),
            groupSelect);
    root.put("records", RecordUIDataProv.convertRecords(acsUnit, reports));
  }

  private void displayVoipReport(Map<String, Object> root, Group groupSelect)
      throws SQLException, IOException {
    ReportVoipGenerator rgVoip = ReportPage.getReportVoipGenerator(req.getSession().getId(), acs);
    Map<String, Report<RecordVoip>> reports =
        rgVoip.generateFromSyslog(
            PeriodType.DAY,
            fromDate,
            toDate,
            unittype.getSelectedOrAllItemsAsList(),
            profile.getSelectedOrAllItemsAsList(),
            groupSelect);
    List<RecordWrapper<RecordVoip>> recordsWorking = new ArrayList<>();
    List<RecordWrapper<RecordVoip>> recordsDown = new ArrayList<>();
    for (Entry<String, Report<RecordVoip>> entry : reports.entrySet()) {
      Unit unit = acsUnit.getUnitById(entry.getKey());

      if (unit == null) {
        unit = new Unit(entry.getKey(), null, null);
      }

      String line = req.getParameter("line");
      root.put("filter_line", line);
      String sw = req.getParameter("softwareversion");
      root.put("filter_softwareversion", sw);

      List<RecordVoip> records = new ArrayList<>();

      List<RecordVoip> _records = new ArrayList<>(entry.getValue().getMap().values());

      /*
       * Iterate over each record and filter it according to parameter input. This is done with a
       * logical AND test.
       */
      for (RecordVoip rec : _records) {
        boolean include =
            sw == null || (rec.getSoftwareVersion() != null && rec.getSoftwareVersion().equals(sw));

        if (include && line != null) {
          include = rec.getLine() != null && rec.getLine().equals(line);
        }

        if (include) {
          records.add(rec);
        }
      }

      if (!records.isEmpty()) {
        RecordVoip record = records.get(0);
        for (int i = 1; i < records.size(); i++) {
          record.add(records.get(i));
        }
        if (record.getCallLengthTotal() != null && record.getCallLengthTotal().get() > 0) {
          recordsWorking.add(new RecordWrapper<RecordVoip>(unit, record));
        } else if (record.getNoSipServiceTime() != null
            && record.getNoSipServiceTime().get() != null
            && record.getNoSipServiceTime().get() > 0) {
          recordsDown.add(new RecordWrapper<RecordVoip>(unit, record));
        }
      }
    }
    Collections.sort(recordsWorking, new RecordTotalScoreComparator());
    Collections.sort(recordsDown, new RecordSipRegFailedComparator());
    root.put("reports", recordsWorking);
    root.put("failed", recordsDown);
    root.put("mosavgbad", new IsMosAvgBad(recordsWorking));
    root.put("sipregbad", new IsSipRegisterCause(recordsWorking));
  }

  /**
   * Process unit hardware records.
   *
   * @param records the records
   * @return the list
   */
  private List<RecordUIDataHwSum> processUnitHardwareRecords(
      Map<Unit, List<RecordUIDataHardware>> records) {
    List<RecordUIDataHwSum> sums = new ArrayList<>();
    for (Entry<Unit, List<RecordUIDataHardware>> entry : records.entrySet()) {
      Unit unit = entry.getKey();
      RecordUIDataHwSum sum = new RecordUIDataHwSum(unit);
      for (RecordUIDataHardware record : entry.getValue()) {
        sum.addRecordIfRelevant(record);
      }
      if (!sum.getRecords().isEmpty()) {
        sums.add(sum);
      }
    }
    return sums;
  }

  /**
   * Gets the start date.
   *
   * @param input the input
   * @return the start date
   * @throws ParseException the parse exception
   */
  public Date getStartDate(Input input) throws ParseException {
    Calendar start = Calendar.getInstance();
    if (input.notNullNorValue("")) {
      start.setTime(input.getDate());
      start.set(Calendar.SECOND, 0);
      start.set(Calendar.MILLISECOND, 0);
    } else {
      start.setTime(new Date());
      start.set(Calendar.SECOND, 0);
      start.set(Calendar.MILLISECOND, 0);
      start.add(Calendar.HOUR, -1);
    }
    return start.getTime();
  }

  /**
   * Gets the end date.
   *
   * @param input the input
   * @return the end date
   * @throws ParseException the parse exception
   */
  public Date getEndDate(Input input) throws ParseException {
    Calendar end = Calendar.getInstance();
    if (input.notNullNorValue("")) {
      end.setTime(input.getDate());
    } else {
      end.setTime(new Date());
    }
    end.set(Calendar.SECOND, 0);
    end.set(Calendar.MILLISECOND, 0);
    return end.getTime();
  }

  /**
   * The Class RecordWrapper.
   *
   * @param <V> the value type
   */
  public class RecordWrapper<V extends RecordVoip> {
    /**
     * Gets the unit.
     *
     * @return the unit
     */
    public Unit getUnit() {
      return unit;
    }

    /**
     * Instantiates a new record wrapper.
     *
     * @param key the key
     * @param record the record
     */
    public RecordWrapper(Unit key, V record) {
      this.voipRecord = record;
      this.unit = key;
    }

    /** The voip record. */
    public RecordVoip voipRecord;

    /** The unit. */
    public Unit unit;

    /**
     * Gets the record.
     *
     * @return the record
     */
    public RecordVoip getRecord() {
      return voipRecord;
    }
  }

  /** The Class RecordTotalScoreComparator. */
  @SuppressWarnings("rawtypes")
  private class RecordTotalScoreComparator implements Comparator<RecordWrapper> {
    public int compare(RecordWrapper o1, RecordWrapper o2) {
      if (o1.getRecord().getVoIPQuality().get() < o2.getRecord().getVoIPQuality().get()) {
        return 1;
      }
      if (o1.getRecord().getVoIPQuality().get() > o2.getRecord().getVoIPQuality().get()) {
        return -1;
      }
      return 0;
    }
  }

  /** The Class RecordSipRegFailedComparator. */
  @SuppressWarnings("rawtypes")
  private class RecordSipRegFailedComparator implements Comparator<RecordWrapper> {
    public int compare(RecordWrapper o1, RecordWrapper o2) {
      if (o1.getRecord().getNoSipServiceTime().get() < o2.getRecord().getNoSipServiceTime().get()) {
        return 1;
      }
      if (o1.getRecord().getNoSipServiceTime().get() > o2.getRecord().getNoSipServiceTime().get()) {
        return -1;
      }
      return 0;
    }
  }

  /**
   * Find record.
   *
   * @param id the id
   * @param recordsWorking the records working
   * @return the record voip
   */
  @SuppressWarnings({"rawtypes"})
  private RecordVoip findRecord(String id, List<RecordWrapper<RecordVoip>> recordsWorking) {
    for (RecordWrapper wrapper : recordsWorking) {
      if (wrapper.unit.getId().equals(id)) {
        return wrapper.getRecord();
      }
    }
    return null;
  }

  /** The Class IsMosAvgBad. */
  public class IsMosAvgBad implements TemplateMethodModel {
    /** The units. */
    private Map<String, Boolean> units = new HashMap<>();

    /** The records. */
    private List<RecordWrapper<RecordVoip>> records;

    /**
     * Instantiates a new checks if is mos avg bad.
     *
     * @param records the records
     */
    public IsMosAvgBad(List<RecordWrapper<RecordVoip>> records) {
      this.records = records;
    }

    @SuppressWarnings("rawtypes")
    public Boolean exec(List arg0) throws TemplateModelException {
      if (arg0.isEmpty()) {
        throw new TemplateModelException("Specify unitId");
      }

      String key = (String) arg0.get(0);

      if (units.containsKey(key)) {
        return units.get(key);
      }

      RecordVoip record = findRecord(key, records);

      if (record != null) {
        return record.getMosAvg().get() != null
            && record.getMosAvg().get() < 300
            && record.getCallLengthTotal().get() > 5;
      }

      return false;
    }
  }

  /** The Class IsSipRegisterCause. */
  public class IsSipRegisterCause implements TemplateMethodModel {
    /** The units. */
    private Map<String, Boolean> units = new HashMap<>();

    /** The records. */
    private List<RecordWrapper<RecordVoip>> records;

    /**
     * Instantiates a new checks if is sip register cause.
     *
     * @param records the records
     */
    public IsSipRegisterCause(List<RecordWrapper<RecordVoip>> records) {
      this.records = records;
    }

    @SuppressWarnings("rawtypes")
    public Boolean exec(List arg0) throws TemplateModelException {
      if (arg0.isEmpty()) {
        throw new TemplateModelException("Specify unitId");
      }

      String key = (String) arg0.get(0);

      if (units.containsKey(key)) {
        return units.get(key);
      }

      RecordVoip record = findRecord(key, records);

      if (record != null) {
        return record.getNoSipServiceTime().get() > 100
            && !new IsMosAvgBad(records).exec(Arrays.asList(key));
      }

      return false;
    }
  }
}
