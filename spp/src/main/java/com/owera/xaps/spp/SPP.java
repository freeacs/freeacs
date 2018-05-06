package com.owera.xaps.spp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Context;
import com.owera.xaps.Properties.Module;
import com.owera.xaps.base.BaseCache;
import com.owera.xaps.base.DownloadLogic;
import com.owera.xaps.base.JobLogic;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.ServiceWindow;
import com.owera.xaps.base.db.DBAccess;
import com.owera.xaps.base.db.DBAccessSession;
import com.owera.xaps.base.db.DBAccessStatic;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.FileType;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.JobFlag.JobServiceWindow;
import com.owera.xaps.dbi.JobFlag.JobType;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.UnitParameter;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.Unittype.ProvisioningProtocol;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.UnittypeParameters;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.util.ProvisioningMessage;
import com.owera.xaps.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.owera.xaps.dbi.util.ProvisioningMessage.ProvOutput;
import com.owera.xaps.dbi.util.ProvisioningMessage.ProvStatus;
import com.owera.xaps.dbi.util.ProvisioningMode;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.dbi.util.TimestampWrapper;
import com.owera.xaps.spp.response.ProvisioningResponse;
import com.owera.xaps.spp.response.SPA;

public class SPP {

  public static byte[] provision(SessionData sessionData) throws SQLException, NoAvailableConnectionException {
    DBI dbi = DBAccess.getDBI();
    sessionData.setDbAccess(new DBAccessSession(dbi));
    long start = System.currentTimeMillis();
    byte[] output = null;
    sessionData.setResp(getProvisioningResponse(sessionData));
    if (sessionData.getSerialNumber() == null) {
      Log.error(SPP.class, "No serialnumber found - cannot provision this unit - will return an empty output");
      output = sessionData.getResp().getEmptyResponse();
    } else {
      Context.put(Context.X, sessionData.getSerialNumber(), BaseCache.SESSIONDATA_CACHE_TIMEOUT);
      Unit unit = readUnit(sessionData);
      if (unit == null) {
        if (Properties.isDiscoveryMode()) {
          if (sessionData.getModelName() != null) {
            Log.warn(SPP.class, "Serialnumber " + sessionData.getSerialNumber() + " was not found, but since discovery mode activiated, unit will be created");
            writeUnittypeProfileUnit(sessionData, sessionData.getModelName(), sessionData.getSerialNumber());
            output = getResponse(sessionData);
          } else {
            Log.warn(SPP.class, "Serialnumber " + sessionData.getSerialNumber() + " was not found in xAPS, even though discovery mode activiated, no modelname was found - so we cannot create unit");
            output = sessionData.getResp().getEmptyResponse();
          }
        } else {
          Log.warn(SPP.class, "Serialnumber " + sessionData.getSerialNumber() + " was not found - will return an empty output");
          output = sessionData.getResp().getEmptyResponse();
        }
      } else {
        sessionData.setUnit(unit);
        output = getResponse(sessionData);
      }
    }
    if (output == null)
      output = sessionData.getResp().getEmptyResponse();
    byte[] encrypted = encrypt(output, sessionData);
    updateXAPS(sessionData, dbi);
    updateSomeLogs(sessionData, output, start);
    if (encrypted != null) {
      sessionData.setEncrypted(true);
      return encrypted;
    }
    sessionData.setEncrypted(false);
    return output;
  }

  /**
   * Responsible for finding the unit in the database. Will first try to read
   * the unit based on serialnumber = unitId. If not, it will search all param
   * values to match with the unit id
   * 
   * @return
   * @throws NoAvailableConnectionException
   * @throws SQLException
   */
  private static Unit readUnit(SessionData sessionData) throws SQLException, NoAvailableConnectionException {
    Unit unit = sessionData.getDbAccess().readUnit(sessionData.getSerialNumber());
    if (unit != null)
      return unit;
    XAPS xaps = sessionData.getDbAccess().getXaps();
    XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
    if (sessionData.getSerialNumber() != null)
      return xapsUnit.getUnitByValue(sessionData.getSerialNumber(), null, null);
    if (sessionData.getMac() != null)
      return xapsUnit.getUnitByValue(sessionData.getMac(), null, null);
    return null;
  }

