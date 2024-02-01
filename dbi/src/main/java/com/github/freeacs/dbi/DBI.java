package com.github.freeacs.dbi;

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
      } catch (Throwable ignored) {
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
  private ACS acs;
  private boolean freeacsUpdated;
  private final Map<Integer, UnittypePublish> publishUnittypes = new HashMap<>();
  private final List<Message> outbox = new ArrayList<>();
  private final Set<Integer> sent = new TreeSet<>();
  private final Syslog syslog;
  private final Random random = new Random(System.nanoTime());
  private final int dbiId;
  private final Map<String, Inbox> inboxes = new HashMap<>();
  private int lastReadId = -1;
  private final Inbox publishInbox = new Inbox();

  private DBI(int lifetimeSec, DataSource dataSource, Syslog syslog) throws SQLException {
    this.dataSource = dataSource;
    this.lifetimeSec = lifetimeSec;
    this.syslog = syslog;
    this.acs = new ACS(dataSource, syslog);
    this.dbiId = new Random().nextInt(1000000);
    acs.setDbi(this);
    this.thread = createDBIThread();
  }

  public static DBI createAndInitialize(int lifetimeSec, DataSource dataSource, Syslog syslog) throws SQLException {
    DBI dbi = new DBI(lifetimeSec, dataSource, syslog);
    dbi.initialize();
    return dbi;
  }

  private void initialize() throws SQLException {
    processMessagesFromDatabase();
    setupPublishInbox();
    thread.start();
    logger.debug("DBI is loaded for user " + (syslog != null ? syslog.getIdentity().getUser().getFullname() : "unknown"));
  }

  private Thread createDBIThread() {
    Thread thread = new Thread(this);
    String threadName = (syslog != null) ? "DBI for " + syslog.getIdentity().getFacilityName() : "DBI";
    thread.setName(threadName);
    thread.setDaemon(true);
    return thread;
  }

  private void setupPublishInbox() {
    publishInbox.deleteReadMessage();
    publishInbox.addFilter(new Message(null, Message.MTYPE_PUB_ADD, null, null));
    publishInbox.addFilter(new Message(null, Message.MTYPE_PUB_CHG, null, null));
    publishInbox.addFilter(new Message(null, Message.MTYPE_PUB_DEL, null, null));
    registerInbox(PUBLISH_INBOX_NAME, publishInbox);
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

  private void processMessagesFromDatabase() throws SQLException {
    try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
      ResultSet rs = s.executeQuery("SELECT * FROM message WHERE id > " + lastReadId + " ORDER BY id");
      while (rs.next()) {
        Message message = createMessageFromResultSet(rs);
        if (shouldSkipMessage(message)) {
          continue;
        }
        updateLastReadId(message);
        distributeMessageToInboxes(message);
      }
    }
  }

  private Message createMessageFromResultSet(ResultSet rs) throws SQLException {
    Message message = new Message();
    message.setId(rs.getInt("id"));
    message.setContent(rs.getString("content"));
    message.setMessageType(rs.getString("type"));
    message.setObjectId(rs.getString("object_id"));
    message.setObjectType(rs.getString("object_type"));
    message.setReceiver(parseReceiver(rs.getString("receiver")));
    message.setSender(rs.getInt("sender"));
    message.setTimestamp(rs.getTimestamp("timestamp_"));
    return message;
  }

  private boolean shouldSkipMessage(Message message) {
    if (isMessageFromSelf(message) || sent.contains(message.getId())) {
      return true;
    }
    return false;
  }

  private boolean isMessageFromSelf(Message message) {
    int colonPos = message.getObjectId().indexOf(':');
    if (colonPos > -1) {
      String sendersDbiId = message.getObjectId().substring(0, colonPos);
      if (sendersDbiId.equals(String.valueOf(dbiId))) {
        message.setObjectId(message.getObjectId().substring(colonPos + 1));
        return true;
      }
    }
    return false;
  }

  private Integer parseReceiver(String receiverStr) {
    return receiverStr != null ? Integer.valueOf(receiverStr) : null;
  }

  private void updateLastReadId(Message message) {
    if (message.getId() > lastReadId) {
      lastReadId = message.getId();
    }
  }

  private void distributeMessageToInboxes(Message message) {
    for (Inbox ibx : inboxes.values()) {
      ibx.addToInbox(message);
    }
  }

  private void cleanupOldMessages() throws SQLException {
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

  private void sendMessage(Message message) throws SQLException {
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
    try {
      while (!Thread.currentThread().isInterrupted() && running) {
        performDBIOperations();
        Thread.sleep(1000); // Replace with appropriate sleep time
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Preserve interrupt status
      logger.info("DBI thread interrupted");
    } catch (Exception e) {
      logger.error("Error in DBI thread: " + e.getMessage(), e);
    }
  }

  private void performDBIOperations() {
    try {
      // Check if the DBI lifetime has expired
      checkLifetime();

      // Read and process messages from the database
      processMessagesFromDatabase();

      // Update objects if necessary based on new messages
      updateObjectsBasedOnMessages();

      // Process the outbox: send messages, handle responses, etc.
      processOutboxMessages();

      // Periodically clean up old messages from the database
      if (shouldCleanup()) {
        cleanupOldMessages();
      }

    } catch (SQLException e) {
      logger.error("SQL error in DBI operations: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Unexpected error in DBI operations: " + e.getMessage(), e);
    }
  }

  private void checkLifetime() {
    if (System.currentTimeMillis() - start > lifetimeSec * 1000L) {
      running = false;
    }
  }

  private boolean shouldCleanup() {
    // Determine if it's time to clean up old messages
    // For example, this could be a random or time-based check
    return random.nextInt(10) == 0;
  }

  /**
   * Must be called if an application is about to be terminated, otherwise it will be run every
   * second.
   */
  public synchronized void processOutboxMessages() throws SQLException {
    prepareAndAddOutboxMessages();
    sendOutboxMessages();
    cleanupSentMessages();
    clearOutboxes();
  }

  private void prepareAndAddOutboxMessages() {
    for (Entry<Integer, UnittypePublish> entry : publishUnittypes.entrySet()) {
      UnittypePublish up = entry.getValue();
      up.addUnittypePublish();
      List<Message> messages = up.getMessages(syslog.getIdentity().getFacility());
      outbox.addAll(messages);
    }
  }

  private void sendOutboxMessages() throws SQLException {
    for (Message message : outbox) {
      message.setObjectId(dbiId + ":" + message.getObjectId());
      sendMessage(message);
    }
  }

  private void cleanupSentMessages() {
    Iterator<Integer> iterator = sent.iterator();
    while (iterator.hasNext()) {
      Integer sentId = iterator.next();
      if (sentId <= lastReadId) {
        iterator.remove();
      } else {
        break;
      }
    }
  }

  private void clearOutboxes() {
    outbox.clear();
    publishUnittypes.clear();
  }

  /**
   * The string format is like this: <unittype-id>(,<id>=<count>)+ These ids are allowed: cnf:
   * completed-no-failures chf: completed-had-failures cf: confirmed-failed uf: unconfirmed-failed
   */
  private void updateJobCounters(Integer jobId, String message) {
    String[] msgArr = message.split(",");
    if (msgArr.length < 2) {
      // Log error or throw an exception due to incorrect message format
      return;
    }

    Unittype unittype = acs.getUnittype(Integer.valueOf(msgArr[0]));
    if (unittype == null) {
      // Log error or handle the case where the unittype is not accessible
      return;
    }

    Job job = unittype.getJobs().getById(jobId);
    if (job == null) {
      // Log error or handle the case where the job is not accessible
      return;
    }

    for (int i = 1; i < msgArr.length; i++) {
      updateCounter(job, msgArr[i]);
    }
  }

  private void updateCounter(Job job, String counterData) {
    String[] parts = counterData.split("=");
    if (parts.length != 2) {
      // Log error or throw an exception due to incorrect counter data format
      return;
    }

    String id = parts[0];
    int counter;
    try {
      counter = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      // Log error or handle the parsing error
      return;
    }

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
      default:
        // Log error or handle unknown id
        break;
    }
  }

  private void updateObjectsBasedOnMessages() throws SQLException {
    boolean updateACS = false;
    for (Message m : publishInbox.getUnreadMessages()) {
      processMessage(m);
      updateACS = updateACS || needsAcsUpdate(m);
    }
    if (updateACS) {
      performAcsUpdate();
    }
    publishInbox.deleteReadMessage();
  }

  private void processMessage(Message m) throws SQLException {
    publishInbox.markMessageAsRead(m);
    logMessageDetails(m);
    handleObjectTypeSpecificUpdates(m);
  }

  private void logMessageDetails(Message m) {
    if (logger.isDebugEnabled()) {
      logger.debug("DBI discovered that " + m.getObjectType() + " with id = " + m.getObjectId() +
              " has changed (" + m.getMessageType() + ") (sent from " +
              SyslogConstants.getFacilityName(m.getSender()) + ")");
    }
  }

  private void handleObjectTypeSpecificUpdates(Message m) throws SQLException {
    if (Message.OTYPE_JOB.equals(m.getObjectType())) {
      handleJobTypeMessage(m);
    } else if (Message.OTYPE_GROUP.equals(m.getObjectType())) {
      handleGroupTypeMessage(m);
    } else if (Message.OTYPE_FILE.equals(m.getObjectType())) {
      handleFileTypeMessage(m);
    }
  }

  private void handleJobTypeMessage(Message m) throws SQLException {
    if (m.getContent() != null && m.getSender() == SyslogConstants.FACILITY_CORE) {
      updateJobCounters(Integer.valueOf(m.getObjectId()), m.getContent());
    } else if (Message.MTYPE_PUB_CHG.equals(m.getMessageType())) {
      Jobs.refreshJob(Integer.valueOf(m.getObjectId()), acs);
    }
  }

  private void handleGroupTypeMessage(Message m) throws SQLException {
    if (Message.MTYPE_PUB_CHG.equals(m.getMessageType())) {
      Groups.refreshGroup(Integer.valueOf(m.getObjectId()), acs);
    }
  }

  private void handleFileTypeMessage(Message m) throws SQLException {
    if (Message.MTYPE_PUB_CHG.equals(m.getMessageType())) {
      Files.refreshFile(Integer.valueOf(m.getObjectId()), Integer.valueOf(m.getContent()), acs);
    }
  }

  private boolean needsAcsUpdate(Message m) {
    return !Message.OTYPE_JOB.equals(m.getObjectType()) &&
            !Message.OTYPE_GROUP.equals(m.getObjectType()) &&
            !Message.OTYPE_FILE.equals(m.getObjectType());
  }

  private void performAcsUpdate() throws SQLException {
    acs.read();
    freeacsUpdated = true;
    if (logger.isDebugEnabled()) {
      logger.debug("ACS object has been updated due to changes in the tables");
    }
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
    UnittypePublish up = getOrCreateUnittypePublish(unittype);
    String objectType = determineObjectType(object);
    Integer objectId = determineObjectId(object, unittype);

    if (objectType != null && objectId != null) {
      up.addPublish(messageType, objectType, objectId);
    }
  }

  private UnittypePublish getOrCreateUnittypePublish(Unittype unittype) {
    return publishUnittypes.computeIfAbsent(unittype.getId(), k -> new UnittypePublish(unittype));
  }

  private String determineObjectType(Object object) {
    if (object instanceof Unittype || object instanceof File ||
            object instanceof SyslogEvent || object instanceof UnittypeParameter ||
            object instanceof Trigger) {
      return Message.OTYPE_UNIT_TYPE;
    } else if (object instanceof Profile || object instanceof ProfileParameter) {
      return Message.OTYPE_PROFILE;
    } else if (object instanceof Group || object instanceof GroupParameter) {
      return Message.OTYPE_GROUP;
    } else if (object instanceof Job || object instanceof JobParameter) {
      return Message.OTYPE_JOB;
    }
    return null;
  }

  private Integer determineObjectId(Object object, Unittype unittype) {
    if (object instanceof Unittype) {
      return unittype.getId();
    } else if (object instanceof Profile) {
      return ((Profile) object).getId();
    } else if (object instanceof ProfileParameter) {
      return ((ProfileParameter) object).getProfile().getId();
    } else if (object instanceof Group) {
      return ((Group) object).getId();
    } else if (object instanceof GroupParameter) {
      return ((GroupParameter) object).getGroup().getId();
    } else if (object instanceof Job) {
      return ((Job) object).getId();
    } else if (object instanceof JobParameter) {
      return ((JobParameter) object).getJob().getId();
    }
    return null;
  }


  private void addMessage(
      String content, String messageType, String objectType, String objectId, Integer receiver) {
    if (this.running) {
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
