package com.owera.xaps.spp.telnet;

import java.sql.SQLException;
import java.util.ArrayList;
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
import com.owera.xaps.dbi.JobFlag.JobType;
import com.owera.xaps.dbi.JobStatus;
import com.owera.xaps.dbi.Message;
import com.owera.xaps.dbi.Parameter;
import com.owera.xaps.dbi.Parameter.Operator;
import com.owera.xaps.dbi.Parameter.ParameterDataType;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.UnitParameter;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.Unittypes;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;

import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.spp.Properties;

public class TelnetProvisioning implements Runnable {

	private static Logger log = new Logger(TelnetProvisioning.class);
	private DBI dbi;
	private ConnectionProperties xapsCp;

	// This inbox listens for changes on job (from other modules in xAPS)
	private Inbox jobChangeInbox = new Inbox();
	// The TelnetJobQueue will be instantiated every time the populateJobUnitSetMapForAllJobs() are run
	private TelnetJobQueue tjq;
	// static map to keep track of active telnet sessions
	private static Set<String> activeTelnetSessions = new HashSet<String>();
	// monitor for synchronizing
	private Monitor monitor = new Monitor();
	// Key: jobId
	// Value: Tms of last refresh
	private Map<Integer, Long> jobRefreshMap = new HashMap<Integer, Long>();

	public TelnetProvisioning(ConnectionProperties xapsCp, DBI dbi) {
		this.xapsCp = xapsCp;
		this.dbi = dbi;
		jobChangeInbox.addFilter(new Message(null, Message.MTYPE_PUB_CHG, null, Message.OTYPE_JOB));
		dbi.registerInbox("jobChangeInbox", jobChangeInbox);
	}

	private void populateJobUnitSetMapForOneJob(Job job, XAPS xaps, Unittype unittype) throws SQLException, NoAvailableConnectionException {
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
//		if (job.getFile() != null) {
//			UnittypeParameter softwareVersionUtp = unittype.getUnittypeParameters().getByName(SystemParameters.SOFTWARE_VERSION);
//			Parameter softwareVersionParam = new Parameter(softwareVersionUtp, job.getFile().getVersion());
//			upList.add(softwareVersionParam);
//			unitsWithSwVer = xapsUnit.getUnits(unittype, profile, upList, Integer.MAX_VALUE);
//			log.info("Found " + unitsWithSwVer.size() + " units in group " + group.getName() + " from job " + job.getName() + " (" + job.getId() + ") with correct software version.");
//		}

		Map<String, TelnetJob> telnetJobMap = new HashMap<String, TelnetJob>();
		for (String unitId : unitsInGroup.keySet()) {
			if (unitsCompleted.get(unitId) == null && unitsInProcess.get(unitId) == null /* && (unitsWithSwVer == null || unitsWithSwVer.get(unitId) != null)*/) {
				telnetJobMap.put(unitId, new TelnetJob(job, unitId, now));
				log.debug("Added  " + unitId + " to list of units to run a telnet'ed");
			}
		}
		tjq.put(job.getId(), telnetJobMap);
	}

	private void populateJobUnitSetMapForAllJobs(XAPS xaps) throws SQLException, NoAvailableConnectionException {
		tjq = new TelnetJobQueue();
		Unittype[] unittypes = xaps.getUnittypes().getUnittypes();
		long now = System.currentTimeMillis();
		for (Unittype unittype : unittypes) {
			Job[] jobs = unittype.getJobs().getJobs();
			for (Job job : jobs) {
				if (job.getFlags().getType() == JobType.TELNET) {
					if (job.getStatus().equals(JobStatus.STARTED)) {
						if (jobRefreshMap.get(job.getId()) == null) {
							log.notice("Job " + job.getName() + " (" + job.getId() + ") is STARTED and discovered for the first time.");
							jobRefreshMap.put(job.getId(), now);
							populateJobUnitSetMapForOneJob(job, xaps, unittype);
						} else {
							long lastRefresh = jobRefreshMap.get(job.getId());
							long repeatInterval = Integer.MAX_VALUE;
							if (job.getRepeatInterval() != null)
								repeatInterval = job.getRepeatInterval();
							long timeSinceLastScan = now - lastRefresh;
							// refresh job if one of these conditions occur: 
							// a) refresh interval specified in propertyfile is passed
							// b) refresh interval for job is passed. 
							if (timeSinceLastScan >= Properties.getTelnetRescan() * 60000 || timeSinceLastScan >= (repeatInterval * 1000l)) {
								log.notice("Job " + job.getId() + " is STARTED and refreshed.");
								jobRefreshMap.put(job.getId(), now);
								populateJobUnitSetMapForOneJob(job, xaps, unittype);
							}
						}
					} else { // The job is STOPPED or READY or COMPLETED
						if (jobRefreshMap.get(job.getId()) != null) {
							log.notice("Job " + job.getName() + " (" + job.getId() + ") is not STARTED and no more telnet session will be started from this job");
							jobRefreshMap.remove(job.getId());
						}
					}
				}
			}
		}
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
					if (tjq.getTelnetJobMap(jobId) == null) {
						log.notice("Job " + jobId + " is STARTED, but is not part of the TelnetQueue. Will abort and rebuild the queue.");
						jobChangeInbox.deleteReadMessage();
						return true;
					} else {
						log.debug("Job " + jobId + " is STARTED, but since it is part of the TelnetQueue no special action is required.");
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

	private void runJobs(XAPS xaps) {
		while (true) {
			TelnetJob tj = tjq.getNextTelnetJob();
			if (tj == null) {
				return;
			}
			if (newJobStartedOrRunningJobStopped(tj.getJob()))
				return;

			synchronized (monitor) {
				while (activeTelnetSessions.size() >= Properties.getTelnetMaxClients()) {
					try {
						monitor.wait(10000);
					} catch (InterruptedException e) {
					}
				}
				TelnetJobThread tjt = new TelnetJobThread(monitor, tj, xaps, xapsCp);
				activeTelnetSessions.add(tj.getUnitId());
				Thread t = new Thread(tjt);
				t.start();
			}
		}
	}

	public void run() {
		Thread.currentThread().setName("TelnetProvisioning");
		
		Sleep sleep = new Sleep(10000, 10000, false);
		while (true) {
			try {
				sleep.sleep();
				if (Sleep.isTerminated())
					break;
				XAPS xaps = dbi.getXaps();
				populateJobUnitSetMapForAllJobs(xaps);
				runJobs(xaps);
			} catch (Throwable t) {
				log.fatal("An error ocurred in TelnetProvisioning.run() - continues anyway", t);
			}
		}
	}

	public static Set<String> getActiveTelnetSessions() {
		return activeTelnetSessions;
	}
}
