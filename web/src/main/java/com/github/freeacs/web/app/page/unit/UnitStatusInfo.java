package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.common.util.IPAddress;
import com.github.freeacs.common.util.TimeWindow;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.report.*;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.web.app.page.report.UnitListData;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataHardware;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataHardwareFilter;
import com.github.freeacs.web.app.page.syslog.SyslogRetriever;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * A place holder for data and logic related to a specific time window.
 *
 * @author Jarl Andre Hubenthal
 */
public class UnitStatusInfo {

	/** The Constant OVERALL_STATUS_MAX. */
	public final static Integer OVERALL_STATUS_MAX = 10;

	/** The Constant OVERALL_STATUS_MIN. */
	public final static Integer OVERALL_STATUS_MIN = 0;

	/** The Constant DIAL_CHART_RED. */
	public final static double DIAL_CHART_RED = 3.33d;

	/** The Constant DIAL_CHART_YELLOW. */
	public final static double DIAL_CHART_YELLOW = 6.66d;

	/** The Constant DEFAULT_SPREAD. */
	private static final float DEFAULT_SPREAD = 50;

	/** The Constant DEFAULT_SERVICE_WINDOW. */
	private static final String DEFAULT_SERVICE_WINDOW = "mo-su:0000-2400";

	/** The Constant DEFAULT_FREQUENCY. */
	private static final int DEFAULT_FREQUENCY = 7;

	/** The current unit. */
	private final Unit currentUnit;
	private final DataSource mainDataSource;
	private final DataSource syslogDataSource;

	/** The first connect timestamp. */
	private String firstConnectTimestamp = null;

	/** The last connect timestamp. */
	private String lastConnectTimestamp = null;

	/** The next connect timestamp. */
	private String nextConnectTimestamp = null;

	/** The line1 configured. */
	private VoipConfigured line1Configured = VoipConfigured.NOT_CONFIGURED;

	/** The line2 configured. */
	private VoipConfigured line2Configured = VoipConfigured.NOT_CONFIGURED;

	/** The has connected. */
	private boolean hasConnected = false;

	/** The session id. */
	private String sessionId = null;

	/** The from date. */
	private Date fromDate = null;

	/** The to date. */
	private Date toDate = null;

	/** The voip report. */
	private Report<RecordVoip> voipReport = null;

	/** The hardware report. */
	private Report<RecordHardware> hardwareReport = null;

	/** The syslog entries. */
	private List<SyslogEntry> syslogEntries = null;

	/** The total score. */
	private Double totalScore = null;

	private Report<RecordSyslog> syslogReport;

	/** The sdf. */
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Gets the unit status info.
	 *
	 * @param unit the unit
	 * @param fromDate the from date
	 * @param toDate the to date
	 * @param sessionId the session id
	 * @param mainDataSource
	 * @param syslogDataSource
	 * @return the unit status info
	 * @throws ParseException the parse exception
	 * @throws NumberFormatException the number format exception
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public static UnitStatusInfo getUnitStatusInfo(Unit unit, Date fromDate, Date toDate, String sessionId, DataSource mainDataSource, DataSource syslogDataSource) throws ParseException, NumberFormatException, SQLException {
		UnitStatusInfo info = new UnitStatusInfo(unit, mainDataSource, syslogDataSource);
		info.fromDate = fromDate;
		info.toDate = toDate;
		info.sessionId = sessionId;
		info.setFirstConnectTimestamp(unit.getParameters().get(SystemParameters.FIRST_CONNECT_TMS));
		info.setLastConnectTimestamp(unit.getParameters().get(SystemParameters.LAST_CONNECT_TMS));
		info.getNextConnect();
		String utName = unit.getUnittype().getName();
		if (utName.contains("NPA201") || utName.contains("RGW208") || utName.contains("IAD208")) {
			info.setLine1Configured(info.isLineConfigured(VoipLine.LINE_0));
			info.setLine2Configured(info.isLineConfigured(VoipLine.LINE_1));
		} else {
			info.setLine1Configured(VoipConfigured.NOT_APPLICABLE);
			info.setLine2Configured(VoipConfigured.NOT_APPLICABLE);
		}
		info.isWithinServiceWindow();
		info.hasConnected();
		return info;
	}

