package com.owera.xaps.stun;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.common.util.Sleep;
import com.owera.xaps.base.JobHistoryEntry;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Inbox;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.JobStatus;
import com.owera.xaps.dbi.Message;
import com.owera.xaps.dbi.Parameter;
import com.owera.xaps.dbi.Parameter.Operator;
import com.owera.xaps.dbi.Parameter.ParameterDataType;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.UnitJob;
import com.owera.xaps.dbi.UnitJobs;
import com.owera.xaps.dbi.UnitParameter;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.Unittype.ProvisioningProtocol;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.Unittypes;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.util.SystemParameters;

public class JobKickThread implements Runnable {

	private static Logger log = new Logger("KickJob");
	//	private static boolean initialized = false;
	private DBI dbi;
	//	private static Syslog syslog;
	private ConnectionProperties xapsCp;
	// Key: jobId
	// Value: Set of unitId
	private Map<Integer, Set<String>> jobKickMap;
	// Key: jobId
	// Value: Tms of last refresh
	private Map<Integer, Long> jobRefreshMap = new HashMap<Integer, Long>();
	// This inbox listens for changes on job (from other modules in xAPS)
	private Inbox jobChangeInbox = new Inbox();

	public JobKickThread(ConnectionProperties xapsCp, DBI dbi) {
		this.xapsCp = xapsCp;
		this.dbi = dbi;
		jobChangeInbox.addFilter(new Message(null, Message.MTYPE_PUB_CHG, null, Message.OTYPE_JOB));
		dbi.registerInbox("jobChangeInbox", jobChangeInbox);

	}

	//	public static void initialize() throws SQLException, NoAvailableConnectionException {
	//		if (!initialized	) {
	//			int maxAge = 600000;
	//			int maxConn = 5;
	//			xapsCp = ConnectionProvider.getConnectionPropertiesOneLiner("xaps-stun.properties", "db.xaps", maxAge, maxConn);
	//			Users users = new Users(xapsCp);
	//			User user = users.getByName(Users.USER_ADMIN);
	//			Identity id = new Identity(SyslogConstants.FACILITY_STUN, StunServlet.VERSION, user);
	//			ConnectionProperties sysCp = ConnectionProvider.getConnectionPropertiesOneLiner("xaps-stun.properties", "db.syslog", maxAge, maxConn);
	//			syslog = new Syslog(sysCp, id);
	//			dbi = new DBI(Integer.MAX_VALUE, xapsCp, syslog);
	//			initialized = true;
	//		}
	//
	//	}

