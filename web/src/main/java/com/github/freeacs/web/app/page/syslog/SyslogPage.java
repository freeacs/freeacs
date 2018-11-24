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
import com.github.freeacs.web.app.util.WebConstants;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The Class SyslogPage. */
public class SyslogPage extends AbstractWebPage {

  private static final int DEFAULT_MAX_ROWS = 100;

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    SyslogData inputData = (SyslogData) InputDataRetriever.parseInto(new SyslogData(), params);

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
      map.put("maxrows", getMaxRowsOrDefault(inputData));
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

  private int getMaxRowsOrDefault(SyslogData syslogData) {
    return syslogData.getMaxRows().getInteger() != null
        ? syslogData.getMaxRows().getInteger()
        : DEFAULT_MAX_ROWS;
  }
}
