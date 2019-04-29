package com.github.freeacs.dbi;

import com.github.freeacs.common.util.Sleep;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Summary
 *
 * <p>This class will act as a perfect cache for a lot of the objects found in this package. The
 * properties of a perfect cache is:
 *
 * <p>- fast access - updated data
 *
 * <p>This last requirement is only possible since DBI will build upon a table called 'message',
 * which will convey messages from ACS applications and inform all other ACS applications about
 * changes to the tables/data.
 *
 * <p>Details
 *
 * <p>The main idea of this class is to be both a listener (subscriber) and a broadcaster
 * (publisher) of changes to the various objects (data) found in the ACS database tables. To do the
 * "listening" we need one thread which constantly polls on a special table called "message". By
 * polling every second we would quickly discover changes made to the various objects in DBI. If
 * user of DBI changes some data in an object (table), the user should in general notify DBI of such
 * a change, to allow DBI to send this information to the message table.
 *
 * <p>These objects/tables are under DBI caching mechanism:
 *
 * <p>Objects Tables ------------------------------------------------------ DBI message ACS
 * filestore (firmware) group_ group_param profile profile_param syslog_event unit_type
 * unit_type_param unit_type_param_value job job_param (only parameters for unitid =
 * ANY_UNIT_IN_GROUP)
 *
 * <p>These objects/tables are *not* part of DBI caching mechanism, and should never be expected to
 * be updated on application level unless explicitly updated.
 *
 * <p>Objects Tables ------------------------------------------------------ Syslog syslog UnitJobs
 * unit_job Users user_ permission_ ACSJobs job_param (all parameters for unitid !=
 * ANY_UNIT_IN_GROUP) ACSUnit unit unit_param
 *
 * <p>Messages that are sent/received must contain this information:
 *
 * <p>Sender Receiver Type Object-type Object-id Timestamp Content
 *
 * @author Morten
 */
public class DBI implements Runnable {
  private final Thread thread;

