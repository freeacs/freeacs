package com.github.freeacs.web.app.util;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitJob;
import com.github.freeacs.web.security.AllowedUnittype;
import com.github.freeacs.web.security.WebUser;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Serves as a kind of a Session Store. Is stored in SessionCache, which is a static container.
 *
 * @author Jarl Andre Hubenthal
 */
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
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the last login target.
   *
   * @param lastLoginTarget the new last login target
   */
  public void setLastLoginTarget(String lastLoginTarget) {
    this.lastLoginTarget = lastLoginTarget;
  }

  /**
   * Gets the last login target.
   *
   * @return the last login target
   */
  public String getLastLoginTarget() {
    return lastLoginTarget;
  }

  /**
   * Sets the error message.
   *
   * @param errorMessage the new error message
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Gets the unittype name.
   *
   * @return the unittype name
   */
  public String getUnittypeName() {
    return unittypeName;
  }

  /**
   * Sets the unittype name.
   *
   * @param unittypeName the new unittype name
   */
  public void setUnittypeName(String unittypeName) {
    this.unittypeName = unittypeName;
  }

  /**
   * Gets the profile name.
   *
   * @return the profile name
   */
  public String getProfileName() {
    return profileName;
  }

  /**
   * Sets the profile name.
   *
   * @param profileName the new profile name
   */
  public void setProfileName(String profileName) {
    this.profileName = profileName;
  }

  /**
   * Sets the unit id.
   *
   * @param unitId the new unit id
   */
  public void setUnitId(String unitId) {
    this.unitId = unitId;
  }

  /**
   * Gets the unit id.
   *
   * @return the unit id
   */
  public String getUnitId() {
    return unitId;
  }

  /**
   * Sets the jobname.
   *
   * @param jobId the new jobname
   */
  public void setJobname(String jobId) {
    this.jobname = jobId;
  }

  /**
   * Gets the jobname.
   *
   * @return the jobname
   */
  public String getJobname() {
    return jobname;
  }

  /**
   * Sets the last accessed.
   *
   * @param lastAccessed the new last accessed
   */
  public void setLastAccessed(Date lastAccessed) {
    this.lastAccessed = lastAccessed;
  }

  /**
   * Gets the last accessed.
   *
   * @return the last accessed
   */
  public Date getLastAccessed() {
    return lastAccessed;
  }

  /**
   * Checks if is unittypes filtered.
   *
   * @return true, if is unittypes filtered
   */
  public boolean isUnittypesFiltered() {
    return filteredUnittypes != null;
  }

  /**
   * Gets the filtered unittypes.
   *
   * @return the filtered unittypes
   */
  public AllowedUnittype[] getFilteredUnittypes() {
    return filteredUnittypes;
  }

  /**
   * Sets the filtered unittypes.
   *
   * @param filteredUnittypes the new filtered unittypes
   */
  public void setFilteredUnittypes(AllowedUnittype[] filteredUnittypes) {
    this.filteredUnittypes = filteredUnittypes;
  }

  /**
   * Sets the filter type.
   *
   * @param filterType the new filter type
   */
  public void setFilterType(String filterType) {
    this.filterType = filterType;
  }

  /**
   * Gets the filter type.
   *
   * @return the filter type
   */
  public String getFilterType() {
    return filterType;
  }

  /**
   * Sets the filter flag.
   *
   * @param filterFlag the new filter flag
   */
  public void setFilterFlag(String filterFlag) {
    this.filterFlag = filterFlag;
  }

  /**
   * Gets the filter flag.
   *
   * @return the filter flag
   */
  public String getFilterFlag() {
    return filterFlag;
  }

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

  /**
   * Sets the failed unit jobs list.
   *
   * @param unitJobsList the new failed unit jobs list
   */
  public void setFailedUnitJobsList(List<UnitJob> unitJobsList) {
    this.failedUnitJobsList = unitJobsList;
  }

  /**
   * Gets the failed unit jobs list.
   *
   * @return the failed unit jobs list
   */
  public List<UnitJob> getFailedUnitJobsList() {
    return failedUnitJobsList;
  }

  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(String group) {
    this.group = group;
  }

  /**
   * Gets the group.
   *
   * @return the group
   */
  public String getGroup() {
    return group;
  }

  /**
   * Sets the job type.
   *
   * @param jobType the new job type
   */
  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  /**
   * Gets the job type.
   *
   * @return the job type
   */
  public String getJobType() {
    return jobType;
  }

  /**
   * Sets the user.
   *
   * @param user the new user
   */
  public void setUser(WebUser user) {
    this.user = user;
  }

  /**
   * Gets the user.
   *
   * @return the user
   */
  public WebUser getUser() {
    return this.user;
  }

  /**
   * Sets the completed unit jobs list.
   *
   * @param units the new completed unit jobs list
   */
  public void setCompletedUnitJobsList(Collection<Unit> units) {
    this.completedUnitJobsList = units;
  }

  /**
   * Gets the completed unit jobs list.
   *
   * @return the completed unit jobs list
   */
  public Collection<Unit> getCompletedUnitJobsList() {
    return this.completedUnitJobsList;
  }
}
