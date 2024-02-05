package com.github.freeacs.dbi;

import lombok.Data;

@Data
public class Identity {
  private final int facility;
  private final String facilityName;
  private final String facilityVersion;
  private final User user;

  public Identity(int facility, String facilityVersion, User user) {
    this.facility = facility;
    this.facilityVersion = facilityVersion;
    this.facilityName = SyslogConstants.getFacilityName(facility);
    if (user == null) {
      throw new IllegalArgumentException("The user does not exist");
    }
    this.user = user;
  }
}
