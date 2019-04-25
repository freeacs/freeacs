package com.github.freeacs.tr069.base;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobFlag.JobServiceWindow;
import com.github.freeacs.dbi.JobFlag.JobType;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.JobStatus;
import com.github.freeacs.dbi.Jobs;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitJobStatus;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemParameters;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JobLogic should do two things, first and foremost: 1) Find a new job 2) Check if a job has
 * executed OK
 *
 * @author morten
 */
@Slf4j
public class JobLogic {
  public static boolean checkJobOK(SessionDataI sessionData, DBI dbi, boolean isDiscoveryMode) {
    try {
      String jobId = sessionData.getAcsParameters().getValue(SystemParameters.JOB_CURRENT);
      if (jobId != null && !jobId.trim().isEmpty()) {
        log.debug("Verification stage entered for job " + jobId);
        Job job = sessionData.getUnittype().getJobs().getById(Integer.valueOf(jobId));
        if (job == null) {
          log.warn("Current job " + jobId + " does no longer exist, cannot be verified");
          return false;
        }
        UnitJob uj = new UnitJob(sessionData, dbi, job, false);
        if (!JobStatus.STARTED.equals(job.getStatus())) {
          log.warn("Current job is not STARTED, UnitJob must be STOPPED");
          uj.stop(UnitJobStatus.STOPPED, isDiscoveryMode);
          return false;
        } else {
          JobType type = job.getFlags().getType();
          if (type == JobType.CONFIG) {
            boolean parameterKeyEquality = sessionData.lastProvisioningOK();
            if (parameterKeyEquality) {
              uj.stop(UnitJobStatus.COMPLETED_OK, isDiscoveryMode);
              return true;
            } else {
              log.warn("The parameterkeys from CPE and ACS does not match, UnitJob FAILED");
              uj.stop(UnitJobStatus.CONFIRMED_FAILED, isDiscoveryMode);
              return false;
            }
          } else if (type == JobType.RESTART
              || type == JobType.RESET
              || type == JobType.KICK
              || type == JobType.SHELL) {
            uj.stop(UnitJobStatus.COMPLETED_OK, isDiscoveryMode);
            return true;
          } else if (type == JobType.SOFTWARE) {
            Map<String, JobParameter> jpMap = job.getDefaultParameters();
            JobParameter dsw = jpMap.get(SystemParameters.DESIRED_SOFTWARE_VERSION);
            String sw = sessionData.getSoftwareVersion();
            if (dsw != null && sw != null && dsw.getParameter().getValue().equals(sw)) {
              uj.stop(UnitJobStatus.COMPLETED_OK, isDiscoveryMode);
              return true;
            } else {
              log.warn("Software version on CPE and ACS (desired) does not match, UnitJob FAILED");
              uj.stop(UnitJobStatus.CONFIRMED_FAILED, isDiscoveryMode);
              return false;
            }
          } else if (type == JobType.TR069_SCRIPT) {
            boolean commandKeyEquality = sessionData.lastProvisioningOK();
            if (commandKeyEquality) {
              uj.stop(UnitJobStatus.COMPLETED_OK, isDiscoveryMode);
              return true;
            } else {
              log.warn("Script/Config version on CPE and ACS (desired) does not match, UnitJob FAILED");
              uj.stop(UnitJobStatus.CONFIRMED_FAILED, isDiscoveryMode);
              return false;
            }
          }
        }
      }
      return true;
    } catch (Throwable t) {
      log.error("Job verification check failed", t);
      return false;
    }
  }

  public static UnitJob checkNewJob(SessionDataI sessionData, DBI dbi, int downloadLimit) {
    if (sessionData.getUnit().getProvisioningMode() == ProvisioningMode.REGULAR) {
      Job job = getJob(sessionData, downloadLimit);
      if (job != null) {
        UnitJob uj;
        if (job.getFlags().getType() == JobType.SHELL) {
          uj = new UnitJob(sessionData, dbi, job, true);
        } else {
          uj = new UnitJob(sessionData, dbi, job, false);
        }
        uj.start();
        sessionData.setJob(job);
        sessionData.getPIIDecision().setCurrentJob(job);
        return uj;
      }
    }
    return null;
  }