  private static byte[] encrypt(byte[] output, SessionData sessionData) {
    if (sessionData.getUnit() != null) {
      Unit u = sessionData.getUnit();
      String secret = u.getParameters().get(SystemParameters.SECRET);
      if (secret != null) {
        String scheme = u.getParameters().get(SystemParameters.SECRET_SCHEME);
        if (scheme != null) {
          try {
            Log.info(SPP.class, "Will encrypt the output");
            return sessionData.getResp().encrypt(output, secret);
          } catch (Throwable t) {
            Log.error(SPP.class, "The encryption of the respose failed - sending it in cleartext", t);
          }
        }
      }
    }
    return null;
  }

  private static byte[] getResponseJob(SessionData sessionData) throws SQLException {
    // If 1, there has already been a job (in this job-chain) which has
    // been disruptive
    // which overrides the service-window constraint.
    byte[] response = null;
    Job job = sessionData.getJob();
    ProvisioningMessage pm = sessionData.getProvisioningMessage();
    pm.setJobId(job.getId());
    pm.setProvMode(ProvisioningMode.REGULAR);
    ServiceWindow sw = null;
    String disruptiveJob = sessionData.getOweraParameters().getValue(SystemParameters.JOB_DISRUPTIVE);
    Unit unit = sessionData.getUnit();
    boolean disruptive = false;
    if (disruptiveJob != null && disruptiveJob.equals("1"))
      disruptive = true;
    boolean disruptiveSw = (job.getFlags().getServiceWindow() == JobServiceWindow.DISRUPTIVE);
    if (job.getFlags().getType() == JobType.RESTART) {
      sw = new ServiceWindow(sessionData, disruptiveSw);
      sessionData.getPIIDecision().setDisruptiveSW(sw);
      sessionData.setPeriodicInterval(sessionData.getPIIDecision().nextPII());
      pm.setProvOutput(ProvOutput.REBOOT);
      if (sw.isWithin()) {
        pm.setProvStatus(ProvStatus.OK);
        response = sessionData.getResp().getRebootResponse();
        sessionData.setRestart("0");
      } else {
        pm.setProvStatus(ProvStatus.DELAYED);
        String msg = "The provisioning is delayed " + sessionData.getPeriodicInterval() + "s, because the service-window is " + sw.toString();
        Log.notice(SPP.class, msg);
        response = sessionData.getResp().getDelayResponse(sessionData.getPeriodicInterval());
      }
    } else if (job.getFlags().getType() == JobType.SOFTWARE) {
      sw = new ServiceWindow(sessionData, disruptiveSw);
      pm.setProvOutput(ProvOutput.SOFTWARE);
      if ((disruptive || sw.isWithin()) && DownloadLogic.downloadAllowed(Module.SPP, job)) {
        unit = sessionData.getUnit();
        String dsw = unit.getParameters().get(SystemParameters.DESIRED_SOFTWARE_VERSION);
        if (sessionData.getUnittype().getFiles().getByVersionType(dsw, FileType.SOFTWARE) == null) {
          String msg = "DesiredSoftwareVersion were set to " + dsw + ", but this software was not found in xAPS - will return an empty output";
          Log.error(SPP.class, msg);
          pm.setProvStatus(ProvStatus.ERROR);
          pm.setErrorMessage(msg);
          pm.setErrorResponsibility(ErrorResponsibility.SERVER);
          response = sessionData.getResp().getEmptyResponse();
        } else {
          sw = new ServiceWindow(sessionData, true);
          sessionData.getPIIDecision().setDisruptiveSW(sw);
          sessionData.setPeriodicInterval(sessionData.getPIIDecision().nextPII());
          pm.setProvStatus(ProvStatus.OK);
          pm.setFileVersion(dsw);
          Log.notice(SPP.class, "DesiredSoftwareVersion is set to upgrade to version " + dsw + ", will only return an upgrade configuration to the device");
          String upgradeURL = getUpgradeURL(sessionData.getReqURL(), sessionData.getContextPath());
          upgradeURL += "file/" + dsw + "/" + sessionData.getUnittype().getName() + "/" + sessionData.getUnitId();
          upgradeURL = upgradeURL.replaceAll(" ", "--");
          response = getResponseSoftwareUpgrade(sessionData, dsw, unit);
          response = sessionData.getResp().getUpgradeResponse(upgradeURL);
        }
      } else {
        pm.setProvStatus(ProvStatus.DELAYED);
        Log.notice(SPP.class, "The provisioning is delayed " + sessionData.getPeriodicInterval() + "s, because the service-window is " + sw.toString());
        response = sessionData.getResp().getDelayResponse(sessionData.getPeriodicInterval());
      }
    } else {
      sw = new ServiceWindow(sessionData, false);
      sessionData.getPIIDecision().setDisruptiveSW(sw);
      sessionData.setPeriodicInterval(sessionData.getPIIDecision().nextPII());
      pm.setProvOutput(ProvOutput.CONFIG);
      if (disruptive || sw.isWithin()) {
        pm.setProvStatus(ProvStatus.OK);
        response = sessionData.getResp().getConfigResponse(unit, sessionData.getPeriodicInterval());
      } else {
        pm.setProvStatus(ProvStatus.DELAYED);
        Log.notice(SPP.class, "The provisioning is delayed " + sessionData.getPeriodicInterval() + "s, because the service-window is " + sw.toString());
        response = sessionData.getResp().getDelayResponse(sessionData.getPeriodicInterval());
      }
    }
    return response;

  }

