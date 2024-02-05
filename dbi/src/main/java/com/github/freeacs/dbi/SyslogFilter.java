package com.github.freeacs.dbi;

import lombok.Data;

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
@Data
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

  public String getMessage() {
    return content;
  }

  public void setMessage(String message) {
    this.content = message;
  }
}
