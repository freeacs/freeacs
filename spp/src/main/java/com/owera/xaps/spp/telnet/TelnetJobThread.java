package com.owera.xaps.spp.telnet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.base.SessionDataI;
import com.owera.xaps.base.UnitJob;
import com.owera.xaps.base.db.DBAccessSession;
import com.owera.xaps.dbi.File;
import com.owera.xaps.dbi.FileType;
import com.owera.xaps.dbi.JobParameter;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.UnitJobStatus;
import com.owera.xaps.dbi.UnitParameter;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.UnittypeParameterFlag;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;

import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.spp.SessionData;

public class TelnetJobThread implements Runnable {

	private static Logger log = new Logger(TelnetJobThread.class);
	private static Map<String, Pattern> compiledPatternMap = new HashMap<String, Pattern>();

	private SessionDataI sessionData = new SessionData();
	private Monitor m;
	private TelnetJob tj;
	private Map<String, JobParameter> jobParams;
	private XAPS xaps;
	private XAPSUnit xapsUnit;
	private ConnectionProperties xapsCp;
	private UnitJob unitJob;

	public TelnetJobThread(Monitor m, TelnetJob tj, XAPS xaps, ConnectionProperties xapsCp) {
		this.m = m;
		this.tj = tj;
		sessionData.setUnittype(tj.getJob().getUnittype());
		sessionData.setUnitId(tj.getUnitId());
		sessionData.setJob(tj.getJob());
		sessionData.setDbAccess(new DBAccessSession(xaps.getDbi()));
		this.xaps = xaps;
		this.xapsCp = xapsCp;
		this.unitJob = new UnitJob(sessionData, tj.getJob(), true);
	}

	@Override
	public void run() {
		AutomatedTelnetClient atc = null;
		try {
			xapsUnit = new XAPSUnit(xapsCp, xaps, xaps.getSyslog());
			Unit unit = xapsUnit.getUnitById(tj.getUnitId());
			if (unit == null) // very unlikely - the units are retrieved from a group in a job quite recently
				throw new TelnetClientException("The unit " + tj.getUnitId() + " was not found in xAPS, cannot execute Telnet-session");
			sessionData.setUnit(unit);
			sessionData.setProfile(unit.getProfile());
			unitJob.start();
			jobParams = sessionData.getUnittype().getJobs().readJobParameters(tj.getJob(), unit, xaps);

			// Validation of input-data
			String ip = getParameter(SystemParameters.TELNET_IP);
			if (ip == null)
				ip = getParameter(SystemParameters.IP_ADDRESS);
			if (ip == null)
				throw new TelnetClientException("The IP address of the unit was not specified for unit " + unit.getId() + ", cannot execute Telnet-session");
			String user = getParameter(SystemParameters.TELNET_USERNAME);

			String pass = getParameter(SystemParameters.TELNET_PASSWORD);
			String portStr = getParameter(SystemParameters.TELNET_PORT);
			int port = 23;
			if (portStr != null)
				port = new Integer(portStr);
			String scriptVersion = tj.getJob().getFile().getVersion();
			File file = sessionData.getUnittype().getFiles().getByVersionType(scriptVersion, FileType.TELNET_SCRIPT);
			if (file == null)
				throw new TelnetClientException("The telnet-script-file with version " + scriptVersion + " was not found, cannot execute Telnet-session");
			Map<String, String> unitParameters = unit.getParameters();
			Map<String, Pattern> parsePatternMap = getParsePatternMap(unitParameters, jobParams);
			Map<String, Pattern> abortPatternMap = getAbortPatternMap(unitParameters, jobParams);

			// start Telnet-session
			log.notice("Will run telnet-sesion to IP: " + ip + ":" + port + " using " + user + "/" + pass + " with file " + file.getName() + ", will search for " + parsePatternMap.size()
					+ " patterns");
			atc = new AutomatedTelnetClient(ip, user, pass, port);
			String s = new String(file.getContent());
			String[] lines = s.split("\n");
			List<UnitParameter> upList = new ArrayList<UnitParameter>();
			for (String line : lines) {
				log.debug("Telnet to " + unit.getId() + ": " + line);
				atc.write(line);
				String output = atc.read();
				for (Entry<String, Pattern> entry : abortPatternMap.entrySet()) {
					Matcher m = entry.getValue().matcher(output);
					if (m.find()) {
						throw new TelnetClientException("AbortCondition " + entry.getKey() + " with pattern " + entry.getValue() + " was met in the output from the session, aborting");
					}
				}
				for (Entry<String, Pattern> entry : parsePatternMap.entrySet()) {
					Matcher m = entry.getValue().matcher(output);
					if (m.find()) {
						String value = m.group(0);
						if (m.groupCount() > 0)
							value = m.group(1);
						String utpName = entry.getKey().substring(0, entry.getKey().length() - 3); // remove _PP from parseParam
						UnittypeParameter utp = sessionData.getUnittype().getUnittypeParameters().getByName(utpName);
						if (utp == null) {
							utp = new UnittypeParameter(sessionData.getUnittype(), utpName, new UnittypeParameterFlag("R"));
							sessionData.getUnittype().getUnittypeParameters().addOrChangeUnittypeParameter(utp, xaps);
						}
						UnitParameter up = new UnitParameter(utp, unit.getId(), value, unit.getProfile());
						upList.add(up);
					}
				}
			}
			if (upList.size() > 0)
				xapsUnit.addOrChangeUnitParameters(upList, unit.getProfile());
			unitJob.setJobStartTime(tj.getJobStartedTms());
			unitJob.stop(UnitJobStatus.COMPLETED_OK);
		} catch (Throwable t) {
			if (t instanceof TelnetClientException) {
				log.error(((TelnetClientException) t).getMessage());
			} else
				log.error("An unknown error occured during Telnet-session for unit " + sessionData.getUnitId() + ": ", t);
			try {
				unitJob.stop(UnitJobStatus.CONFIRMED_FAILED);
			} catch (Throwable t2) {
				// Ignore
			}
		} finally {
			if (atc != null) {
				try {
					atc.disconnect();
				} catch (IOException e) {
					// Ignore
				}
			}
			synchronized (m) {
				TelnetProvisioning.getActiveTelnetSessions().remove(sessionData.getUnitId());
				m.notifyAll();
			}
		}
	}