  private static String getUpgradeURL(String reqURL, String contextPath) {
    String upgradeURL = null;
    if (reqURL.indexOf("://") > -1) {
      int lastSlashPos = reqURL.indexOf("/", reqURL.indexOf("://") + 3);
      if (lastSlashPos == -1)
        upgradeURL = reqURL + "/";
      else
        upgradeURL = reqURL.substring(0, lastSlashPos + 1);
    } else {
      upgradeURL = "/";
    }
    if (contextPath != null && contextPath.trim().length() > 0)
      upgradeURL += contextPath + "/";
    return upgradeURL;
  }

  private static byte[] getResponseSoftwareUpgrade(SessionData sessionData, String dsw, Unit unit) throws SQLException {
    Log.notice(SPP.class, "DesiredSoftwareVersion is set to upgrade to version " + dsw + ", will only return an upgrade configuration to the device");
    if (Properties.getUpgradeOutput(sessionData.getModelName()).equals("Regular")) {
      String upgradeURL = unit.getParameters().get(SystemParameters.SOFTWARE_URL);
      if (upgradeURL == null) {
        upgradeURL = getUpgradeURL(sessionData.getReqURL(), sessionData.getContextPath());
        upgradeURL += "file/" + dsw + "/" + sessionData.getUnittype().getName() + "/" + sessionData.getUnitId();
      }
      upgradeURL = upgradeURL.replaceAll(" ", "--");
      return sessionData.getResp().getUpgradeResponse(upgradeURL);
    } // Staging upgrade output - return binaries directly
    DownloadLogic.removeOldest();
    // XAPS xaps = DBAccess.getDBI().getXaps();
    // xaps.updateFirmwares(sessionData.getUnittype());
    com.owera.xaps.dbi.File firmware = sessionData.getUnittype().getFiles().getByVersionType(dsw, FileType.SOFTWARE);
    Log.notice(SPP.class, "The firmware " + firmware.getName() + " is found and will be returned directly (upgrade-output=Staging)");
    sessionData.setBinaries(true);
    return DBAccessStatic.readFirmwareImage(firmware);
  }

