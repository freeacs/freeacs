package com.owera.xaps.base;

import java.sql.SQLException;
import java.util.Map;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.base.db.DBAccessSession;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.JobParameter;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.util.ProvisioningMessage;
import com.owera.xaps.tr069.xml.ParameterValueStruct;

public interface SessionDataI {

	public OweraParameters getOweraParameters();

	public void setOweraParameters(OweraParameters oweraParameters);

	public Unittype getUnittype();

	public void setUnittype(Unittype unittype);

	public Profile getProfile();

	public void setProfile(Profile profile);

	public Unit getUnit();

	public void setUnit(Unit unit);

	public String getUnitId();

	public void setUnitId(String unitId);

	public DBAccessSession getDbAccess();

	public void setDbAccess(DBAccessSession dbAccess);

	public Job getJob();

	public void setJob(Job job);

	public Map<String, JobParameter> getJobParams();

	public void setJobParams(Map<String, JobParameter> jobParams);

	public String getSoftwareVersion();

	public void setSoftwareVersion(String softwareVersion);

	public Map<String, ParameterValueStruct> getFromDB();

	public void updateParametersFromDB(String unitId) throws SQLException, NoAvailableConnectionException, NoDataAvailableException;

	public void setFromDB(Map<String, ParameterValueStruct> fromDB);

	public boolean lastProvisioningOK();

	//	public CPEParameters getCpeParameters();

	/**
	 * The PIIDecision object contains information need to calculate the next
	 * periodic inform. The information needed in this object is listed in
	 * the javadoc for that class. Make sure the method never return null!!
	 * @return
	 */
	public PIIDecision getPIIDecision();

	public void setSerialNumber(String serialNumber);

	public String getSerialNumber();

	public void setMac(String mac);

	public String getMac();

	public ProvisioningMessage getProvisioningMessage();

	public void setProvisioningMessage(ProvisioningMessage provisioningMessage);
	
	public Long getStartupTmsForSession();

}
