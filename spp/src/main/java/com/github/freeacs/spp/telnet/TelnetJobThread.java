package com.github.freeacs.spp.telnet;

import com.github.freeacs.base.SessionDataI;
import com.github.freeacs.base.UnitJob;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.db.DBAccessSession;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.spp.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelnetJobThread implements Runnable {

	private static Logger log = LoggerFactory.getLogger(TelnetJobThread.class);
	private static Map<String, Pattern> compiledPatternMap = new HashMap<String, Pattern>();

	private SessionDataI sessionData = new SessionData();
	private Monitor m;
	private TelnetJob tj;
	private Map<String, JobParameter> jobParams;
	private XAPS xaps;
	private XAPSUnit xapsUnit;
	private com.github.freeacs.base.UnitJob unitJob;

	public TelnetJobThread(Monitor m, TelnetJob tj, DBAccess dbAccess) throws SQLException {
		this.m = m;
		this.tj = tj;
		sessionData.setUnittype(tj.getJob().getUnittype());
		sessionData.setUnitId(tj.getUnitId());
		sessionData.setJob(tj.getJob());
		sessionData.setDbAccess(new DBAccessSession(dbAccess));
		this.xaps = dbAccess.getDBI().getXaps();
		this.unitJob = new UnitJob(sessionData, tj.getJob(), true);
	}

	@Override
	public void run() {
		AutomatedTelnetClient atc = null;
		try {
			xapsUnit = new XAPSUnit(sessionData.getDbAccess().getDbAccess().getXapsDataSource(), xaps, xaps.getSyslog());
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
			log.info("Will run telnet-sesion to IP: " + ip + ":" + port + " using " + user + "/" + pass + " with file " + file.getName() + ", will search for " + parsePatternMap.size()
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

	private String getParameter(String paramName) throws SQLException {
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