	private void populateJobKickMapForOneJob(Job job, XAPS xaps, Unittype unittype) throws SQLException, NoAvailableConnectionException {
		Group group = job.getGroup();
		long now = jobRefreshMap.get(job.getId());
		XAPSUnit xapsUnit = new XAPSUnit(xapsCp, xaps, xaps.getSyslog());
		Map<String, Unit> unitsInGroup = xapsUnit.getUnits(group);
		log.info("Found " + unitsInGroup.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ")");
		Group topParent = group.getTopParent();
		Profile profile = topParent.getProfile();

		UnittypeParameter historyUtp = unittype.getUnittypeParameters().getByName(SystemParameters.JOB_HISTORY);
		Parameter historyParam = new Parameter(historyUtp, "%," + job.getId() + ":%", Operator.EQ, ParameterDataType.TEXT);
		Map<String, Unit> unitsCompleted = xapsUnit.getUnits(unittype, profile, historyParam, Integer.MAX_VALUE);
		Iterator<Unit> iterator = unitsCompleted.values().iterator();
		int runButRunAgainCounter = 0;
		if (job.getRepeatCount() != null && job.getRepeatCount() > 0 && job.getRepeatInterval() != null) {
			while (iterator.hasNext()) {
				Unit u = iterator.next();
				UnitParameter up = u.getUnitParameters().get(historyUtp.getName());
				String historyParameterValue = up.getValue();
				for (String entry : historyParameterValue.split(",")) {
					if (entry.trim().equals(""))
						continue;
					JobHistoryEntry jhEntry = new JobHistoryEntry(entry);
					if (jhEntry.getJobId().intValue() != job.getId())
						continue;
					if (jhEntry.getRepeatedCount() >= job.getRepeatCount())
						continue;
					long timeSinceLastRun = now - jhEntry.getLastRunTms();
					if (timeSinceLastRun <= job.getRepeatInterval() * 1000)
						continue;
					// The job was run before, but it should now be run again, hence: not completed
					iterator.remove();
					runButRunAgainCounter++;
				}
			}
		}
		String msg = "Found " + unitsCompleted.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ") already completed.";
		if (runButRunAgainCounter > 0)
			msg += " (" + runButRunAgainCounter + " have run before, but will be repeated)";
		log.info(msg);

		List<Parameter> upList = new ArrayList<Parameter>();
		UnittypeParameter currentUtp = unittype.getUnittypeParameters().getByName(SystemParameters.JOB_CURRENT);
		Parameter currentParam = new Parameter(currentUtp, "%," + job.getId() + ":%");
		upList.add(currentParam);
		Map<String, Unit> unitsInProcess = xapsUnit.getUnits(unittype, profile, upList, Integer.MAX_VALUE);
		log.info("Found " + unitsInProcess.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ") in process.");

		//		Map<String, Unit> unitsWithSwVer = null;
		//		if (job.getSoftware() != null) {
		//			UnittypeParameter softwareVersionUtp = unittype.getUnittypeParameters().getByName(SystemParameters.SOFTWARE_VERSION);
		//			Parameter softwareVersionParam = new Parameter(softwareVersionUtp, job.getSoftware().getVersionNumber());
		//			upList.add(softwareVersionParam);
		//			unitsWithSwVer = xapsUnit.getUnits(unittype, profile, upList, Integer.MAX_VALUE);
		//			log.info("Found " + unitsWithSwVer.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ") with correct software version.");
		//		}

		Set<String> unitSet = new HashSet<String>();
		for (String unitId : unitsInGroup.keySet()) {
			if (unitsCompleted.get(unitId) == null && unitsInProcess.get(unitId) == null) {
				unitSet.add(unitId);
				log.debug("Added  " + unitId + " to list of units to run a telnet'ed");
			}
		}
		jobKickMap.put(job.getId(), unitSet);
	}

	//	private void populateJobKickMapForOneJob(Job job, XAPS xaps, Unittype unittype) throws SQLException, NoAvailableConnectionException {
	//		Group group = job.getGroup();
	//		XAPSUnit xapsUnit = new XAPSUnit(xapsCp, xaps, xaps.getSyslog());
	//		Map<String, Unit> unitsInGroup = xapsUnit.getUnitMap(group);
	//		log.info("Found " + unitsInGroup.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ")");
	//		Group topParent = group.getTopParent();
	//		Profile profile = topParent.getProfile();
	//		List<Parameter> upList = new ArrayList<Parameter>();
	//		UnittypeParameter historyUtp = unittype.getUnittypeParameters().getByName(SystemParameters.JOB_HISTORY);
	//		Parameter historyParam = new Parameter(historyUtp, "%," + job.getId() + ":%", true);
	//		upList.add(historyParam);
	//		Map<String, Unit> unitsCompleted = xapsUnit.getUnitMap(null, unittype, profile, upList, Integer.MAX_VALUE);
	//		log.info("Found " + unitsCompleted.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ") already completed.");
	//		upList = new ArrayList<Parameter>();
	//		UnittypeParameter currentUtp = unittype.getUnittypeParameters().getByName(SystemParameters.JOB_CURRENT);
	//		Parameter currentParam = new Parameter(currentUtp, "%," + job.getId() + ":%", true);
	//		upList.add(currentParam);
	//		Map<String, Unit> unitsInProcess = xapsUnit.getUnitMap(null, unittype, profile, upList, Integer.MAX_VALUE);
	//		log.info("Found " + unitsInProcess.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ") in process.");
	//		Map<String, Unit> unitsWithSwVer = null;
	//		if (job.getSoftware() != null) {
	//			UnittypeParameter softwareVersionUtp = unittype.getUnittypeParameters().getByName(SystemParameters.SOFTWARE_VERSION);
	//			Parameter softwareVersionParam = new Parameter(softwareVersionUtp, job.getSoftware().getVersionNumber(), true);
	//			upList.add(softwareVersionParam);
	//			unitsWithSwVer = xapsUnit.getUnitMap(null, unittype, profile, upList, Integer.MAX_VALUE);
	//			log.info("Found " + unitsWithSwVer.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ") with correct software version.");
	//		}
	//		for (String unitId : unitsInGroup.keySet()) {
	//			if (unitsCompleted.get(unitId) == null && unitsInProcess.get(unitId) == null && (unitsWithSwVer == null || unitsWithSwVer.get(unitId) != null)) {
	//				Set<String> unitSet = jobKickMap.get(job.getId());
	//				log.debug("Added  " + unitId + " to list of units to be kicked");
	//				unitSet.add(unitId);
	//			}
	//		}
	//		Set<String> unitSet = jobKickMap.get(job.getId());
	//		Iterator<String> iterator = unitSet.iterator();
	//		while (iterator.hasNext()) {
	//			String unitId = iterator.next();
	//			if (unitsInGroup.get(unitId) == null) {
	//				log.debug("Removed " + unitId + " from list of units to be kicked (no longer in group)");
	//				iterator.remove();
	//			}
	//		}
	//	}

