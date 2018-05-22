package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.RecordJob;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import com.github.freeacs.web.app.util.XAPSLoader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * The Class JobInterface.
 */
public class JobRetriever extends ReportRetriever {

	/** The generator. */
	private ReportGenerator generator;

	/**
	 * Instantiates a new job interface.
	 *
	 * @param inputData the input data
	 * @param params the params
	 * @param acs the xaps
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public JobRetriever(ReportData inputData, ParameterParser params, ACS acs) throws SQLException {
		super(inputData, params, acs);
		generator = new ReportGenerator(acs.getDataSource(), acs.getSyslog().getDataSource(), acs, null,
				XAPSLoader.getIdentity(params.getSession().getId(), acs.getDataSource()));
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#generateReport(com.owera.xaps.dbi.report.PeriodType, java.util.Date, java.util.Date, java.util.List, java.util.List)
	 */
	@Override
	public Report<RecordJob> generateReport(PeriodType periodType, Date start, Date end, List<Unittype> unittypes, List<Profile> profiles, Group groupSelect) throws
			SQLException, IOException {
		return generator.generateJobReport(periodType, start, end, unittypes);
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
		return generator;
	}



}
