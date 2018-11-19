package com.github.freeacs.web.app.page.heartbeat;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Heartbeat;
import com.github.freeacs.dbi.Heartbeats;
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

/** The Class HeartbeatsPage. */
public class HeartbeatsPage extends AbstractWebPage {
  /** The input data. */
  private HeartbeatsData inputData;

  /** The xaps. */
  private ACS acs;

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    /* Parse input data to the servlet */
    inputData = (HeartbeatsData) InputDataRetriever.parseInto(new HeartbeatsData(), params);

    /* Retrieve the XAPS object from session */
    acs = ACSLoader.getXAPS(params.getSession().getId(), xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    /* Update (if necessary) the session state, so that unittype/profile context-menus are ok */
    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype());

    /* Make the unittype-dropdown */
    DropDownSingleSelect<Unittype> unittypes =
        InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    outputHandler.getTemplateMap().put("unittypes", unittypes);

    if (unittypes.getSelected() != null) {
      Heartbeat heartbeat = action(params, outputHandler, unittypes.getSelected());
      output(outputHandler, unittypes.getSelected(), heartbeat);
    }
    outputHandler.setTemplatePath("heartbeat/heartbeats.ftl");
  }

  /**
   * Should output an event, if an event is chosen. If not a default "blank" event must be the
   * output Will always output a list of all possible events within this unittype.
   */
  private void output(Output outputHandler, Unittype unittype, Heartbeat hb) {
    Map<String, Object> fmMap = outputHandler.getTemplateMap();

    /* Get the heartbeats object */
    Heartbeats heartbeats = unittype.getHeartbeats();

    /* Output for the configuration */
    if (hb != null) {
      fmMap.put("heartbeat", hb);
    } else if (inputData.getId().getInteger() != null) {
      hb = heartbeats.getById(inputData.getId().getInteger());
      fmMap.put("heartbeat", hb);
    }

    Group selectedGroup = hb != null ? hb.getGroup() : null;
    DropDownSingleSelect<Group> groups =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getGroupId(), selectedGroup, Arrays.asList(unittype.getGroups().getGroups()));
    fmMap.put("groups", groups);

    /* Output for the event list */
    fmMap.put("heartbeats", heartbeats.getHeartbeats());
  }

  /**
   * Will delete, add or edit a heartbeat event. Will only update fmMap with error/info in the fmMap
   * If a heartbeat is returned, it is part of an add/update action and should be displayed in the
   * output
   */
  private Heartbeat action(ParameterParser params, Output outputHandler, Unittype unittype)
      throws IllegalAccessException, InvocationTargetException {
    Map<String, Object> fmMap = outputHandler.getTemplateMap();
    Heartbeats heartbeats = unittype.getHeartbeats();

    if (inputData.getAction().isValue("delete") && inputData.getId().getInteger() != null) {
      try {
        heartbeats.deleteHeartbeat(heartbeats.getById(inputData.getId().getInteger()), acs);
      } catch (Throwable ex) {
        fmMap.put("error", "Could not delete Heartbeat " + ex.getLocalizedMessage());
      }
    } else if (inputData.getFormSubmit().notNullNorValue("")) { // add or edit a trigger
      Heartbeat heartbeat = heartbeats.getById(inputData.getId().getInteger());
      if (heartbeat == null) { // Extra code to add heartbeat
        heartbeat = new Heartbeat();
        heartbeat.setUnittype(unittype);
        if (inputData.getName().getString() != null) {
          heartbeat.setName(inputData.getName().getString());
        }
      }
      heartbeat.validateInput(false);
      //			if (inputData.getGroupId().getInteger() != null)
      heartbeat.setGroup(unittype.getGroups().getById(inputData.getGroupId().getInteger()));
      //			if (inputData.getExpression().getString() != null)
      heartbeat.setExpression(inputData.getExpression().getString());
      //			if (inputData.getTimeout().getInteger() != null)
      heartbeat.setTimeoutHours(inputData.getTimeout().getInteger());
      heartbeat.validateInput(true);
      if (inputData.validateForm()) {
        try {
          heartbeats.addOrChangeHeartbeat(heartbeat, acs);
        } catch (Throwable ex) {
          fmMap.put("error", "Could not add Heartbeat: " + ex.getLocalizedMessage());
        }
      } else {
        fmMap.put("errors", inputData.getErrors());
      }
      return heartbeat;
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
        new MenuItem("Trigger overview", Page.TRIGGEROVERVIEW)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Trigger releases", Page.TRIGGERRELEASE)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Trigger release history", Page.TRIGGERRELEASEHISTORY)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Syslog Events overview", Page.SYSLOGEVENTS)
            .addParameter("unittype", sessionData.getUnittypeName()));
    return list;
  }
}
