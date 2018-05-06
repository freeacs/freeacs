package com.owera.xaps.tr069;

import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.log.Logger;
import com.owera.xaps.base.DownloadLogic;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.OweraParameters;
import com.owera.xaps.dbi.File;
import com.owera.xaps.dbi.FileType;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.JobParameter;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.dbi.util.SystemParameters.TR069ScriptType;
import com.owera.xaps.tr069.SessionData.Download;
import com.owera.xaps.tr069.xml.ParameterValueStruct;

public class DownloadLogicTR069 {

  // public static boolean download(RequestResponseData reqRes) {
  // String softwareUrl = getSoftwareURL(reqRes);
  // String scriptUrl = getScriptURL(reqRes);
  // if (softwareUrl != null) {
  // reqRes.getResponse().setDownloadURL(softwareUrl);
  // reqRes.getResponse().setDownloadType(DOreq.FILE_TYPE_FIRMWARE);
  // return true;
  // } else if (scriptUrl != null) {
  // reqRes.getResponse().setDownloadURL(scriptUrl);
  // reqRes.getResponse().setDownloadType(DOreq.FILE_TYPE_CONFIG);
  // return true;
  // } else {
  // return false;
  // }
  // }

  private static Logger logger = new Logger();

  public static boolean isScriptDownloadSetup(HTTPReqResData reqRes, Job job) {
    SessionData sessionData = reqRes.getSessionData();
    OweraParameters oweraParams = sessionData.getOweraParameters();
    CPEParameters cpeParams = sessionData.getCpeParameters();
    String scriptVersionFromDB = null;
    String scriptName = null;
    if (job != null) { // retrieve desired-script-version from Job-parameters
      Map<String, JobParameter> jobParams = sessionData.getJobParams();
      for (Entry<String, JobParameter> entry : jobParams.entrySet()) {
        if (SystemParameters.isTR069ScriptVersionParameter(entry.getKey())) {
          scriptName = SystemParameters.getTR069ScriptName(entry.getKey());
          scriptVersionFromDB = entry.getValue().getParameter().getValue();
          // If a job is set to upgrade/upload a script, it should be done, even
          // if
          // the CPE already has been upgraded to the same version. This is done
          // both to avoid creating more logic to handle a job which is
          // "completed, but not
          // executed" and because sometimes two files/script/software can have
          // the same
          // version number AND still be different! We a job we can then force
          // an
          // upgrade to the same version - that is considered a feature!
          break;
        }
      }
    } else {
      Map<String, ParameterValueStruct> opMap = oweraParams.getOweraParams();
      for (Entry<String, ParameterValueStruct> entry : opMap.entrySet()) {
        if (SystemParameters.isTR069ScriptVersionParameter(entry.getKey())) {
          String svDB = entry.getValue().getValue();
          // The config-file-name is the same as the script-name retrieved from
          // the system-parameter
          String name = SystemParameters.getTR069ScriptName(entry.getKey());
          String scriptVersionFromCPE = cpeParams.getConfigFileMap().get(name);
          if (svDB != null && !svDB.equals(scriptVersionFromCPE)) {
            // upgrade
            scriptVersionFromDB = svDB;
            scriptName = name;
            break;
          }
        }
      }
    }
    if (scriptVersionFromDB != null) {
      // scriptVersionFromDB has been found and we must find/build the
      // download-URL
      File file = sessionData.getUnittype().getFiles().getByVersionType(scriptVersionFromDB, FileType.TR069_SCRIPT);
      if (file == null) {
        logger.error("File-type " + FileType.TR069_SCRIPT + " and version " + scriptVersionFromDB + " does not exists - indicate wrong setup of version number");
        return false;
      }

      String downloadURL = null;
      String scriptURLName = SystemParameters.getTR069ScriptParameterName(scriptName, TR069ScriptType.URL);
      if (oweraParams.getValue(scriptURLName) != null)
        downloadURL = oweraParams.getValue(scriptURLName);
      else {
        StringBuffer reqURL = reqRes.getReq().getRequestURL();
        int port = reqRes.getReq().getLocalPort();
        String contextPath = reqRes.getReq().getContextPath();
        int hostEndPos = reqURL.indexOf(contextPath);
        downloadURL = reqURL.substring(0, hostEndPos);
        if (downloadURL.lastIndexOf(":") <= 6)
          downloadURL += ":" + port;
        downloadURL += contextPath;
        downloadURL += "/file/" + FileType.TR069_SCRIPT + "/" + scriptVersionFromDB + "/" + sessionData.getUnittype().getName();
        if (sessionData.getUnitId() != null)
          downloadURL += "/" + sessionData.getUnitId();
        downloadURL += "/" + file.getName();
        downloadURL = downloadURL.replaceAll(" ", "--");
      }
      Log.debug(DownloadLogic.class, "Download script/config URL found (" + downloadURL + "), may trigger a Download");
      sessionData.getUnit().toWriteQueue(SystemParameters.JOB_CURRENT_KEY, scriptVersionFromDB);
      sessionData.setDownload(new Download(downloadURL, file));
      return true;
    }
    return false;
  }