	// Will always return a pattern-map, never null
	private Map<String, Pattern> getParsePatternMap(Map<String, String> unitParameters, Map<String, JobParameter> jobParams) {
		Map<String, Pattern> patternMap = new HashMap<String, Pattern>();
		for (Entry<String, String> entry : unitParameters.entrySet()) {
			if (entry.getKey().startsWith("TelnetDevice.") && entry.getKey().endsWith("_PP")) {
				if (compiledPatternMap.get(entry.getKey()) == null) {
					compiledPatternMap.put(entry.getKey(), Pattern.compile(entry.getValue()));
				}
				patternMap.put(entry.getKey(), compiledPatternMap.get(entry.getKey()));
			}
		}
		for (Entry<String, JobParameter> entry : jobParams.entrySet()) {
			if (entry.getKey().startsWith("TelnetDevice.") && entry.getKey().endsWith("_PP")) {
				if (compiledPatternMap.get(entry.getKey()) == null) {
					compiledPatternMap.put(entry.getKey(), Pattern.compile(entry.getValue().getParameter().getValue()));
				}
				patternMap.put(entry.getKey(), compiledPatternMap.get(entry.getKey()));
			}
		}
		return patternMap;
	}

	// Will always return a pattern-map, never null
	private Map<String, Pattern> getAbortPatternMap(Map<String, String> unitParameters, Map<String, JobParameter> jobParams) {
		Map<String, Pattern> patternMap = new HashMap<String, Pattern>();
		for (Entry<String, String> entry : unitParameters.entrySet()) {
			if (entry.getKey().startsWith("System.X_OWERA-COM.Telnet.AbortCondition.") && entry.getKey().endsWith("_PP")) {
				if (compiledPatternMap.get(entry.getKey()) == null) {
					compiledPatternMap.put(entry.getKey(), Pattern.compile(entry.getValue()));
				}
				patternMap.put(entry.getKey(), compiledPatternMap.get(entry.getKey()));
			}
		}
		for (Entry<String, JobParameter> entry : jobParams.entrySet()) {
			if (entry.getKey().startsWith("System.X_OWERA-COM.Telnet.AbortCondition.") && entry.getKey().endsWith("_PP")) {
				if (compiledPatternMap.get(entry.getKey()) == null) {
					compiledPatternMap.put(entry.getKey(), Pattern.compile(entry.getValue().getParameter().getValue()));
				}
				patternMap.put(entry.getKey(), compiledPatternMap.get(entry.getKey()));
			}
		}
		return patternMap;
	}

	private static Pattern paramPattern = Pattern.compile("(\\$\\{([^\\}]+)\\})");

	private String getParameter(String paramName) throws SQLException, NoAvailableConnectionException {
		if (jobParams != null && jobParams.get(paramName) != null)
			return jobParams.get(paramName).getParameter().getValue();
		String param = sessionData.getUnit().getParameters().get(paramName);
		if (param != null) {
			Matcher m = paramPattern.matcher(param);
			if (m.find()) {
				return sessionData.getUnit().getParameters().get(m.group(2));
			} else {
				return param;
			}
		}
		return null;
	}
}
