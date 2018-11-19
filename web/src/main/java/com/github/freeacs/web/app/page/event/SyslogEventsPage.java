package com.github.freeacs.web.app.page.event;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.SyslogEvent;
import com.github.freeacs.dbi.SyslogEvent.StorePolicy;
import com.github.freeacs.dbi.SyslogEvents;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/** The Class SyslogEventsPage. */
public class SyslogEventsPage extends AbstractWebPage {
  /** The input data. */
  private SyslogEventsData inputData;

  /** The xaps. */
  private ACS acs;

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    /* Parse input data to the servlet */
    inputData = (SyslogEventsData) InputDataRetriever.parseInto(new SyslogEventsData(), params);

    /* Retrieve the XAPS object from session */
    acs = ACSLoader.getXAPS(params.getSession().getId(), xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    /* Update (if necessary) the session state, so that unittype/profile context-menus are ok */
    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile());

    /* FreeMarker-map, contains all necessary objects for the template */
    Map<String, Object> fmMap = outputHandler.getTemplateMap();

    /* Make the unittype-dropdown */
    DropDownSingleSelect<Unittype> unittypes =
        InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    fmMap.put("unittypes", unittypes);
    if (unittypes.getSelected() != null) {
      SyslogEvent syslogEvent = action(params, outputHandler, unittypes.getSelected());
      output(outputHandler, unittypes.getSelected(), syslogEvent);
    }
    outputHandler.setTemplatePath("events/events.ftl");
  }

  /**
   * Should output an event, if an event is chosen. If not a default "blank" event must be the
   * output Will always output a list of all possible events within this unittype.
   */
  private void output(Output outputHandler, Unittype unittype, SyslogEvent event) {
    Map<String, Object> fmMap = outputHandler.getTemplateMap();

    /* Get the syslog events object */
    SyslogEvents events = unittype.getSyslogEvents();

    /* Output for the configuration */
    if (event != null) {
      fmMap.put("event", event);
    } else if (inputData.getEventId().getInteger() != null) {
      event = events.getByEventId(inputData.getEventId().getInteger());
      fmMap.put("event", event);
    }

    Group selectedGroup = event != null ? event.getGroup() : null;
    DropDownSingleSelect<Group> groups =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getGroupId(), selectedGroup, Arrays.asList(unittype.getGroups().getGroups()));
    fmMap.put("groups", groups);

    StorePolicy selectedSP = event != null ? event.getStorePolicy() : null;
    DropDownSingleSelect<StorePolicy> storePolicies =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getStorePolicy(), selectedSP, Arrays.asList(StorePolicy.values()));
    fmMap.put("storepolicies", storePolicies);

    List<File> allFiles = Arrays.asList(unittype.getFiles().getFiles());
    List<File> scriptFiles = new ArrayList<>();
    for (File f : allFiles) {
      if (f.getType() == FileType.SHELL_SCRIPT) {
        scriptFiles.add(f);
      }
    }
    File selectedScript = event != null ? event.getScript() : null;
    fmMap.put(
        "scripts",
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getScript(), selectedScript, scriptFiles));

    /* Output for the event list */
    fmMap.put("events", events.getSyslogEvents());
  }

  /**
   * Will delete, add or edit a syslog event. Will only update fmMap with error/info in the fmMap If
   * a syslogEvent is returned, it is part of an add/update action and should be displayed in the
   * output
   */
  private SyslogEvent action(ParameterParser params, Output outputHandler, Unittype unittype)
      throws IllegalAccessException, InvocationTargetException {
    Map<String, Object> fmMap = outputHandler.getTemplateMap();
    SyslogEvents events = unittype.getSyslogEvents();

    if (inputData.getAction().isValue("delete") && inputData.getEventId().getInteger() != null) {
      try {
        events.deleteSyslogEvent(events.getByEventId(inputData.getEventId().getInteger()), acs);
      } catch (Throwable ex) {
        fmMap.put("error", "Could not delete syslog event " + ex.getLocalizedMessage());
      }
    } else if (inputData.getFormSubmit().notNullNorValue("")) { // add or edit a trigger
      if (inputData.validateForm()) {
        try {
          SyslogEvent syslogEvent = events.getByEventId(inputData.getEventId().getInteger());
          if (syslogEvent == null) { // Extra code to add syslog-event
            syslogEvent = new SyslogEvent();
            syslogEvent.setUnittype(unittype);
            syslogEvent.setEventId(inputData.getEventId().getInteger());
          }
          // add or update
          syslogEvent.setName(inputData.getName().getString());
          syslogEvent.setDescription(inputData.getDescription().getString());
          if (inputData.getGroupId().getValue() != null) {
            syslogEvent.setGroup(unittype.getGroups().getById(inputData.getGroupId().getInteger()));
          } else {
            syslogEvent.setGroup(null);
          }
          syslogEvent.setExpression(inputData.getExpression().getString());
          syslogEvent.setStorePolicy(StorePolicy.valueOf(inputData.getStorePolicy().getString()));
          if (inputData.getScript().getValue() != null) {
            syslogEvent.setScript(unittype.getFiles().getById(inputData.getScript().getInteger()));
          } else {
            syslogEvent.setScript(null);
          }
          syslogEvent.setDeleteLimit(inputData.getLimit().getInteger());
          events.addOrChangeSyslogEvent(syslogEvent, acs);
          return syslogEvent;
        } catch (Throwable ex) {
          fmMap.put("error", "Could not add Syslog Event: " + ex.getLocalizedMessage());
        }
      } else {
        fmMap.put("errors", inputData.getErrors());
      }
    }
    return null;
  }

  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Unit type overview", Page.UNITTYPEOVERVIEW));
    list.add(
        new MenuItem("Trigger overview", Page.TRIGGEROVERVIEW)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Trigger releases", Page.TRIGGERRELEASE)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Trigger release history", Page.TRIGGERRELEASEHISTORY)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Heartbeat overview", Page.HEARTBEATS)
            .addParameter("unittype", sessionData.getUnittypeName()));
    return list;
  }
}