	/**
	 * Gets the serial number.
	 *
	 * @return the serial number
	 */
	public String getSerialNumber() {
		UnittypeParameter serialUtp = currentUnit.getUnittype().getUnittypeParameters().getByName("Device.DeviceInfo.SerialNumber");
		if (serialUtp == null)
			serialUtp = currentUnit.getUnittype().getUnittypeParameters().getByName("InternetGatewayDevice.DeviceInfo.SerialNumber");
		if (serialUtp != null) {
			UnitParameter param = currentUnit.getUnitParameters().get(serialUtp.getName());
			return param != null ? param.getValue() : null;
		}
		return null;
	}

	/**
	 * Gets the software version.
	 *
	 * @return the software version
	 */
	public String getSoftwareVersion() {
		UnittypeParameter serialUtp = currentUnit.getUnittype().getUnittypeParameters().getByName(SystemParameters.SOFTWARE_VERSION);
		if (serialUtp == null)
			serialUtp = currentUnit.getUnittype().getUnittypeParameters().getByName("Device.DeviceInfo.SoftwareVersion");
		if (serialUtp == null)
			serialUtp = currentUnit.getUnittype().getUnittypeParameters().getByName("InternetGatewayDevice.DeviceInfo.SoftwareVersion");
		if (serialUtp != null) {
			UnitParameter param = currentUnit.getUnitParameters().get(serialUtp.getName());
			return param != null ? param.getValue() : null;
		}
		return null;
	}

	/**
	 * Gets the desired software version.
	 *
	 * @return the desired software version
	 */
	public String getDesiredSoftwareVersion() {
		UnittypeParameter desiredSw = currentUnit.getUnittype().getUnittypeParameters().getByName(SystemParameters.DESIRED_SOFTWARE_VERSION);
		if (desiredSw != null) {
			String param = currentUnit.getParameters().get(desiredSw.getName());
			return param != null ? param : null;
		}
		return null;
	}

	/**
	 * Gets the total score.
	 *
	 * @return the total score
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized Double getTotalScore() throws SQLException, IOException {
		if (totalScore == null) {
			Report<RecordVoip> report = getVoipReport();
			report = ReportConverter.convertVoipReport(report, PeriodType.ETERNITY);
			if (getNumberOfCallsFromReport(report) == 0)
				return null;
			Iterator<Entry<Key, RecordVoip>> reportIterator = report.getMap().entrySet().iterator();
			Double newTotalScore = null;
			if (reportIterator.hasNext()) {
				RecordVoip monthReport = (RecordVoip) reportIterator.next().getValue();
				if (monthReport.getVoIPQuality() != null && monthReport.getVoIPQuality().get() != null) {
					double ts = (double) monthReport.getVoIPQuality().get();
					double div = (double) monthReport.getVoIPQuality().getDividend();
					newTotalScore = ts / div;
				}
			}
			totalScore = newTotalScore;
		}
		return totalScore;
	}

	/**
	 * Returns the number of calls for a report.
	 *
	 * @param report the report
	 * @return Number
	 */
	public int getNumberOfCallsFromReport(Report<RecordVoip> report) {
		int number = 0;
		for (RecordVoip record : report.getMap().values()) {
			if (record.getVoIPQuality() != null && record.getVoIPQuality().get() > 0)
				number++;
		}
		return number;
	}

