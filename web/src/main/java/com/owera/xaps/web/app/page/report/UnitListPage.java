package com.owera.xaps.web.app.page.report;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Certificate;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.report.PeriodType;
import com.owera.xaps.dbi.report.RecordHardware;
import com.owera.xaps.dbi.report.RecordProvisioning;
import com.owera.xaps.dbi.report.RecordSyslog;
import com.owera.xaps.dbi.report.RecordVoip;
import com.owera.xaps.dbi.report.Report;
import com.owera.xaps.dbi.report.ReportHardwareGenerator;
import com.owera.xaps.dbi.report.ReportProvisioningGenerator;
import com.owera.xaps.dbi.report.ReportSyslogGenerator;
import com.owera.xaps.dbi.report.ReportVoipGenerator;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.Input;
import com.owera.xaps.web.app.input.InputDataIntegrity;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.page.report.uidata.RecordUIDataHardware;
import com.owera.xaps.web.app.page.report.uidata.RecordUIDataHardwareFilter;
import com.owera.xaps.web.app.page.report.uidata.RecordUIDataHwSum;
import com.owera.xaps.web.app.page.report.uidata.RecordUIDataProv;
import com.owera.xaps.web.app.page.report.uidata.RecordUIDataSyslogFilter;
import com.owera.xaps.web.app.page.report.uidata.RecordUIDataSyslogFromReport;
import com.owera.xaps.web.app.page.report.uidata.RecordUIDataSyslogSumFromReport;
import com.owera.xaps.web.app.util.DateUtils;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * This page represents the unit list page that you will land on after zooming down into a syslog report.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public class UnitListPage extends AbstractWebPage {
	private DropDownSingleSelect<Unittype> unittype;
	private DropDownSingleSelect<Profile> profile;
	private Date fromDate;
	private Date toDate;
	private XAPS xaps;
	private XAPSUnit xapsUnit;
	private ParameterParser req;
	private UnitListData inputData;

	public void process(ParameterParser req, Output outputHandler) throws Exception {
		this.req = req;

		inputData = (UnitListData) InputDataRetriever.parseInto(new UnitListData(), req);

		xaps = XAPSLoader.getXAPS(req.getSession().getId());
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		InputDataIntegrity.loadAndStoreSession(req, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile());

		Map<String, Object> root = outputHandler.getTemplateMap();

		/*
		Certificate cert = xaps.getCertificates().getCertificate(Certificate.CERT_TYPE_REPORT);
		if (cert == null || cert.isValid(null) == false || !SessionCache.getSessionData(req.getHttpServletRequest().getSession().getId()).getUser().isReportsAllowed()) {
			root.put("message", "No valid certificate found for Reports page. Please contact your systems administrator.");
			outputHandler.setTemplatePath("/exception.ftl");
			return;
		}
		*/

		xapsUnit = XAPSLoader.getXAPSUnit(req.getSession().getId());

		unittype = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		profile = InputSelectionFactory.getProfileSelection(inputData.getProfile(), inputData.getUnittype(), xaps);
		fromDate = getStartDate(inputData.getStart());
		toDate = getEndDate(inputData.getEnd());

		ReportType reportType = ReportType.getEnum(inputData.getType().getString());

		/**
		 * For use by mostly all types of reports.
		 * The Group report does NOT use on this variable, since it uses the default group Input from inputData.
		 */
		Group groupSelect = null;
		if (unittype.getSelected() != null)
			groupSelect = unittype.getSelected().getGroups().getByName(inputData.getGroupSelect().getString());

		if (reportType == ReportType.VOIP)
			displayVoipReport(root, groupSelect);
		else if (reportType == ReportType.HARDWARE) {
			ReportHardwareGenerator rgHardware = ReportPage.getReportHardwareGenerator(req.getSession().getId(), xaps);
			List<String> swVersions = ReportPage.getSwVersion(ReportType.HARDWARE, fromDate, toDate, unittype.getSelected(), profile.getSelected(), rgHardware);
			displayHardwareReport(root, rgHardware, groupSelect, swVersions);
		} else if (reportType == ReportType.SYS) {
			displaySyslogReport(root, groupSelect);
			//		} else if (reportType == ReportType.GROUP) {
			//			displayGroupReport(root);
		} else if (reportType == ReportType.PROV) {
			displayProvReport(root, groupSelect);
		} else {
			throw new UnsupportedOperationException("ReportType not valid: " + inputData.getType().getString());
		}

		root.put("backgroundcolor", new RowBackgroundColorMethod());
		root.put("divideby", new DivideBy());
		root.put("friendlytime", new FriendlyTimeRepresentationMethod());
		root.put("start", DateUtils.formatDateDefault(fromDate));
		root.put("end", DateUtils.formatDateDefault(toDate));
		root.put("unittype", unittype);
		root.put("profile", profile);
		root.put("type", reportType.getName());

		outputHandler.setTemplatePathWithIndex("unit-list");
	}

	//	private void displayGroupReport(Map<String, Object> root) throws SQLException, NoAvailableConnectionException, IOException {
	//		Group group = null;
	//		if (unittype.getSelected() != null)
	//			group = unittype.getSelected().getGroups().getByName(inputData.getGroup().getString());
	//		if (group == null || group.getTimeParameter() == null)
	//			throw new UnsupportedOperationException("Group for ReportType Group is not a time rolling group");
	//		ReportGroupGenerator rgGroup = ReportPage.getReportGroupGenerator(req.getSession().getId(), xaps);
	//		Map<String, Report<RecordGroup>> reports = rgGroup.generateFromSyslog(PeriodType.DAY, fromDate, toDate, unittype.getSelectedOrAllItemsAsList(), group);
	//		Map<Unit, RecordUIDataGroupSumFromReport> records = new HashMap<Unit, RecordUIDataGroupSumFromReport>();
	//		for (Entry<String, Report<RecordGroup>> entry : reports.entrySet()) {
	//			Unit unit = xapsUnit.getUnitById(entry.getKey());
	//
	//			if (unit == null)
	//				unit = new Unit(entry.getKey(), null, null);
	//
	//			RecordUIDataGroupSumFromReport unitRecordSum = null;
	//
	//			if (records.get(unit) == null) {
	//				unitRecordSum = new RecordUIDataGroupSumFromReport(unit);
	//				records.put(unit, unitRecordSum);
	//			}
	//
	//			Collection<? extends RecordUIDataGroupFromReport> convertedRecords = RecordUIDataGroupFromReport.convertRecords(unit, entry.getValue().getMap().values());
	//
	//			for (RecordUIDataGroupFromReport rec : convertedRecords)
	//				unitRecordSum.addRecord(rec);
	//		}
	//		DropDownSingleSelect<Group> groups = InputSelectionFactory.getGroupSelection(inputData.getGroup(), unittype.getSelected(), xaps);
	//		root.put("groups", groups);
	//		root.put("reports", records.values());
	//	}

	private void displaySyslogReport(Map<String, Object> root, Group groupSelect) throws SQLException, NoAvailableConnectionException, IOException, ParseException {
		ReportSyslogGenerator rgSyslog = ReportPage.getReportSyslogGenerator(req.getSession().getId(), xaps);
		Map<String, Report<RecordSyslog>> reports = rgSyslog.generateFromSyslog(PeriodType.DAY, fromDate, toDate, unittype.getSelectedOrAllItemsAsList(), profile.getSelectedOrAllItemsAsList(),
				groupSelect);
		Map<Unit, RecordUIDataSyslogSumFromReport> records = new HashMap<Unit, RecordUIDataSyslogSumFromReport>();
		RecordUIDataSyslogFilter filter = new RecordUIDataSyslogFilter(inputData, root);
		for (Entry<String, Report<RecordSyslog>> entry : reports.entrySet()) {
			Unit unit = xapsUnit.getUnitById(entry.getKey());

			if (unit == null)
				unit = new Unit(entry.getKey(), null, null);

			RecordUIDataSyslogSumFromReport unitRecordSum = null;

			if (records.get(unit) == null) {
				unitRecordSum = new RecordUIDataSyslogSumFromReport(unit);
				records.put(unit, unitRecordSum);
			}

			List<RecordUIDataSyslogFromReport> convertedRecords = RecordUIDataSyslogFromReport.convertRecords(unit, entry.getValue().getMap().values());

			/**
			 * Iterate over each record and filter it according to parameter input.
			 * This is done with a logical AND test.
			 */
			for (RecordUIDataSyslogFromReport rec : convertedRecords) {
				boolean isRelevant = true;

				if (filter.eventid != null && rec.getEntry().getEventId() != null)
					isRelevant = rec.getEntry().getEventId().equals(filter.eventid);
				if (isRelevant && filter.facility != null && rec.getEntry().getFacility() != null)
					isRelevant = rec.getEntry().getFacility().equals(filter.facility);
				if (isRelevant && filter.severity != null && rec.getEntry().getSeverity() != null)
					isRelevant = rec.getEntry().getSeverity().equals(filter.severity);

				if (isRelevant)
					unitRecordSum.addRecord(rec);
			}

			if (unitRecordSum.getRecords().isEmpty() || !filter.isRecordSumRelevant(unitRecordSum))
				records.remove(unit);
		}
		List<RecordUIDataSyslogSumFromReport> list = new ArrayList<RecordUIDataSyslogSumFromReport>(records.values());
		if (filter.max_rows != null && list.size() > filter.max_rows) {
			Collections.sort(list);
			list = list.subList(0, filter.max_rows);
		}
		root.put("reports", list);
	}

	private void displayHardwareReport(Map<String, Object> root, ReportHardwareGenerator rgHardware, Group groupSelect, List<String> swVersions) throws NoAvailableConnectionException, SQLException,
			IOException {
		Map<String, Report<RecordHardware>> reports = rgHardware.generateFromSyslog(PeriodType.DAY, fromDate, toDate, unittype.getSelectedOrAllItemsAsList(), profile.getSelectedOrAllItemsAsList(),
				groupSelect);
		Map<Unit, List<RecordUIDataHardware>> records = new HashMap<Unit, List<RecordUIDataHardware>>();
		RecordUIDataHardwareFilter limits = new RecordUIDataHardwareFilter(inputData, root);
		String swVersionFromReport = req.getParameter("softwareversion");
		String selectedSoftwareVersion = swVersionFromReport != null ? swVersionFromReport : inputData.getSwVersion().getString();
		DropDownSingleSelect<String> swVersionList = InputSelectionFactory.getDropDownSingleSelect(inputData.getSwVersion(), selectedSoftwareVersion, swVersions);
		for (Entry<String, Report<RecordHardware>> entry : reports.entrySet()) {
			Unit unit = xapsUnit.getUnitById(entry.getKey());

			if (unit == null)
				unit = new Unit(entry.getKey(), null, null);

			if (swVersionList.getSelected() != null) {
				String cpSW = unit.getParameters().get(SystemParameters.SOFTWARE_VERSION);
				if (cpSW == null || !cpSW.equals(swVersionList.getSelected()))
					continue;
			}

			if (records.get(unit) == null)
				records.put(unit, new ArrayList<RecordUIDataHardware>());

			records.get(unit).addAll(RecordUIDataHardware.convertRecords(unit, new ArrayList<RecordHardware>(entry.getValue().getMap().values()), limits));

			List<RecordUIDataHardware> recs = new ArrayList<RecordUIDataHardware>();

			List<RecordUIDataHardware> _recs = records.get(unit);

			/**
			 * Iterate over each record and filter it according to parameter input.
			 * This is done with a logical AND test.
			 */
			for (RecordUIDataHardware uiDataRecord : _recs) {
				boolean include = true;

				if (swVersionFromReport != null)
					include = (uiDataRecord.getSoftwareVersion() != null && uiDataRecord.getSoftwareVersion().equals(swVersionFromReport));

				if (include)
					recs.add(uiDataRecord);
			}

			if (recs.size() == 0)
				records.remove(unit);
			else
				records.put(unit, recs);
		}
		List<RecordUIDataHwSum> hwSums = processUnitHardwareRecords(records);
		root.put("reports", hwSums);
		root.put("swVersionList", swVersionList);
	}

	private void displayProvReport(Map<String, Object> root, Group groupSelect) throws SQLException, NoAvailableConnectionException, IOException {
		ReportProvisioningGenerator rgProv = ReportPage.getReportProvGenerator(req.getSession().getId(), xaps);
		Map<String, Report<RecordProvisioning>> reports = rgProv.generateFromSyslog(PeriodType.DAY, fromDate, toDate, unittype.getSelectedOrAllItemsAsList(), profile.getSelectedOrAllItemsAsList(),
				groupSelect);
		root.put("records", RecordUIDataProv.convertRecords(xapsUnit, reports));
	}

	private void displayVoipReport(Map<String, Object> root, Group groupSelect) throws SQLException, NoAvailableConnectionException, IOException {
		ReportVoipGenerator rgVoip = ReportPage.getReportVoipGenerator(req.getSession().getId(), xaps);
		Map<String, Report<RecordVoip>> reports = rgVoip.generateFromSyslog(PeriodType.DAY, fromDate, toDate, unittype.getSelectedOrAllItemsAsList(), profile.getSelectedOrAllItemsAsList(), groupSelect);
		List<RecordWrapper<RecordVoip>> recordsWorking = new ArrayList<RecordWrapper<RecordVoip>>();
		List<RecordWrapper<RecordVoip>> recordsDown = new ArrayList<RecordWrapper<RecordVoip>>();
		for (Entry<String, Report<RecordVoip>> entry : reports.entrySet()) {
			Unit unit = xapsUnit.getUnitById(entry.getKey());

			if (unit == null)
				unit = new Unit(entry.getKey(), null, null);

			String line = req.getParameter("line");
			root.put("filter_line", line);
			String sw = req.getParameter("softwareversion");
			root.put("filter_softwareversion", sw);

			List<RecordVoip> records = new ArrayList<RecordVoip>();

			List<RecordVoip> _records = new ArrayList<RecordVoip>(entry.getValue().getMap().values());

			/**
			 * Iterate over each record and filter it according to parameter input.
			 * This is done with a logical AND test.
			 */
			for (RecordVoip rec : _records) {
				boolean include = true;

				if (sw != null)
					include = (rec.getSoftwareVersion() != null && rec.getSoftwareVersion().equals(sw));
				if (include && line != null)
					include = (rec.getLine() != null && rec.getLine().equals(line));

				if (include)
					records.add(rec);
			}

			if (records.size() > 0) {
				RecordVoip record = records.get(0);
				for (int i = 1; i < records.size(); i++)
					record.add(records.get(i));
				if (record.getCallLengthTotal() != null && record.getCallLengthTotal().get() > 0)
					recordsWorking.add(new RecordWrapper<RecordVoip>(unit, record));
				else if (record.getNoSipServiceTime() != null && record.getNoSipServiceTime().get() != null && record.getNoSipServiceTime().get() > 0)
					recordsDown.add(new RecordWrapper<RecordVoip>(unit, record));
			}
		}
		Collections.sort(recordsWorking, new RecordTotalScoreComparator());
		Collections.sort(recordsDown, new RecordSipRegFailedComparator());
		root.put("reports", recordsWorking);
		root.put("failed", recordsDown);
		root.put("mosavgbad", new IsMosAvgBad(recordsWorking));
		root.put("sipregbad", new IsSipRegisterCause(recordsWorking));
	}

	/**
	 * Process unit hardware records.
	 *
	 * @param records the records
	 * @return the list
	 */
	private List<RecordUIDataHwSum> processUnitHardwareRecords(Map<Unit, List<RecordUIDataHardware>> records) {
		List<RecordUIDataHwSum> sums = new ArrayList<RecordUIDataHwSum>();
		for (Entry<Unit, List<RecordUIDataHardware>> entry : records.entrySet()) {
			Unit unit = entry.getKey();
			RecordUIDataHwSum sum = new RecordUIDataHwSum(unit);
			for (RecordUIDataHardware record : entry.getValue())
				sum.addRecordIfRelevant(record);
			if (sum.getRecords().size() > 0)
				sums.add(sum);
		}
		return sums;
	}

	/**
	 * Gets the start date.
	 *
	 * @param input the input
	 * @return the start date
	 * @throws ParseException the parse exception
	 */
	public Date getStartDate(Input input) throws ParseException {
		Calendar start = Calendar.getInstance();
		if (input.notNullNorValue("")) {
			start.setTime(input.getDate());
			start.set(Calendar.SECOND, 0);
			start.set(Calendar.MILLISECOND, 0);
		} else {
			start.setTime(new Date());
			start.set(Calendar.SECOND, 0);
			start.set(Calendar.MILLISECOND, 0);
			start.add(Calendar.HOUR, -1);
		}
		return start.getTime();
	}

	/**
	 * Gets the end date.
	 *
	 * @param input the input
	 * @return the end date
	 * @throws ParseException the parse exception
	 */
	public Date getEndDate(Input input) throws ParseException {
		Calendar end = Calendar.getInstance();
		if (input.notNullNorValue("")) {
			end.setTime(input.getDate());
			end.set(Calendar.SECOND, 0);
			end.set(Calendar.MILLISECOND, 0);
		} else {
			end.setTime(new Date());
			end.set(Calendar.SECOND, 0);
			end.set(Calendar.MILLISECOND, 0);
		}
		return end.getTime();
	}

	/**
	 * The Class RecordWrapper.
	 *
	 * @param <V> the value type
	 */
	public class RecordWrapper<V extends RecordVoip> {

		/**
		 * Gets the unit.
		 *
		 * @return the unit
		 */
		public Unit getUnit() {
			return unit;
		}

		/**
		 * Instantiates a new record wrapper.
		 *
		 * @param key the key
		 * @param record the record
		 */
		public RecordWrapper(Unit key, V record) {
			this.voipRecord = record;
			this.unit = key;
		}

		/** The voip record. */
		public RecordVoip voipRecord;

		/** The unit. */
		public Unit unit;

		/**
		 * Gets the record.
		 *
		 * @return the record
		 */
		public RecordVoip getRecord() {
			return voipRecord;
		}
	}

	/**
	 * The Class RecordTotalScoreComparator.
	 */
	@SuppressWarnings("rawtypes")
	private class RecordTotalScoreComparator implements Comparator<RecordWrapper> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(RecordWrapper o1, RecordWrapper o2) {
			if (o1.getRecord().getVoIPQuality().get() < o2.getRecord().getVoIPQuality().get())
				return 1;
			if (o1.getRecord().getVoIPQuality().get() > o2.getRecord().getVoIPQuality().get())
				return -1;
			return 0;
		}
	}

	/**
	 * The Class RecordSipRegFailedComparator.
	 */
	@SuppressWarnings("rawtypes")
	private class RecordSipRegFailedComparator implements Comparator<RecordWrapper> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(RecordWrapper o1, RecordWrapper o2) {
			if (o1.getRecord().getNoSipServiceTime().get() < o2.getRecord().getNoSipServiceTime().get())
				return 1;
			if (o1.getRecord().getNoSipServiceTime().get() > o2.getRecord().getNoSipServiceTime().get())
				return -1;
			return 0;
		}
	}

	/**
	 * Find record.
	 *
	 * @param id the id
	 * @param recordsWorking the records working
	 * @return the record voip
	 */
	@SuppressWarnings({ "rawtypes" })
	private RecordVoip findRecord(String id, List<RecordWrapper<RecordVoip>> recordsWorking) {
		for (RecordWrapper wrapper : recordsWorking) {
			if (wrapper.unit.getId().equals(id))
				return wrapper.getRecord();
		}
		return null;
	}

	/**
	 * The Class IsMosAvgBad.
	 */
	public class IsMosAvgBad implements TemplateMethodModel {

		/** The units. */
		private Map<String, Boolean> units = new HashMap<String, Boolean>();

		/** The records. */
		private List<RecordWrapper<RecordVoip>> records;

		/**
		 * Instantiates a new checks if is mos avg bad.
		 *
		 * @param records the records
		 */
		public IsMosAvgBad(List<RecordWrapper<RecordVoip>> records) {
			this.records = records;
		}

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public Boolean exec(List arg0) throws TemplateModelException {
			if (arg0.size() < 1)
				throw new TemplateModelException("Specify unitId");

			String key = (String) arg0.get(0);

			if (units.containsKey(key))
				return units.get(key);

			RecordVoip record = findRecord(key, records);

			if (record == null)
				return false;

			if (record.getMosAvg().get() != null && record.getMosAvg().get() < 300 && record.getCallLengthTotal().get() > 5)
				return true;

			return false;
		}
	}

	/**
	 * The Class IsSipRegisterCause.
	 */
	public class IsSipRegisterCause implements TemplateMethodModel {

		/** The units. */
		private Map<String, Boolean> units = new HashMap<String, Boolean>();

		/** The records. */
		private List<RecordWrapper<RecordVoip>> records;

		/**
		 * Instantiates a new checks if is sip register cause.
		 *
		 * @param records the records
		 */
		public IsSipRegisterCause(List<RecordWrapper<RecordVoip>> records) {
			this.records = records;
		}

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public Boolean exec(List arg0) throws TemplateModelException {
			if (arg0.size() < 1)
				throw new TemplateModelException("Specify unitId");

			String key = (String) arg0.get(0);

			if (units.containsKey(key))
				return units.get(key);

			RecordVoip record = findRecord(key, records);

			if (record == null)
				return false;

			return record.getNoSipServiceTime().get() > 100 && !new IsMosAvgBad(records).exec(Arrays.asList(key));
		}

	}
}