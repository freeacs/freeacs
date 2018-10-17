package com.github.freeacs.dbi;

import java.util.Date;
import java.util.List;

/**
 * The filters that have start/end-methods are used like this:
 *
 * <p>....field >= startField AND field < endField....
 *
 * <p>In other words, the start is inclusive, but the end is exclusive. If you want to just search
 * for, let's say severity-level 6, then use these filters:
 *
 * <p>setSeverityStart(6) setSeverityEnd(7)
 *
 * <p>All other filters/fields are straight forward.
 *
 * @author Morten
 */
public class SyslogFilter {
  private String content;
  private Integer facility;
  private String facilityVersion;
  private Integer eventId;
  private Integer[] severity;
  private Date collectorTmsStart;
  private Date collectorTmsEnd;
  private String flags;
  private String ipAddress;
  private List<Unittype> unittypes;
  private List<Profile> profiles;
  private String unitId;
  private String userId;
  private Integer maxRows;

  public Integer getFacility() {
    return facility;
  }

  public void setFacility(Integer facility) {
    this.facility = facility;
  }

  public String getMessage() {
    return content;
  }

  public void setMessage(String message) {
    this.content = message;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Integer getMaxRows() {
    return maxRows;
  }

  public void setMaxRows(Integer maxRows) {
    this.maxRows = maxRows;
  }

  public String getUnitId() {
    return unitId;
  }

  public void setUnitId(String unitId) {
    this.unitId = unitId;
  }

  public Date getCollectorTmsStart() {
    return collectorTmsStart;
  }

  public void setCollectorTmsStart(Date collectorTmsStart) {
    this.collectorTmsStart = collectorTmsStart;
  }

  public Date getCollectorTmsEnd() {
    return collectorTmsEnd;
  }

  public void setCollectorTmsEnd(Date collectorTmsEnd) {
    this.collectorTmsEnd = collectorTmsEnd;
  }

  protected Integer getEventId() {
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

  public void setSeverity(Integer[] severity) {
    this.severity = severity;
  }

  public Integer[] getSeverity() {
    return severity;
  }

  public List<Unittype> getUnittypes() {
    return unittypes;
  }

  public void setUnittypes(List<Unittype> unittypes) {
    this.unittypes = unittypes;
  }

  public List<Profile> getProfiles() {
    return profiles;
  }

  public void setProfiles(List<Profile> profiles) {
    this.profiles = profiles;
  }

  public String getFacilityVersion() {
    return facilityVersion;
  }

  public void setFacilityVersion(String facilityVersion) {
    this.facilityVersion = facilityVersion;
  }
}
