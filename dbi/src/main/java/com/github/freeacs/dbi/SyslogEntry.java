package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.TimestampWrapper;
import java.util.Date;

public class SyslogEntry {
  private Integer id;

  private Date collectorTimestamp;

  private Integer eventId;

  private Integer facility;

  private String facilityVersion;

  private Integer severity;

  private String deviceTimestamp;

  private String hostname;

  private String tag;

  private String content;

  private String flags;

  private String ipAddress;

  private String unittypeName;

  /** For backward compatibility. */
  private Integer unittypeId;

  private String profileName;

  /** For backward compatibility. */
  private Integer profileId;

  private String unitId;

  private String userId;

  public Integer getFacility() {
    return facility;
  }

  public void setFacility(Integer facility) {
    this.facility = facility;
  }

  public Integer getId() {
    return id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public Integer getSeverity() {
    return severity;
  }

  public void setSeverity(Integer severity) {
    this.severity = severity;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Override
  public String toString() {
    String tmsStr =
        collectorTimestamp != null ? TimestampWrapper.tmsFormat.format(collectorTimestamp) : "n/a";
    String msgStr = content;
    if (msgStr.length() > 80) {
      msgStr = content.substring(0, 77) + "...";
    }
    return String.format(
        tmsStr + "%1$4s %2$-80s %3$20s %4$30s %5$30s %6$15s",
        severity,
        msgStr,
        unittypeName,
        profileName,
        unitId,
        userId);
  }

  public String getUnitId() {
    return unitId;
  }

  public void setUnitId(String unitId) {
    this.unitId = unitId;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public Date getCollectorTimestamp() {
    return collectorTimestamp;
  }

  public void setCollectorTimestamp(Date collectorTimestamp) {
    this.collectorTimestamp = collectorTimestamp;
  }

  public String getDeviceTimestamp() {
    return deviceTimestamp;
  }

  public void setDeviceTimestamp(String deviceTimestamp) {
    this.deviceTimestamp = deviceTimestamp;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Integer getEventId() {
    return eventId;
  }

  public void setEventId(Integer eventId) {
    this.eventId = eventId;
  }

  public String getFlags() {
    return flags;
  }

  public void setFlags(String flags) {
    this.flags = flags;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUnittypeName() {
    return unittypeName;
  }

  public void setUnittypeName(String unittypeName) {
    this.unittypeName = unittypeName;
  }

  public String getProfileName() {
    return profileName;
  }

  public void setProfileName(String profileName) {
    this.profileName = profileName;
  }

  public Integer getUnittypeId() {
    return unittypeId;
  }

  public void setUnittypeId(Integer unittypeId) {
    this.unittypeId = unittypeId;
  }

  public Integer getProfileId() {
    return profileId;
  }

  public void setProfileId(Integer profileId) {
    this.profileId = profileId;
  }

  public String getFacilityVersion() {
    return facilityVersion;
  }

  public void setFacilityVersion(String facilityVersion) {
    this.facilityVersion = facilityVersion;
  }
}
