package com.owera.xaps.monitor.task;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.html.Element;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Trigger;
import com.owera.xaps.dbi.TriggerRelease;
import com.owera.xaps.dbi.Triggers;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.monitor.Properties;
import com.owera.xaps.monitor.SendMail;

public class TriggerNotification {

	private static Logger log = new Logger();
	private static SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm");
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.US);
	private static SimpleDateFormat urlFormat = new SimpleDateFormat("yyyy-MM-dd&20HH:mm");

	public static void checkAllTriggers(DBI dbi) {
		for (Unittype unittype : dbi.getXaps().getUnittypes().getUnittypes()) {
			Triggers triggers = unittype.getTriggers();
			for (Trigger trigger : triggers.getTriggers()) {
				if (trigger.getNotifyType() == Trigger.NOTIFY_TYPE_ALARM || trigger.getNotifyType() == Trigger.NOTIFY_TYPE_REPORT) {
					try {
						checkTrigger(unittype, triggers, trigger, dbi.getXaps());
						long hours = Trigger.NOTIFY_INTERVAL_MAX + 1;
						// The timestamp is 1 hour more back in time than NOTIFY_INTERVAL_MAX (probably 168)
						long upUntilTms = System.currentTimeMillis() - (hours * 3600 * 1000);
						int rowsDeleted = triggers.deleteHistory(new Date(upUntilTms), dbi.getXaps());
						if (rowsDeleted > 0) {
							log.info("Deleted " + rowsDeleted + " rows from TriggerRelease because they were more than " + hours + " old");
						}
					} catch (Throwable t) {
						log.warn("Checked all triggers and trigger " + trigger.getName() + " failed for some reason: " + t);
					}
				}
			}
		}
	}

	public static void checkTrigger(Unittype unittype, Triggers triggers, Trigger trigger, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Date now = new Date();
		Date notifyIntervalStart = new Date(now.getTime() - trigger.getNotifyIntervalHours() * 3600 * 1000);
		List<TriggerRelease> triggerReleaseList = triggers.readTriggerReleases(trigger, notifyIntervalStart, now, xaps, null);
		log.debug("Found " + triggerReleaseList.size() + " entries in trigger_release table for trigger " + trigger.getName() + " (" + trigger.getId() + ") in unittype " + unittype.getName());
		if (triggerReleaseList.size() > 0) {
			TriggerRelease newestTH = triggerReleaseList.get(0);
			log.debug("The newest trigger history is from " + newestTH.getReleaseTms());
			boolean notifySentInNotifyPeriod = false;
			for (TriggerRelease th : triggerReleaseList) {
				if (th.getSentTms() != null)
					notifySentInNotifyPeriod = true;
			}
			if (!notifySentInNotifyPeriod) {
				boolean releaseInEvalutionPeriod = newestTH.getReleaseTms().after(new Date(now.getTime() - trigger.getEvalPeriodMinutes() * 3600));
				if (trigger.getNotifyType() == Trigger.NOTIFY_TYPE_REPORT || (trigger.getNotifyType() == Trigger.NOTIFY_TYPE_ALARM && releaseInEvalutionPeriod)) {
					String msg = makeNotification(triggerReleaseList);
					SendMail.sendNotification(trigger, msg);
					newestTH.setSentTms(new Date());
					triggers.addOrChangeHistory(newestTH, xaps);
				}
			}
		}
	}

	private static String getWebURL() {
		return Properties.getFusionURLBase() + "xapsweb/web";
	}

	private static String makeNotification(List<TriggerRelease> triggerReleaseList) {
		Trigger trigger = triggerReleaseList.get(0).getTrigger();
		Element html = new Element("html");
		Element body = html.body();
		Element heading = body.h(3, null);

		heading.add(trigger.getNotifyTypeAsStr() + " from trigger ");

		StringBuffer queryString = new StringBuffer();
		queryString.append("page=trigger-release-history&");
		queryString.append("triggerId=" + trigger.getId() + "&");
		queryString.append("unittype=" + trigger.getUnittype().getName() + "&");
		queryString.append("tmsstart=" + urlFormat.format(new Date(System.currentTimeMillis() - 24 * 3600 * 1000)) + "&");
		queryString.append("tmsend=" + urlFormat.format(new Date()));

		heading.a(trigger.getName(), "href=" + getWebURL() + "?" + queryString);
		body.add("Number of trigger releases : " + triggerReleaseList.size());
		body.p();
		String text = "";
		for (TriggerRelease triggerRelease : triggerReleaseList) {
			String firstEventClock = clockFormat.format(triggerRelease.getFirstEventTms());
			String firstEventDate = dateFormat.format(triggerRelease.getFirstEventTms());
			String releaseEventClock = clockFormat.format(triggerRelease.getReleaseTms());
			String releaseEventDate = dateFormat.format(triggerRelease.getReleaseTms());
			if (trigger.getTriggerType() == Trigger.TRIGGER_TYPE_BASIC) {
				text += "Based on <b>" + triggerRelease.getNoEvents() + "</b> trigger events from <b>" + triggerRelease.getNoUnits() + "</b>";
				text += " units (timeframe: ";
				if (firstEventDate.equals(releaseEventDate))
					text += "<b>" + firstEventClock + "-" + releaseEventClock + "</b> on <b>" + releaseEventDate + "</b>)<br>";
				else
					text += "<b>" + firstEventClock + "-" + releaseEventClock + "</b> on <b>" + firstEventDate + "</b>)<br>";
			} else {
				for (Trigger child : trigger.getAllChildren()) {
					if (child.getTriggerType() == Trigger.TRIGGER_TYPE_BASIC) {
						text += "Based on trigger <b>" + child.getName() + "</b> with <b>" + child.getNoEvents() + "</b> trigger events from <b>" + child.getNoUnits() + "</b>";
						text += " units (timeframe: ";
						if (firstEventDate.equals(releaseEventDate))
							text += "<b>" + firstEventClock + "-" + releaseEventClock + "</b> on <b>" + releaseEventDate + "</b>)<br>";
						else
							text += "<b>" + firstEventClock + "-" + releaseEventClock + "</b> on <b>" + firstEventDate + "</b>)<br>";
					}
				}
			}
		}
		body.add(text);
		body.p();
		return html.toString("");
	}

}
