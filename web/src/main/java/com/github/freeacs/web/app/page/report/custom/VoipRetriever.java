package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.XAPS;
import com.github.freeacs.dbi.report.*;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.XAPSLoader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * The Class VoipInterface.
 */
public class VoipRetriever extends ReportRetriever {

	/** The generator voip. */
	private ReportVoipGenerator generatorVoip;

	/** The generator voip call. */
	private ReportVoipCallGenerator generatorVoipCall;

	/**
	 * Instantiates a new voip interface.
	 *
	 * @param inputData the input data
	 * @param params the params
	 * @param xaps the xaps
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public VoipRetriever(ReportData inputData, ParameterParser params, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		super(inputData, params, xaps);
		generatorVoip = new ReportVoipGenerator(SessionCache.getSyslogConnectionProperties(params.getSession().getId()), SessionCache.getXAPSConnectionProperties(params.getSession().getId()), xaps, null,
				XAPSLoader.getIdentity(params.getSession().getId()));
		generatorVoipCall = new ReportVoipCallGenerator(SessionCache.getSyslogConnectionProperties(params.getSession().getId()), SessionCache.getXAPSConnectionProperties(params.getSession().getId()),
				xaps, null, XAPSLoader.getIdentity(params.getSession().getId()));
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#generateReport(com.owera.xaps.dbi.report.PeriodType, java.util.Date, java.util.Date, java.util.List, java.util.List)
	 */
	@Override
	public Report<?> generateReport(PeriodType periodType, Date start, Date end, List<Unittype> unittypes, List<Profile> profiles, Group groupSelect) throws NoAvailableConnectionException,
			SQLException, IOException {
		Report<?> report = null;
		if (getInputData().getRealtime().getBoolean() || groupSelect != null) {
			if (getInputData().getMethod().hasValue("MosAvg"))
				report = (Report<RecordVoipCall>) generatorVoipCall.generateFromSyslog(periodType, start, end, unittypes, profiles, null, null, groupSelect);
			else
				report = (Report<RecordVoip>) generatorVoip.generateFromSyslog(periodType, start, end, unittypes, profiles, null, groupSelect);
		} else
			report = (Report<RecordVoip>) generatorVoip.generateFromReport(periodType, start, end, unittypes, profiles);
		return report;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#applyObjects(java.util.Map)
	 */
	@Override
	public void applyObjects(Map<String, Object> root) {

	}

	@Override
	public ReportGenerator getReportGenerator() {
		return generatorVoip;
	}

}