  /**
   * The method filters through all kinds of factors to find which Job (if any) should be the next
   * to run.
   */
  private static Job getJob(SessionDataI sessionData, int downloadLimit) {
    Unit unit = sessionData.getUnit();
    Jobs jobs = sessionData.getUnittype().getJobs();
    String message = "";
    Job[] allJobs = jobs.getJobs();
    sessionData.getPIIDecision().setAllJobs(allJobs);
    Job chosenJob = null;
    try {
      Map<Integer, Job> possibleJobs = filterOnStatusAndType(allJobs);
      message += "Status/Type:" + possibleJobs.size() + ", ";
      if (possibleJobs.isEmpty()) {
        return null;
      }

      possibleJobs = filterOnGroupMatch(possibleJobs, unit);
      message += "GroupMatch:" + possibleJobs.size() + ", ";
      if (possibleJobs.isEmpty()) {
        return null;
      }

      Map<Integer, JobHistoryEntry> jobHistory = getJobHistory(unit, jobs);

      possibleJobs = filterOnJobDependency(possibleJobs, jobHistory);
      message += "Dependencies:" + possibleJobs.size() + ", ";
      if (possibleJobs.isEmpty()) {
        return null;
      }

      possibleJobs = filterOnJobHistory(possibleJobs, jobHistory);
      message += "JobHistory:" + possibleJobs.size() + ", ";
      if (possibleJobs.isEmpty()) {
        return null;
      }

      possibleJobs = filterOnDownloadAllowed(possibleJobs, downloadLimit);
      message += "DownloadAllowed:" + possibleJobs.size() + ", ";
      if (possibleJobs.isEmpty()) {
        return null;
      }

      possibleJobs = filterOnRunTime(sessionData, possibleJobs, jobHistory);
      message += "RunTms:" + possibleJobs.size() + ", ";
      if (possibleJobs.isEmpty()) {
        return null;
      }

      Job j = findJobWithHighestPriority(possibleJobs);
      if (j == null) {
        message += "Priority:0";
        return null;
      } else {
        chosenJob = j;
        message += "Priority:1";
        return chosenJob;
      }
    } catch (Throwable t) {
      log.error("An error ocurred in getJob()", t);
      return null;
    } finally {
      if (chosenJob != null) {
        log.debug("Found job "
                + chosenJob.getId()
                + " of type "
                + chosenJob.getFlags().getType()
                + "(filter: "
                + message
                + ")");
      } else {
        log.info("No job found (filter: " + message + ")");
      }
    }
  }

  private static Map<Integer, Job> filterOnJobDependency(
      Map<Integer, Job> possibleJobs, Map<Integer, JobHistoryEntry> jobHistory) {
    Iterator<Entry<Integer, Job>> i = possibleJobs.entrySet().iterator();
    while (i.hasNext()) {
      Job j = i.next().getValue();
      // The job depends on another job, but the other job has not yet run (according to history)
      if (j.getDependency() != null && jobHistory.get(j.getDependency().getId()) == null) {
        i.remove();
      }
    }
    return possibleJobs;
  }

