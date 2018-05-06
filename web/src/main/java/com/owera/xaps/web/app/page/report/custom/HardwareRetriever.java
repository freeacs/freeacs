package com.owera.xaps.web.app.page.report.custom;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.report.PeriodType;
import com.owera.xaps.dbi.report.RecordHardware;
import com.owera.xaps.dbi.report.Report;
import com.owera.xaps.dbi.report.ReportGenerator;
import com.owera.xaps.dbi.report.ReportHardwareGenerator;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.report.ReportData;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.XAPSLoader;


/**
 * The Class HardwareInterface.
 */
public class HardwareRetriever extends ReportRetriever {

	/** The generator. */
	private ReportHardwareGenerator generator;

	/**
	 * Instantiates a new hardware interface.
	 *
	 * @param inputData the input data
	 * @param params the params
	 * @param xaps the xaps
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public HardwareRetriever(ReportData inputData, ParameterParser params, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		super(inputData, params, xaps);
		generator = new ReportHardwareGenerator(SessionCache.getSyslogConnectionProperties(params.getSession().getId()), SessionCache.getXAPSConnectionProperties(params.getSession().getId()), xaps, null, XAPSLoader.getIdentity(params.getSession().getId()));
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.report.custom.ReportRetriever#generateReport(com.owera.xaps.dbi.report.PeriodType, java.util.Date, java.util.Date, java.util.List, java.util.List)
	 */
	@Override
	public Report<?> generateReport(PeriodType periodType, Date start, Date end, List<Unittype> unittypes, List<Profile> profiles, Group group) throws NoAvailableConnectionException, SQLException, IOException {
		if(group!=null)
			return (Report<RecordHardware>) generator.generateFromSyslog(periodType, start, end, unittypes, profiles, null, group);
		return (Report<RecordHardware>) generator.generateFromReport(periodType, start, end, unittypes, profiles);
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
