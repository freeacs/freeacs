package com.github.freeacs.tr069;


import com.github.freeacs.base.DownloadLogic;
import com.github.freeacs.base.ACSParameters;
import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.dbi.util.SystemParameters.TR069ScriptType;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;

public class DownloadLogicTR069 {

  private static Logger logger = LoggerFactory.getLogger(DownloadLogicTR069.class);

  public static boolean isScriptDownloadSetup(HTTPReqResData reqRes, Job job) {
    SessionData sessionData = reqRes.getSessionData();
    ACSParameters oweraParams = sessionData.getAcsParameters();
    CPEParameters cpeParams = sessionData.getCpeParameters();
    String scriptVersionFromDB = null;
    String scriptName = null;
    if (job != null) { // retrieve desired-script-version from Job-parameters
      Map<String, JobParameter> jobParams = sessionData.getJobParams();
      for (Entry<String, JobParameter> entry : jobParams.entrySet()) {
        if (SystemParameters.isTR069ScriptVersionParameter(entry.getKey())) {
          scriptName = SystemParameters.getTR069ScriptName(entry.getKey());
          scriptVersionFromDB = entry.getValue().getParameter().getValue();
          break;
        }
      }
    } else {
      Map<String, ParameterValueStruct> opMap = oweraParams.getAcsParams();
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
      sessionData.setDownload(new SessionData.Download(downloadURL, file));
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
      ACSParameters oweraParams = sessionData.getAcsParameters();
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
      sessionData.setDownload(new SessionData.Download(downloadURL, file));
      return true;
    } else if (job != null && softwareVersionFromDB != null && !softwareVersionFromDB.trim().equals("") && softwareVersionFromDB.equals(softwareVersionFromCPE)) {
      logger.warn("Software is already upgraded to " + softwareVersionFromCPE + " - will not issue an software job");
    }
    return false;
  }

}
