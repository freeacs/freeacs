package com.github.freeacs.base;

import com.github.freeacs.base.db.DBAccessSession;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import java.sql.SQLException;
import java.util.Map;

public interface SessionDataI {

  ACSParameters getAcsParameters();

  void setAcsParameters(ACSParameters acsParameters);

  Unittype getUnittype();

  void setUnittype(Unittype unittype);

  Profile getProfile();

  void setProfile(Profile profile);

  Unit getUnit();

  void setUnit(Unit unit);

  String getUnitId();

  void setUnitId(String unitId);

  DBAccessSession getDbAccessSession();

  Job getJob();

  void setJob(Job job);

  Map<String, JobParameter> getJobParams();

  void setJobParams(Map<String, JobParameter> jobParams);

  String getSoftwareVersion();

  void setSoftwareVersion(String softwareVersion);

  Map<String, ParameterValueStruct> getFromDB();

  void updateParametersFromDB(String unitId) throws SQLException, NoDataAvailableException;

  void setFromDB(Map<String, ParameterValueStruct> fromDB);

  boolean lastProvisioningOK();

  //	public CPEParameters getCpeParameters();

  /**
   * The PIIDecision object contains information need to calculate the next periodic inform. The
   * information needed in this object is listed in the javadoc for that class. Make sure the method
   * never return null!!
   *
   * @return
   */
  PIIDecision getPIIDecision();

  void setSerialNumber(String serialNumber);

  String getSerialNumber();

  ProvisioningMessage getProvisioningMessage();

  Long getStartupTmsForSession();
}
