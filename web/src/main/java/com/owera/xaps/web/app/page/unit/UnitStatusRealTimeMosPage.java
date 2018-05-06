package com.owera.xaps.web.app.page.unit;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Certificate;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Syslog;
import com.owera.xaps.dbi.SyslogEntry;
import com.owera.xaps.dbi.SyslogFilter;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.report.Chart;
import com.owera.xaps.dbi.report.PeriodType;
import com.owera.xaps.dbi.report.RecordVoipCall;
import com.owera.xaps.dbi.report.Report;
import com.owera.xaps.dbi.report.ReportVoipCallGenerator;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.page.report.ReportPage;
import com.owera.xaps.web.app.util.BrowserDetect;
import com.owera.xaps.web.app.util.CertificateVerification;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.UserAgent;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;



/**
 * The Class UnitStatusRealTimeMosPage.
 */
public class UnitStatusRealTimeMosPage extends AbstractWebPage {

	/** The input data. */
	private UnitStatusRealTimeMosData inputData;

	/** The xaps. */
	private XAPS xaps;
	
	/** The logger. */
	private static Logger logger = new Logger();

        /* (non-Javadoc)
         * @see com.owera.xaps.web.app.page.AbstractWebPage#requiresNoCache()
         */
        @Override
        public boolean requiresNoCache() {
            return true;
        }

    	/* (non-Javadoc)
	     * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	     */
	    public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (UnitStatusRealTimeMosData) InputDataRetriever.parseInto(new UnitStatusRealTimeMosData(), params);
		
		String sessionId = params.getSession().getId();
		
		xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		
		Map<String, Object> root = outputHandler.getTemplateMap();
		
		/* Morten jan 2014 - Certificate checks disabled due to open source
		if(!CertificateVerification.isCertificateValid(Certificate.CERT_TYPE_REPORT, sessionId)){ // If not valid (see the "!")
			root.put("message", "No valid certificate found for Reports page. Please contact your systems administrator.");
			outputHandler.setTemplatePath("/exception.ftl");
			return;
		}
		*/
		
		Unittype unittype = null;
		if(inputData.getUnittype().notNullNorValue(""))
			unittype = xaps.getUnittype(inputData.getUnittype().getString());
		
		Profile profile = null;
		if(inputData.getProfile().notNullNorValue("") && unittype!=null)
			profile = unittype.getProfiles().getByName(inputData.getProfile().getString());
		
		Unit unit = null;

		if (inputData.getUnit().notNullNorValue("")){
			unit = xapsUnit.getUnitById(inputData.getUnit().getString());
		}
	
		Date start = inputData.getStart().getDate();
		if(start==null){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -5);
			start = cal.getTime();
			Date start_old = (Date) start.clone();
			if(unit!=null)
				start = getLastQoSTimestamp(sessionId, unit, start);
			if(start==null)
				start = start_old;
		}else{
			root.put("start",inputData.getStart().getDateFormat().format(start));
		}
		
		Date end = inputData.getEnd().getDate();
		if(end!=null)
			root.put("end",inputData.getEnd().getDateFormat().format(end));
		
		boolean shouldContinueToReload = shouldContinueToReload(params);
		
		boolean isIELessThan7 = BrowserDetect.lessThan(params.getHttpServletRequest(), UserAgent.IE, 7);
		
		if(params.getBoolean("get-image-for-ie") && params.getSession().getAttribute("realtime")!=null){
			outputHandler.writeImageBytesToResponse((byte[]) params.getSession().getAttribute("realtime"));
			return;
		}
		
		String line = inputData.getChannel().getInteger()!=null?inputData.getChannel().getInteger().toString():null;
		
		if(params.getBoolean("display-chart")){
            ReportVoipCallGenerator rgVoip = ReportPage.getReportVoipCallGenerator(params.getSession().getId(), xaps);
			List<Unittype> unittypes = unittype!=null?Arrays.asList(unittype):getAllowedUnittypes(sessionId);
			List<Profile> profiles = profile!=null?Arrays.asList(profile):getAllowedProfiles(sessionId, unittype);
			String unitId = unit!=null?unit.getId():null;
			Report<RecordVoipCall> report = rgVoip.generateFromSyslog(PeriodType.SECOND,start, end,unittypes,profiles, unitId,line,null);
			logger.info("Found "+report.getMap().size()+" record voip call entries. From: "+start.toString()+". To: "+(end!=null?end.toString():"N/A"));
			Chart<RecordVoipCall> chartMaker = new Chart<RecordVoipCall>((Report<RecordVoipCall>) report, "MosAvg", false,null,"Channel");
			byte[] image = UnitStatusPage.getReportChartImageBytes(chartMaker,null,600,250);
			
			params.getSession().setAttribute("realtime", image);
			
			if(isIELessThan7){
				outputHandler.setDirectResponse("<img src='web?page=unit-status-realtime-mos&get-image-for-ie=true&t="+System.nanoTime()+"&unit="+unit.getId()+"' alt='chart' />");
			}else{
				String base64 = Base64.encodeBase64String(image); 
				outputHandler.setDirectResponse("<img src='data:image/png;base64,"+(base64.replace("\n", "").replace("\r", ""))+"' alt='chart' />");
			}
		}
		
		root.put("line", line);
		root.put("active",shouldContinueToReload);
		root.put("unit", unit);
		root.put("profile",profile);
		root.put("unittype",unittype);
		
		outputHandler.setTemplatePathWithIndex("unit-status-mos");
	}
	
	/**
	 * Gets the last qo s timestamp.
	 *
	 * @param sessionId the session id
	 * @param unit the unit
	 * @param start the start
	 * @param line the line
	 * @param xaps the xaps
	 * @return the last qo s timestamp
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public static Date getLastQoSTimestamp(String sessionId,Unit unit,Date start,String line,XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Syslog syslog = new Syslog(SessionCache.getSyslogConnectionProperties(sessionId), XAPSLoader.getIdentity(sessionId));
		SyslogFilter filter = new SyslogFilter();
		filter.setMaxRows(1);
		String keyToFind = "QoS report for channel "+(line!=null?line:"");
		filter.setMessage("^"+keyToFind);
		filter.setCollectorTmsStart(start);
		filter.setUnitId("^" + unit.getId() + "$");
		List<SyslogEntry> qosEntry = syslog.read(filter, xaps);
		if(qosEntry.size()>0)
			return qosEntry.get(0).getCollectorTimestamp();
		return null;
	}
	
	/**
	 * Gets the last qo s timestamp.
	 *
	 * @param sessionId the session id
	 * @param unit the unit
	 * @param start the start
	 * @return the last qo s timestamp
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	private Date getLastQoSTimestamp(String sessionId,Unit unit,Date start) throws SQLException, NoAvailableConnectionException{
		return getLastQoSTimestamp(sessionId, unit, start,null,xaps);
	}
	
	/**
	 * Should continue to reload.
	 *
	 * @param params the params
	 * @return true, if successful
	 */
	private boolean shouldContinueToReload(ParameterParser params) {
		boolean reload = params.getBoolean("reload");
		Date endDate = inputData.getEnd().getDate();
		if(reload && endDate==null && params.getSession().getAttribute("activetooldialog")!=null)
			return true;
		return false;
	}
}
