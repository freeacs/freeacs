package com.github.freeacs.web.app.context;

import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.input.ParameterParser;

/**
 * ContextKeeper for xAPS trail history.
 *
 * <p>Keeps information for one specific point in the history trail.
 *
 * @author Jarl Andre Hubenthal
 */
public class ContextItem {
  /** The current page. */
  private Page currentPage = Page.NONE;

  /** The unit type name. */
  private String unitTypeName;

  /** The profile name. */
  private String profileName;

  /** The unit id. */
  private String unitId;

  /** The group name. */
  private String groupName;

  /** The job name. */
  private String jobName;

  /** The context unit type editable. */
  private boolean contextUnitTypeEditable = true;

  /** The didplay unit type. */
  private boolean didplayUnitType = true;

  /** The display all option unit type. */
  private boolean displayAllOptionUnitType = true;

  /** The context profile editable. */
  private boolean contextProfileEditable = true;

  /** The display profile. */
  private boolean displayProfile = true;

  /** The display all option profile. */
  private boolean displayAllOptionProfile = true;

  /** The current url. */
  private String currentUrl;

  /** The display unit type. */
  private boolean displayUnitType = true;

  /**
   * Initializes a new ContextKeeper instance
   *
   * <p>Makes sure that this current context item only stores relevant info, and also what selects
   * should and should not display.
   *
   * <p>Also controls whether or not the "All" option in the selects is visible.
   *
   * @param inputData the inputData instance (any subclass of InputData)
   * @param params the parameter parser
   */
  public ContextItem(InputData inputData, ParameterParser params) {
    String urlPage = params.getParameter("page");
    currentUrl = params.getRequestURL();
    currentPage = Page.getById(urlPage);
    switch (currentPage) {

        /* Unittype pages */
      case UNITTYPEOVERVIEW:
        contextUnitTypeEditable = false;
        break;
      case UNITTYPEPARAMETERS:
      case UNITTYPE:
        displayAllOptionUnitType = false;
        unitTypeName = inputData.getUnittype().getString();
        break;
      case UNITTYPECREATE:
        displayUnitType = false;
        break;

        /* Profile pages */
      case PROFILECREATE:
      case PROFILEOVERVIEW:
        contextProfileEditable = false;
        displayAllOptionUnitType = false;
        unitTypeName = inputData.getUnittype().getString();
        break;
      case WINDOWPROFILE:
      case PROFILE:
        displayAllOptionProfile = false;
        unitTypeName = inputData.getUnittype().getString();
        profileName = inputData.getProfile().getString();
        break;

        /* Unit pages */
      case UNIT:
      case UNITSTATUS:
      case WINDOWUNIT:
        unitId = inputData.getUnit().getString();
        profileName = inputData.getProfile().getString();
        unitTypeName = inputData.getUnittype().getString();
        break;

        /* Group detail page */
      case GROUP:
        displayAllOptionUnitType = false;
        groupName = inputData.getGroup().getString();
        unitTypeName = inputData.getUnittype().getString();
        break;

        /* Job detail page */
      case JOB:
        displayAllOptionUnitType = false;
        jobName = inputData.getJob().getString();
        unitTypeName = inputData.getUnittype().getString();
        break;

        /* Misc pages - no profile "level" - or profile in dropdown on page */
      case FILES:
      case CREATETRIGGER:
      case JOBSOVERVIEW:
      case SOFTWARE:
      case SYSLOGEVENTS:
      case TRIGGEROVERVIEW:
      case TRIGGERRELEASEHISTORY:
      case TRIGGERRELEASE:
      case UPGRADE:
        displayAllOptionUnitType = false;
        displayProfile = false;
        unitTypeName = inputData.getUnittype().getString();
        break;

        /* Misc pages - with profile "level" */
      case GROUPSOVERVIEW:
      case REPORT:
      case SEARCH:
      case SYSLOG:
      case UNITLIST:
        unitTypeName = inputData.getUnittype().getString();
        profileName = inputData.getProfile().getString();
        break;
      default:
        break;
    }
  }

  /**
   * Checks if is specific.
   *
   * @param item the item
   * @return true, if is specific
   */
  public static boolean isSpecific(ContextItem item) {
    return item != null && item.isSpecific();
  }

