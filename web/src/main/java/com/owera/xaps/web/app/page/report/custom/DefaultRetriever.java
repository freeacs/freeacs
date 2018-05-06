package com.owera.xaps.web.app.page.report.custom;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.report.PeriodType;
import com.owera.xaps.dbi.report.Report;
import com.owera.xaps.dbi.report.ReportGenerator;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.report.ReportData;


/**
 * The Class DefaultInterface.
 */
public class DefaultRetriever extends ReportRetriever {

	/**
	 * Instantiates a new default interface.
	 *
	 * @param inputData the input data
	 * @param params the params
	 * @param xaps the xaps
	 */
	public DefaultRetriever(ReportData inputData, ParameterParser params, XAPS xaps) {
		super(inputData, params, xaps);
	}
	
	public DefaultRetriever() {
		super(null,null,null);
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#generateReport(com.owera.xaps.dbi.report.PeriodType, java.util.Date, java.util.Date, java.util.List, java.util.List)
	 */
	@Override
	public Report<?> generateReport(PeriodType periodType, Date start, Date end, List<Unittype> unittypes, List<Profile> profiles, Group group) throws NoAvailableConnectionException, SQLException,
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