  private static byte[] getResponseRegular(SessionData sessionData) {
    byte[] response = null;
    Unit unit = sessionData.getUnit();
    ProvisioningMessage pm = sessionData.getProvisioningMessage();
    pm.setProvMode(ProvisioningMode.REGULAR);
    String restart = unit.getParameters().get(SystemParameters.RESTART);
    String dsw = unit.getParameters().get(SystemParameters.DESIRED_SOFTWARE_VERSION);
    if (restart != null && restart.equals("1")) {
      ServiceWindow sw = new ServiceWindow(sessionData, true);
      sessionData.getPIIDecision().setDisruptiveSW(sw);
      sessionData.setPeriodicInterval(sessionData.getPIIDecision().nextPII());
      pm.setProvOutput(ProvOutput.REBOOT);
      if (sw.isWithin()) {
        pm.setProvStatus(ProvStatus.OK);
        response = sessionData.getResp().getRebootResponse();
        sessionData.setRestart("0");
      } else {
        pm.setProvStatus(ProvStatus.DELAYED);
        Log.notice(SPP.class, "The provisioning is delayed " + sessionData.getPeriodicInterval() + "s, because the service-window is " + sw.toString());
        response = sessionData.getResp().getDelayResponse(sessionData.getPeriodicInterval());
      }
    } else if (dsw != null && !dsw.equals(sessionData.getSoftwareVersion())) {
      if (sessionData.getUnittype().getFiles().getByVersionType(dsw, FileType.SOFTWARE) == null) {
        Log.error(SPP.class, "DesiredSoftwareVersion were set to " + dsw + ", but this software was not found - will return an empty output");
        pm.setErrorMessage("DesiredSoftwareVersion were set to " + dsw + ", but this software was not found");
        pm.setErrorResponsibility(ErrorResponsibility.SERVER);
        pm.setProvOutput(ProvOutput.SOFTWARE);
        pm.setProvStatus(ProvStatus.ERROR);
        response = sessionData.getResp().getEmptyResponse();
      } else {
        ServiceWindow sw = new ServiceWindow(sessionData, true);
        sessionData.getPIIDecision().setDisruptiveSW(sw);
        sessionData.setPeriodicInterval(sessionData.getPIIDecision().nextPII());
        pm.setProvOutput(ProvOutput.SOFTWARE);
        if (sw.isWithin() && DownloadLogic.downloadAllowed(Module.SPP, null)) {
          // firmware prov
          pm.setProvStatus(ProvStatus.OK);
          pm.setFileVersion(dsw);
          Log.notice(SPP.class, "DesiredSoftwareVersion is set to upgrade to version " + dsw + ", will only return an upgrade configuration to the device");
          String upgradeURL = getUpgradeURL(sessionData.getReqURL(), sessionData.getContextPath());
          upgradeURL += "file/" + dsw + "/" + sessionData.getUnittype().getName() + "/" + sessionData.getUnitId();
          upgradeURL = upgradeURL.replaceAll(" ", "--");
          response = sessionData.getResp().getUpgradeResponse(upgradeURL);
          DownloadLogic.add();
        } else {
          pm.setProvStatus(ProvStatus.DELAYED);
          Log.notice(SPP.class, "The provisioning is delayed " + sessionData.getPeriodicInterval() + "s, because the service-window is " + sw.toString());
          response = sessionData.getResp().getDelayResponse(sessionData.getPeriodicInterval());
        }
      }
    } else {
      ServiceWindow sw = new ServiceWindow(sessionData, false);
      sessionData.getPIIDecision().setDisruptiveSW(sw);
      sessionData.setPeriodicInterval(sessionData.getPIIDecision().nextPII());
      pm.setProvOutput(ProvOutput.CONFIG);
      if (sw.isWithin()) {
        // config prov
        pm.setProvStatus(ProvStatus.OK);
        response = sessionData.getResp().getConfigResponse(unit, sessionData.getPeriodicInterval());
      } else {
        pm.setProvStatus(ProvStatus.DELAYED);
        Log.notice(SPP.class, "The provisioning is delayed " + sessionData.getPeriodicInterval() + "s, because the service-window is " + sw.toString());
        response = sessionData.getResp().getDelayResponse(sessionData.getPeriodicInterval());
      }
    }
    return response;

  }

