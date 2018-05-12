package com.owera.xaps.base;

import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.UnitJobStatus;

/**
 * This class should be the final decision maker on the next Periodic 
 * Inform Interval (PII). The following logic is applied:
 * 
 * Minimum PII set from this class is 31 because the minimum PII that a client 
 * can accept (according to spec) is 30.
 * 
 * If a job is in progress or the status of a job is COMPLETED_OK, PII = 31. The reason
 * is that the client must return a soon as possible to check for job verification
 * or to see if more jobs are waiting.
 * 
 * In case no job is in progress or not COMPLETED_OK we can assume that either no job
 * was found or the job had completed with status CONFIRMED_FAILED or UNCONFIRMED_FAILED.
 * In all these cases we treat the situation as no job was found.
 * 
 * If "no job found", find all jobs scheduled to run in future and calculate the next PII.
 * Additionally, if a disruptive parameter (SOFTWARE/SCRIPT/REBOOT/RESET) has been set,
 * calculate the next PII according to Disruptive Service Window. Choose the lowest PII
 * as next PII.
 * 
 * If no future job is found, and no disruptive parameter set, calculate PII according
 * to Regular Service Window.
 * 
 * Necessary information
 * 
 * 1. Current job (if any) and status of job (if available)
 * 2. All jobs with calculated nextPII
 * 3. Time stamp for calculation of nextPII (to offset final nextPII)
 * 4. Disruptive Service Window (if disruptive parameter set)
 * 
 * @author morten
 *
 */
public class PIIDecision {

	private SessionDataI sessionData;
	private Job currentJob;
	private String currentJobStatus;
	private Job[] allJobs;
	private long calcTms;
	private ServiceWindow disruptiveSW;

	/**
	 * The minimum Periodic Inform Interval is set to 31 (seconds), since 30 sec is the 
	 * minimum PII in the TR-069 specification. We add one second (30+1) to avoid
	 * TR-069 client implementations which may have interpreted the spec as "PII 
	 * must be greater than 30". 
	 */
	public final static long MINIMUM_PII = 31;

	public PIIDecision(SessionDataI sessionData) {
		this.sessionData = sessionData;
	}

	public long nextPII() {
		if (currentJob != null) {
			if (currentJobStatus != null) {
				if (currentJobStatus.equals(UnitJobStatus.COMPLETED_OK)) {
					log(MINIMUM_PII, "Job is found and completed OK");
					return MINIMUM_PII;
				} else {
					// continue to next steps
				}
			} else {
				log(MINIMUM_PII, "Job is found but no status, indicates job not verified or serverside job");
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
					if (nextScheduledJob == null) {
						nextScheduledJob = job;
						nextScheduledJobPII = job.getNextPII();
					} else if (nextScheduledJobPII > job.getNextPII()) {
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
			if (nextPII < MINIMUM_PII)
				nextPII = MINIMUM_PII;
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
				log(nextPII, "No job found or any job scheduled for the future, but using a disruptive sw since a Reset/Restart/Donwload is expected next");
			}
			return nextPII;
		}
	}

	private void log(long pii, String reason) {
		Log.debug(ServiceWindow.class, "PeriodicInformInterval (final): " + pii + " (reason: " + reason + ")");
	}

	/**
	 * Will calculate the next PeriodicInformInterval, so we can control when
	 * the CPE will connect to the ACS the next time.
	 * 
	 * nextPII = nextPeriodicInformInterval is calculated based on the following rules:
	 * 1. If a job exists and status is not set (the job has not ended so far in this session)
	 * 		or status is COMPLETED_OK, then PII = 31.
	 * 2. Else if job exists (but status is otherwise) then PII = PII_STD
	 * 3. Else
	 * 	3.1 If NextJobTms-CurrentTms < PII_STD then PII = Start of next ServiceWindow
	 * 	3.2 Else PII = PII_STD
	 * 
	 * PII_STD:
	 * 	If Enabled it will be calculated according these rules
	 * 	a) PII must be within ServiceWindow
	 * 	b) PII must be set according to the Frequency -> interval
	 * 	c) PII must be spread according to Spread
	 * 	If Not Enabled, it will only be calculated according to a)
	 */
	/*
	public long oldNextPII() {
		if (sessionData.getJob() != null) {
			// UnitJobStatus is updated in UnitJob.stop() whenever a Job is stopped (can be both OK and FAILED)
			if (sessionData.getUnitJobStatus() == null || sessionData.getUnitJobStatus().equals(UnitJobStatus.COMPLETED_OK)) {
				Log.debug(ServiceWindow.class, "Final decision on PeriodicInformInterval: 31 (reason: Job is found and either completed or still in proces");
				return 31; // 31 seconds is returned in case we're in a job or trying to verfiy a job
			} else {
				long PII_STD = calculateStdPII();
				Log.debug(ServiceWindow.class, "Final decision on PeriodicInformInterval: " + PII_STD + " (reason: Job is found but has failed");
				return PII_STD;// A job failed, we will wait a full interval (to avoid jobfail-loops)
			}
		} else {
			long PII_STD = calculateStdPII();
			// TmsForNextJob is updated in JobLogic.findJobWithHighestPriority whenever a new job is sought after
			if (sessionData.getTmsForNextJob() != null && sessionData.getTmsForNextJob() - currentTms < PII_STD * 1000) {
				// The next repeatable job is scheduled before next standard periodic inform - modify
				// PII to hit earliest possible time, but still within ServiceWindow.
				long nextPII = (timeWindow.getNextStartTms(currentTms) - currentTms) / 1000;
				//				long nextPII = (sessionData.getTmsForNextJob() - currentTms) / 1000;
				if (nextPII < 31) {
					Log.debug(ServiceWindow.class, "Final decision on PeriodicInformInterval: 31 (reason: repeatable Job should start in " + nextPII + " seconds)");
					return 31;
				} else {
					Log.debug(ServiceWindow.class, "Final decision on PeriodicInformInterval: " + nextPII + "  (reason: repeatable Job should start in " + nextPII + " seconds)");
					return nextPII;
				}
			} else {
				Log.debug(ServiceWindow.class, "Final decision on PeriodicInformInterval: " + PII_STD + " (reason: no jobs running or repeatable jobs found, use standard PII)");
				return PII_STD;
			}
		}
		
	}
	*/

	/** 
	 * Set by JobLogic.checkNewJob() - verified
	 * @param currentJob
	 */
	public void setCurrentJob(Job currentJob) {
		this.currentJob = currentJob;
	}

	/**
	 * Set by UnitJob.stop() - verified
	 * @param currentJobStatus
	 */
	public void setCurrentJobStatus(String currentJobStatus) {
		this.currentJobStatus = currentJobStatus;
	}

	/**
	 * Set by JobLogic.checkNewJob() - verified
	 * @param allJobs
	 */
	public void setAllJobs(Job[] allJobs) {
		this.allJobs = allJobs;
	}

	/**
	 * Set by JobLogic.filterOnRunTime() - verified
	 * @param calcTms
	 */
	public void setCalcTms(long calcTms) {
		this.calcTms = calcTms;
	}

	/**
	 * Set by GPVDecision.somemethod()
	 * @param disruptiveSW
	 */
	public void setDisruptiveSW(ServiceWindow disruptiveSW) {
		this.disruptiveSW = disruptiveSW;
	}

}
