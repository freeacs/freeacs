package com.github.freeacs.dbi;

import lombok.Data;

import java.util.Date;

@Data
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
}
