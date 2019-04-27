package com.github.freeacs.tr069.base;

import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.UnitJobStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * This class should be the final decision maker on the next Periodic Inform Interval (PII). The
 * following logic is applied:
 *
 * <p>Minimum PII set from this class is 31 because the minimum PII that a client can accept
 * (according to spec) is 30.
 *
 * <p>If a job is in progress or the status of a job is COMPLETED_OK, PII = 31. The reason is that
 * the client must return a soon as possible to check for job verification or to see if more jobs
 * are waiting.
 *
 * <p>In case no job is in progress or not COMPLETED_OK we can assume that either no job was found
 * or the job had completed with status CONFIRMED_FAILED or UNCONFIRMED_FAILED. In all these cases
 * we treat the situation as no job was found.
 *
 * <p>If "no job found", find all jobs scheduled to run in future and calculate the next PII.
 * Additionally, if a disruptive parameter (SOFTWARE/SCRIPT/REBOOT/RESET) has been set, calculate
 * the next PII according to Disruptive Service Window. Choose the lowest PII as next PII.
 *
 * <p>If no future job is found, and no disruptive parameter set, calculate PII according to Regular
 * Service Window.
 *
 * <p>Necessary information
 *
 * <p>1. Current job (if any) and status of job (if available) 2. All jobs with calculated nextPII
 * 3. Time stamp for calculation of nextPII (to offset final nextPII) 4. Disruptive Service Window
 * (if disruptive parameter set)
 *
 * @author morten
 */
@Slf4j
public class PIIDecision {
  private SessionDataI sessionData;
  private Job currentJob;
  private String currentJobStatus;
  private Job[] allJobs;
  private long calcTms;
  private ServiceWindow disruptiveSW;

  /**
   * The minimum Periodic Inform Interval is set to 31 (seconds), since 30 sec is the minimum PII in
   * the TR-069 specification. We add one second (30+1) to avoid TR-069 client implementations which
   * may have interpreted the spec as "PII must be greater than 30".
   */
  public static final long MINIMUM_PII = 31;

  public PIIDecision(SessionDataI sessionData) {
    this.sessionData = sessionData;
  }

  public long nextPII() {
    if (currentJob != null) {
      if (currentJobStatus != null) {
        if (currentJobStatus.equals(UnitJobStatus.COMPLETED_OK)) {
          log(MINIMUM_PII, "Job is found and completed OK");
          return MINIMUM_PII;
        } // continue to next steps
      } else {
        log(
            MINIMUM_PII,
            "Job is found but no status, indicates job not verified or serverside job");
        return MINIMUM_PII;
      }
    }

    // Find the next Job to schedule for, and then remove nextPII from all job-object
    // to avoid this information leak over to another thread/session (since Job-objects are
    // part of the XAPS-cache
    Job nextScheduledJob = null;
    Long nextScheduledJobPII = null;
    if (allJobs != null) {
      for (Job job : allJobs) {
        if (job.getNextPII() != null) {
          if (nextScheduledJob == null || nextScheduledJobPII > job.getNextPII()) {
            nextScheduledJob = job;
            nextScheduledJobPII = job.getNextPII();
          }
          job.setNextPII(null);
        }
      }
    }

    if (nextScheduledJob != null) {
      // A next scheduled job was found
      long timeSinceCalculation = (System.currentTimeMillis() - calcTms) / 1000;
      long nextPII = nextScheduledJobPII - timeSinceCalculation;
      if (nextPII < MINIMUM_PII) {
        nextPII = MINIMUM_PII;
      }
      if (disruptiveSW != null) {
        long dswPII = disruptiveSW.calculateStdPII();
        if (nextPII > dswPII) {
          nextPII = dswPII;
          log(nextPII, "Disruptive parameter set, calculate PII according to Disrupte SW");
        } else {
          log(nextPII, "Job scheduled for future run, using calculated PII");
        }
      } else {
        log(nextPII, "Job scheduled for future run, using calculated PII");
      }
      return nextPII;
    } else {
      ServiceWindow sw = disruptiveSW;
      long nextPII = 0;
      if (sw == null) {
        sw = new ServiceWindow(sessionData, false);
        nextPII = sw.calculateStdPII();
        log(nextPII, "No job found or any job scheduled for the future, using regular SW");
      } else {
        nextPII = sw.calculateStdPII();
        log(
            nextPII,
            "No job found or any job scheduled for the future, but using a disruptive sw since a Reset/Restart/Donwload is expected next");
      }
      return nextPII;
    }
  }

  private void log(long pii, String reason) {
    log.debug("PeriodicInformInterval (final): " + pii + " (reason: " + reason + ")");
  }

  /**
   * Set by JobLogic.checkNewJob() - verified
   *
   * @param currentJob
   */
  public void setCurrentJob(Job currentJob) {
    this.currentJob = currentJob;
  }

  /**
   * Set by UnitJob.stop() - verified
   *
   * @param currentJobStatus
   */
  public void setCurrentJobStatus(String currentJobStatus) {
    this.currentJobStatus = currentJobStatus;
  }

  /**
   * Set by JobLogic.checkNewJob() - verified
   *
   * @param allJobs
   */
  public void setAllJobs(Job[] allJobs) {
    this.allJobs = allJobs;
  }

  /**
   * Set by JobLogic.filterOnRunTime() - verified
   *
   * @param calcTms
   */
  public void setCalcTms(long calcTms) {
    this.calcTms = calcTms;
  }

  /**
   * Set by GPVDecision.somemethod()
   *
   * @param disruptiveSW
   */
  public void setDisruptiveSW(ServiceWindow disruptiveSW) {
    this.disruptiveSW = disruptiveSW;
  }
}
