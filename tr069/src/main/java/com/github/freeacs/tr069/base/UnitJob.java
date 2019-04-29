package com.github.freeacs.tr069.base;

import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobFlag.JobServiceWindow;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitJobStatus;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class UnitJob {
  private final DBI dbi;
  private final SessionDataI sessionData;
  private Job job;

  /**
   * Server-side provisioning differs from TR-069/TFTP/HTTP in several ways 1. Triggered from server
   * side 2. Continuous/synchronous - not splitted in several sessions (execution/verification) 3.
   * Requires a job to trigger the provisioning 4. Not possible to change profile for a unit
   * (Additionally Telnet-provisioning (which is server-side) also has another speciality) 5.
   * Handles params quite differently, every param is paired with a parse-param
   *
   * <p>These differences have several implications: a. Job-current parameter is not needed (ref 2.)
   * b. Job-object is known at all times - don't need to read it from Job-current-parameter (ref a.,
   * 2.) c. Handles only Job-history/Disruptive params here, rest is handled in TelnetJobThread (ref
   * 5.)
   */
  private boolean serverSideJob;

  public UnitJob(SessionDataI sessionData, DBI dbi, Job job, boolean serverSideJob) {
    this.sessionData = sessionData;
    this.job = job;
    this.serverSideJob = serverSideJob;
    this.dbi = dbi;
  }

  private UnitParameter makeUnitParameter(String name, String value) {
    UnittypeParameter utp = sessionData.getUnittype().getUnittypeParameters().getByName(name);
    return new UnitParameter(utp, sessionData.getUnitId(), value, sessionData.getProfile());
  }

  /**
   * This method updates the sessiondata object with the job parameters. Specifically it updates:
   * oweraparameters: job-current fromDB : all job-parameters (except job-current/job-history)
   */
  private void updateSessionWithJobParams() {
    Map<String, JobParameter> jobParams = job.getDefaultParameters();
    sessionData.setJobParams(jobParams);
  }

  private void updateSessionWithJobCurrent() {
    ParameterValueStruct jobIdPvs =
        new ParameterValueStruct(SystemParameters.JOB_CURRENT, String.valueOf(job.getId()));
    sessionData.getAcsParameters().putPvs(SystemParameters.JOB_CURRENT, jobIdPvs);
  }

  /**
   * Computes the history parameter. This parameter will add the newest jobId to the front of the
   * parameter. Job id which refer to a no longer existing job will be removed from the
   * comma-separated list.
   */
  private UnitParameter makeHistoryParameter(Integer jobId) {
    Unittype unittype = sessionData.getUnittype();
    Unit unit = sessionData.getUnit();
    Map<String, UnitParameter> upMap = unit.getUnitParameters();
    UnittypeParameter jhUtp =
        unittype.getUnittypeParameters().getByName(SystemParameters.JOB_HISTORY);
    UnitParameter jobHistoryUp = upMap.get(jhUtp.getName());

    String jh1 = unit.getParameters().get(SystemParameters.JOB_HISTORY);
    long tms = System.currentTimeMillis();
    if (jh1 == null || "".equals(jh1.trim())) {
      return makeUnitParameter(SystemParameters.JOB_HISTORY, "," + jobId + ":0:" + tms + ",");
    }

    String jh2 = ","; // + jobId + ",";
    boolean found = false;
    for (String entry : jh1.split(",")) {
      if ("".equals(entry.trim())) {
        continue;
      }
      JobHistoryEntry jhEntry = new JobHistoryEntry(entry);
      Job entryJob = sessionData.getUnittype().getJobs().getById(Integer.valueOf(String.valueOf(jhEntry.getJobId())));
      if (entryJob != null) {
        if (Objects.equals(entryJob.getId(), jobId)) { // inc repeated-counter
          jh2 += jhEntry.incEntry(tms) + ",";
          found = true;
        } else {
          jh2 += entry + ",";
        }
      }
    }
    if (!found) {
      jh2 = "," + jobId + ":0:" + tms + jh2;
    }
    jobHistoryUp.getParameter().setValue(jh2);
    return jobHistoryUp;
  }

  /**
   * These steps are performed when starting a job: - write job-current parameter to DB (with
   * job.getId()) if asynchronous mode - write unit-job entry to DB - update session data with job
   * parameters - update session data with profile parameters - update session data with job current
   */
  public void start() {
    try {
      try {
        String unitId = sessionData.getUnitId();
        if (!serverSideJob) {
          UnitParameter jobUp = makeUnitParameter(SystemParameters.JOB_CURRENT, String.valueOf(job.getId()));
          List<UnitParameter> upList = new ArrayList<>();
          upList.add(jobUp);
          upList.forEach(sessionData.getUnit()::toWriteQueue);
        }
        DBIActions.startUnitJob(unitId, job.getId(), dbi);
        if (!serverSideJob) {
          updateSessionWithJobParams();
          updateSessionWithJobCurrent();
          log.debug("UnitJob status is updated to STARTED and job parameters / job profile are written to session.");
        } else {
          log.debug("UnitJob status is updated to STARTED");
        }
      } catch (SQLException sqle) {
        log.error("UnitJob update failed", sqle);
        throw sqle;
      }
    } catch (Throwable t) {
      log.error("An error ocurred in start()", t);
    }
  }

  /**
   * These steps are performed when stopping a job: If ok - write profile-change to DB - write
   * unit-job entry to DB (ok) - write job parameters to DB - write job-current to DB (as "") -
   * write job-history to DB (remove old jobs) - read unit again and update session data (must clear
   * fromDB to do this) Else - write unit-job entry to DB (failed) - write job-current to DB (as "")
   */
  public void stop(String unitJobStatus, boolean isDiscoveryMode) {
    try {
      JobInfo jobInfo = new JobInfo(unitJobStatus).invoke();
      if (jobInfo.isIrrelevant()) {
        return;
      }
      unitJobStatus = jobInfo.getUnitJobStatus();
      Integer jobId = jobInfo.getJobId();
      try {
        List<UnitParameter> upList = getUnitParameters(unitJobStatus);
        DBIActions.stopUnitJob(
            sessionData.getUnitId(),
            jobId,
            unitJobStatus,
            dbi);
        sessionData.getPIIDecision().setCurrentJobStatus(unitJobStatus);
        // Write directly to database, no queuing, since the all data are flushed in next step (most likely)
        ACSUnit acsUnit = dbi.getACSUnit();
        acsUnit.addOrChangeUnitParameters(upList, sessionData.getProfile());
        if (!serverSideJob) {
          sessionData.setFromDB(null);
          sessionData.setAcsParameters(null);
          sessionData.setJobParams(null);
          log.debug("Unit-information will be reloaded to reflect changes in profile/unit parameters");
          DBIActions.updateParametersFromDB((SessionData) sessionData, isDiscoveryMode, dbi);
        }
      } catch (SQLException sqle) {
        log.error("UnitJob update failed", sqle);
        throw sqle;
      }
    } catch (Throwable t) {
      log.error("An error ocurred in stop()", t);
    }
  }

  private List<UnitParameter> getUnitParameters(String unitJobStatus) {
    List<UnitParameter> upList = new ArrayList<>();
    if (!serverSideJob) {
      upList.add(makeUnitParameter(SystemParameters.JOB_CURRENT, ""));
      upList.add(makeUnitParameter(SystemParameters.JOB_CURRENT_KEY, ""));
    }
    if (unitJobStatus.equals(UnitJobStatus.COMPLETED_OK)) {
      upList.add(makeHistoryParameter(job.getId()));
      if (job.getFlags().getServiceWindow() == JobServiceWindow.DISRUPTIVE) {
        upList.add(makeUnitParameter(SystemParameters.JOB_DISRUPTIVE, "1"));
      }
      if (serverSideJob) {
        log.debug("UnitJob is COMPLETED, job history is updated");
      } else {
        Map<String, JobParameter> jobParams = job.getDefaultParameters();
        sessionData.setJobParams(jobParams);
        for (JobParameter jp : sessionData.getJobParams().values()) {
          String jpName = jp.getParameter().getUnittypeParameter().getName();
          if (SystemParameters.RESTART.equals(jpName)
              || SystemParameters.RESET.equals(jpName)
              || jp.getParameter().getUnittypeParameter().getFlag().isReadOnly()) {
            continue;
          }
          UnitParameter up =
              new UnitParameter(
                  jp.getParameter(), sessionData.getUnitId(), sessionData.getProfile());
          upList.add(up);
        }
        log.debug("UnitJob is COMPLETED, job history, profile/unit parameters are updated");
      }
    }
    return upList;
  }

  private class JobInfo {
    private boolean irrelevant;
    private String unitJobStatus;
    private Integer jobId;

    JobInfo(String unitJobStatus) {
      this.unitJobStatus = unitJobStatus;
    }

    boolean isIrrelevant() {
      return irrelevant;
    }

    String getUnitJobStatus() {
      return unitJobStatus;
    }

    Integer getJobId() {
      return jobId;
    }

    JobInfo invoke() {
      if (serverSideJob) {
        jobId = sessionData.getJob().getId();
      } else {
        if (sessionData == null || sessionData.getAcsParameters() == null) {
          irrelevant = true;
          return this;
        }
        String jobIdStr = sessionData.getAcsParameters().getValue(SystemParameters.JOB_CURRENT);
        if (jobIdStr == null) {
          irrelevant = true;
          return this;
        }
        try {
          jobId = Integer.parseInt(jobIdStr);
        } catch (NumberFormatException nfe) {
          irrelevant = true;
          return this;
        }
        log.debug("Current jobId param is "
                + jobId
                + ", will stop unit job with unit job status set to "
                + unitJobStatus);
        job = sessionData.getUnittype().getJobs().getById(Integer.valueOf(jobIdStr));
        if (job == null && !unitJobStatus.equals(UnitJobStatus.CONFIRMED_FAILED)) {
          log.warn("Couldn't find job with jobId "
                  + jobId
                  + ", unit job status changed to "
                  + UnitJobStatus.CONFIRMED_FAILED);
          unitJobStatus = UnitJobStatus.CONFIRMED_FAILED;
        }
      }
      irrelevant = false;
      return this;
    }
  }
}