  /**
   * This filter removes all jobs that are not scheduled to run now (that is: within 31 seconds).
   * There are two reasons why jobs cannot run now:
   *
   * <p>1. If a job is repeatable, it should run at fixed time intervals. Thus it may be that a
   * repeatable job cannot run before more than 31 seconds.
   *
   * <p>2. If a job is not repeatable, it can start at any moment. But, it must still obey the
   * service window.
   *
   * <p>For all jobs that is not scheduled to run right away, we calculate the next periodic inform
   * interval and place it on the job object (and use that in PIIDecision).
   *
   * <p>The only jobs returned from this filter is jobs that can run right away!
   */
  private static Map<Integer, Job> filterOnRunTime(
      SessionDataI sessionData,
      Map<Integer, Job> possibleJobs,
      Map<Integer, JobHistoryEntry> jobHistory) {
    /*
     * Discussion about inDisruptiveJobChain: This flag is set on the unit-level, if a job has been
     * run under a disruptive service window. If any other job immediately follows this job it is
     * considered likely that the jobs were dependent upon the first or previous job. Thus all jobs
     * following should be allowed to run and the service window calculation is ignored. However,
     * this assumption of dependency between the jobs might not always be true, especially if there
     * are many jobs defined in the system, and if some of them are repeatable (at a short interval).
     * For now, I have chosen to ignore this problem. The solution to the problem would be to
     * specify (in the JOB_DISRUPTIVE parameter) which job triggered this setting. The problematic thing
     * is to trace for the jobs coming after, if they are dependent on that job or not. Yes, we
     * have the Job-dependency field which states such things explicitly. But there is also the case
     * where change of a parameter (ex: software-version) will make the unit hit a new group, which in
     * turn triggers a new job execution. This relationship is not found easily, and is the prime
     * reason why I decide to ignore the problem for now. Worst case scenario is that a job is run
     * when it shouldn't. The job will most likely be a repeatable job, which hopefully is of rather
     * harmless character.
     *
     * Morten Simonsen, Nov 2011
     */
    String disruptiveJob = sessionData.getAcsParameters().getValue(SystemParameters.JOB_DISRUPTIVE);
    boolean inDisruptiveJobChain = "1".equals(disruptiveJob);
    Iterator<Entry<Integer, Job>> i = possibleJobs.entrySet().iterator();
    long lowestNRT = Long.MAX_VALUE;
    Job nextRepeatableJob = null;
    while (i.hasNext()) {
      Entry<Integer, Job> entry = i.next();
      Job job = entry.getValue();
      boolean repeatable = job.getRepeatCount() != null && job.getRepeatCount() > 0;
      boolean isDisruptiveSw =
          entry.getValue().getFlags().getServiceWindow() == JobServiceWindow.DISRUPTIVE;
      ServiceWindow serviceWindow = new ServiceWindow(sessionData, isDisruptiveSw);
      if (repeatable) {
        JobHistoryEntry jhEntry = jobHistory.get(job.getId());
        Long lastRunTms = (jhEntry == null) ? null : jhEntry.getLastRunTms();
        long NRT = serviceWindow.calculateNextRepeatableTms(lastRunTms, job.getRepeatInterval());
        long nextPII = convertToPII(serviceWindow, NRT);
        job.setNextPII(
            nextPII); // important for PIIDecision, must be removed in PIIDecision (must not leak
        // over to another thread/session)
        if (nextPII > PIIDecision.MINIMUM_PII) {
          i.remove();
          log.debug("FilterOnRunTime removed job "
                  + job.getId()
                  + " since it was scheduled to run in "
                  + nextPII
                  + " seconds");
        } else if (NRT < lowestNRT) {
          lowestNRT = NRT;
          nextRepeatableJob = job;
        }
      } else if (!inDisruptiveJobChain && !serviceWindow.isWithin()) {
        i.remove();
        long nextPII = serviceWindow.calculateStdPII();
        log.debug("FilterOnRunTime removed job "
                + entry.getValue().getId()
                + " since it was outside SW and scheduled to run in "
                + nextPII
                + " seconds");
        job.setNextPII(
            nextPII); // important for PIIDecision, must be removed in PIIDecision (must not leak
        // over to another thread/session)
      } else {
        log.debug("FilterOnRunTime kept job "
                + entry.getValue().getId()
                + " since it was inside SW or in a disruptive job chain, and not repeatable");
      }
    }

    // Second pass over possibleJobs, to remove repeatableJobs which is not the "nextRepeatableJob"
    if (nextRepeatableJob != null) {
      nextRepeatableJob.setNextPII(null);
      Iterator<Entry<Integer, Job>> i2 = possibleJobs.entrySet().iterator();
      while (i2.hasNext()) {
        Entry<Integer, Job> entry = i2.next();
        Job job = entry.getValue();
        boolean repeatable = job.getRepeatCount() != null && job.getRepeatCount() > 0;
        if (repeatable && !job.getId().equals(nextRepeatableJob.getId())) {
          log.debug("FilterOnRunTime removed job "
                  + job.getId()
                  + " since it was not first in line to run.");
          i2.remove();
        }
      }
    }

    sessionData.getPIIDecision().setCalcTms(System.currentTimeMillis());
    return possibleJobs;
  }

  private static long convertToPII(ServiceWindow serviceWindow, long NRT) {
    long nextPII = (NRT - serviceWindow.getCurrentTms()) / 1000L;
    log.debug("Repeatable Job Interval calculated to "
            + nextPII
            + "("
            + ServiceWindow.convert(NRT)
            + ") (TimeWindow is : "
            + serviceWindow.getTimeWindow()
            + ")");
    if (nextPII < PIIDecision.MINIMUM_PII) {
      log.debug("Repeatable Job Interval was calculated too low, changed to " + PIIDecision.MINIMUM_PII);
      nextPII = PIIDecision.MINIMUM_PII;
    }
    return nextPII;
  }

