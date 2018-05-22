package com.github.freeacs.base;

import com.github.freeacs.base.db.DBAccessSession;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.sql.SQLException;
import java.util.Map;

public interface SessionDataI {

	public ACSParameters getAcsParameters();

	public void setAcsParameters(ACSParameters acsParameters);

	public Unittype getUnittype();

	public void setUnittype(Unittype unittype);

	public Profile getProfile();

	public void setProfile(Profile profile);

	public Unit getUnit();

	public void setUnit(Unit unit);

	public String getUnitId();

	public void setUnitId(String unitId);

	public DBAccessSession getDbAccessSession();

	public Job getJob();

	public void setJob(Job job);

	public Map<String, JobParameter> getJobParams();

	public void setJobParams(Map<String, JobParameter> jobParams);

	public String getSoftwareVersion();

	public void setSoftwareVersion(String softwareVersion);

	public Map<String, ParameterValueStruct> getFromDB();

	public void updateParametersFromDB(String unitId) throws SQLException, NoDataAvailableException;

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

	public ProvisioningMessage getProvisioningMessage();

	public Long getStartupTmsForSession();

}
