package com.owera.xaps.web.app.page.report.custom;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.report.PeriodType;
import com.owera.xaps.dbi.report.RecordGroup;
import com.owera.xaps.dbi.report.Report;
import com.owera.xaps.dbi.report.ReportGenerator;
import com.owera.xaps.dbi.report.ReportGroupGenerator;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.report.ReportData;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.XAPSLoader;

/**
 * The Class GroupInterface.
 */
public class GroupRetriever extends ReportRetriever {

	/** The generator. */
	private ReportGroupGenerator generator;

	/** The groups. */
	private DropDownSingleSelect<Group> groups;

	public DropDownSingleSelect<Group> getGroups() {
		return groups;
	}

	/** The types. */
	//	private DropDownSingleSelect<String> types;

	/**
	 * Instantiates a new group interface.
	 *
	 * @param inputData the input data
	 * @param params the params
	 * @param xaps the xaps
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public GroupRetriever(ReportData inputData, ParameterParser params, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		super(inputData, params, xaps);

		generator = generateGroupGenerator();

		//		String selectedType = params.getParameter("grouptype") != null ? params.getParameter("grouptype") : "All";
		//		types = InputSelectionFactory.getDropDownSingleSelect(Input.getStringInput("grouptype"), selectedType, Arrays.asList("All", "Normal", "Time"));

		Unittype unittype = null;
		if (inputData.getUnittype().notNullNorValue(""))
			unittype = xaps.getUnittype(inputData.getUnittype().getString());

		List<Group> groupList = new ArrayList<Group>();
		if (unittype != null) {
			List<Group> _all = Arrays.asList(unittype.getGroups().getGroups());
			groupList.addAll(_all);
		}

		Group group = null;
		if (unittype != null && inputData.getGroup().notNullNorValue(""))
			group = unittype.getGroups().getByName(inputData.getGroup().getString());

		groups = InputSelectionFactory.getDropDownSingleSelect(inputData.getGroup(), group, groupList);
	}

	/**
	 * Generate group generator.
	 *
	 * @return the report group generator
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	private ReportGroupGenerator generateGroupGenerator() throws SQLException, NoAvailableConnectionException {
		return new ReportGroupGenerator(SessionCache.getSyslogConnectionProperties(getParams().getSession().getId()), SessionCache.getXAPSConnectionProperties(getParams().getSession().getId()),
				getXaps(), null, XAPSLoader.getIdentity(getParams().getSession().getId()));
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#generateReport(com.owera.xaps.dbi.report.PeriodType, java.util.Date, java.util.Date, java.util.List, java.util.List)
	 */
	@Override
	public Report<RecordGroup> generateReport(PeriodType periodType, Date start, Date end, List<Unittype> unittypes, List<Profile> profiles, Group group) throws NoAvailableConnectionException,
			SQLException, IOException {
		return (Report<RecordGroup>) generator.generateGroupReport(periodType, start, end, unittypes, groups.getSelected());
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#applyObjects(java.util.Map)
	 */
	@Override
	public void applyObjects(Map<String, Object> root) {
		root.put("groups", groups);
		//		root.put("types", types);
	}

	@Override
	public ReportGenerator getReportGenerator() {
		return generator;
	}

}
