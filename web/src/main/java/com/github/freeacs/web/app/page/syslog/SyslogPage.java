package com.github.freeacs.web.app.page.syslog;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownMultiSelect;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.DateUtils;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/** The Class SyslogPage. */
public class SyslogPage extends AbstractWebPage {
  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    SyslogData inputData = (SyslogData) InputDataRetriever.parseInto(new SyslogData(), params);

    if (params.getBoolean("history")) {
      printSyslogList(params, outputHandler);
      return;
    }

    ACS acs = ACSLoader.getXAPS(params.getSession().getId(), xapsDataSource, syslogDataSource);

    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    ACSLoader.getACSUnit(params.getSession().getId(), xapsDataSource, syslogDataSource);

    // Fix
    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile());

    DropDownSingleSelect<Unittype> unittypes =
        InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    DropDownSingleSelect<Profile> profiles =
        InputSelectionFactory.getProfileSelection(
            inputData.getProfile(), inputData.getUnittype(), acs);

    String startTimeString =
        inputData.getTimestampStart().getDateOrDefaultFormatted(SyslogUtil.getDate());
    String endTimeString = inputData.getTimestampEnd().getDateFormatted();
    String unitIdString = inputData.getUnit().getString();

    Map<String, Object> map = outputHandler.getTemplateMap();

    map.put("unittypes", unittypes);
    map.put("profiles", profiles);
    map.put("unitid", unitIdString);

    map.put("starttime", startTimeString);
    map.put("endtime", endTimeString);

    map.put("syslogSupported", true);

    map.put("background", new SyslogUtil.GetBackgroundColor());
    map.put("fontcolor", new SyslogUtil.GetFontColor());
    map.put("eventdesc", new SyslogUtil.GetEventMouseOver(acs));
    map.put("severitytext", new SyslogUtil.GetSeverityText());
    map.put("facilitytext", new SyslogUtil.GetFacilityText());
    map.put("getprofilebyid", new SyslogUtil.GetUnittypeProfileById(acs));
    map.put("getprofilebyname", new SyslogUtil.GetUnittypeProfileByName(acs));
    map.put(inputData.getAdvanced().getKey(), inputData.getAdvanced().getBoolean());

    if (inputData.getAdvanced().getBoolean()) {
      map.put("events", getEventDropdown(unittypes, inputData));
      map.put("facilities", getFacilityDropdown(inputData));
      map.put("facilityversion", getFacilityVersionInput(inputData));
      map.put("severities", getSeverityDropdown(inputData));
      map.put("userid", getUserId(inputData));
      map.put("ipaddress", getIpAddress(inputData));
      map.put("message", getMessage(inputData));
      map.put("maxrows", getMaxRowsOrDefault(100, inputData));
    }

    if (inputData.getFormSubmit().isValue("Retrieve syslog")) {
      int maxrows = SyslogUtil.convertToInt(inputData.getMaxRows().getString());
      SyslogRetriever syslogRetriever = SyslogRetriever.getInstance(inputData);
      List<SyslogEntry> entries =
          syslogRetriever.getSyslogEntries(
              maxrows,
              unittypes.getSelected(),
              profiles.getSelected(),
              params.getSession().getId(),
              xapsDataSource,
              syslogDataSource);
      map.put("rowsreturned", entries.size());
      if (entries.size() > maxrows) {
        entries = entries.subList(0, maxrows);
      }
      map.put("result", entries);
      map.put("expectedrows", maxrows);
    }

    map.put("hashistory", false); // fixme
    String cmd = inputData.getCmd().getString();
    map.put("cmd", cmd);
    map.put("url", inputData.getUrl().getString());

    if (params.getBoolean("noreturn")) {
      return;
    }

    outputHandler.setTemplatePathWithIndex("syslog");
  }

  private DropDownSingleSelect<SyslogUtil.Event> getEventDropdown(
      DropDownSingleSelect<Unittype> unittypes, SyslogData syslogData) {
    return InputSelectionFactory.getDropDownSingleSelect(
        syslogData.getEvent(),
        SyslogUtil.getEvent(unittypes.getSelected(), syslogData.getEvent().getInteger()),
        SyslogUtil.getEvents(unittypes.getSelected()));
  }

  private DropDownSingleSelect<SyslogUtil.Facility> getFacilityDropdown(SyslogData syslogData) {
    return InputSelectionFactory.getDropDownSingleSelect(
        syslogData.getFacility(),
        SyslogUtil.getFacility(syslogData.getFacility().getInteger()),
        SyslogUtil.getFacilities());
  }

  private Input getFacilityVersionInput(SyslogData syslogData) {
    return syslogData.getFacilityVersion();
  }

  private DropDownMultiSelect<String> getSeverityDropdown(SyslogData syslogData) {
    List<String> severityValues = new ArrayList<>(SyslogConstants.severityMap.values());
    return getSeverityDropdown(severityValues, syslogData);
  }

  private DropDownMultiSelect<String> getSeverityDropdown(
      List<String> severityValues, SyslogData syslogData) {
    return InputSelectionFactory.getDropDownMultiSelect(
        syslogData.getSeverity(), syslogData.getSeverity().getStringArray(), severityValues);
  }

  private String getUserId(SyslogData syslogData) {
    return syslogData.getUserId().getString();
  }

  private String getIpAddress(SyslogData syslogData) {
    return syslogData.getIpaddress().getString();
  }

  private String getMessage(SyslogData syslogData) {
    return syslogData.getMessage().getString();
  }

  private int getMaxRowsOrDefault(int def, SyslogData syslogData) {
    return syslogData.getMaxRows().getInteger() != null
        ? syslogData.getMaxRows().getInteger()
        : def;
  }

  /**
   * Prints the syslog list.
   *
   * @param req the req
   * @param res the res
   */
  private void printSyslogList(ParameterParser req, Output res) {
    res.setContentType("text/plain");
    res.setDownloadAttachment("syslogexport.txt");
    List<SyslogEntry> entries = new ArrayList<>(); // fixme SessionCache.getSyslogEntries(req.getSession().getId());
    StringBuilder string = new StringBuilder();
    if (entries != null && !entries.isEmpty()) {
      string.append("Timestamp" + "\t");
      string.append("Severity" + "\t");
      string.append("Facility" + "\t");
      string.append("Event ID" + "\t");
      string.append("Content" + "\t");
      string.append("User" + "\t");
      string.append("Host name" + "\t");
      string.append("IP address" + "\t");
      string.append("Unit ID" + "\t");
      string.append("Profile" + "\t");
      string.append("Unit Type" + "\t");
      string.append("\n");
      for (SyslogEntry entry : entries) {
        string.append(DateUtils.formatDateDefault(entry.getCollectorTimestamp())).append("\t");
        string.append(entry.getSeverity()).append("\t");
        string.append(entry.getFacility()).append("\t");
        string.append(entry.getEventId()).append("\t");
        string.append(entry.getContent()).append("\t");
        string.append(entry.getUserId()).append("\t");
        string.append(entry.getHostname()).append("\t");
        string.append(entry.getIpAddress()).append("\t");
        string.append(entry.getUnitId()).append("\t");
        string.append(entry.getProfileName()).append("\t");
        string.append(entry.getUnittypeName()).append("\t");
        string.append("\n");
      }
    } else {
      string.append(
          "This feature is not currently not working.");
    }
    res.setDirectResponse(string.toString());
  }
}
