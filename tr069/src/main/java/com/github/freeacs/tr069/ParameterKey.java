package com.github.freeacs.tr069;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

public class ParameterKey {
	private String cpeKey;
	private String serverKey;

	public String getCpeKey() {
		return cpeKey;
	}

	public void setCpeKey(String cpeKey) {
		this.cpeKey = cpeKey;
	}

	public String getServerKey() {
		return serverKey;
	}

	public void setServerKey(HTTPReqResData reqRes) throws NoSuchAlgorithmException, SQLException {
		this.serverKey = calculateParameterKey(reqRes);
	}

	public boolean isEqual() {
		if (cpeKey != null && serverKey != null && cpeKey.equals(serverKey))
			return true;
		return false;
	}

	private static String calculateParameterKey(HTTPReqResData reqRes) throws NoSuchAlgorithmException {
		SessionData sessionData = reqRes.getSessionData();
		UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
		Map<String, ParameterValueStruct> fromDB = sessionData.getFromDB();
		if (sessionData.getJobParams() == null) { // already populated job params in fromDB.
			String jobId = sessionData.getFreeacsParameters().getValue(SystemParameters.JOB_CURRENT);
			if (jobId != null && !jobId.trim().equals("")) {
				Job job = DBAccess.getJob(sessionData, jobId);
				if (job != null) {
					Log.debug(ParameterKey.class, "Current job has jobId: " + job.getId() + " -> verification stage, must retrieve job parameters (with RW-flag) to calculate parameterkey correctly");
					Map<String, JobParameter> jobParams = job.getDefaultParameters();
					for (Entry<String, JobParameter> jobParamEntry : jobParams.entrySet()) {
						if (jobParamEntry.getValue().getParameter().getUnittypeParameter().getFlag().isReadWrite()) {
							ParameterValueStruct jobParamPvs = new ParameterValueStruct(jobParamEntry.getKey(), jobParamEntry.getValue().getParameter().getValue());
							fromDB.put(jobParamEntry.getKey(), jobParamPvs);
						}

					}
				}
			}
		}
		String values = "";
		for (Entry<String, ParameterValueStruct> entry : fromDB.entrySet()) {
			String utpName = entry.getKey();
			//			ParameterInfoStruct pis = infoMap.get(utpName);
			UnittypeParameter utp = utps.getByName(utpName);
			//			if (pis != null && pis.isWritable()) {
			if (utp != null && utp.getFlag().isReadWrite()) {
				if (utpName.contains("PeriodicInformInterval"))
					continue;
				if (entry.getValue().getValue().equals("ExtraCPEParam"))
					continue;
				values += entry.getValue().getValue();
			}
		}
		if (values.equals("")) {
			Log.debug(ParameterKey.class, "No device parameter values found, ACS parameterkey = \"No data in DB\"");
			return "No data in DB";
		} else {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hash = md.digest(values.getBytes());
			String parameterKey = "";
			for (byte b : hash) {
				String hex = Integer.toHexString(b + 128);
				if (hex.length() == 1)
					hex = "0" + hex;
				parameterKey += hex;
			}
			String pk = parameterKey.substring(0, 32);
			Log.debug(ParameterKey.class, "The values to be hashed: " + values + " -> ACS parameterkey = " + pk);
			return pk;
		}
	}
}