	private void populateJobKickMapForAllJobs(XAPS xaps) throws SQLException, NoAvailableConnectionException {
		jobKickMap = new HashMap<Integer, Set<String>>();
		Unittype[] unittypes = xaps.getUnittypes().getUnittypes();
		for (Unittype unittype : unittypes) {
			if (ProvisioningProtocol.TR069 != unittype.getProtocol()) {
				continue;
			}
			Job[] jobs = unittype.getJobs().getJobs();
			for (Job job : jobs) {
				if (job.getFlags().getType().equals("KICK")) {
					if (job.getStatus().equals(JobStatus.STARTED)) {
						if (jobKickMap.get(job.getId()) == null) {
							log.notice("Job " + job.getName() + " (" + job.getId() + ") is STARTED and discovered for the first time.");
							jobKickMap.put(job.getId(), new HashSet<String>());
							jobRefreshMap.put(job.getId(), System.currentTimeMillis());
							populateJobKickMapForOneJob(job, xaps, unittype);
						} else {
							long lastRefresh = jobRefreshMap.get(job.getId());
							if (lastRefresh + Properties.getKickRescan() * 60000 < System.currentTimeMillis()) {
								log.notice("Job " + job.getId() + " is STARTED and refreshed.");
								jobRefreshMap.put(job.getId(), System.currentTimeMillis());
								populateJobKickMapForOneJob(job, xaps, unittype);
							}
						}
					} else { // The job is STOPPED or READY or COMPLETED
						if (jobKickMap.get(job.getId()) != null) {
							log.notice("Job " + job.getName() + " (" + job.getId() + ") is not STARTED and no more units will be kicked from this job");
							jobKickMap.remove(job.getId());
						}
					}
				}
			}
		}
	}

	private Job findJobById(Integer jobId) {
		for (Unittype unittype : dbi.getXaps().getUnittypes().getUnittypes()) {
			Job job = unittype.getJobs().getById(jobId);
			if (job != null)
				return job;
		}
		return null;
	}

	private void kickJobs(XAPS xaps) {
		int kickInterval = Properties.getKickInterval();
		Sleep kickSleep = new Sleep(kickInterval, kickInterval / 10, false);
		for (Integer jobId : jobKickMap.keySet()) {
			Job job = findJobById(jobId);
			Set<String> unitIdSet = jobKickMap.get(jobId);
			Iterator<String> iterator = unitIdSet.iterator();
			while (iterator.hasNext()) {
				String unitId = iterator.next();
				try {
					if (newJobStartedOrRunningJobStopped(job))
						return;
					kickSleep.sleep();
					XAPSUnit xapsUnit = new XAPSUnit(xapsCp, xaps, xaps.getSyslog());
					Unit unit = xapsUnit.getUnitById(unitId);
					if (unit != null) {
						startUnitJob(unit, jobId, xaps);
						Kick.kick(unit, xapsUnit);
						iterator.remove();
					} else {
						log.error(unitId + " was not found in xAPS, not possible to kick");
						iterator.remove();
					}
				} catch (Throwable t) {
					log.error(unitId + " experienced an error during kick, will be tried again later " + t);
				}
			}
		}
	}

