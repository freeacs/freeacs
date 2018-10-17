package com.github.freeacs.dbi;

public class Identity {
  private int facility;
  private String facilityName;
  private String facilityVersion;
  private User user;

  public Identity(int facility, String facilityVersion, User user) {
    this.facility = facility;
    this.facilityVersion = facilityVersion;
    this.facilityName = SyslogConstants.getFacilityName(facility);
    if (facilityName == null) {
      throw new IllegalArgumentException("The facility supplied (" + facility + ") does not exist");
    }
    if (user == null) {
      throw new IllegalArgumentException("The user does not exist");
    }
    this.user = user;
  }

  public int getFacility() {
    return facility;
  }

  public String getFacilityName() {
    return facilityName;
  }

  public User getUser() {
    return user;
  }

  public String getFacilityVersion() {
    return facilityVersion;
  }
}
