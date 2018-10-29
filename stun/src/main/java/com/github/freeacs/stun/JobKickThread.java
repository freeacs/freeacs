package com.github.freeacs.stun;

import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Inbox;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobStatus;
import com.github.freeacs.dbi.Message;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Parameter.Operator;
import com.github.freeacs.dbi.Parameter.ParameterDataType;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitJob;
import com.github.freeacs.dbi.UnitJobs;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.Unittypes;
import com.github.freeacs.dbi.util.SystemParameters;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobKickThread implements Runnable {
  private static Logger log = LoggerFactory.getLogger(JobKickThread.class);
  private final Properties properties;
  private DBI dbi;
  private DataSource xapsCp;
  /** Key: jobId Value: Set of unitId */
  private Map<Integer, Set<String>> jobKickMap;
  /** Key: jobId Value: Tms of last refresh */
  private Map<Integer, Long> jobRefreshMap = new HashMap<>();
  /** This inbox listens for changes on job (from other modules in xAPS). */
  private Inbox jobChangeInbox = new Inbox();

  public JobKickThread(DataSource xapsCp, DBI dbi, Properties properties) {
    this.xapsCp = xapsCp;
    this.dbi = dbi;
    this.properties = properties;
    jobChangeInbox.addFilter(new Message(null, Message.MTYPE_PUB_CHG, null, Message.OTYPE_JOB));
    dbi.registerInbox("jobChangeInbox", jobChangeInbox);
  }

  private void populateJobKickMapForOneJob(Job job, ACS acs, Unittype unittype)
      throws SQLException {
    Group group = job.getGroup();
    long now = jobRefreshMap.get(job.getId());
    ACSUnit acsUnit = new ACSUnit(xapsCp, acs, acs.getSyslog());
    Map<String, Unit> unitsInGroup = acsUnit.getUnits(group);
    log.info(
        "Found "
            + unitsInGroup.size()
            + " units in group "
            + group.getName()
            + " from job "
            + job.getName()
            + " ("
            + job.getId()
            + ")");
    Group topParent = group.getTopParent();
    Profile profile = topParent.getProfile();

    UnittypeParameter historyUtp =
        unittype.getUnittypeParameters().getByName(SystemParameters.JOB_HISTORY);
    Parameter historyParam =
        new Parameter(historyUtp, "%," + job.getId() + ":%", Operator.EQ, ParameterDataType.TEXT);
    Map<String, Unit> unitsCompleted =
        acsUnit.getUnits(unittype, profile, historyParam, Integer.MAX_VALUE);
    Iterator<Unit> iterator = unitsCompleted.values().iterator();
    int runButRunAgainCounter = 0;
    if (job.getRepeatCount() != null
        && job.getRepeatCount() > 0
        && job.getRepeatInterval() != null) {
      while (iterator.hasNext()) {
        Unit u = iterator.next();
        UnitParameter up = u.getUnitParameters().get(historyUtp.getName());
        String historyParameterValue = up.getValue();
        for (String entry : historyParameterValue.split(",")) {
          if ("".equals(entry.trim())) {
            continue;
          }
          JobHistoryEntry jhEntry = new JobHistoryEntry(entry);
          if (jhEntry.getJobId().intValue() != job.getId()
              || jhEntry.getRepeatedCount() >= job.getRepeatCount()) {
            continue;
          }
          long timeSinceLastRun = now - jhEntry.getLastRunTms();
          if (timeSinceLastRun <= job.getRepeatInterval() * 1000) {
            continue;
          }
          // The job was run before, but it should now be run again, hence: not completed
          iterator.remove();
          runButRunAgainCounter++;
        }
      }
    }
    String msg =
        "Found "
            + unitsCompleted.size()
            + " units in group "
            + group.getName()
            + " from job "
            + job.getName()
            + " ("
            + job.getId()
            + ") already completed.";
    if (runButRunAgainCounter > 0) {
      msg += " (" + runButRunAgainCounter + " have run before, but will be repeated)";
    }
    log.info(msg);

    List<Parameter> upList = new ArrayList<>();
    UnittypeParameter currentUtp =
        unittype.getUnittypeParameters().getByName(SystemParameters.JOB_CURRENT);
    Parameter currentParam = new Parameter(currentUtp, "%," + job.getId() + ":%");
    upList.add(currentParam);
    Map<String, Unit> unitsInProcess =
        acsUnit.getUnits(unittype, profile, upList, Integer.MAX_VALUE);
    log.info(
        "Found "
            + unitsInProcess.size()
            + " units in group "
            + group.getName()
            + " from job "
            + job.getName()
            + " ("
            + job.getId()
            + ") in process.");

    Set<String> unitSet = new HashSet<>();
    for (String unitId : unitsInGroup.keySet()) {
      if (unitsCompleted.get(unitId) == null && unitsInProcess.get(unitId) == null) {
        unitSet.add(unitId);
        log.debug("Added  " + unitId + " to list of units to run a telnet'ed");
      }
    }
    jobKickMap.put(job.getId(), unitSet);
  }

  private void populateJobKickMapForAllJobs(ACS acs) throws SQLException {
    jobKickMap = new HashMap<>();
    Unittype[] unittypes = acs.getUnittypes().getUnittypes();
    for (Unittype unittype : unittypes) {
      if (ProvisioningProtocol.TR069 != unittype.getProtocol()) {
        continue;
      }
      Job[] jobs = unittype.getJobs().getJobs();
      for (Job job : jobs) {
        if ("KICK".equals(job.getFlags().getType().name())) {
          if (JobStatus.STARTED.equals(job.getStatus())) {
            if (jobKickMap.get(job.getId()) == null) {
              log.info(
                  "Job "
                      + job.getName()
                      + " ("
                      + job.getId()
                      + ") is STARTED and discovered for the first time.");
              jobKickMap.put(job.getId(), new HashSet<>());
              jobRefreshMap.put(job.getId(), System.currentTimeMillis());
              populateJobKickMapForOneJob(job, acs, unittype);
            } else {
              long lastRefresh = jobRefreshMap.get(job.getId());
              if (lastRefresh + properties.getKickRescan() * 60000 < System.currentTimeMillis()) {
                log.info("Job " + job.getId() + " is STARTED and refreshed.");
                jobRefreshMap.put(job.getId(), System.currentTimeMillis());
                populateJobKickMapForOneJob(job, acs, unittype);
              }
            }
          } else if (jobKickMap.get(job.getId()) != null) {
            log.info(
                "Job "
                    + job.getName()
                    + " ("
                    + job.getId()
                    + ") is not STARTED and no more units will be kicked from this job");
            jobKickMap.remove(job.getId());
          }
        }
      }
    }
  }

  private Job findJobById(Integer jobId) {
    for (Unittype unittype : dbi.getAcs().getUnittypes().getUnittypes()) {
      Job job = unittype.getJobs().getById(jobId);
      if (job != null) {
        return job;
      }
    }
    return null;
  }

  private void kickJobs(ACS acs) {
    int kickInterval = properties.getKickInterval();
    Sleep kickSleep = new Sleep(kickInterval, kickInterval / 10, false);
    for (Map.Entry<Integer, Set<String>> entry : jobKickMap.entrySet()) {
      Integer jobId = entry.getKey();
      Job job = findJobById(jobId);
      Set<String> unitIdSet = entry.getValue();
      Iterator<String> iterator = unitIdSet.iterator();
      while (iterator.hasNext()) {
        String unitId = iterator.next();
        try {
          if (newJobStartedOrRunningJobStopped(job)) {
            return;
          }
          kickSleep.sleep();
          ACSUnit acsUnit = new ACSUnit(xapsCp, acs, acs.getSyslog());
          Unit unit = acsUnit.getUnitById(unitId);
          if (unit != null) {
            startUnitJob(unit, jobId, acs);
            Kick.kick(unit, properties);
          } else {
            log.error(unitId + " was not found in xAPS, not possible to kick");
          }
          iterator.remove();
        } catch (Throwable t) {
          log.error(unitId + " experienced an error during kick, will be tried again later " + t);
        }
      }
    }
  }

  public void run() {
    try {
      Sleep sleep = new Sleep(1000, 1000, true);
      do {
        sleep.sleep();
        if (Sleep.isTerminated()) {
          break;
        }
        ACS acs = dbi.getAcs();
        populateJobKickMapForAllJobs(acs);
        kickJobs(acs);
      } while (true);
    } catch (Throwable t) {
      log.error(
          "An error ocurred, JobKickSpawner exits - server is not able to process job-kick anymore!!!",
          t);
    }
  }

  private void startUnitJob(Unit u, Integer jobId, ACS acs) throws SQLException {
    ACSUnit acsUnit = new ACSUnit(xapsCp, acs, acs.getSyslog());
    Unittype unittype = u.getUnittype();
    UnittypeParameter currentUtp =
        unittype.getUnittypeParameters().getByName(SystemParameters.JOB_CURRENT);
    UnitParameter currentUp =
        new UnitParameter(currentUtp, u.getId(), String.valueOf(jobId), u.getProfile());
    List<UnitParameter> upList = new ArrayList<>();
    upList.add(currentUp);
    acsUnit.addOrChangeUnitParameters(upList, u.getProfile());
    UnitJobs unitJobs = new UnitJobs(xapsCp);
    UnitJob uj = new UnitJob(u.getId(), jobId);
    uj.setStartTimestamp(new Date());
    boolean updated = unitJobs.start(uj);
    log.debug(
        u.getId()
            + " was marked as started in unit-job table (result from insert: "
            + updated
            + ")");
  }

  private boolean newJobStartedOrRunningJobStopped(Job j) {
    if (!JobStatus.STARTED.equals(j.getStatus())) {
      log.info("Job " + j.getId() + " is no longer running, aborting this job");
      return true;
    }
    List<Message> messages = jobChangeInbox.getUnreadMessages();
    for (Message jcMessage : messages) {
      Inbox dbiInbox = dbi.retrieveInbox(DBI.PUBLISH_INBOX_NAME);
      List<Message> dbiMessages = dbiInbox.getAllMessages();
      String jcM = jcMessage.getMessageType() + jcMessage.getObjectType() + jcMessage.getObjectId();
      log.debug("Job change inbox reported that job " + jcMessage.getObjectId() + " has changed");
      boolean messageProcessed;
      do {
        messageProcessed = true;
        for (Message dbiMessage : dbiMessages) {
          String dbiM =
              dbiMessage.getMessageType() + dbiMessage.getObjectType() + dbiMessage.getObjectId();
          if (jcM.equals(dbiM)) {
            log.debug(
                "DBI has not yet processed the change message for job "
                    + dbiMessage.getObjectId()
                    + ", waiting 500 ms");
            messageProcessed = false;
            break;
          }
        }
        if (messageProcessed) {
          break;
        }
        try {
          Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
      } while (true);
      log.debug("DBI has processed the change message for job " + jcMessage.getObjectId());
      jobChangeInbox.markMessageAsRead(jcMessage);
      Unittypes unittypes = dbi.getAcs().getUnittypes();
      for (Unittype unittype : unittypes.getUnittypes()) {
        Integer jobId = Integer.valueOf(jcMessage.getObjectId());
        Job job = unittype.getJobs().getById(jobId);
        if (job != null && JobStatus.STARTED.equals(job.getStatus())) {
          if (jobKickMap.get(jobId) == null) {
            log.info(
                "Job "
                    + jobId
                    + " is STARTED, but is not part of the JobKickMap. Will abort and rebuild the queue.");
            jobChangeInbox.deleteReadMessage();
            return true;
          } else {
            log.debug(
                "Job "
                    + jobId
                    + " is STARTED, but since it is part of the JobKickMap no special action is required.");
          }
        } else if (job != null) {
          log.debug(
              "Job "
                  + jobId
                  + " is changed, but since the  status is not STARTED no special action is required.");
        }
      }
    }
    if (!messages.isEmpty()) {
      jobChangeInbox.deleteReadMessage();
    }
    return false;
  }
}
