package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.core.util.FractionStopRuleCounter;
import com.github.freeacs.core.util.UnitJobResult;
import com.github.freeacs.core.util.UnitResultMap;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.Job.StopRule;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRuleEnforcer extends DBIOwner {
  private final Properties properties;

  public class JobControl {
    private List<FractionStopRuleCounter> fractionStopRuleCounters = new ArrayList<>();

    private Job job;

    public JobControl(Job job) {
      this.job = job;
      if (job.getStopRules() != null) {
        for (StopRule rule : job.getStopRules()) {
          if (rule.getNumberMax() != null) {
            fractionStopRuleCounters.add(new FractionStopRuleCounter(rule));
          }
          logger.info("JobRuleEnforcer: \tRule found: " + rule);
        }
      }
    }

    /**
     * 1. Loop through all fraction-rules, find rules which matches 2. For each new fraction rule
     * that do not match any old fraction rule: 2.1. Search all old fraction rules and find all of
     * same type (a, c, u) 2.2. Choose largest old fraction rule which matches 2.3. Copy data from
     * old fraction rule to new one, cut excess data if needed 2.4. Log 3. Delete all old fraction
     * rules 4. Set all new fraction rules 5. Loop through all non-fraction rules, find rules which
     * do not match 5.1. Log 6. Set job.setRules() in JobControls job-copy.
     */
    public void updateRules(Job newJob) {
      List<FractionStopRuleCounter> tmpList = new ArrayList<>();
      for (StopRule newRule : newJob.getStopRules()) {
        if (newRule.getNumberMax() == null) {
          continue;
        } // only focus on Fraction Stop Rules
        boolean match = false;
        for (FractionStopRuleCounter fsrc : fractionStopRuleCounters) {
          if (newRule.toString().equals(fsrc.getRule().toString())) {
            match = true;
            logger.info("JobRuleEnforcer: \tKept fractional rule: " + fsrc.getRule());
            tmpList.add(fsrc);
            break;
          }
        }
        if (!match) {
          FractionStopRuleCounter newFsrc = new FractionStopRuleCounter(newRule);
          FractionStopRuleCounter preferred = null;
          for (FractionStopRuleCounter fsrc : fractionStopRuleCounters) {
            if (fsrc.getRule().getRuleType() == newRule.getRuleType()
                && (preferred == null
                    || fsrc.getRule().getNumberMax() > preferred.getRule().getNumberMax())) {
              preferred = fsrc;
            }
          }
          if (preferred != null) {
            UnitResultMap<String, UnitJobResult> unitJobResults = preferred.getUnitJobResults();
            UnitResultMap<String, UnitJobResult> newUnitJobResults = newFsrc.getUnitJobResults();
            String[] unitIds = new String[unitJobResults.size()];
            unitJobResults.keySet().toArray(unitIds);
            for (String unitId : unitIds) {
              UnitJobResult ujr = unitJobResults.get(unitId);
              newUnitJobResults.put(unitId, unitJobResults.get(unitId));
              newFsrc.addResult(ujr);
            }
            logger.info(
                "JobRuleEnforcer: \tAdded fractional rule: "
                    + newRule
                    + " (copied old data from "
                    + preferred.getRule()
                    + ")");
          } else {
            logger.info("JobRuleEnforcer: \tAdded fractional rule: " + newRule);
          }
          tmpList.add(newFsrc);
        }
      }
      for (FractionStopRuleCounter fsrc : fractionStopRuleCounters) {
        boolean match = false;
        for (FractionStopRuleCounter newFsrc : tmpList) {
          if (fsrc.getRule().toString().equals(newFsrc.getRule().toString())) {
            match = true;
            break;
          }
        }
        if (!match) {
          logger.info("JobRuleEnforcer: \tDeleted fractional rule: " + fsrc.getRule());
        }
      }
      fractionStopRuleCounters = tmpList;
      for (StopRule newRule : newJob.getStopRules()) {
        if (newRule.getNumberMax() != null) {
          continue;
        } // only focus on Absolute Rules
        boolean match = false;
        for (StopRule rule : job.getStopRules()) {
          if (newRule.toString().equals(rule.toString())) {
            match = true;
            break;
          }
        }
        if (match) {
          logger.info("JobRuleEnforcer: \tKept absolute rule: " + newRule);
        } else {
          logger.info("JobRuleEnforcer: \tAdded absolute rule: " + newRule);
        }
      }
      for (StopRule rule : job.getStopRules()) {
        if (rule.getNumberMax() != null) {
          continue;
        } // only focus on Absolute Rules
        boolean match = false;
        for (StopRule newRule : newJob.getStopRules()) {
          if (newRule.toString().equals(rule.toString())) {
            match = true;
            break;
          }
        }
        if (!match) {
          logger.info("JobRuleEnforcer: \tDeleted absolute rule: " + rule);
        }
      }
      job = newJob;
      //			job.setStopRules(newJob.getStopRulesSerialized());
    }

    /**
     * Private void info(String msg) { Log.log(LightProcessingDaemon.loggerId,
     * com.owera.common.log.Log.INFO_INT, msg, null); }
     */
    public List<FractionStopRuleCounter> getFractionStopRuleCounters() {
      return fractionStopRuleCounters;
    }

    public Job getJob() {
      return job;
    }

    public FractionStopRuleCounter fractionRuleMatch() {
      for (FractionStopRuleCounter fr : fractionStopRuleCounters) {
        if (fr.ruleMatch()) {
          return fr;
        }
      }
      return null;
    }
  }

  private static Logger logger = LoggerFactory.getLogger(JobRuleEnforcer.class);
  private ACS acs;
  private UnitJobs unitJobs;
  private Map<Integer, JobControl> jobControlMap = new HashMap<>();

  public JobRuleEnforcer(String taskName, DBI dbi, Properties properties) {
    super(taskName, dbi);
    this.properties = properties;
  }

  @Override
  public void runImpl() throws Exception {
    populate();
    process();
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  private void populate() {
    acs = getLatestACS();
    Unittype[] unittypeArr = acs.getUnittypes().getUnittypes();
    for (Unittype ut : unittypeArr) {
      Job[] jobList = ut.getJobs().getJobs();
      for (Job j : jobList) {
        if (jobControlMap.get(j.getId()) == null) {
          if (j.getStatus() == JobStatus.STARTED || j.getStatus() == JobStatus.PAUSED) {
            logger.info("JobRuleEnforcer: Found new job: " + j.getName() + " (" + j.getId() + ")");
            jobControlMap.put(j.getId(), new JobControl(j));
          }
        } else {
          JobControl jc = jobControlMap.get(j.getId());
          Job jcJob = jc.getJob();
          if ((jcJob.getStopRulesSerialized() != null
                  && j.getStopRulesSerialized() != null
                  && !jcJob.getStopRulesSerialized().equals(j.getStopRulesSerialized()))
              || (jcJob.getStopRulesSerialized() != null && j.getStopRulesSerialized() == null)
              || (jcJob.getStopRulesSerialized() == null && j.getStopRulesSerialized() != null)) {
            logger.info(
                "JobRuleEnforcer: Detected stop rules change in job "
                    + j.getName()
                    + " ("
                    + j.getId()
                    + ").");
            jc.updateRules(j);
          }
          if (jcJob.getUnconfirmedTimeout() != j.getUnconfirmedTimeout()) {
            logger.info(
                "JobRuleEnforcer: Detected unconfirmed timeout change in job "
                    + j.getName()
                    + " ("
                    + j.getId()
                    + "), new timeout is "
                    + j.getUnconfirmedTimeout());
            jcJob.setUnconfirmedTimeout(j.getUnconfirmedTimeout());
          }
          if (!jcJob.getStatus().equals(j.getStatus())) {
            logger.info(
                "JobRuleEnforcer: Detected status change in job "
                    + j.getName()
                    + " ("
                    + j.getId()
                    + "), new status is "
                    + j.getStatus());
            jcJob.setStatus(j.getStatus());
          }
        }
      }
      removeDeletedJobs(jobList, ut);
    }
    unitJobs = new UnitJobs(getDataSource());
  }

  private void process() throws Exception {
    for (JobControl jc : jobControlMap.values()) {
      Job job = jc.getJob();
      if (properties.isStaging()) {
        int countCompleted = unitJobs.markAsCompleted(job);
        if (countCompleted > 0) {
          logger.info(
              "JobRuleEnforcer: ["
                  + job.getName()
                  + "] "
                  + countCompleted
                  + " unitjobs changed status to "
                  + UnitJobStatus.COMPLETED_OK
                  + " after one hour in "
                  + UnitJobStatus.UNCONFIRMED_FAILED
                  + " state (the unit has been staged)");
        }
      }
      int countUnconfirmed = unitJobs.markAsUnconfirmed(job);
      if (countUnconfirmed > 0) {
        logger.info(
            "JobRuleEnforcer: ["
                + job.getName()
                + "] "
                + countUnconfirmed
                + " unitjobs changed status to "
                + UnitJobStatus.UNCONFIRMED_FAILED);
      }
      List<UnitJob> ujList = unitJobs.readAllUnprocessed(job);
      if (!ujList.isEmpty()) {
        processUnprocessed(ujList, jc);
      }
      if (job.getStatus() != JobStatus.PAUSED) {
        ruleMatching(jc);
      }
    }
  }

  private void processUnprocessed(List<UnitJob> ujList, JobControl jc) throws SQLException {
    Job job = jc.getJob();
    for (UnitJob uj : ujList) {
      if (uj.getStatus().equals(UnitJobStatus.STOPPED)) {
        unitJobs.markAsProcessed(uj);
        logger.info(
            "JobRuleEnforcer: ["
                + job.getName()
                + "] "
                + "Unitjob for "
                + uj.getUnitId()
                + " marked as processed (was STOPPED - hence not counted)");
        continue; // do not count stopped unit jobs
      }
      for (FractionStopRuleCounter fr : jc.getFractionStopRuleCounters()) {
        fr.addResult(uj);
      }
      unitJobs.markAsProcessed(uj);
      logger.info(
          "JobRuleEnforcer: ["
              + job.getName()
              + "] "
              + "Unitjob for "
              + uj.getUnitId()
              + " marked as processed (was "
              + uj.getStatus()
              + " - hence counted)");
    }
    updateJob(job, unitJobs);
    logger.info(
        "JobRuleEnforcer: ["
            + job.getName()
            + "] "
            + "Have processed and marked "
            + ujList.size()
            + " unit jobs");
  }

  private void removeDeletedJobs(Job[] jobList, Unittype unittype) {
    List<Integer> deletedJobs = new ArrayList<>();
    for (JobControl jc : jobControlMap.values()) {
      boolean match = false;
      Job jcJob = jc.getJob();
      for (Job j : jobList) {
        if (j.getId() == jcJob.getId().intValue()) {
          match = true;
          break;
        }
      }
      if (!match && jc.getJob().getGroup().getUnittype().getId() == unittype.getId()) {
        deletedJobs.add(jc.getJob().getId());
      }
    }
    for (Integer jobId : deletedJobs) {
      String jobName = jobControlMap.get(jobId).getJob().getName();
      logger.info("JobRuleEnforcer: [" + jobName + "] " + "Remove from memory structure");
      jobControlMap.remove(jobId);
    }
  }

  private void ruleMatching(JobControl jc) throws SQLException {
    Job job = jc.getJob();
    int unitJobsPerformed =
        job.getCompletedNoFailures()
            + job.getConfirmedFailed()
            + job.getUnconfirmedFailed()
            + job.getCompletedHadFailures();
    FractionStopRuleCounter fr = jc.fractionRuleMatch();
    boolean ruleMatch = false;
    if (job.getTimeoutTms() < System.currentTimeMillis()) {
      String msg = "The job timeout rule t" + job.getTimeoutTms() + " (";
      msg += new Date(job.getTimeoutTms()) + ") matched!. Job will get STOPPED status.";
      logger.info("JobRuleEnforcer: [" + job.getName() + "] " + msg);
      ruleMatch = true;
    } else if (fr != null) {
      logger.info(
          "JobRuleEnforcer: ["
              + job.getName()
              + "] "
              + "The stop rule "
              + fr
              + " matched! Job will get STOPPED status.");
      ruleMatch = true;
    } else if (unitJobsPerformed >= job.getMaxCount()) {
      logger.info(
          "JobRuleEnforcer: ["
              + job.getName()
              + "] "
              + "The stop rule n"
              + job.getMaxCount()
              + " matched! Job will get STOPPED status.");
      ruleMatch = true;
    } else if (job.getConfirmedFailed() >= job.getMaxFailureConfirmed()) {
      logger.info(
          "JobRuleEnforcer: ["
              + job.getName()
              + "] "
              + "The stop rule c"
              + job.getMaxFailureConfirmed()
              + " matched! Job will get STOPPED status.");
      ruleMatch = true;
    } else if (job.getUnconfirmedFailed() >= job.getMaxFailureUnconfirmed()) {
      logger.info(
          "JobRuleEnforcer: ["
              + job.getName()
              + "] "
              + "The stop rule u"
              + job.getMaxFailureUnconfirmed()
              + " matched! Job will get STOPPED status.");
      ruleMatch = true;
    } else if (job.getConfirmedFailed() + job.getUnconfirmedFailed() >= job.getMaxFailureAny()) {
      logger.info(
          "JobRuleEnforcer: ["
              + job.getName()
              + "] "
              + "The stop rule a"
              + job.getMaxFailureAny()
              + " matched! Job will get STOPPED status.");
      ruleMatch = true;
    }
    if (ruleMatch) {
      job.setStatus(JobStatus.PAUSED);
      job.getGroup().getUnittype().getJobs().changeStatus(job, acs);
      // Must also change the job object found in the ACS object -
      Unittype acsUnittype = acs.getUnittype(job.getUnittype().getId());
      Job acsJob = acsUnittype.getJobs().getById(job.getId());
      acsJob.setStatus(job.getStatus());
    }
  }

  private void updateJob(Job job, UnitJobs unitJobs) throws SQLException {
    String publishMsg = "";
    int completedThisRound = unitJobs.countAndDeleteCompletedNoFailure(job);
    if (completedThisRound != 0) {
      publishMsg += "cnf=" + (job.getCompletedNoFailures() + completedThisRound) + ",";
    }
    int stoppedThisRound = unitJobs.countAndDeleteStoppedNoFailure(job);
    int confirmedFailedButCompleted = unitJobs.count(job, "confirmed", true);
    int unconfirmedFailedButCompleted = unitJobs.count(job, "unconfirmed", true);
    int completedHadFailuresThisRound =
        (confirmedFailedButCompleted + unconfirmedFailedButCompleted)
            - job.getCompletedHadFailures();
    if (completedHadFailuresThisRound != 0) {
      publishMsg += "chf=" + (confirmedFailedButCompleted + unconfirmedFailedButCompleted) + ",";
    }
    int confirmedFailed = unitJobs.count(job, "confirmed", false);
    int confirmedFailedThisRound = confirmedFailed - job.getConfirmedFailed();
    if (confirmedFailedThisRound != 0) {
      publishMsg += "cf=" + confirmedFailed + ",";
    }
    int unconfirmedFailed = unitJobs.count(job, "unconfirmed", false);
    int unconfirmedFailedThisRound = unconfirmedFailed - job.getUnconfirmedFailed();
    if (unconfirmedFailedThisRound != 0) {
      publishMsg += "uf=" + unconfirmedFailed + ",";
    }
    if (!publishMsg.isEmpty()) {
      publishMsg =
          job.getGroup().getUnittype().getId()
              + ","
              + publishMsg.substring(0, publishMsg.length() - 1);
    }

    String logMsg =
        "Summary: [This round: "
            + completedThisRound
            + " OK, "
            + completedHadFailuresThisRound
            + " OKHF, ";
    logMsg += stoppedThisRound + " STOPPED, ";
    logMsg += confirmedFailedThisRound + " CF, " + unconfirmedFailedThisRound + " UCF] ";
    job.setConfirmedFailed(confirmedFailed);
    job.setUnconfirmedFailed(unconfirmedFailed);
    job.setCompletedNoFailures(job.getCompletedNoFailures() + completedThisRound);
    job.setCompletedHadFailures(confirmedFailedButCompleted + unconfirmedFailedButCompleted);
    job.getGroup().getUnittype().getJobs().changeFromCore(job, publishMsg, acs);
    logMsg +=
        "[Total: "
            + job.getCompletedNoFailures()
            + " OK, "
            + job.getCompletedHadFailures()
            + " OKHF, ";
    logMsg += job.getConfirmedFailed() + " CF, " + job.getUnconfirmedFailed() + " UCF]";
    logger.info("JobRuleEnforcer: [" + job.getName() + "] " + logMsg);
  }
}
