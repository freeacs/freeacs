package com.github.freeacs.web.app.context;

import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.input.ParameterParser;
import lombok.Getter;
import lombok.Setter;

/**
 * ContextKeeper for xAPS trail history.
 *
 * <p>Keeps information for one specific point in the history trail.
 *
 * @author Jarl Andre Hubenthal
 */
@Getter
@Setter
public class ContextItem {
  /** The current page. */
  private final Page currentPage;

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
  private final boolean didplayUnitType = true;

  /** The display all option unit type. */
  private boolean displayAllOptionUnitType = true;

  /** The context profile editable. */
  private boolean contextProfileEditable = true;

  /** The display profile. */
  private boolean displayProfile = true;

  /** The display all option profile. */
  private boolean displayAllOptionProfile = true;

  /** The current url. */
  private final String currentUrl;

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

  @Override
  public boolean equals(Object obj) {
    throw new IllegalArgumentException("ContextItem.equals() was not expected to be in use");
  }

}