  /**
   * Checks if the current page is a specific object, for example if its a Unit Configuration page
   * or a Profile Configuration page. A page that is NOT specific is the Group Overview page or
   * Search page.
   *
   * @return true or false
   */
  public boolean isSpecific() {
    switch (currentPage) {
      case UNIT:
      case UNITSTATUS:
      case WINDOWUNIT:
        if (unitId != null) {
          return true;
        }
      case GROUP:
        if (groupName != null && unitTypeName != null) {
          return true;
        }
      case JOB:
        if (jobName != null && unitTypeName != null) {
          return true;
        }
      case UNITTYPE:
        if (unitTypeName != null) {
          return true;
        }
      case WINDOWPROFILE:
      case PROFILE:
        if (unitTypeName != null && profileName != null) {
          return true;
        }
      default:
        return false;
    }
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  public String getTitle() {
    return currentPage.getTitle();
  }

  /**
   * Gets the current url.
   *
   * @return the current url
   */
  public String getCurrentUrl() {
    return getPageUrl(currentPage);
  }

  /**
   * Gets the page url.
   *
   * @param currentPage the current page
   * @return the page url
   */
  public String getPageUrl(Page currentPage) {
    switch (currentPage) {
      case SEARCH:
        return Page.SEARCH.getUrl();
      case UNIT:
        return Page.UNIT.getUrl(
            "unit=" + unitId + "&profile=" + profileName + "&unittype=" + unitTypeName);
      case UNITSTATUS:
        return Page.UNITSTATUS.getUrl(
            "unit=" + unitId + "&profile=" + profileName + "&unittype=" + unitTypeName);
      case PROFILE:
        return Page.PROFILE.getUrl("profile=" + profileName + "&unittype=" + unitTypeName);
      case PROFILEOVERVIEW:
        return Page.PROFILEOVERVIEW.getUrl();
      case UNITTYPE:
        return Page.UNITTYPE.getUrl("unittype=" + unitTypeName);
      case UNITTYPEOVERVIEW:
        return Page.UNITTYPEOVERVIEW.getUrl();
      case GROUP:
        return Page.GROUP.getUrl("group=" + groupName + "&unittype=" + unitTypeName);
      case GROUPSOVERVIEW:
        return Page.GROUPSOVERVIEW.getUrl();
      case JOB:
        return Page.JOB.getUrl("job=" + jobName + "&unittype=" + unitTypeName);
      case JOBSOVERVIEW:
        return Page.JOBSOVERVIEW.getUrl();
      default:
        return currentUrl;
    }
  }

  /**
   * Gets the current page.
   *
   * @return the current page
   */
  public Page getCurrentPage() {
    return currentPage;
  }

  /**
   * Sets the current page.
   *
   * @param currentPage the new current page
   */
  public void setCurrentPage(Page currentPage) {
    this.currentPage = currentPage;
  }

  /**
   * Gets the unit type name.
   *
   * @return the unit type name
   */
  public String getUnitTypeName() {
    return unitTypeName;
  }

  /**
   * Sets the unit type name.
   *
   * @param unitTypeName the new unit type name
   */
  public void setUnitTypeName(String unitTypeName) {
    this.unitTypeName = unitTypeName;
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
   * Gets the unit id.
   *
   * @return the unit id
   */
  public String getUnitId() {
    return unitId;
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
   * Gets the group name.
   *
   * @return the group name
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Sets the group name.
   *
   * @param groupName the new group name
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  /**
   * Gets the job name.
   *
   * @return the job name
   */
  public String getJobName() {
    return jobName;
  }

  /**
   * Sets the job name.
   *
   * @param jobName the new job name
   */
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  @Override
  public boolean equals(Object obj) {
    throw new IllegalArgumentException("ContextItem.equals() was not expected to be in use");
    //		if (this == obj)
    //			return true;
    //		if (obj == null)
    //			return false;
    //		if (getClass() != obj.getClass())
    //			return false;
    //		ContextItem other = (ContextItem) obj;
    //		Page otherSelectedPage = Page.getById(Page.getSelectedPage(other.currentPage.getId()));
    //		if (currentPage != otherSelectedPage)
    //			return false;
    //		switch (currentPage) {
    //		case UNIT:
    //		case UNITSTATUS:
    //			if (unitTypeName != null && other.unitTypeName == null)
    //				return false;
    //			else if (unitTypeName != null && !unitTypeName.equals(other.unitTypeName))
    //				return false;
    //			if (profileName != null && other.profileName == null)
    //				return false;
    //			else if (profileName != null && !profileName.equals(other.profileName))
    //				return false;
    //			if (unitId != null && other.unitId == null)
    //				return false;
    //			else if (unitId != null && !unitId.equals(other.unitId))
    //				return false;
    //		case GROUP:
    //			if (unitTypeName != null && other.unitTypeName == null)
    //				return false;
    //			else if (unitTypeName != null && !unitTypeName.equals(other.unitTypeName))
    //				return false;
    //			if (groupName != null && other.groupName == null)
    //				return false;
    //			else if (groupName != null && !groupName.equals(other.groupName))
    //				return false;
    //		case JOB:
    //			if (unitTypeName != null && other.unitTypeName == null)
    //				return false;
    //			else if (unitTypeName != null && !unitTypeName.equals(other.unitTypeName))
    //				return false;
    //			if (jobName != null && other.jobName == null)
    //				return false;
    //			else if (jobName != null && !jobName.equals(other.jobName))
    //				return false;
    //		default:
    //			break;
    //		}
    //		return true;
  }

  /**
   * Checks if is context profile editable.
   *
   * @return true, if is context profile editable
   */
  public boolean isContextProfileEditable() {
    return contextProfileEditable;
  }

  /**
   * Checks if is context unit type editable.
   *
   * @return true, if is context unit type editable
   */
  public boolean isContextUnitTypeEditable() {
    return contextUnitTypeEditable;
  }

  /**
   * Checks if is didplay unit type.
   *
   * @return true, if is didplay unit type
   */
  public boolean isDidplayUnitType() {
    return didplayUnitType;
  }

  /**
   * Checks if is display profile.
   *
   * @return true, if is display profile
   */
  public boolean isDisplayProfile() {
    return displayProfile;
  }

  /**
   * Checks if is display all option unit type.
   *
   * @return true, if is display all option unit type
   */
  public boolean isDisplayAllOptionUnitType() {
    return displayAllOptionUnitType;
  }

  /**
   * Checks if is display all option profile.
   *
   * @return true, if is display all option profile
   */
  public boolean isDisplayAllOptionProfile() {
    return displayAllOptionProfile;
  }

  /**
   * Checks if is display unit type.
   *
   * @return true, if is display unit type
   */
  public boolean isDisplayUnitType() {
    return displayUnitType;
  }
}