	public void run() {
		Thread.currentThread().setName("JobKickSpawner");
		try {
			Sleep sleep = new Sleep(1000, 1000, true);
			while (true) {
				
				sleep.sleep();
				if (Sleep.isTerminated())
					break;
				XAPS xaps = dbi.getXaps();
				populateJobKickMapForAllJobs(xaps);
				kickJobs(xaps);
			}
		} catch (Throwable t) {
			OKServlet.setJobKickError(t);
			log.fatal("An error ocurred, JobKickSpawner exits - server is not able to process job-kick anymore!!!", t);
		}
	}

	private void startUnitJob(Unit u, Integer jobId, XAPS xaps) throws NoAvailableConnectionException, SQLException {
		XAPSUnit xapsUnit = new XAPSUnit(xapsCp, xaps, xaps.getSyslog());
		Unittype unittype = u.getUnittype();
		UnittypeParameter currentUtp = unittype.getUnittypeParameters().getByName(SystemParameters.JOB_CURRENT);
		UnitParameter currentUp = new UnitParameter(currentUtp, u.getId(), "" + jobId, u.getProfile());
		List<UnitParameter> upList = new ArrayList<UnitParameter>();
		upList.add(currentUp);
		xapsUnit.addOrChangeUnitParameters(upList, u.getProfile());
		UnitJobs unitJobs = new UnitJobs(xapsCp);
		UnitJob uj = new UnitJob(u.getId(), jobId);
		uj.setStartTimestamp(new Date());
		boolean updated = unitJobs.start(uj);
		log.debug(u.getId() + " was marked as started in unit-job table (result from insert: " + updated + ")");
	}

	private boolean newJobStartedOrRunningJobStopped(Job j) {
		if (!j.getStatus().equals(JobStatus.STARTED)) {
			log.notice("Job " + j.getId() + " is no longer running, aborting this job");
			return true;
		}
		List<Message> messages = jobChangeInbox.getUnreadMessages();
		for (Message jcMessage : messages) {
			Inbox dbiInbox = dbi.retrieveInbox(DBI.PUBLISH_INBOX_NAME);
			List<Message> dbiMessages = dbiInbox.getAllMessages();
			String jcM = jcMessage.getMessageType() + jcMessage.getObjectType() + jcMessage.getObjectId();
			log.debug("Job change inbox reported that job " + jcMessage.getObjectId() + " has changed");
			boolean messageProcessed = true;
			while (true) {
				messageProcessed = true;
				for (Message dbiMessage : dbiMessages) {
					String dbiM = dbiMessage.getMessageType() + dbiMessage.getObjectType() + dbiMessage.getObjectId();
					if (jcM.equals(dbiM)) {
						log.debug("DBI has not yet processed the change message for job " + dbiMessage.getObjectId() + ", waiting 500 ms");
						messageProcessed = false;
						break;
					}
				}
				if (messageProcessed)
					break;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
			log.debug("DBI has processed the change message for job " + jcMessage.getObjectId());
			jobChangeInbox.markMessageAsRead(jcMessage);
			Unittypes unittypes = dbi.getXaps().getUnittypes();
			for (Unittype unittype : unittypes.getUnittypes()) {
				Integer jobId = new Integer(jcMessage.getObjectId());
				Job job = unittype.getJobs().getById(jobId);
				if (job != null && job.getStatus().equals(JobStatus.STARTED)) {
					if (jobKickMap.get(jobId) == null) {
						log.notice("Job " + jobId + " is STARTED, but is not part of the JobKickMap. Will abort and rebuild the queue.");
						jobChangeInbox.deleteReadMessage();
						return true;
					} else {
						log.debug("Job " + jobId + " is STARTED, but since it is part of the JobKickMap no special action is required.");
					}
				} else if (job != null) {
					log.debug("Job " + jobId + " is changed, but since the  status is not STARTED no special action is required.");
				}
			}
		}
		if (messages.size() > 0) {
			jobChangeInbox.deleteReadMessage();
		}
		return false;

	}
}