  private static byte[] getResponse(SessionData sessionData) throws NoAvailableConnectionException, SQLException {
    Unit unit = sessionData.getUnit();
    sessionData.setUnittype(unit.getUnittype());
    sessionData.setProfile(unit.getProfile());
    sessionData.setUnitId(unit.getId());
    sessionData.updateParametersFromDB(unit.getId());

    boolean ok = JobLogic.checkJobOK(sessionData);
    if (ok)
      JobLogic.checkNewJob(Module.SPP, sessionData);

    Job job = sessionData.getJob();
    if (job == null) {
      return getResponseRegular(sessionData);
    }
    return getResponseJob(sessionData);
  }

  private static void writeUnittypeProfileUnit(SessionData sessionData, String modelName, String serialNumber) throws NoAvailableConnectionException, SQLException {
    XAPS xaps = sessionData.getDbAccess().getXaps();
    XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
    Unittype unittype = xaps.getUnittype(modelName);
    if (unittype == null) {
      // Will automatically create "Default" profile
      unittype = new Unittype(modelName, "Unknown", "An autogenerated unittype", ProvisioningProtocol.NA);
      xaps.getUnittypes().addOrChangeUnittype(unittype, xaps);
    }
    Profile profile = unittype.getProfiles().getByName("Default");
    if (profile == null) {
      profile = new Profile("Default", unittype);
      unittype.getProfiles().addOrChangeProfile(profile, xaps);
    }
    List<String> unitIds = new ArrayList<String>();
    String unitId = serialNumber;
    if (modelName.equals("HydrogenHA"))
      unitId = "002194-HydrogenHA-" + unitId;
    if (modelName.equals("VoiceCathcer") || modelName.equals("NPA211E"))
      unitId = "002194-VoiceCatcher-" + unitId;
    unitIds.add(unitId);
    xapsUnit.addUnits(unitIds, profile);
    sessionData.setUnit(xapsUnit.getUnitByValue(serialNumber, unittype, profile));

    List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
    UnittypeParameter secretUtp = unittype.getUnittypeParameters().getByName(SystemParameters.SECRET);
    UnitParameter up = new UnitParameter(secretUtp, serialNumber, sessionData.getSecret(), profile);
    unitParameters.add(up);
    xapsUnit.addOrChangeUnitParameters(unitParameters, profile);

  }

  @SuppressWarnings("rawtypes")
  private static ProvisioningResponse getProvisioningResponse(SessionData sessionData) {
    ProvisioningResponse resp = new SPA();
    String className = null;
    if (sessionData.getModelName() != null) {
      try {
        className = Properties.getProvisioningOutput(sessionData.getModelName());
        Class c = Class.forName("com.owera.xaps.spp.response." + className);
        Object o = c.newInstance();
        if (o instanceof ProvisioningResponse)
          return (ProvisioningResponse) o;
        Log.error(SPP.class, "The class " + c.getName() + " did not implement the ProvisioningResponse interface - using SPA as default output generator.");
      } catch (Throwable t) {
        Log.error(SPP.class, "Tried to load a ProvisioningOutput class " + className + ", but it failed - using SPA as default output generator.", t);
      }
    } else {
      Log.error(SPP.class, "No modelname found - device type is unknown - using SPA as default output generator.");
    }
    return resp;
  }

