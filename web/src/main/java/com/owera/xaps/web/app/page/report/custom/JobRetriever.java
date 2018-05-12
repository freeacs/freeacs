package com.owera.xaps.web.app.page.report.custom;

import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.XAPS;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.RecordJob;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.report.ReportData;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.XAPSLoader;

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
	 * @param xaps the xaps
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public JobRetriever(ReportData inputData, ParameterParser params, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		super(inputData, params, xaps);
		generator = new ReportGenerator(SessionCache.getSyslogConnectionProperties(params.getSession().getId()), SessionCache.getXAPSConnectionProperties(params.getSession().getId()), xaps, null,
				XAPSLoader.getIdentity(params.getSession().getId()));
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#generateReport(com.owera.xaps.dbi.report.PeriodType, java.util.Date, java.util.Date, java.util.List, java.util.List)
	 */
	@Override
	public Report<RecordJob> generateReport(PeriodType periodType, Date start, Date end, List<Unittype> unittypes, List<Profile> profiles, Group groupSelect) throws NoAvailableConnectionException,
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
