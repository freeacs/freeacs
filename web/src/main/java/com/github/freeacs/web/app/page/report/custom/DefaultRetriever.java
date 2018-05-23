package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * The Class DefaultInterface.
 */
public class DefaultRetriever extends ReportRetriever {

	/**
	 * Instantiates a new default interface.
	 *
	 * @param inputData the input data
	 * @param params the params
	 * @param acs the xaps
	 */
	public DefaultRetriever(ReportData inputData, ParameterParser params, ACS acs) {
		super(inputData, params, acs);
	}
	
	public DefaultRetriever() {
		super(null,null,null);
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#generateReport(com.owera.xaps.dbi.report.PeriodType, java.util.Date, java.util.Date, java.util.List, java.util.List)
	 */
	@Override
	public Report<?> generateReport(PeriodType periodType, Date start, Date end, List<Unittype> unittypes, List<Profile> profiles, Group group) throws SQLException,
			IOException {
		throw new NotImplementedException("The report is not implemented correctly.");
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#applyObjects(java.util.Map)
	 */
	@Override
	public void applyObjects(Map<String, Object> root) {
		// TODO Auto-generated method stub

	}

	@Override
	public ReportGenerator getReportGenerator() {
		return null;
	}

}
