package com.owera.xaps.spp;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.NoDataAvailableException;
import com.owera.xaps.base.OweraParameters;
import com.owera.xaps.base.PIIDecision;
import com.owera.xaps.base.SessionDataI;
import com.owera.xaps.base.db.DBAccessSession;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.JobParameter;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.util.ProvisioningMessage;
import com.owera.xaps.spp.response.ProvisioningResponse;
import com.owera.xaps.tr069.xml.ParameterValueStruct;

public class SessionData implements SessionDataI {

	private String serialNumber;
	private String mac;
	private String softwareVersion;
	private String reqURL;
	private String contextPath;
	private String ipAddress;
	private String modelName;

	private Unit unit;
	private Unittype unittype;
	private Profile profile;
	private OweraParameters oweraParameters;
	private DBAccessSession dbAccess;
	private String unitId;
	private ProvisioningResponse resp;
	private long periodicInterval;
	private String restart;
	//	private String method;
	private Job job;
	private Map<String, JobParameter> jobParams;
	private Map<String, ParameterValueStruct> fromDB;
	private PIIDecision piiDecision;
	private boolean authenticated;
	private boolean firstConnect;
	/* The secret obtained by discovery-mode, basic auth.*/
	private String secret = null;

	// Signal to HTTPProvisioning if the byte[] should be transformed back to String
	private boolean encrypted;

	// Signal to HTTPProvisioning if the byte[] contains a String or binaries (fw)
	private boolean binaries = false;

	// An object to store all kinds of data about the provisioning
	private ProvisioningMessage provisioningMessage = new ProvisioningMessage();

	public SessionData() {
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		if (mac != null)
			this.mac = mac.replaceAll(":", "").toUpperCase();
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Unittype getUnittype() {
		return unittype;
	}

	public void setUnittype(Unittype unittype) {
		this.unittype = unittype;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public OweraParameters getOweraParameters() {
		return oweraParameters;
	}

	public void setOweraParameters(OweraParameters oweraParameters) {
		this.oweraParameters = oweraParameters;
	}

	public DBAccessSession getDbAccess() {
		return dbAccess;
	}

	public void setDbAccess(DBAccessSession dbAccess) {
		this.dbAccess = dbAccess;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public ProvisioningResponse getResp() {
		return resp;
	}

	public void setResp(ProvisioningResponse resp) {
		this.resp = resp;
	}

	public long getPeriodicInterval() {
		return periodicInterval;
	}

	public void setPeriodicInterval(long periodicInterval) {
		this.periodicInterval = periodicInterval;
		this.provisioningMessage.setPeriodicInformInterval((int) periodicInterval);				
	}

	public String getRestart() {
		return restart;
	}

	public void setRestart(String restart) {
		this.restart = restart;
	}

	//	public String getMethod() {
	//		return method;
	//	}
	//
	//	public void setMethod(String method) {
	//		this.method = method;
	//	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Map<String, JobParameter> getJobParams() {
		return jobParams;
	}

	public void setJobParams(Map<String, JobParameter> jobParams) {
		this.jobParams = jobParams;
	}

	public Map<String, ParameterValueStruct> getFromDB() {
		return fromDB;
	}

	public void updateParametersFromDB(String unitId) throws SQLException, NoAvailableConnectionException {
		if (this.getUnit() == null)
			this.unit = this.getDbAccess().readUnit(unitId);
		OweraParameters oweraParameters = new OweraParameters();
		Map<String, ParameterValueStruct> fromDB = new HashMap<String, ParameterValueStruct>();
		for (Entry<String, String> entry : unit.getParameters().entrySet()) {
			if (entry.getKey().startsWith("TelnetDevice."))
				continue;
			if (entry.getKey().startsWith("System."))
				oweraParameters.putPvs(entry.getKey(), new ParameterValueStruct(entry.getKey(), entry.getValue()));
			else
				fromDB.put(entry.getKey(), new ParameterValueStruct(entry.getKey(), entry.getValue()));
		}
		setOweraParameters(oweraParameters);
		setFromDB(fromDB);

		if (fromDB.isEmpty()) {
			if (Properties.isDiscoveryMode()) {
				Log.debug(SessionData.class, "No unit data found & discovery mode true -> first-connect = true, allow to continue");
				this.setFirstConnect(true);
			} else
				throw new NoDataAvailableException();
		}

	}

	public void setFromDB(Map<String, ParameterValueStruct> fromDB) {
		this.fromDB = fromDB;

	}

	/* We choose an easy implementation here */
	public boolean lastProvisioningOK() {
		return true;
	}

	public String getReqURL() {
		return reqURL;
	}

	public void setReqURL(String reqURL) {
		this.reqURL = reqURL;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	@Override
	public PIIDecision getPIIDecision() {
		if (piiDecision == null)
			piiDecision = new PIIDecision(this);
		return piiDecision;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public boolean isFirstConnect() {
		return firstConnect;
	}

	public void setFirstConnect(boolean firstConnect) {
		this.firstConnect = firstConnect;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public ProvisioningMessage getProvisioningMessage() {
		return provisioningMessage;
	}

	@Override
	public void setProvisioningMessage(ProvisioningMessage provisioningMessage) {
		this.provisioningMessage = provisioningMessage;
	}

	public boolean isBinaries() {
		return binaries;
	}

	public void setBinaries(boolean binaries) {
		this.binaries = binaries;
	}

	@Override
	public Long getStartupTmsForSession() {
		// TODO Auto-generated method stub
		return null;
	}
}