  private static Map<Integer, Job> filterOnDownloadAllowed(
      Map<Integer, Job> possibleJobs, int downloadLimit) {
    Iterator<Entry<Integer, Job>> i = possibleJobs.entrySet().iterator();
    while (i.hasNext()) {
      Entry<Integer, Job> entry = i.next();
      Job job = entry.getValue();
      JobType type = job.getFlags().getType();
      if ((type == JobType.SOFTWARE || type == JobType.TR069_SCRIPT)
          && !DownloadLogic.downloadAllowed(job, downloadLimit)) {
        i.remove();
      }
    }
    return possibleJobs;
  }

  private static Map<Integer, Job> filterOnGroupMatch(Map<Integer, Job> possibleJobs, Unit unit) {
    Iterator<Entry<Integer, Job>> i = possibleJobs.entrySet().iterator();
    while (i.hasNext()) {
      Job job = i.next().getValue();
      if (!job.getGroup().match(unit)) {
        i.remove();
      }
    }
    return possibleJobs;
  }

  private static Map<Integer, Job> filterOnStatusAndType(Job[] allJobs) {
    Map<Integer, Job> possibleJobs = new HashMap<>();
    for (Job j : allJobs) {
      JobType type = j.getFlags().getType();
      if (JobStatus.STARTED.equals(j.getStatus())
          && type != JobType.KICK
          && type != JobType.TELNET) {
        possibleJobs.put(j.getId(), j);
      }
    }
    return possibleJobs;
  }

  private static Map<Integer, JobHistoryEntry> getJobHistory(Unit unit, Jobs jobs) {
    Map<Integer, JobHistoryEntry> jobHistoryMap = new HashMap<>();
    String utpJobHistory = SystemParameters.JOB_HISTORY;
    UnitParameter jobHistoryUp = unit.getUnitParameters().get(utpJobHistory);
    if (jobHistoryUp != null) {
      String[] jobHistoryArr = jobHistoryUp.getValue().split(",");
      for (String str : jobHistoryArr) {
        if ("".equals(str.trim())) {
          continue;
        }
        try {
          JobHistoryEntry jhEntry = new JobHistoryEntry(str);
          // A job in the history is deleted from the database, we'll ignore that one
          if (jobs.getById(jhEntry.getJobId()) == null) {
            continue;
          }
          jobHistoryMap.put(jhEntry.getJobId(), jhEntry);
        } catch (NumberFormatException nfe) {
          // Ignore error...will occur if job-history is "" or someone has entered bogus history
        }
      }
    }
    return jobHistoryMap;
  }

  private static Map<Integer, Job> filterOnJobHistory(
      Map<Integer, Job> possibleJobs, Map<Integer, JobHistoryEntry> jobHistory) {
    Iterator<Entry<Integer, Job>> i = possibleJobs.entrySet().iterator();
    while (i.hasNext()) {
      Job job = i.next().getValue();
      JobHistoryEntry jhEntry = jobHistory.get(job.getId());
      if (jhEntry == null) {
        continue;
      }
      boolean repeatableJob = job.getRepeatCount() != null && job.getRepeatCount() > 0;
      if (repeatableJob) {
        // If a job has been repeated enough, it will be filtered out
        if (jhEntry.getRepeatedCount() != null
            && job.getRepeatCount() <= jhEntry.getRepeatedCount()) {
          i.remove();
        }
      } else {
        // If a job is not repeatable and represented in the history, it is already executed, hence
        // filtered out
        i.remove();
      }
    }
    return possibleJobs;
  }

  /**
   * If several jobs has passed through all filters, we have to make a prioritization between the
   * jobs. The rules are shown below, in prioritized order: 1. Non-repeatable jobs have priority
   * over those that are repeatable. 2. Non-dependent jobs have priority over those that are
   * dependent.
   */
  private static Job findJobWithHighestPriority(Map<Integer, Job> possibleJobs) {
    Iterator<Entry<Integer, Job>> i = possibleJobs.entrySet().iterator();
    Job nonRepeatableAndDependent = null;
    Job repeatableJob = null;
    while (i.hasNext()) {
      Job job = i.next().getValue();
      boolean repeatable = job.getRepeatCount() != null && job.getRepeatCount() > 0;
      if (!repeatable && job.getDependency() == null) {
        return job;
      } // return immediately
      if (!repeatable && job.getDependency() != null) {
        nonRepeatableAndDependent = job;
      } else {
        repeatableJob = job;
      }
    }
    if (nonRepeatableAndDependent != null) {
      return nonRepeatableAndDependent;
    }
    return repeatableJob;
  }
}
