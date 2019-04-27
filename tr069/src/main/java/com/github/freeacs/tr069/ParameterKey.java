package com.github.freeacs.tr069;

import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

@Data
@Slf4j
public class ParameterKey {
  private String cpeKey;
  private String serverKey;

  public void setServerKey(HTTPRequestResponseData reqRes) throws NoSuchAlgorithmException {
    this.serverKey = calculateParameterKey(reqRes);
  }

  public boolean isEqual() {
    return cpeKey != null && cpeKey.equals(serverKey);
  }

  private static String calculateParameterKey(HTTPRequestResponseData reqRes)
      throws NoSuchAlgorithmException {
    SessionData sessionData = reqRes.getSessionData();
    UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
    Map<String, ParameterValueStruct> fromDB = sessionData.getFromDB();
    String jobId = sessionData.getAcsParameters().getValue(SystemParameters.JOB_CURRENT);
    if (jobId != null && !"".equals(jobId.trim())) {
      Job job = sessionData.getUnittype().getJobs().getById(Integer.valueOf(jobId));
      if (job != null) {
        log.debug("Current job has jobId: "
                + job.getId()
                + " -> verification stage, must retrieve job parameters (with RW-flag) to calculate parameterkey correctly");
        Map<String, JobParameter> jobParams = job.getDefaultParameters();
        for (Entry<String, JobParameter> jobParamEntry : jobParams.entrySet()) {
          if (jobParamEntry
              .getValue()
              .getParameter()
              .getUnittypeParameter()
              .getFlag()
              .isReadWrite()) {
            ParameterValueStruct jobParamPvs =
                new ParameterValueStruct(
                    jobParamEntry.getKey(), jobParamEntry.getValue().getParameter().getValue());
            fromDB.put(jobParamEntry.getKey(), jobParamPvs);
          }
        }
      }
    }
    StringBuilder valuesBuilder = new StringBuilder();
    for (Entry<String, ParameterValueStruct> entry : fromDB.entrySet()) {
      String utpName = entry.getKey();
      //			ParameterInfoStruct pis = infoMap.get(utpName);
      UnittypeParameter utp = utps.getByName(utpName);
      //			if (pis != null && pis.isWritable()) {
      if (utp != null && utp.getFlag().isReadWrite()) {
        if (utpName.contains("PeriodicInformInterval")
            || "ExtraCPEParam".equals(entry.getValue().getValue())) {
          continue;
        }
        valuesBuilder.append(entry.getValue().getValue());
      }
    }
    String values = valuesBuilder.toString();
    if ("".equals(values)) {
      log.debug("No device parameter values found, ACS parameterkey = \"No data in DB\"");
      return "No data in DB";
    } else {
      MessageDigest md = MessageDigest.getInstance("SHA");
      byte[] hash = md.digest(values.getBytes());
      StringBuilder parameterKey = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(b + 128);
        if (hex.length() == 1) {
          hex = "0" + hex;
        }
        parameterKey.append(hex);
      }
      String pk = parameterKey.substring(0, 32);
      log.debug("The values to be hashed: " + values + " -> ACS parameterkey = " + pk);
      return pk;
    }
  }
}
