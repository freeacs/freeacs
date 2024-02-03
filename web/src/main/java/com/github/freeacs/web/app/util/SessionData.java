package com.github.freeacs.web.app.util;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitJob;
import com.github.freeacs.web.security.AllowedUnittype;
import com.github.freeacs.web.security.WebUser;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Serves as a kind of a Session Store. Is stored in SessionCache, which is a static container.
 *
 * @author Jarl Andre Hubenthal
 */
@Setter
@Getter
public class SessionData {
  /** The name. */
  private String name;

  /** The filtered unittypes. */
  private AllowedUnittype[] filteredUnittypes;

  /** The last login target. */
  private String lastLoginTarget;

  /** The error message. */
  private String errorMessage;

  /** The last accessed. */
  private Date lastAccessed;

  /** The unittype name. */
  private String unittypeName;

  /** The profile name. */
  private String profileName;

  /** The unit id. */
  private String unitId;

  /** The jobname. */
  private String jobname;

  /** The filter type. */
  private String filterType;

  /** The filter search. */
  private String filterSearch;

  /** The filter flag. */
  private String filterFlag;

  /** The group. */
  private String group;

  /** The job type. */
  private String jobType;

  /** The failed unit jobs list. */
  private List<UnitJob> failedUnitJobsList;

  /** The completed unit jobs list. */
  private Collection<Unit> completedUnitJobsList;

  /** The user. */
  private WebUser user;

  /**
   * Sets the filter string.
   *
   * @param filterSearch the new filter string
   */
  public void setFilterString(String filterSearch) {
    this.filterSearch = filterSearch;
  }

  /**
   * Gets the filter string.
   *
   * @return the filter string
   */
  public String getFilterString() {
    return filterSearch;
  }

}
