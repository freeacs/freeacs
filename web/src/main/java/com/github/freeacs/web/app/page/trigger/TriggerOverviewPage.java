package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class TriggerOverviewPage extends AbstractWebPage {
  private TriggerData inputData;
  private TriggerHandler triggerHandler;

  private static final String DELETE_ACTION = "delete";
  private static final String EDIT_ACTION = "edit";
  public static final String CREATE_BUTTON = "Create Trigger";
  public static final String EDIT_BUTTON = "Update Trigger";

  public String getTitle(String page) {
    return super.getTitle(page);
  }

  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Unit type overview", Page.UNITTYPEOVERVIEW));
    list.add(
        new MenuItem("Create new trigger", Page.CREATETRIGGER)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Trigger overview", Page.TRIGGEROVERVIEW)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Trigger release status", Page.TRIGGERRELEASE)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Trigger release history", Page.TRIGGERRELEASEHISTORY)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Syslog Events overview", Page.SYSLOGEVENTS)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Heartbeat overview", Page.HEARTBEATS)
            .addParameter("unttype", sessionData.getUnittypeName()));
    return list;
  }

  @Override
  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    inputData = (TriggerData) InputDataRetriever.parseInto(new TriggerData(), params);
    String sessionId = params.getSession().getId();

    ACS acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile());

    outputHandler
        .getTemplateMap()
        .put("unittypes", InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs));

    Unittype unittype = acs.getUnittype(inputData.getUnittype().getString());

    if (unittype != null) {
      triggerHandler = new TriggerHandler(sessionId, inputData, xapsDataSource, syslogDataSource);
      outputHandler
          .getTemplateMap()
          .put("triggertablelist", triggerHandler.getTriggerTableElements());
      if (inputData.getAction().hasValue(EDIT_ACTION)) {
        setOutputDataForEditTriggerMode(outputHandler);
      } else { // CREATE_ACTION - put a "blank" configuration
        setOutputDataForCreateTriggerMode(outputHandler);
      }
      if (inputData.getAction().hasValue(DELETE_ACTION)) {
        triggerHandler.deleteTrigger(inputData);
        outputHandler.setRedirectTarget(Page.TRIGGEROVERVIEW.getUrl());
      } else if (inputData.getFormSubmit().hasValue(EDIT_BUTTON)) {
        triggerHandler.editTrigger(inputData);
        outputHandler.setRedirectTarget(
            Page.TRIGGEROVERVIEW.getUrl()
                + "&action=edit&triggerId="
                + inputData.getTriggerId().getValue());
      } else if (inputData.getFormSubmit().hasValue(CREATE_BUTTON)) {
        triggerHandler.createTrigger(inputData);
        outputHandler.setRedirectTarget(Page.TRIGGEROVERVIEW.getUrl());
      }
    }
    outputHandler.setTemplatePath("/trigger/triggerOverview.ftl");
  }

  private void setOutputDataForEditTriggerMode(Output outputHandler) {
    Trigger trigger = triggerHandler.getTrigger(inputData.getTriggerId().getInteger());
    outputHandler.getTemplateMap().put("buttonname", EDIT_BUTTON);
    outputHandler.getTemplateMap().put("createUpdateHeaderName", "Edit Trigger");
    outputHandler.getTemplateMap().put("trigger", trigger);
    outputHandler
        .getTemplateMap()
        .put("syslogEvents", triggerHandler.getSyslogEventDropdown(trigger.getSyslogEvent()));
    outputHandler
        .getTemplateMap()
        .put("typeTrigger", triggerHandler.getTriggertypeDropdown(trigger.getTriggerType()));
    outputHandler
        .getTemplateMap()
        .put(
            "evalPeriodMinutes",
            triggerHandler.getNewEvalPeriodDropdown(trigger.getEvalPeriodMinutes()));
    outputHandler
        .getTemplateMap()
        .put(
            "notifyIntervalHours",
            triggerHandler.getNotifyIntervalHoursDropdown(trigger.getNotifyIntervalHours()));
    outputHandler
        .getTemplateMap()
        .put("parentTrigger", triggerHandler.getTriggerParentDropdown(trigger));
    outputHandler
        .getTemplateMap()
        .put("notifyType", triggerHandler.getNotifyTypeDropdown(trigger.getNotifyType()));
    outputHandler
        .getTemplateMap()
        .put("scriptFiles", triggerHandler.getScriptFileDropdown(trigger.getScript()));
  }

  private void setOutputDataForCreateTriggerMode(Output outputHandler) {
    outputHandler.getTemplateMap().put("buttonname", CREATE_BUTTON);
    outputHandler.getTemplateMap().put("createUpdateHeaderName", "Create Trigger");
    outputHandler.getTemplateMap().put("syslogEvents", triggerHandler.getSyslogEventDropdown(null));
    outputHandler.getTemplateMap().put("typeTrigger", triggerHandler.getTriggertypeDropdown(null));
    outputHandler
        .getTemplateMap()
        .put("evalPeriodMinutes", triggerHandler.getNewEvalPeriodDropdown(null));
    outputHandler
        .getTemplateMap()
        .put("notifyIntervalHours", triggerHandler.getNotifyIntervalHoursDropdown(null));
    outputHandler
        .getTemplateMap()
        .put("parentTrigger", triggerHandler.getTriggerParentDropdown(null));
    outputHandler.getTemplateMap().put("notifyType", triggerHandler.getNotifyTypeDropdown(null));
    outputHandler.getTemplateMap().put("scriptFiles", triggerHandler.getScriptFileDropdown(null));
  }
}