	/**
	 * Checks if is 1 lines has problems.
	 *
	 * @return true, if is 1 lines has problems
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public boolean is1LinesHasProblems() throws SQLException {
		if (((isLine2Configured() && !isLine2Registered()) || isLine2ConfiguredError()) || ((isLine1Configured() && !isLine1Registered()) || isLine1ConfiguredError()))
			return true;
		return false;
	}

	/**
	 * Checks if is 2 lines has problems.
	 *
	 * @return true, if is 2 lines has problems
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public boolean is2LinesHasProblems() throws SQLException {
		return ((isLine2Configured() && !isLine2Registered()) || isLine2ConfiguredError()) && ((isLine1Configured() && !isLine1Registered()) || isLine1ConfiguredError());
	}

	/**
	 * Checks for connected.
	 *
	 * @return true, if successful
	 */
	public boolean hasConnected() {
		String firstConnect = Parameters.getUnitParameterValue(currentUnit, SystemParameters.FIRST_CONNECT_TMS);
		String lastConnect = Parameters.getUnitParameterValue(currentUnit, SystemParameters.LAST_CONNECT_TMS);
		boolean hasConnected = firstConnect != null || lastConnect != null;
		this.hasConnected = hasConnected;
		return hasConnected;
	}

	/**
	 * Gets the integer or default.
	 *
	 * @param string the string
	 * @param defInt the def int
	 * @return the integer or default
	 */
	public Integer getIntegerOrDefault(String string, Integer defInt) {
		if (string == null)
			return defInt;
		if (Pattern.matches("[0-9]+", string))
			return Integer.parseInt(string);
		return defInt;
	}

	/**
	 * Gets the float or default.
	 *
	 * @param string the string
	 * @param defFloat the def float
	 * @return the float or default
	 */
	public Float getFloatOrDefault(String string, Float defFloat) {
		if (string == null)
			return defFloat;
		if (Pattern.matches("[0-9]+", string))
			return Float.parseFloat(string);
		return defFloat;
	}

	/**
	 * Checks if is line configured.
	 *
	 * @param index the index
	 * @return the voip configured
	 * @throws NumberFormatException the number format exception
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public VoipConfigured isLineConfigured(VoipLine index) throws NumberFormatException, SQLException {
		String line = "Services.VoiceService.1.VoiceProfile." + index.toNonZero() + ".Line.1";
		String voiceEnabled = Parameters.getUnitParameterValue(currentUnit, line + ".Enable");
		String user = Parameters.getUnitParameterValue(currentUnit, line + ".SIP.AuthUserName");
		String pass = Parameters.getUnitParameterValue(currentUnit, line + ".SIP.AuthPassword");
		String uri = Parameters.getUnitParameterValue(currentUnit, line + ".SIP.URI");
		boolean basicConfiguration = user != null && user.length() > 0 && pass != null && pass.length() > 0 && uri != null && uri.length() > 0;
		if (voiceEnabled != null && voiceEnabled.equals("1")) {
			if (basicConfiguration)
				return VoipConfigured.CONFIGURED;
			return VoipConfigured.CONFIGURE_ERROR;
		} else if (basicConfiguration)
			return VoipConfigured.CONFIGURED_NOT_ENABLED;
		return VoipConfigured.NOT_CONFIGURED;
	}

	/**
	 * The Enum VoipConfigured.
	 */
	public enum VoipConfigured {

		NOT_CONFIGURED, CONFIGURED, CONFIGURE_ERROR, CONFIGURED_NOT_ENABLED, NOT_APPLICABLE
	}

	/**
	 * The Enum VoipLine.
	 */
	public enum VoipLine {

		/** The LIN e_0. */
		LINE_0,
		/** The LIN e_1. */
		LINE_1;

		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return name().substring(name().length() - 1);
		}