  private static void addToUnitParameterList(String paramName, List<UnitParameter> unitParameters, UnittypeParameters utps, String sessionParameter, Unit unit, Profile profile) {
    UnittypeParameter utp = utps.getByName(paramName);
    if (utp != null && sessionParameter != null && unit.getParameters().get(paramName) == null)
      unitParameters.add(new UnitParameter(utp, unit.getId(), sessionParameter, profile));
    else if (utp != null && sessionParameter != null && unit.getParameters().get(paramName) != null && !unit.getParameters().get(paramName).equals(sessionParameter))
      unitParameters.add(new UnitParameter(utp, unit.getId(), sessionParameter, profile));
  }

  private static void updateXAPS(SessionData sessionData, DBI dbi) throws NoAvailableConnectionException, SQLException {
    if (sessionData.getUnit() != null) {
      Unit unit = sessionData.getUnit();
      List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
      UnittypeParameters utps = unit.getUnittype().getUnittypeParameters();
      addToUnitParameterList(SystemParameters.SOFTWARE_VERSION, unitParameters, utps, sessionData.getSoftwareVersion(), unit, sessionData.getProfile());
      // addToUnitParameterList(SystemParameters.MAC, unitParameters, utps,
      // sessionData.getMac(), unit, sessionData.getProfile());
      addToUnitParameterList(SystemParameters.SERIAL_NUMBER, unitParameters, utps, sessionData.getSerialNumber(), unit, sessionData.getProfile());
      addToUnitParameterList(SystemParameters.PERIODIC_INTERVAL, unitParameters, utps, "" + sessionData.getPeriodicInterval(), unit, sessionData.getProfile());
      addToUnitParameterList(SystemParameters.IP_ADDRESS, unitParameters, utps, sessionData.getIpAddress(), unit, sessionData.getProfile());
      addToUnitParameterList(SystemParameters.RESTART, unitParameters, utps, sessionData.getRestart(), unit, sessionData.getProfile());
      // Missing parameters:
      // lastConnectMessage - will not have a purpose until we have the
      // situation of a registered device
      // which is not possible to provision (like failed authentication - which
      // is currently not implemented)

      // lastProvType
      // lastProvStatus
      // lastProvMessage
      String lct = TimestampWrapper.tmsFormat.format(new Date());
      String fct = unit.getParameters().get(SystemParameters.FIRST_CONNECT_TMS);
      if (fct == null)
        addToUnitParameterList(SystemParameters.FIRST_CONNECT_TMS, unitParameters, utps, lct, unit, sessionData.getProfile());
      addToUnitParameterList(SystemParameters.LAST_CONNECT_TMS, unitParameters, utps, lct, unit, sessionData.getProfile());
      XAPSUnit xapsUnit = DBAccess.getXAPSUnit(dbi.getXaps());
      xapsUnit.addOrChangeUnitParameters(unitParameters, unit.getProfile());
      Log.info(SPP.class, unitParameters.size() + " unit parameters were updated in xAPS");
    }
  }

  private static void updateSomeLogs(SessionData sessionData, byte[] output, long start) {
    ProvisioningMessage pm = sessionData.getProvisioningMessage();
    pm.setSessionLength((int) (System.currentTimeMillis() - start));

    String message = "Response to [serialnumber: " + sessionData.getSerialNumber();
    message += ", mac: " + sessionData.getMac() + ", softwareversion: " + sessionData.getSoftwareVersion();
    message += ", ipaddress: " + sessionData.getIpAddress() + "]:\n";
    if (sessionData.isBinaries()) {
      message += "Binaries with length " + output.length + "\n";
      Log.notice(SPP.class, "Have provisioned data to the device");
    } else {
      if (new String(output).equals(new String(sessionData.getResp().getEmptyResponse())))
        Log.notice(SPP.class, "Have not provisioned anything to the device");
      else
        Log.notice(SPP.class, "Have provisioned data to the device");
      message += new String(output);
    }
    Log.conversation(sessionData, message);
  }
}
