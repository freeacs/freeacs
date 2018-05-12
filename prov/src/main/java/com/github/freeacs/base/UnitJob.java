package com.github.freeacs.base;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.db.DBAccessStatic;
import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.dbi.*;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import com.owera.xaps.base.db.DBAccess;
import com.owera.xaps.base.db.DBAccessStatic;
import com.owera.xaps.dbi.*;
import com.github.freeacs.dbi.JobFlag.JobServiceWindow;
import com.github.freeacs.dbi.util.SystemParameters;
import com.owera.xaps.tr069.xml.ParameterValueStruct;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnitJob {

	private SessionDataI sessionData;
	private Job job;
	/*
	 * Server-side provisioning differs from TR-069/TFTP/HTTP is several ways
	 * 1. Triggered from server side
	 * 2. Continuous/synchronous - not splitted in several sessions (execution/verification)
	 * 3. Requires a job to trigger the provisioning
	 * 4. Not possible to change profile for a unit
	 * (Additionally Telnet-provisioning (which is server-side) also has another speciality)
	 * 5. Handles params quite differently, every param is paired with a parse-param
	 * 
	 * These differences have several implications:
	 * a. Job-current parameter is not needed (ref 2.) 
	 * b. Job-object is known at all times - don't need to read it from Job-current-parameter (ref a., 2.)
	 * c. Handles only Job-history/Disruptive params here, rest is handled in TelnetJobThread (ref 5.)
	 */
	private boolean serverSideJob;

	private Long jobStartTime = null;

	public UnitJob(SessionDataI sessionData, Job job, boolean serverSideJob) {
		this.sessionData = sessionData;
		this.job = job;
		this.serverSideJob = serverSideJob;
	}

	private UnitParameter makeUnitParameter(String name, String value) {
		UnittypeParameter utp = sessionData.getUnittype().getUnittypeParameters().getByName(name);
		return new UnitParameter(utp, sessionData.getUnitId(), value, sessionData.getProfile());
	}

	//	private Map<String, String> getParameters(Unit unit, Profile profile) {
	//		Map<String, String> map = new TreeMap<String, String>(new NaturalComparator());
	//		ProfileParameters profileParameters = profile.getProfileParameters();
	//		Map<String, UnitParameter> unitParameters = unit.getUnitParameters();
	//		if (profileParameters != null) {
	//			ProfileParameter[] pparams = profileParameters.getProfileParameters();
	//			unit.getUnitParameters();
	//			for (int i = 0; pparams != null && i < pparams.length; i++) {
	//				if (unitParameters != null && unitParameters.get(pparams[i].getUnittypeParameter().getName()) != null)
	//					continue;
	//				map.put(pparams[i].getUnittypeParameter().getName(), pparams[i].getValue());
	//			}
	//		}
	//		if (unitParameters != null) {
	//			for (Entry<String, UnitParameter> entry : unitParameters.entrySet()) {
	//				map.put(entry.getKey(), entry.getValue().getParameter().getValue());
	//			}
	//		}
	//		return map;
	//	}

	//	private void updateSessionWithProfile() {
	//		//		sessionData.setProfile(job.getMoveToProfile());
	//		Map<String, String> parameters = getParameters(sessionData.getUnit(), sessionData.getProfile());
	//		UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
	//		for (Entry<String, String> entry : parameters.entrySet()) {
	//			UnittypeParameter utp = utps.getByName(entry.getKey());
	//			String utpName = utp.getName();
	//			ParameterValueStruct pvs = new ParameterValueStruct(utpName, entry.getValue());
	//			if (utp.getFlag().isSpecial())
	//				sessionData.getOweraParameters().putPvs(utpName, pvs);
	//			else
	//				sessionData.getFromDB().put(utpName, pvs);
	//		}
	//	}

	/**
	 * This method updates the sessiondata object with the job parameters.
	 * Specifically it updates:
	 * 		oweraparameters: job-current
	 * 		fromDB : all job-parameters (except job-current/job-history)
	 * @param sessionData
	 * @param job
	 * @throws SQLException
	 * @throws NoAvailableConnectionException
	 */
	private void updateSessionWithJobParams(boolean verification) throws SQLException, NoAvailableConnectionException {
		Map<String, JobParameter> jobParams = job.getDefaultParameters();
		sessionData.setJobParams(jobParams);
		//		for (JobParameter jp : jobParams.values()) {
		//			Parameter jup = jp.getParameter();
		//			String jpName = jup.getUnittypeParameter().getName();
		//			//			if (jpName.equals(SystemParameters.JOB_CURRENT) || jpName.equals(SystemParameters.JOB_HISTORY))
		//			//				continue;
		//			String jpValue = jup.getValue();
		//			ParameterValueStruct jpPvs = new ParameterValueStruct(jpName, jpValue);
		//			if (jp.getParameter().getUnittypeParameter().getFlag().isSystem()) {
		//				Log.debug(UnitJob.class, "Added " + jpName + ", value:[" + jpValue + "] to system parameters");
		//				sessionData.getOweraParameters().putPvs(jpName, jpPvs);
		//			} else if (verification && jp.getParameter().getUnittypeParameter().getFlag().isReadOnly()) {
		//				Log.debug(UnitJob.class, "Skipped " + jpName + " in verification stage since it's read-only, should not be asked for in GPV");
		//			} else {
		//				Log.debug(UnitJob.class, "Added " + jpName + ", value:[" + jpValue + "] to session - will be asked for in GPV");
		//				sessionData.getFromDB().put(jpName, jpPvs);
		//			}
		//		}
	}

	private void updateSessionWithJobCurrent() {
		ParameterValueStruct jobIdPvs = new ParameterValueStruct(SystemParameters.JOB_CURRENT, "" + job.getId());
		sessionData.getOweraParameters().putPvs(SystemParameters.JOB_CURRENT, jobIdPvs);
	}

	/*
	 * Computes the history parameter. This parameter will add the newest
	 * jobId to the front of the parameter. Job id which refer to a no longer
	 * existing job will be removed from the comma-separated list.
	 */
	private UnitParameter makeHistoryParameter(Integer jobId) throws SQLException, NoAvailableConnectionException {
		Unittype unittype = sessionData.getUnittype();
		Unit unit = sessionData.getUnit();
		Map<String, UnitParameter> upMap = unit.getUnitParameters();
		UnittypeParameter jhUtp = unittype.getUnittypeParameters().getByName(SystemParameters.JOB_HISTORY);
		UnitParameter jobHistoryUp = upMap.get(jhUtp.getName());

		String jh1 = unit.getParameters().get(SystemParameters.JOB_HISTORY);
		long tms = System.currentTimeMillis();
		if (jobStartTime != null)
			tms = jobStartTime;
		if (jh1 == null || jh1.trim().equals(""))
			return makeUnitParameter(SystemParameters.JOB_HISTORY, "," + jobId + ":0:" + tms + ",");

		String jh2 = ",";// + jobId + ",";
		boolean found = false;
		for (String entry : jh1.split(",")) {
			if (entry.trim().equals(""))
				continue;
			JobHistoryEntry jhEntry = new JobHistoryEntry(entry);
			Job entryJob = DBAccess.getJob(sessionData, "" + jhEntry.getJobId());
			if (entryJob != null) {
				if (entryJob.getId() == jobId) { // inc repeated-counter
					jh2 += jhEntry.incEntry(tms) + ",";
					found = true;
				} else {
					jh2 += entry + ",";
				}
			}
		}
		if (!found)
			jh2 = "," + jobId + ":0:" + tms + jh2;
		jobHistoryUp.getParameter().setValue(jh2);
		return jobHistoryUp;
	}

	/*
	 * These steps are performed when starting a job:
	 * - write job-current parameter to DB (with job.getId()) if asynchronous mode
	 * - write unit-job entry to DB
	 * - update session data with job parameters
	 * - update session data with profile parameters  
	 * - update session data with job current
	 */
	public void start() throws SQLException {
		try {
			try {
				String unitId = sessionData.getUnitId();
				if (!serverSideJob) {
					UnitParameter jobUp = makeUnitParameter(SystemParameters.JOB_CURRENT, "" + job.getId());
					List<UnitParameter> upList = new ArrayList<UnitParameter>();
					upList.add(jobUp);
					DBAccessStatic.queueUnitParameters(sessionData.getUnit(), upList, sessionData.getProfile());
				}
				DBAccessStatic.startUnitJob(unitId, job.getId());
				if (!serverSideJob) {
					//					if (job.getMoveToProfile() != null)
					//						updateSessionWithProfile();
					updateSessionWithJobParams(false);
					updateSessionWithJobCurrent();
					Log.debug(UnitJob.class, "UnitJob status is updated to STARTED and job parameters / job profile are written to session.");
				} else {
					Log.debug(UnitJob.class, "UnitJob status is updated to STARTED");
				}

			} catch (SQLException sqle) {
				Log.error(UnitJob.class, "UnitJob update failed", sqle);
				throw sqle;
			}
		} catch (Throwable t) {
			Log.error(UnitJob.class, "An error ocurred in start()", t);
		}
	}

	/*
	 * These steps are performed when stopping a job:
	 * If ok
	 *  - write profile-change to DB 
	 * 	- write unit-job entry to DB (ok)
	 *  - write job parameters to DB
	 *  - write job-current to DB (as "")
	 *  - write job-history to DB (remove old jobs)
	 *  - read unit again and update session data (must clear fromDB to do this)
	 * Else
	 * 	- write unit-job entry to DB (failed)
	 *  - write job-current to DB (as "")
	 */
	public void stop(String unitJobStatus) throws SQLException, NoAvailableConnectionException {
		try {
			Integer jobId = null;
			if (serverSideJob)
				jobId = sessionData.getJob().getId();
			else {
				if (sessionData == null || sessionData.getOweraParameters() == null)
					return;
				String jobIdStr = sessionData.getOweraParameters().getValue(SystemParameters.JOB_CURRENT);
				if (jobIdStr == null)
					return;
				try {
					jobId = Integer.parseInt(jobIdStr);
				} catch (NumberFormatException nfe) {
					return;
				}
				Log.debug(UnitJob.class, "Current jobId param is " + jobId + ", will stop unit job with unit job status set to " + unitJobStatus);
				job = DBAccess.getJob(sessionData, jobIdStr);
				if (job == null && !unitJobStatus.equals(UnitJobStatus.CONFIRMED_FAILED)) {
					Log.warn(UnitJob.class, "Couldn't find job with jobId " + jobId + ", unit job status changed to " + UnitJobStatus.CONFIRMED_FAILED);
					unitJobStatus = UnitJobStatus.CONFIRMED_FAILED;
				}
			}
			try {
				List<UnitParameter> upList = new ArrayList<UnitParameter>();
				if (!serverSideJob) {
					upList.add(makeUnitParameter(SystemParameters.JOB_CURRENT, ""));
					upList.add(makeUnitParameter(SystemParameters.JOB_CURRENT_KEY, ""));
				}
				if (unitJobStatus.equals(UnitJobStatus.COMPLETED_OK)) {
					//					if (!serverSideJob && job.getMoveToProfile() != null) {
					//						sessionData.getDbAccess().writeProfileChange(sessionData.getUnitId(), job.getMoveToProfile());
					//						sessionData.setProfile(job.getMoveToProfile());
					//					}
					upList.add(makeHistoryParameter(job.getId()));
					if (job.getFlags().getServiceWindow() == JobServiceWindow.DISRUPTIVE)
						upList.add(makeUnitParameter(SystemParameters.JOB_DISRUPTIVE, "1"));
					if (serverSideJob) {
						Log.notice(UnitJob.class, "UnitJob is COMPLETED, job history is updated");
					} else {
						//						updateSessionWithJobParams(true);
						Map<String, JobParameter> jobParams = job.getDefaultParameters();
						sessionData.setJobParams(jobParams);
						for (JobParameter jp : sessionData.getJobParams().values()) {
							String jpName = jp.getParameter().getUnittypeParameter().getName();
							if (jpName.equals(SystemParameters.RESTART) || jpName.equals(SystemParameters.RESET))
								continue;
							if (jp.getParameter().getUnittypeParameter().getFlag().isReadOnly())
								continue;
							UnitParameter up = new UnitParameter(jp.getParameter(), sessionData.getUnitId(), sessionData.getProfile());
							upList.add(up);
						}
						Log.notice(UnitJob.class, "UnitJob is COMPLETED, job history, profile/unit parameters are updated");
					}
				}
				DBAccessStatic.stopUnitJob(sessionData.getUnitId(), jobId, unitJobStatus);
				sessionData.getPIIDecision().setCurrentJobStatus(unitJobStatus);
				// Write directly to database, no queuing, since the all data are flushed in next step (most likely)
				XAPS xaps = sessionData.getDbAccess().getXaps();
				XAPSUnit xapsUnit = DBAccess.getXAPSUnit(xaps);
				xapsUnit.addOrChangeUnitParameters(upList, sessionData.getProfile());
				//				sessionData.getDbAccess().writeUnitParameters(sessionData.getUnit(), upList, sessionData.getProfile());
				if (!serverSideJob) {
					sessionData.setFromDB(null);
					sessionData.setOweraParameters(null);
					sessionData.setJobParams(null);
					Log.debug(UnitJob.class, "Unit-information will be reloaded to reflect changes in profile/unit parameters");
					sessionData.updateParametersFromDB(sessionData.getUnitId());
				}
			} catch (SQLException sqle) {
				Log.error(UnitJob.class, "UnitJob update failed", sqle);
				throw sqle;
			}
		} catch (Throwable t) {
			Log.error(UnitJob.class, "An error ocurred in stop()", t);
		}
	}

	public void setJobStartTime(Long jobStartTime) {
		this.jobStartTime = jobStartTime;
	}

}
