package com.owera.xaps.web.app.page.trigger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.owera.xaps.dbi.ScriptExecution;
import com.owera.xaps.dbi.ScriptExecutions;
import com.owera.xaps.dbi.Trigger;
import com.owera.xaps.dbi.TriggerRelease;
import com.owera.xaps.dbi.Triggers;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.Input;
import com.owera.xaps.web.app.input.InputDataIntegrity;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.menu.MenuItem;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.table.TableElement;
import com.owera.xaps.web.app.table.TableElementMaker;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.SessionData;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

public class TriggerReleasePage extends AbstractWebPage {

	private static SimpleDateFormat urlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private TriggerReleaseData inputData;
	private String sessionId;
	@SuppressWarnings("unused")
	private Output outputHandler;
	private XAPS xaps;
	private Unittype unittype;

	public List<MenuItem> getShortcutItems(SessionData sessionData) {
		List<MenuItem> list = new ArrayList<MenuItem>();
		list.addAll(super.getShortcutItems(sessionData));
		list.add(new MenuItem("Unit type overview", Page.UNITTYPEOVERVIEW).addParameter("unittype", sessionData.getUnittypeName()));
		list.add(new MenuItem("Create new trigger", Page.CREATETRIGGER).addParameter("unittype", sessionData.getUnittypeName()));
		list.add(new MenuItem("Trigger overview", Page.TRIGGEROVERVIEW).addParameter("unittype", sessionData.getUnittypeName()));
		list.add(new MenuItem("Trigger release history", Page.TRIGGERRELEASEHISTORY).addParameter("unittype", sessionData.getUnittypeName()));
		list.add(new MenuItem("Syslog Events overview", Page.SYSLOGEVENTS).addParameter("unittype", sessionData.getUnittypeName()));
		list.add(new MenuItem("Heartbeat overview", Page.HEARTBEATS).addParameter("unttype", sessionData.getUnittypeName()));
		return list;
	}

	private String getURLDate(Date d) {
		if (d != null)
			try {
				return URLEncoder.encode(urlFormat.format(d), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				return "";
			}
		return "";
	}

	@Override
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (TriggerReleaseData) InputDataRetriever.parseInto(new TriggerReleaseData(), params);
		this.sessionId = params.getSession().getId();
		this.outputHandler = outputHandler;
		Map<String, Object> fmMap = outputHandler.getTemplateMap();
		this.xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}
		Date tms = new Date();
		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile(), inputData.getUnit());
		Input tmsInput = inputData.getTms();
		if (tmsInput != null && tmsInput.getValue() != null)
			tms = tmsInput.getDate();
		Date twoHoursBeforeTms = new Date(tms.getTime() - 7200 * 1000);
		fmMap.put("tms", urlFormat.format(tms));
		fmMap.put("twohoursbeforetms", urlFormat.format(twoHoursBeforeTms));
		fmMap.put("unittypes", InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps));
		this.unittype = xaps.getUnittype(inputData.getUnittype().getString());
		if (unittype != null) {
			/* The table elements */
			List<TableElement> triggerTableElements = new TableElementMaker().getTriggers(unittype, xaps);
			outputHandler.getTemplateMap().put("triggertablelist", triggerTableElements);

			/* Map to retrieve ReleaseTrigger object for every trigger */
			//			Map<Integer, ReleaseTrigger> releaseTriggerMap = new HashMap<Integer, ReleaseTrigger>();
			Triggers triggers = unittype.getTriggers();
			for (Trigger trigger : triggers.getTriggers()) {
				ReleaseTrigger rt = new ReleaseTrigger();
				//				Date evalPeriodStart = new Date(tms.getTime() - trigger.getEvalPeriodMinutes() * 60000);
				Date evalPeriodStart = new Date(tms.getTime() - 3600 * 1000 * 2);
				List<TriggerRelease> trList = triggers.readTriggerReleases(trigger, evalPeriodStart, tms, xaps, null);
				for (TriggerRelease tr : trList) {
					if (rt.getReleasedTms() == null) {
						rt.setReleasedTms(tr.getReleaseTms());
						rt.setFirstEventTms(tr.getFirstEventTms());
						rt.setReleaseId(tr.getId());
					}
					if (rt.getNotifiedTms() == null && tr.getSentTms() != null)
						rt.setNotifiedTms(tr.getSentTms());
					rt.setNoEvents(tr.getNoEvents());
					rt.setNoEventsPrUnit(tr.getNoEventsPrUnit());
					rt.setNoUnits(tr.getNoUnits());					
				}
				if (trigger.getTriggerType() == Trigger.TRIGGER_TYPE_BASIC && rt.getReleasedTms() != null) {
					String syslogQS = "?";
					syslogQS += "page=syslog&";
					syslogQS += "tmsstart=" + getURLDate(rt.getFirstEventTms()) + "&";
					syslogQS += "tmsend=" + getURLDate(rt.getReleasedTms()) + "&";
					syslogQS += "event=" + trigger.getSyslogEvent().getEventId() + "&";
					syslogQS += "cmd=auto&advancedView=true";
					rt.setSyslogPageQueryString(syslogQS);
				}
				if (trigger.getScript() != null && rt.getReleasedTms() != null) {
					ScriptExecutions scriptExecutions = new ScriptExecutions(SessionCache.getXAPSConnectionProperties(sessionId));
					ScriptExecution exec = scriptExecutions.getExecution(unittype, "TRIGGER:" + rt.getReleaseId());
					rt.setScriptExecution(exec);
				}
				// A very cumbersome and rather ugly way to do it:
				// We add each ReleaseTrigger object to the TableElements-list, to be able to retrieve
				// it on the Freemarker Template page. I tried with a simple map first, but was unable
				// to retrieve any object from that map, based on keys from the table-element list.
				for (TableElement te : triggerTableElements) {
					if (te.getTrigger().getId().intValue() == trigger.getId()) {
						te.setReleaseTrigger(rt);
					}
				}
			}
		}
		outputHandler.setTemplatePath("/trigger/triggerRelease.ftl");
	}
}