  public static boolean isSoftwareDownloadSetup(HTTPReqResData reqRes, Job job) {
    SessionData sessionData = reqRes.getSessionData();
    CPEParameters cpeParams = sessionData.getCpeParameters();
    String softwareVersionFromCPE = cpeParams.getValue(cpeParams.SOFTWARE_VERSION);

    String softwareVersionFromDB = null;
    String downloadURL = null;
    if (job == null) {
      OweraParameters oweraParams = sessionData.getOweraParameters();
      softwareVersionFromDB = oweraParams.getValue(SystemParameters.DESIRED_SOFTWARE_VERSION);
      if (oweraParams.getValue(SystemParameters.SOFTWARE_URL) != null)
        downloadURL = oweraParams.getValue(SystemParameters.SOFTWARE_URL);
    } else {
      Map<String, JobParameter> jobParams = job.getDefaultParameters();
      if (jobParams.get(SystemParameters.DESIRED_SOFTWARE_VERSION) != null)
        softwareVersionFromDB = jobParams.get(SystemParameters.DESIRED_SOFTWARE_VERSION).getParameter().getValue();
      else {
        Log.error(DownloadLogic.class, "No desired software version found in job " + job.getId() + " aborting the job");
        return false;
      }
      if (jobParams.get(SystemParameters.SOFTWARE_URL) != null)
        downloadURL = jobParams.get(SystemParameters.SOFTWARE_URL).getParameter().getValue();
    }
    if (downloadURL == null) {
      StringBuffer reqURL = reqRes.getReq().getRequestURL();
      int port = reqRes.getReq().getLocalPort();
      String contextPath = reqRes.getReq().getContextPath();
      int hostEndPos = reqURL.indexOf(contextPath);
      downloadURL = reqURL.substring(0, hostEndPos);
      if (downloadURL.lastIndexOf(":") <= 6)
        downloadURL += ":" + port;
      downloadURL += contextPath;
      downloadURL += "/file/" + FileType.SOFTWARE + "/" + softwareVersionFromDB + "/" + sessionData.getUnittype().getName();
      if (sessionData.getUnitId() != null)
        downloadURL += "/" + sessionData.getUnitId();
      downloadURL = downloadURL.replaceAll(" ", "--");
    }

    if (softwareVersionFromDB != null && !softwareVersionFromDB.trim().equals("") && !softwareVersionFromDB.equals(softwareVersionFromCPE)) {
      Log.debug(DownloadLogic.class, "Download software URL found (" + downloadURL + "), may trigger a Download");
      File file = sessionData.getUnittype().getFiles().getByVersionType(softwareVersionFromDB, FileType.SOFTWARE);
      if (file == null) {
        logger.error("File-type " + FileType.SOFTWARE + " and version " + softwareVersionFromDB + " does not exists - indicate wrong setup of version number");
        return false;
      }
      sessionData.setDownload(new Download(downloadURL, file));
      return true;
    } else if (job != null && softwareVersionFromDB != null && !softwareVersionFromDB.trim().equals("") && softwareVersionFromDB.equals(softwareVersionFromCPE)) {
      logger.warn("Software is already upgraded to " + softwareVersionFromCPE + " - will not issue an software job");
    }
    return false;
  }

}
