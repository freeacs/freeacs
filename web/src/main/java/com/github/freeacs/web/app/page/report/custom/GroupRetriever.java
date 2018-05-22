package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.*;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import com.github.freeacs.web.app.util.XAPSLoader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

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
	 * @param acs the xaps
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public GroupRetriever(ReportData inputData, ParameterParser params, ACS acs) throws SQLException {
		super(inputData, params, acs);

		generator = generateGroupGenerator();

		//		String selectedType = params.getParameter("grouptype") != null ? params.getParameter("grouptype") : "All";
		//		types = InputSelectionFactory.getDropDownSingleSelect(Input.getStringInput("grouptype"), selectedType, Arrays.asList("All", "Normal", "Time"));

		Unittype unittype = null;
		if (inputData.getUnittype().notNullNorValue(""))
			unittype = acs.getUnittype(inputData.getUnittype().getString());

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
	 *  the no available connection exception
	 */
	private ReportGroupGenerator generateGroupGenerator() throws SQLException {
		ACS acs = getAcs();
		return new ReportGroupGenerator(acs.getDataSource(), acs.getSyslog().getDataSource(),
                acs, null, XAPSLoader.getIdentity(getParams().getSession().getId(), acs.getDataSource()));
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#generateReport(com.owera.xaps.dbi.report.PeriodType, java.util.Date, java.util.Date, java.util.List, java.util.List)
	 */
	@Override
	public Report<RecordGroup> generateReport(PeriodType periodType, Date start, Date end, List<Unittype> unittypes, List<Profile> profiles, Group group) throws SQLException, IOException {
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
