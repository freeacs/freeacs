package com.owera.xaps.web.app.page.report.uidata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.owera.xaps.dbi.SyslogEntry;
import com.owera.xaps.dbi.SyslogEvent;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.app.page.syslog.SyslogUtil;

import freemarker.template.TemplateModelException;

/**
 * This wrapper class contains helper methods, and also a static converter method <code>convertRecords(List<SyslogEntry> records,XAPS xaps)</code> for a larger set of SyslogEntry objects.
 * 
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataSyslog {

	/** The xaps. */
	private XAPS xaps;

	/** The entry. */
	private SyslogEntry entry;

	/**
	 * Instantiates a new record ui data syslog.
	 */
	RecordUIDataSyslog() {
	}

	/**
	 * Gets the severity.
	 *
	 * @return the severity
	 * @throws TemplateModelException the template model exception
	 */
	public String getSeverity() throws TemplateModelException {
		return new SyslogUtil.GetSeverityText().exec(Arrays.asList(entry.getSeverity().toString()));
	}

	/**
	 * Gets the facility.
	 *
	 * @return the facility
	 * @throws TemplateModelException the template model exception
	 */
	public String getFacility() throws TemplateModelException {
		return new SyslogUtil.GetFacilityText().exec(Arrays.asList(entry.getFacility().toString()));
	}

	/**
	 * Gets the ip address.
	 *
	 * @return the ip address
	 */
	public String getIpAddress() {
		return entry.getIpAddress();
	}

	/**
	 * Instantiates a new record ui data syslog.
	 *
	 * @param record the record
	 * @param xaps the xaps
	 */
	public RecordUIDataSyslog(SyslogEntry record, XAPS xaps) {
		this.entry = record;
		this.xaps = xaps;
	}

	/**
	 * Convert records.
	 *
	 * @param records the records
	 * @param xaps the xaps
	 * @return the list
	 */
	public static List<RecordUIDataSyslog> convertRecords(List<SyslogEntry> records, XAPS xaps) {
		List<RecordUIDataSyslog> list = new ArrayList<RecordUIDataSyslog>();
		for (SyslogEntry record : records) {
			list.add(new RecordUIDataSyslog(record, xaps));
		}
		return list;
	}

	/**
	 * Gets the message excerpt.
	 *
	 * @return the message excerpt
	 */
	public String getMessageExcerpt() {
		if (entry.getContent() == null || entry.getContent().length() > 90)
			return entry.getContent().substring(0, 90);
		return entry.getContent();
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return entry.getContent();
	}

	/**
	 * Gets the event id as string.
	 *
	 * @return the event id as string
	 */
	public String getEventIdAsString() {
		Unittype unittype = xaps.getUnittype(entry.getUnittypeName());
		if (unittype != null) {
			@SuppressWarnings("static-access")
			SyslogEvent event = unittype.getSyslogEvents().getById(entry.getEventId());
			if (event != null)
				return entry.getEventId() + "(" + event.getName() + ")";
		}
		return "" + entry.getEventId();
	}

	/**
	 * Gets the tms as string.
	 *
	 * @return the tms as string
	 */
	public String getTmsAsString() {
		return RecordUIDataConstants.DATE_FORMAT.format(entry.getCollectorTimestamp()).replace(" ", "&nbsp;");
	}

	/**
	 * Gets the row background style.
	 *
	 * @return the row background style
	 */
	public String getRowBackgroundStyle() {
		return "background-color:#" + SyslogUtil.getBackgroundColor(entry.getSeverity()) + ";";
	}
}
