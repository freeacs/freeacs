package com.github.freeacs.dbi;

import java.util.Date;

public class Message {
  /**
   * Public static final String MTYPE_REQ = "REQUEST"; public static final String MTYPE_RES =
   * "RESPONSE"; ADDED.
   */
  public static final String MTYPE_PUB_ADD = "PUBLISH-ADD";
  /** CHANGED. */
  public static final String MTYPE_PUB_CHG = "PUBLISH-CHG";
  /** DELETED. */
  public static final String MTYPE_PUB_DEL = "PUBLISH-DEL";

  public static final String MTYPE_PUB_IM = "PUBLISH-IM";
  /** INSPECTION-MODE - published from Web to STUN. */
  public static final String MTYPE_PUB_PS = "PUBLISH-PS";
  /** PROVISIONING-STATUS - published from TR069 to WebServiceClient. */
  public static final String MTYPE_PUB_TR069_TEST_END = "PUBLISH-TR069_TEST-END";
  /** TR069-Test is ended for a particular unit. */
  public static final String MTYPE_PUB_TRG_REL = "PUBLISH-TRG-REL";
  /** TRIGGER_RELEASED - published from Core to Monitor Covers everything within a unittype. */
  public static final String OTYPE_UNIT_TYPE = "UNIT_TYPE";
  /** Covers profile/profile_param. */
  public static final String OTYPE_PROFILE = "PROFILE";
  /** Covers job/job_param. */
  public static final String OTYPE_JOB = "JOB";
  /** Covers group_/group_param. */
  public static final String OTYPE_GROUP = "GROUP";

  public static final String OTYPE_UNIT = "UNIT";
  public static final String OTYPE_CERTIFICATE = "CERTIFICATE";
  public static final String OTYPE_FILE = "FILE";

  /** The message id. */
  private int id;
  /** A SyslogContstants facility number determines the sender. */
  private Integer sender;
  /** A list of SyslogContstants facility number determines the receivers. */
  private Integer receiver;
  /** Tells the type of message. See list of constants within this class for valid types */
  private String messageType;
  /** Tells the type of object. See list of constants within this class for valid types */
  private String objectType;
  /** The id (usually an integer) of the object. */
  private String objectId;
  /** An optional message. */
  private String content;
  /** Timestamp of the message. */
  private Date timestamp;
  /** True if message has been 'consumed'. */
  private boolean processed;

  public Message() {}

  /** Used when Message is a filter in the inbox - but may of course be used otherwise as well. */
  public Message(Integer sender, String messageType, Integer receiver, String objectType) {
    this.sender = sender;
    this.messageType = messageType;
    this.receiver = receiver;
    this.objectType = objectType;
  }

  public Integer getSender() {
    return sender;
  }

  public void setSender(Integer sender) {
    this.sender = sender;
  }

  public Integer getReceiver() {
    return receiver;
  }

  public void setReceiver(Integer receiver) {
    this.receiver = receiver;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MTYPE: ").append(messageType);
    sb.append(", OTYPE: ").append(objectType);
    sb.append(", FROM: ").append(SyslogConstants.getFacilityName(sender));
    if (receiver != null) {
      sb.append(", TO: ").append(SyslogConstants.getFacilityName(receiver));
    } else {
      sb.append(", TO: ALL");
    }
    if (objectId != null) {
      sb.append(", OBJECTID: ").append(objectId);
    }
    if (content != null) {
      sb.append(", CONTENT: ").append(content);
    }
    return sb.toString();
  }

  public boolean isProcessed() {
    return processed;
  }

  public void setProcessed(boolean processed) {
    this.processed = processed;
  }
}
