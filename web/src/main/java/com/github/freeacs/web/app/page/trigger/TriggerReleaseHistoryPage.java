package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ScriptExecution;
import com.github.freeacs.dbi.ScriptExecutions;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.TriggerRelease;
import com.github.freeacs.dbi.Triggers;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class TriggerReleaseHistoryPage extends AbstractWebPage {
  private static SimpleDateFormat urlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(
        new MenuItem("Unit type overview", Page.UNITTYPEOVERVIEW)
            .addParameter("unittype", sessionData.getUnittypeName()));
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
        new MenuItem("Syslog Events overview", Page.SYSLOGEVENTS)
            .addParameter("unittype", sessionData.getUnittypeName()));
    list.add(
        new MenuItem("Heartbeat overview", Page.HEARTBEATS)
            .addParameter("unttype", sessionData.getUnittypeName()));
    return list;
  }

  private String getURLDate(Date d) {
    if (d != null) {
      try {
        return URLEncoder.encode(urlFormat.format(d), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        return "";
      }
    }
    return "";
  }

  @Override
  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    TriggerReleaseHistoryData inputData =
        (TriggerReleaseHistoryData)
            InputDataRetriever.parseInto(new TriggerReleaseHistoryData(), params);
    String sessionId = params.getSession().getId();
    outputHandler.getTemplateMap().put("triggerOverviewUrl", Page.TRIGGEROVERVIEW.getUrl());
    Map<String, Object> fmMap = outputHandler.getTemplateMap();
    ACS acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }
    Date tmsEnd = new Date();
    Date tmsStart = new Date(tmsEnd.getTime() - 24 * 3600 * 1000);
    InputDataIntegrity.loadAndStoreSession(
        params,
        outputHandler,
        inputData,
        inputData.getUnittype(),
        inputData.getProfile(),
        inputData.getUnit());
    Input tmsEndInput = inputData.getTmsEnd();
    if (tmsEndInput != null && tmsEndInput.getValue() != null) {
      tmsEnd = tmsEndInput.getDate();
    }
    Input tmsStartInput = inputData.getTmsStart();
    if (tmsStartInput != null && tmsStartInput.getValue() != null) {
      tmsStart = tmsStartInput.getDate();
      if (tmsStart.after(tmsEnd)) {
        tmsStart = new Date(tmsEnd.getTime() - 24 * 3600 * 1000);
      }
    }
    fmMap.put("tmsEnd", urlFormat.format(tmsEnd));
    fmMap.put("tmsStart", urlFormat.format(tmsStart));
    fmMap.put(
        "unittypes", InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs));
    Unittype unittype = acs.getUnittype(inputData.getUnittype().getString());
    if (unittype != null) {
      Triggers triggers = unittype.getTriggers();
      Input triggerIdInput = inputData.getTriggerId();
      Trigger trigger = null;
      fmMap.put(
          "triggers", InputSelectionFactory.getTriggerSelection(triggerIdInput, unittype, acs));
      List<ReleaseTrigger> releaseTriggerList = new ArrayList<>();
      fmMap.put("releasetriggers", releaseTriggerList);
      if (triggerIdInput != null && triggerIdInput.getValue() != null) {
        trigger = triggers.getById(triggerIdInput.getInteger());
        fmMap.put("trigger", trigger);
      }
      List<TriggerRelease> trList =
          triggers.readTriggerReleases(trigger, tmsStart, tmsEnd, acs, null);
      for (TriggerRelease tr : trList) {
        ReleaseTrigger rt = new ReleaseTrigger();
        rt.setTrigger(tr.getTrigger());
        if (rt.getReleasedTms() == null) {
          rt.setReleasedTms(tr.getReleaseTms());
          rt.setFirstEventTms(tr.getFirstEventTms());
          rt.setReleaseId(tr.getId());
        }
        if (rt.getNotifiedTms() == null && tr.getSentTms() != null) {
          rt.setNotifiedTms(tr.getSentTms());
        }
        if (rt.getTrigger().getTriggerType() == Trigger.TRIGGER_TYPE_BASIC
            && rt.getReleasedTms() != null) {
          String syslogQS = "?";
          syslogQS += "page=syslog&";
          syslogQS += "unittype=" + unittype.getName() + "&";
          syslogQS += "tmsstart=" + getURLDate(rt.getFirstEventTms()) + "&";
          syslogQS += "tmsend=" + getURLDate(rt.getReleasedTms()) + "&";
          syslogQS += "event=" + rt.getTrigger().getSyslogEvent().getEventId() + "&";
          syslogQS += "cmd=auto&advancedView=true";
          rt.setSyslogPageQueryString(syslogQS);
        }
        if (rt.getTrigger().getScript() != null && rt.getReleasedTms() != null) {
          ScriptExecutions scriptExecutions = new ScriptExecutions(xapsDataSource);
          ScriptExecution exec =
              scriptExecutions.getExecution(unittype, "TRIGGER:" + rt.getReleaseId());
          rt.setScriptExecution(exec);
        }
        rt.setNoEvents(tr.getNoEvents());
        rt.setNoEventsPrUnit(tr.getNoEventsPrUnit());
        rt.setNoUnits(tr.getNoUnits());
        releaseTriggerList.add(rt);
      }
    }
    outputHandler.setTemplatePath("/trigger/triggerReleaseHistory.ftl");
  }
}