		/**
		 * To non zero.
		 *
		 * @return the string
		 */
		public String toNonZero() {
			if (name().endsWith("0"))
				return "1";
			if (name().endsWith("1"))
				return "2";
			return name();
		}
	}

	/**
	 * Checks if is line registered ok.
	 *
	 * @param sessionId the session id
	 * @param line the line
	 * @return the boolean
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public Boolean isLineRegisteredOk(String sessionId, VoipLine line) throws SQLException {
		Syslog syslog = new Syslog(syslogDataSource, ACSLoader.getIdentity(sessionId, mainDataSource));
		SyslogFilter filter = new SyslogFilter();
		filter.setMaxRows(100);
		String keyToFind = "ua_: ";
		filter.setMessage("^" + keyToFind);
		filter.setCollectorTmsStart(getMaxSipRegisterIntervalDate());
		filter.setUnitId("^" + currentUnit.getId() + "$"); //The unit object can never become NULL since this is checked in UnitStatusPage very early.
		List<SyslogEntry> entries = syslog.read(filter, ACSLoader.getXAPS(sessionId, mainDataSource, syslogDataSource));
		if (entries != null) {
			Date lastFailed = null;
			Date lastRegged = null;
			for (int i = 0; i < entries.size(); i++) {
				String content = entries.get(i).getContent();
				if (content != null && content.contains(keyToFind.replace("_", line.toString()) + "reg ok")) {
					if (lastRegged == null || lastRegged.before(entries.get(i).getCollectorTimestamp()))
						lastRegged = entries.get(i).getCollectorTimestamp();
				} else if (content != null
						&& (content.contains(keyToFind.replace("_", line.toString()) + "reg failed") || content.contains(keyToFind.replace("_", line.toString()) + "unreg failed") || content
								.contains(keyToFind.replace("_", line.toString()) + "unreg ok"))) {
					if (lastFailed == null || lastFailed.before(entries.get(i).getCollectorTimestamp()))
						lastFailed = entries.get(i).getCollectorTimestamp();
				}
			}
			if ((lastFailed == null && lastRegged != null) || (lastFailed != null && lastRegged != null && lastFailed.before(lastRegged)))
				return true;
		}
		return false;
	}

	/**
	 * Get the sip register interval for each line (1 and 2) and return the greatest interval.
	 *
	 * @return An integer (seconds)
	 */
	private int getMaxSipRegisterInterval() {
		int line1seconds = getSipRegisterInterval(1);
		int line2seconds = getSipRegisterInterval(2);
		int maxSeconds = Math.max(line1seconds, line2seconds);
		return maxSeconds;
	}

	/**
	 * Gets the max sip register interval date.
	 *
	 * @return the max sip register interval date
	 */
	private Date getMaxSipRegisterIntervalDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -getMaxSipRegisterInterval());
		return cal.getTime();
	}

	/**
	 * Gets the sip register interval.
	 *
	 * @param line the line
	 * @return the sip register interval
	 */
	private int getSipRegisterInterval(int line) {
		int lineSeconds = 3600;
		String lineValue = currentUnit.getParameters().get("InternetGatewayDevice.Services.VoiceService.2.VoiceProfile." + line + ".SIP.RegistrationPeriod");
		if (lineValue != null && lineValue.trim().length() > 0)
			try {
				lineSeconds = Integer.parseInt(lineValue);
			} catch (NumberFormatException e) {

			}
		return lineSeconds;
	}

	/**
	 * Gets the next connect.
	 *
	 * @return the next connect
	 * @throws ParseException the parse exception
	 */
	private String getNextConnect() throws ParseException {
		if (getLastConnectTimestamp() == null)
			return nextConnectTimestamp = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar lastConnected = Calendar.getInstance();
		lastConnected.setTime(sdf.parse(getLastConnectTimestamp()));
		String toParse = getPeriodicInformInterval(getUnit());
		if (toParse != null) {
			lastConnected.add(Calendar.SECOND, Integer.parseInt(toParse));
			return nextConnectTimestamp = sdf.format(lastConnected.getTime());
		}
		return nextConnectTimestamp = null;
	}

	/**
	 * Gets the periodic inform interval.
	 *
	 * @param unit the unit
	 * @return the periodic inform interval
	 */
	private static String getPeriodicInformInterval(Unit unit) {
		return Parameters.getUnitParameterValue(unit, "ManagementServer.PeriodicInformInterval");
	}

	
	public boolean isLine1Applicable() throws SQLException {
		return line1Configured != VoipConfigured.NOT_APPLICABLE;
	}
	public boolean isLine2Applicable() throws SQLException {
		return line2Configured != VoipConfigured.NOT_APPLICABLE;
	}
	
	/**
	 * Checks if is line1 registered.
	 *
	 * @return true, if is line1 registered
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public boolean isLine1Registered() throws SQLException {
		return isLineRegisteredOk(sessionId, VoipLine.LINE_0);
	}

	/**
	 * Checks if is line2 registered.
	 *
	 * @return true, if is line2 registered
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	public boolean isLine2Registered() throws SQLException {
		return isLineRegisteredOk(sessionId, VoipLine.LINE_1);
	}

	/**
	 * Gets the line1 configured.
	 *
	 * @return the line1 configured
	 */
	public VoipConfigured getLine1Configured() {
		return line1Configured;
	}

	/**
	 * Checks if is line1 configured.
	 *
	 * @return true, if is line1 configured
	 */
	public boolean isLine1Configured() {
		return line1Configured.equals(VoipConfigured.CONFIGURED);
	}

	/**
	 * Checks if is line1 configured error.
	 *
	 * @return true, if is line1 configured error
	 */
	public boolean isLine1ConfiguredError() {
		return line1Configured.equals(VoipConfigured.CONFIGURE_ERROR);
	}

	/**
	 * Checks if is line1 configured not enabled.
	 *
	 * @return true, if is line1 configured not enabled
	 */
	public boolean isLine1ConfiguredNotEnabled() {
		return line1Configured.equals(VoipConfigured.CONFIGURED_NOT_ENABLED);
	}

	/**
	 * Sets the line1 configured.
	 *
	 * @param line1active the new line1 configured
	 */
	public void setLine1Configured(VoipConfigured line1active) {
		this.line1Configured = line1active;
	}

	/**
	 * Gets the line2 configured.
	 *
	 * @return the line2 configured
	 */
	public VoipConfigured getLine2Configured() {
		return line2Configured;
	}

	/**
	 * Checks if is line2 configured.
	 *
	 * @return true, if is line2 configured
	 */
	public boolean isLine2Configured() {
		return line2Configured.equals(VoipConfigured.CONFIGURED);
	}

	/**
	 * Checks if is line2 configured error.
	 *
	 * @return true, if is line2 configured error
	 */
	public boolean isLine2ConfiguredError() {
		return line2Configured.equals(VoipConfigured.CONFIGURE_ERROR);
	}

	/**
	 * Checks if is line2 configured not enabled.
	 *
	 * @return true, if is line2 configured not enabled
	 */
	public boolean isLine2ConfiguredNotEnabled() {
		return line2Configured.equals(VoipConfigured.CONFIGURED_NOT_ENABLED);
	}

	/**
	 * Sets the line2 configured.
	 *
	 * @param line2active the new line2 configured
	 */
	public void setLine2Configured(VoipConfigured line2active) {
		this.line2Configured = line2active;
	}

	/**
	 * Checks if is within service window.
	 *
	 * @return boolean A boolean saying wether or not the current unit is within the service window
	 * @throws ParseException the parse exception
	 */
	public boolean isWithinServiceWindow() throws ParseException {
		String serviceWindow = currentUnit.getParameters().get(SystemParameters.SERVICE_WINDOW_REGULAR);
		if (serviceWindow == null)
			serviceWindow = DEFAULT_SERVICE_WINDOW;

		TimeWindow timeWindow = new TimeWindow(serviceWindow);

		String freqStr = currentUnit.getParameters().get(SystemParameters.SERVICE_WINDOW_FREQUENCY);
		Integer frequencyInteger = getIntegerOrDefault(freqStr, DEFAULT_FREQUENCY);

		int msPerInterval = (int) (timeWindow.getWeeklyLength() / frequencyInteger);

		String spreadStr = currentUnit.getParameters().get(SystemParameters.SERVICE_WINDOW_SPREAD);
		float spread = getFloatOrDefault(spreadStr, DEFAULT_SPREAD) / 100;

		String lastConnectTimestamp = currentUnit.getParameters().get(SystemParameters.LAST_CONNECT_TMS);

		if (lastConnectTimestamp != null) {
			Date lastConnect = sdf.parse(lastConnectTimestamp);
			Date nextConnectNoSpread = new Date(lastConnect.getTime() + msPerInterval);
			Date nextConnectUpperBound = new Date((long) (nextConnectNoSpread.getTime() + (spread * msPerInterval)));
			Date currentTime = new Date(System.currentTimeMillis());
			if (currentTime.before(nextConnectUpperBound))
				return true;
		}

		return false;
	}

	/**
	 * Instantiates a new unit status info.
	 *
	 * @param unit the unit
	 * @param mainDataSource
	 * @param syslogDataSource
	 */
	private UnitStatusInfo(Unit unit, DataSource mainDataSource, DataSource syslogDataSource) {
		this.currentUnit = unit;
		this.mainDataSource = mainDataSource;
		this.syslogDataSource = syslogDataSource;
	}

	/**
	 * Sets the last connect timestamp.
	 *
	 * @param string the new last connect timestamp
	 */
	private void setLastConnectTimestamp(String string) {
		this.lastConnectTimestamp = string;
	}

	/**
	 * Sets the first connect timestamp.
	 *
	 * @param string the new first connect timestamp
	 */
	private void setFirstConnectTimestamp(String string) {
		this.firstConnectTimestamp = string;
	}

	/**
	 * Gets the unit.
	 *
	 * @return the unit
	 */
	public Unit getUnit() {
		return currentUnit;
	}

	/**
	 * Gets the first connect timestamp.
	 *
	 * @return the first connect timestamp
	 */
	public String getFirstConnectTimestamp() {
		return firstConnectTimestamp;
	}

	/**
	 * Gets the last connect timestamp.
	 *
	 * @return the last connect timestamp
	 */
	public String getLastConnectTimestamp() {
		return lastConnectTimestamp;
	}

	/**
	 * Gets the next connect timestamp.
	 *
	 * @return the next connect timestamp
	 */
	public String getNextConnectTimestamp() {
		return nextConnectTimestamp;
	}

	/**
	 * Checks if is checks for connected.
	 *
	 * @return true, if is checks for connected
	 */
	public boolean isHasConnected() {
		return hasConnected;
	}

	/**
	 * Gets the overall status.
	 *
	 * @return the overall status
	 * @throws ParseException the parse exception
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws SecurityException the security exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws NoSuchMethodException the no such method exception
	 */
	public UnitStatusScore getOverallStatus() throws ParseException, SQLException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		return getOverallStatus(getTotalScore());
	}

	/**
	 * Gets the overall status.
	 *
	 * @param totalScore the total score
	 * @return the overall status
	 * @throws ParseException the parse exception
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws SecurityException the security exception
	 */
	private UnitStatusScore getOverallStatus(Double totalScore) throws ParseException, SQLException, IOException, IllegalArgumentException, SecurityException {
		return new UnitStatusScore(totalScore, getHardwareRecords(), getSyslogEntriesFromCache(), isWithinServiceWindow(), is1LinesHasProblems(), is2LinesHasProblems());
	}

	/**
	 * Gets the hardware report.
	 *
	 * @return the hardware report
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized Report<RecordHardware> getHardwareReport() throws SQLException, IOException {
		if (this.hardwareReport == null) {
			Report<RecordHardware> hardwareReport = SessionCache.getHardwareReport(sessionId, currentUnit.getId(), fromDate, toDate, mainDataSource, syslogDataSource);
			if (hardwareReport == null) // Generated report is empty or reports is not supported
				hardwareReport = new Report<RecordHardware>(RecordHardware.class, PeriodType.ETERNITY);
			this.hardwareReport = hardwareReport;
		}
		return this.hardwareReport;
	}

	/**
	 * Gets the hardware records.
	 *
	 * @return the hardware records
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized List<RecordUIDataHardware> getHardwareRecords() throws SQLException, IOException {
		Collection<RecordHardware> hwRecords = getHardwareReport().getMap().values();
		return RecordUIDataHardware.convertRecords(getUnit(), new ArrayList<RecordHardware>(hwRecords), new RecordUIDataHardwareFilter(new UnitListData(),
				new HashMap<>()));
	}

	/**
	 * Get The syslog entries for the current unit, from and to dates.
	 *
	 * @return A list of syslog entries
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws SecurityException the security exception
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public synchronized List<SyslogEntry> getSyslogEntries() throws IllegalArgumentException, SecurityException, SQLException {
		return SyslogRetriever.getInstance().getSyslogEntries(currentUnit, fromDate, toDate, 100, sessionId, mainDataSource, syslogDataSource);
	}

	/**
	 * Gets the syslog entries from cache.
	 *
	 * @return the syslog entries from cache
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws SecurityException the security exception
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public synchronized List<SyslogEntry> getSyslogEntriesFromCache() throws IllegalArgumentException, SecurityException, SQLException {
		if (this.syslogEntries == null)
			return (this.syslogEntries = SyslogRetriever.getInstance().getSyslogEntries(currentUnit, fromDate, toDate, 100, sessionId, mainDataSource, syslogDataSource));
		return this.syslogEntries;
	}

	/**
	 * Gets the syslog report.
	 * @param syslogFilter
	 * @return the syslog report
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 */
	public synchronized Report<RecordSyslog> getSyslogReport(String syslogFilter) throws SQLException, IOException, ParseException {
		if (this.syslogReport == null) {
			String toUseAsFilter = (syslogFilter != null ? ("%" + syslogFilter + "%") : null);
			Report<RecordSyslog> _syslogReport = SessionCache.getSyslogReport(sessionId, currentUnit.getId(), fromDate, toDate, toUseAsFilter, mainDataSource, syslogDataSource);
			if (_syslogReport == null) // Generated report is empty or reports is not supported
				_syslogReport = new Report<RecordSyslog>(RecordSyslog.class, PeriodType.ETERNITY);
			this.syslogReport = _syslogReport;
		}
		return this.syslogReport;
	}

	/**
	 * Gets the voip report.
	 *
	 * @return the voip report
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized Report<RecordVoip> getVoipReport() throws SQLException, IOException {
		if (this.voipReport == null) {
			Report<RecordVoip> voipReport = SessionCache.getVoipReport(sessionId, currentUnit.getId(), fromDate, toDate, mainDataSource, syslogDataSource);
			if (voipReport == null) // Generated report is empty or reports is not supported
				voipReport = new Report<RecordVoip>(RecordVoip.class, PeriodType.ETERNITY);
			this.voipReport = voipReport;
		}
		return this.voipReport;
	}

	/**
	 * Sets the start tms.
	 *
	 * @param date the new start tms
	 */
	public void setStartTms(Date date) {
		this.fromDate = date;
	}

	/**
	 * Sets the end tms.
	 *
	 * @param date the new end tms
	 */
	public void setEndTms(Date date) {
		this.toDate = date;
	}

	public boolean supportsTr111() {
		String udpcra = Parameters.getUnitParameterValue(currentUnit, "InternetGatewayDevice.ManagementServer.UDPConnectionRequestAddress");
		if (udpcra != null && udpcra.length() > 0)
			return true;
		return false;
	}

	public boolean isBehindNat() {
		String cru = Parameters.getUnitParameterValue(currentUnit, "InternetGatewayDevice.ManagementServer.ConnectionRequestURL");
		if (cru != null)
			return !IPAddress.isPublic(getBaseAddress(cru));
		else
			return false;
	}

	private String getBaseAddress(String addr) {
		if (addr.indexOf("http://") > -1)
			addr = addr.substring(addr.indexOf("http://") + 7);
		if (addr.indexOf("/") > -1)
			addr = addr.substring(0, addr.indexOf("/"));
		return addr;
	}

}