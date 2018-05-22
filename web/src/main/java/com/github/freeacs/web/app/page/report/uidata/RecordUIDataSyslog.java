package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.web.app.page.syslog.SyslogUtil;
import freemarker.template.TemplateModelException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This wrapper class contains helper methods, and also a static converter method <code>convertRecords(List<SyslogEntry> records,XAPS xaps)</code> for a larger set of SyslogEntry objects.
 * 
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataSyslog {

	/** The entry. */
	private SyslogEntry entry;

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
	 * Instantiates a new record ui data syslog.
	 *
	 * @param record the record
	 */
	public RecordUIDataSyslog(SyslogEntry record) {
		this.entry = record;
	}

	/**
	 * Convert records.
	 *
	 * @param records the records
	 * @return the list
	 */
	public static List<RecordUIDataSyslog> convertRecords(List<SyslogEntry> records) {
		List<RecordUIDataSyslog> list = new ArrayList<RecordUIDataSyslog>();
		for (SyslogEntry record : records) {
			list.add(new RecordUIDataSyslog(record));
		}
		return list;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return entry.getContent();
	}

}