  private boolean running = true;

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
    this.finished = !running;
    if (!running) {
      try {
        thread.interrupt();
      } catch (Throwable e) {
      }
      try {
        thread.stop();
      } catch (Throwable e) {
      }
    }
  }

  public class PublishType {
    private Set<String> messageTypes = new TreeSet<>();

    public Set<String> getMessageTypes() {
      return messageTypes;
    }

    public int size() {
      return messageTypes.size();
    }

    public void addMessageType(String messageType) {
      messageTypes.add(messageType);
    }
  }

  public class UnittypePublish {
    private Map<Integer, PublishType> groups = new HashMap<>();
    private Map<Integer, PublishType> jobs = new HashMap<>();
    private Map<Integer, PublishType> profiles = new HashMap<>();
    private PublishType publish = new PublishType();
    private Unittype unittype;

    public UnittypePublish(Unittype unittype) {
      this.unittype = unittype;
    }

    public void addPublish(String messageType, String objectType, Integer objectId) {
      if (Message.OTYPE_UNIT_TYPE.equals(objectType)) {
        publish.addMessageType(messageType);
      } else {
        Map<Integer, PublishType> map = profiles;
        if (Message.OTYPE_JOB.equals(objectType)) {
          map = jobs;
        }
        if (Message.OTYPE_GROUP.equals(objectType)) {
          map = groups;
        }
        PublishType pt = map.get(objectId);
        if (pt == null) {
          pt = new PublishType();
          map.put(objectId, pt);
        }
        pt.addMessageType(messageType);
      }
    }

    /**
     * Decide whether or not to replace/override all the profile/group publish-messages with one
     * PUBLISH-CHG on unittype level.
     *
     * <p>For now we decide that if the number of publish-messages in total on profiles, groups and
     * jobs are higher than 10, a unittype publish will be issued
     */
    public void addUnittypePublish() {
      int counter = 0;
      for (PublishType pt : profiles.values()) {
        counter += pt.size();
      }
      for (PublishType pt : groups.values()) {
        counter += pt.size();
      }
      for (PublishType pt : jobs.values()) {
        counter += pt.size();
      }
      if (counter > 10) {
        publish.addMessageType(Message.MTYPE_PUB_CHG);
      }
    }

    public List<Message> getMessages(int facility) {
      List<Message> list = new ArrayList<>();
      if (!publish.getMessageTypes().isEmpty()) {
        for (String messageType : publish.getMessageTypes()) {
          Message m = new Message();
          m.setMessageType(messageType);
          m.setObjectId(String.valueOf(unittype.getId()));
          m.setObjectType(Message.OTYPE_UNIT_TYPE);
          m.setSender(facility);
          m.setTimestamp(new Date());
          list.add(m);
        }
      } else {
        for (Entry<Integer, PublishType> entry : profiles.entrySet()) {
          for (String messageType : entry.getValue().getMessageTypes()) {
            Message m = new Message();
            m.setMessageType(messageType);
            m.setObjectId(String.valueOf(entry.getKey()));
            m.setObjectType(Message.OTYPE_PROFILE);
            m.setSender(facility);
            m.setTimestamp(new Date());
            list.add(m);
          }
        }
        for (Entry<Integer, PublishType> entry : groups.entrySet()) {
          for (String messageType : entry.getValue().getMessageTypes()) {
            Message m = new Message();
            m.setMessageType(messageType);
            m.setObjectId(String.valueOf(entry.getKey()));
            m.setObjectType(Message.OTYPE_GROUP);
            m.setSender(facility);
            m.setTimestamp(new Date());
            list.add(m);
          }
        }
      }
      for (Entry<Integer, PublishType> entry : jobs.entrySet()) {
        for (String messageType : entry.getValue().getMessageTypes()) {
          Message m = new Message();
          m.setMessageType(messageType);
          m.setObjectId(String.valueOf(entry.getKey()));
          m.setObjectType(Message.OTYPE_JOB);
          m.setSender(facility);
          m.setTimestamp(new Date());
          list.add(m);
        }
      }
      return list;
    }
  }

  private static Logger logger = LoggerFactory.getLogger(DBI.class);
  public static String PUBLISH_INBOX_NAME = "publishACSInbox";

  private DataSource dataSource;
  private int lifetimeSec;
  private long start = System.currentTimeMillis();
  private boolean finished;
  private Sleep sleep;
  private ACS acs;
  private boolean freeacsUpdated;
  private Map<Integer, UnittypePublish> publishUnittypes = new HashMap<>();
  private List<Message> outbox = new ArrayList<>();
  private Set<Integer> sent = new TreeSet<>();
  private Syslog syslog;
  private Random random = new Random(System.nanoTime());
  private int dbiId;
  private boolean dbiRun;
  private Map<String, Inbox> inboxes = new HashMap<>();
  private int lastReadId = -1;
  private Inbox publishInbox = new Inbox();
  /** Set if an error occurs within DBI, used to signal ERROR in monitor. */
  private Throwable dbiThrowable;

  public DBI(int lifetimeSec, DataSource dataSource, Syslog syslog) throws SQLException {
    this.dataSource = dataSource;
    this.lifetimeSec = lifetimeSec;
    this.syslog = syslog;
    populateInboxes();
    publishInbox.deleteReadMessage();
    this.sleep = new Sleep(1000, 1000, true);
    this.acs = new ACS(dataSource, syslog);
    this.dbiId = random.nextInt(1000000);
    acs.setDbi(this);
    publishInbox.addFilter(new Message(null, Message.MTYPE_PUB_ADD, null, null));
    publishInbox.addFilter(new Message(null, Message.MTYPE_PUB_CHG, null, null));
    publishInbox.addFilter(new Message(null, Message.MTYPE_PUB_DEL, null, null));
    registerInbox(PUBLISH_INBOX_NAME, publishInbox);
    thread = new Thread(this);
    if (syslog != null) {
      thread.setName("DBI for " + syslog.getIdentity().getFacilityName());
    } else {
      thread.setName("DBI");
    }
    thread.setDaemon(true);
    thread.start();
    logger.debug("DBI is loaded for user " + syslog.getIdentity().getUser().getFullname());
  }

  public ACS getAcs() {
    if (finished) {
      throw new RuntimeException("DBI does not run anymore since it passed it's lifetime timeout");
    }
    return acs;
  }

  public void registerInbox(String key, Inbox inbox) {
    inboxes.put(key, inbox);
  }

  public Inbox retrieveInbox(String key) {
    return inboxes.get(key);
  }

  private void populateInboxes() throws SQLException {
      try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
          ResultSet rs = s.executeQuery("SELECT * FROM message WHERE id > " + lastReadId + " ORDER BY id");
          while (rs.next()) {
              Message message = new Message();
              message.setId(rs.getInt("id"));
              message.setContent(rs.getString("content"));
              message.setMessageType(rs.getString("type"));
              message.setObjectId(rs.getString("object_id"));
              message.setObjectType(rs.getString("object_type"));
              String receiverStr = rs.getString("receiver");
              if (receiverStr != null) {
                  message.setReceiver(Integer.valueOf(receiverStr));
              }
              message.setSender(rs.getInt("sender"));
              message.setTimestamp(rs.getTimestamp("timestamp_"));
              int colonPos = message.getObjectId().indexOf(':');
              if (colonPos > -1) {
                  String sendersDbiId = message.getObjectId().substring(0, colonPos);
                  if (sendersDbiId.equals(String.valueOf(dbiId))) {
                      continue;
                  }
                  message.setObjectId(message.getObjectId().substring(colonPos + 1));
              }
              if (sent.contains(message.getId())) {
                  continue;
              }

              if (message.getId() > lastReadId) {
                  lastReadId = message.getId();
              }
              for (Inbox ibx : inboxes.values()) {
                  ibx.addToInbox(message);
              }
          }
      }
  }

  private void cleanup() throws SQLException {
    Connection c = dataSource.getConnection();
    PreparedStatement ps = null;
    try {
      String sql = "DELETE FROM message WHERE timestamp_ < ?";
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments(sql, new Timestamp(System.currentTimeMillis() - 60000));
      ps = ds.makePreparedStatement(c);
      int rowsDeleted = ps.executeUpdate();
      if (logger.isDebugEnabled() && rowsDeleted > 0) {
        logger.debug(rowsDeleted + " messages was deleted from message table");
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  private void send(Message message) throws SQLException {
    Connection c = dataSource.getConnection();
    PreparedStatement ps = null;
    try {
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("INSERT INTO message (sender, ", message.getSender());
      if (message.getReceiver() != null) {
        ds.addSqlAndArguments("receiver, ", message.getReceiver());
      }
      if (message.getContent() != null) {
        ds.addSqlAndArguments("content, ", message.getContent());
      }
      ds.addSqlAndArguments(
          "type, object_type, object_id, timestamp_) ",
          message.getMessageType(),
          message.getObjectType(),
          message.getObjectId(),
          message.getTimestamp());
      ds.addSql("VALUES (" + ds.getQuestionMarks() + ")");
      ps = ds.makePreparedStatement(c, "id");
      ps.executeUpdate();
      ResultSet gk = ps.getGeneratedKeys();
      if (gk.next()) {
        Integer id = gk.getInt(1);
        sent.add(id);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Message: [" + message + "] sent/inserted");
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  public void run() {
    boolean errorOccured = false;
    while (running && !finished) {
      dbiRun = true;
      try {
        if (System.currentTimeMillis() - start > lifetimeSec * 1000L) {
          finished = true;
          break;
        }
        if (Sleep.isTerminated()) {
          break;
        }
        sleep.sleep();
        // Check message table for changes (read all with id > last_id_read)
        populateInboxes();
        // Update objects if necessary
        processPublishInbox();
        // Check outbox - may add/remove messages - then send them
        processOutbox();
        // Once in a while - clean out old messages
        if (random.nextInt(10) == 0) {
          cleanup();
        }
        dbiThrowable = null;
        errorOccured = false;
      } catch (Throwable t) {
        dbiThrowable = t;
        try {
          if (!errorOccured) {
            errorOccured = true;
            logger.error(
                "An error occurred in DBI.run(): %s, %s",
                t.getCause().toString(), t.getLocalizedMessage());
          } else {
            logger.error(
                "An error occurred in DBI.run() (stacktrace printed earlier): " + t.getMessage(), dbiThrowable);
          }
        } catch (Throwable ignored) {
        }
      }
    }
  }

  /**
   * Must be called if an application is about to be terminated, otherwise it will be run every
   * second.
   */
  public synchronized void processOutbox() throws SQLException {
    for (Entry<Integer, UnittypePublish> entry : publishUnittypes.entrySet()) {
      UnittypePublish up = entry.getValue();
      up.addUnittypePublish();
      List<Message> messages = up.getMessages(syslog.getIdentity().getFacility());
      outbox.addAll(messages);
    }
    for (Message message : outbox) {
      message.setObjectId(dbiId + ":" + message.getObjectId());
      send(message);
    }
    // Clean up the sent-box
    Iterator<Integer> iterator = sent.iterator();
    while (iterator.hasNext()) {
      Integer sentId = iterator.next();
      if (sentId <= lastReadId) {
        iterator.remove();
      } else {
        break;
      }
    }
    outbox.clear();
    publishUnittypes.clear();
  }

  /**
   * The string format is like this: <unittype-id>(,<id>=<count>)+ These ids are allowed: cnf:
   * completed-no-failures chf: completed-had-failures cf: confirmed-failed uf: unconfirmed-failed
   */
  private void updateJobCounters(Integer jobId, String message) {
    String[] msgArr = message.split(",");
    Unittype unittype = acs.getUnittype(Integer.valueOf(msgArr[0]));
    if (unittype == null) {
      return;
    } // the user does not have access to this unittype
    Job job = unittype.getJobs().getById(jobId);
    if (job == null) {
      return;
    } // the user does not have access to this job
    for (int i = 1; i < msgArr.length; i++) {
      String id = msgArr[i].split("=")[0];
      int counter = Integer.parseInt(msgArr[i].split("=")[1]);
      switch (id) {
        case "cnf":
          job.setCompletedNoFailures(counter);
          break;
        case "chf":
          job.setCompletedHadFailures(counter);
          break;
        case "cf":
          job.setConfirmedFailed(counter);
          break;
        case "uf":
          job.setUnconfirmedFailed(counter);
          break;
      }
    }
  }

  private void processPublishInbox() throws SQLException {
    boolean updateACS = false;
    for (Message m : publishInbox.getUnreadMessages()) {
      publishInbox.markMessageAsRead(m);
      if (logger.isDebugEnabled()) {
        logger.debug(
            "DBI discovered that "
                + m.getObjectType()
                + " with id = "
                + m.getObjectId()
                + " has changed ("
                + m.getMessageType()
                + ") (sent from "
                + SyslogConstants.getFacilityName(m.getSender())
                + ")");
      }
      if (!updateACS) {
        if (Message.OTYPE_JOB.equals(m.getObjectType())
            && m.getContent() != null
            && m.getSender() == SyslogConstants.FACILITY_CORE) {
          updateJobCounters(Integer.valueOf(m.getObjectId()), m.getContent());
        } else if (Message.OTYPE_JOB.equals(m.getObjectType())
            && Message.MTYPE_PUB_CHG.equals(m.getMessageType())) {
          Jobs.refreshJob(Integer.valueOf(m.getObjectId()), acs);
        } else if (Message.OTYPE_GROUP.equals(m.getObjectType())
            && Message.MTYPE_PUB_CHG.equals(m.getMessageType())) {
          Groups.refreshGroup(Integer.valueOf(m.getObjectId()), acs);
        } else if (Message.OTYPE_FILE.equals(m.getObjectType())
            && Message.MTYPE_PUB_CHG.equals(m.getMessageType())) {
          Files.refreshFile(Integer.valueOf(m.getObjectId()), Integer.valueOf(m.getContent()), acs);
        } else {
          updateACS = true;
        }
      }
    }
    if (updateACS) {
      acs.read();
      freeacsUpdated = true;
      if (logger.isDebugEnabled()) {
        logger.debug("ACS object has been updated due to changes in the tables");
      }
    }
    publishInbox.deleteReadMessage();
  }

  public boolean isACSUpdated() {
    if (freeacsUpdated) {
      freeacsUpdated = false;
      return true;
    }
    return false;
  }

  public synchronized void publishDelete(Object object, Unittype unittype) {
    addToPublish(Message.MTYPE_PUB_DEL, object, unittype);
  }

  public synchronized void publishChange(Object object, Unittype unittype) {
    addToPublish(Message.MTYPE_PUB_CHG, object, unittype);
  }

  public synchronized void publishAdd(Object object, Unittype unittype) {
    addToPublish(Message.MTYPE_PUB_ADD, object, unittype);
  }

  public synchronized void publishFile(File file, Unittype unittype) {
    addMessage(
        String.valueOf(unittype.getId()),
        Message.MTYPE_PUB_CHG,
        Message.OTYPE_FILE,
        String.valueOf(file.getId()),
        null);
  }

  public synchronized void publishJobCounters(Integer jobId, String counters) {
    addMessage(counters, Message.MTYPE_PUB_CHG, Message.OTYPE_JOB, String.valueOf(jobId), null);
  }

  public synchronized void publishKick(Unit u, int receiver) {
    addMessage(null, Message.MTYPE_PUB_IM, Message.OTYPE_UNIT, u.getId(), receiver);
  }

  public synchronized void publishTriggerReleased(Trigger trigger, int receiver) {
    addMessage(
        String.valueOf(trigger.getId()),
        Message.MTYPE_PUB_TRG_REL,
        Message.OTYPE_UNIT_TYPE,
        String.valueOf(trigger.getUnittype().getId()),
        receiver);
  }

  private void addToPublish(String messageType, Object object, Unittype unittype) {
    UnittypePublish up = publishUnittypes.get(unittype.getId());
    if (up == null) {
      up = new UnittypePublish(unittype);
      publishUnittypes.put(unittype.getId(), up);
    }
    if (object instanceof Unittype) {
      up.addPublish(messageType, Message.OTYPE_UNIT_TYPE, unittype.getId());
    } else if (object instanceof File
        || object instanceof SyslogEvent
        || object instanceof UnittypeParameter
        || object instanceof Trigger) {
      up.addPublish(Message.MTYPE_PUB_CHG, Message.OTYPE_UNIT_TYPE, unittype.getId());
    } else if (object instanceof Profile) {
      Profile profile = (Profile) object;
      up.addPublish(messageType, Message.OTYPE_PROFILE, profile.getId());
    } else if (object instanceof ProfileParameter) {
      ProfileParameter pp = (ProfileParameter) object;
      Profile profile = pp.getProfile();
      up.addPublish(Message.MTYPE_PUB_CHG, Message.OTYPE_PROFILE, profile.getId());
    } else if (object instanceof Group) {
      Group group = (Group) object;
      up.addPublish(messageType, Message.OTYPE_GROUP, group.getId());
    } else if (object instanceof GroupParameter) {
      GroupParameter gp = (GroupParameter) object;
      Group group = gp.getGroup();
      up.addPublish(Message.MTYPE_PUB_CHG, Message.OTYPE_GROUP, group.getId());
    } else if (object instanceof Job) {
      Job job = (Job) object;
      up.addPublish(messageType, Message.OTYPE_JOB, job.getId());
    } else if (object instanceof JobParameter) {
      JobParameter jp = (JobParameter) object;
      Job job = jp.getJob();
      up.addPublish(Message.MTYPE_PUB_CHG, Message.OTYPE_JOB, job.getId());
    }
  }

  private void addMessage(
      String content, String messageType, String objectType, String objectId, Integer receiver) {
    if (dbiRun) {
      Message message = new Message();
      message.setContent(content);
      message.setMessageType(messageType);
      message.setObjectType(objectType);
      message.setObjectId(objectId);
      message.setReceiver(receiver);
      message.setSender(syslog.getIdentity().getFacility());
      message.setTimestamp(new Date());
      outbox.add(message);
    }
  }

  public boolean isFinished() {
    return finished;
  }

  public void setLifetimeSec(int lifetimeSec) {
    this.lifetimeSec = lifetimeSec;
  }

  public Throwable getDbiThrowable() {
    return dbiThrowable;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public Syslog getSyslog() {
    return syslog;
  }

  public ACSUnit getACSUnit() throws SQLException {
    return new ACSUnit(dataSource, acs, syslog);
  }
}
