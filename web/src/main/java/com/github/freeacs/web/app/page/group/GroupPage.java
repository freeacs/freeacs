package com.github.freeacs.web.app.page.group;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.GroupParameter;
import com.github.freeacs.dbi.GroupParameters;
import com.github.freeacs.dbi.Groups;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Parameter.Operator;
import com.github.freeacs.dbi.Parameter.ParameterDataType;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.page.search.SearchParameter;
import com.github.freeacs.web.app.page.unittype.UnittypeParameterFlags;
import com.github.freeacs.web.app.page.unittype.UnittypeParameterTypes;
import com.github.freeacs.web.app.table.TableElement;
import com.github.freeacs.web.app.table.TableElementMaker;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * A page for Group Configuration and Group Create.
 *
 * @author Jarl Andre Hubenthal
 */
public class GroupPage extends AbstractWebPage {
  /** The xaps. */
  private ACS acs;

  /** The xaps unit. */
  private ACSUnit acsUnit;

  /** The input data. */
  private GroupData inputData;

  /** The session id. */
  private String sessionId;

  /** The unittypes. */
  private DropDownSingleSelect<Unittype> unittypes;

  /** The groups. */
  private DropDownSingleSelect<Group> groups;

  /** The parents. */
  private DropDownSingleSelect<Group> parents;

  /**
   * (non-Javadoc)
   *
   * @see com.owera.xaps.web.app.page.AbstractWebPage#getTitle(java.lang.String)
   */
  public String getTitle(String page) {
    return super.getTitle(page)
        + (groups.getSelected() != null ? " | " + groups.getSelected().getName() : "");
  }

  /**
   * (non-Javadoc)
   *
   * @see com.owera.xaps.web.app.page.AbstractWebPage#getShortcutItems(com.owera
   *     .xaps.web.app.util.SessionData)
   */
  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Group overview", Page.GROUPSOVERVIEW));
    if (groups.getSelected() != null) {
      MenuItem unitsThatMatches = new MenuItem("Search for units that matches", Page.SEARCH);
      unitsThatMatches.addCommand("auto");
      unitsThatMatches.addParameter("advancedView", "true");
      unitsThatMatches.addParameter("formsubmit", "Search");
      unitsThatMatches.addParameter("unittype", groups.getSelected().getUnittype().getName());
      unitsThatMatches.addParameter(
          "profile",
          groups.getSelected().getTopParent().getProfile() != null
              ? groups.getSelected().getTopParent().getProfile().getName()
              : WebConstants.ALL_ITEMS_OR_DEFAULT);
      unitsThatMatches.addParameter("group", groups.getSelected().getName());
      list.add(unitsThatMatches);
    }
    return list;
  }

  /**
   * Action delete group.
   *
   * @throws Exception the exception
   */
  private void actionDeleteGroup() throws Exception {
    if (groups.getSelected() != null) {
      unittypes.getSelected().getGroups().deleteGroup(groups.getSelected(), acs);
      inputData.getGroup().setValue(null);
      SessionCache.getSessionData(sessionId).setGroup(null);
    }
  }

  /**
   * Action cud parameters.
   *
   * @param req the req
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  private void actionCUDParameters(ParameterParser req) throws Exception {
    GroupParameters gParams = groups.getSelected().getGroupParameters();

    Enumeration requestParameters = req.getHttpServletRequest().getParameterNames();
    while (requestParameters.hasMoreElements()) {
      String key = (String) requestParameters.nextElement();

      // Parameter keys are prefixed with either create::, delete:: or
      // update::
      // If parameter key is shorter than eight characters, the upName
      // will remain null
      String gpName = null;
      if (key.length() > 8) {
        gpName = key.substring(8);
      }
      String convertedGroupParameterName = SearchParameter.convertParameterId(gpName);
      GroupParameter groupParameter = gParams.getByName(convertedGroupParameterName);

      // First we process updating and creating
      if (key.startsWith("update::") || key.startsWith("create::")) {
        String opStr = req.getParameter("operator::" + gpName);
        Operator operator = Operator.EQ;
        if (opStr != null && isOperatorValid(opStr)) {
          operator = Operator.getOperator(opStr);
        }

        String typeStr = req.getParameter("datatype::" + gpName);
        ParameterDataType type = ParameterDataType.TEXT;
        if (typeStr != null && isParameterDataTypeValid(typeStr)) {
          type = ParameterDataType.getDataType(typeStr);
        }

        // if no group parameter exists and this is a create key
        if (groupParameter == null && key.startsWith("create::")) {
          String newValue = req.getParameter("update::" + gpName);
          if ("NULL".equals(newValue)) {
            newValue = null;
          }
          newValue = SearchParameter.convertParameterValue(newValue);
          UnittypeParameter utp =
              groups.getSelected().getUnittype().getUnittypeParameters().getByName(gpName);
          Parameter param = new Parameter(utp, newValue, operator, type);
          GroupParameter newGP = new GroupParameter(param, groups.getSelected());
          gParams.addOrChangeGroupParameter(newGP, acs);
        }

        // If a group parameter exists and there is a value to replace
        // the current value
        // The last part in this if test is to check if this is not a
        // previously created group parameter or a parameter that should
        // be created
        // We only want to process normal updating of existing group
        // parameters
        if (groupParameter != null
            && req.getParameter("update::" + gpName) != null
            && req.getParameter("create::" + gpName) == null) {
          String updatedValue = req.getParameter("update::" + gpName);
          if ("NULL".equals(updatedValue)) {
            updatedValue = null;
          }
          updatedValue = SearchParameter.convertParameterValue(updatedValue);
          if (!groupParameter.getParameter().getValue().equals(updatedValue)
              || groupParameter.getParameter().getOp() != operator
              || groupParameter.getParameter().getType() != type) {
            Parameter param =
                new Parameter(
                    groupParameter.getParameter().getUnittypeParameter(),
                    updatedValue,
                    operator,
                    type);
            groupParameter.setParameter(param);
            gParams.addOrChangeGroupParameter(groupParameter, acs);
          }
        }
      }

      // Then we delete those that should be deleted
      if (key.startsWith("delete::") && groupParameter != null) {
        gParams.deleteGroupParameter(groupParameter, acs);
      }
    }
  }

  private boolean isParameterDataTypeValid(String typeStr) {
    try {
      return ParameterDataType.getDataType(typeStr) != null;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isOperatorValid(String opStr) {
    try {
      return Operator.getOperator(opStr) != null;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Action update group.
   *
   * @throws Exception the exception
   */
  private void actionUpdateGroup() throws Exception {
    if (groups.getSelected() != null) {
      Groups allGroups = unittypes.getSelected().getGroups();

      Group oldParent = groups.getSelected().getParent();
      Profile oldProfile = groups.getSelected().getProfile();
      String oldDescription = groups.getSelected().getDescription();

      Group parentGroup = parents.getSelected();

      try {
        groups.getSelected().setDescription(inputData.getDescription().getString());

        groups.getSelected().setParent(parentGroup);

        if (inputData.getProfile().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
          groups
              .getSelected()
              .setProfile(
                  unittypes
                      .getSelected()
                      .getProfiles()
                      .getByName(inputData.getProfile().getString()));
        } else {
          groups.getSelected().setProfile(null);
        }
      } catch (IllegalArgumentException ie) {
        groups.getSelected().setParent(oldParent);
        groups.getSelected().setProfile(oldProfile);
        groups.getSelected().setDescription(oldDescription);
        throw ie;
      }

      allGroups.addOrChangeGroup(groups.getSelected(), acs);

      inputData.getGroup().setValue(groups.getSelected().getName());
    }
  }

  /**
   * Action create group.
   *
   * @return the string
   * @throws Exception the exception
   */
  private String actionCreateGroup(DataSource xapsDataSource, DataSource syslogDataSource)
      throws Exception {
    String gName = inputData.getGroupname().getStringWithoutTags();
    String desc = inputData.getDescription().getString();

    Group parent = parents.getSelected();
    Profile profile = null;

    Groups allGroups = unittypes.getSelected().getGroups();

    if (inputData.getProfile().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
      profile = unittypes.getSelected().getProfiles().getByName(inputData.getProfile().getString());
    } else if (isProfilesLimited(
        unittypes.getSelected(), sessionId, xapsDataSource, syslogDataSource)) {
      throw new Exception("You are not allowed to create groups!");
    }

    if (gName != null && desc != null) {
      if (allGroups.getByName(gName) != null) {
        return "The group " + gName + " is already created";
      }
      groups.setSelected(new Group(gName, desc, parent, unittypes.getSelected(), profile));
      allGroups.addOrChangeGroup(groups.getSelected(), acs);
      SessionCache.getSessionData(sessionId).setGroup(gName);
      return "OK";
    } else {
      return "Name and description is required";
    }
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    // important object var
    inputData = (GroupData) InputDataRetriever.parseInto(new GroupData(), params);

    sessionId = params.getSession().getId();

    acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    acsUnit = ACSLoader.getACSUnit(sessionId, xapsDataSource, syslogDataSource);

    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype(), inputData.getGroup());

    if (inputData.getCmd().hasValue("create")) {
      inputData.getGroup().setValue(null);
    }

    unittypes = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    groups =
        InputSelectionFactory.getGroupSelection(inputData.getGroup(), unittypes.getSelected(), acs);
    Group selectedParent =
        unittypes.getSelected() != null && inputData.getParentgroup().getString() != null
            ? unittypes.getSelected().getGroups().getByName(inputData.getParentgroup().getString())
            : (groups.getSelected() != null ? groups.getSelected().getParent() : null);
    List<Group> possibleParents =
        groups.getSelected() != null
            ? Arrays.asList(
                calculatePossibleParents(
                    groups.getSelected(), unittypes.getSelected().getGroups().getGroups()))
            : (unittypes.getSelected() != null
                ? Arrays.asList(unittypes.getSelected().getGroups().getGroups())
                : new ArrayList<Group>());
    parents =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getParentgroup(), selectedParent, possibleParents);

    String createMessage = null;

    // action
    if (inputData.getFormSubmit().isValue(WebConstants.DELETE)) {
      actionDeleteGroup();
      outputHandler.setDirectToPage(Page.GROUPSOVERVIEW);
      return;
    } else if (inputData.getFormSubmit().isValue(WebConstants.UPDATE)) {
      actionUpdateGroup();
    } else if (inputData.getFormSubmit().isValue("Create group")) {
      createMessage = actionCreateGroup(xapsDataSource, syslogDataSource);
    } else if (inputData.getFormSubmit().isValue(WebConstants.UPDATE_PARAMS)) {
      actionCUDParameters(params);
    }

    Map<String, Object> map = outputHandler.getTemplateMap();

    if (inputData.getCmd().hasValue("create")) {
      if ("OK".equals(createMessage) && groups.getSelected() != null) {
        outputHandler.setDirectToPage(Page.GROUP);
        return;
      } else {
        groups.setSelected(null);
        map.put("message", createMessage);
      }
      map.put("name", inputData.getGroupname().getString());
      map.put("description", inputData.getDescription().getString());
    } else if (unittypes.getSelected() != null && groups.getSelected() != null) {
      List<Parameter> gParams =
          groups.getSelected().getGroupParameters().getAllParameters(groups.getSelected());
      int unitCount =
          acsUnit.getUnitCount(
              unittypes.getSelected(), findProfile(groups.getSelected().getName()), gParams);
      Groups allGroups = unittypes.getSelected().getGroups();
      groups.getSelected().setCount(unitCount);
      allGroups.addOrChangeGroup(groups.getSelected(), acs);
      map.put("count", unitCount);

      GroupParameter[] groupParameters =
          groups.getSelected().getGroupParameters().getGroupParameters();
      List<TableElement> parameters =
          new TableElementMaker()
              .getParameters(
                  unittypes.getSelected().getUnittypeParameters().getUnittypeParameters(),
                  groupParameters);
      map.put("params", parameters);
      map.put(
          "filterflags",
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getFilterFlag(),
              UnittypeParameterFlags.getByValue(inputData.getFilterFlag().getString()),
              Arrays.asList(UnittypeParameterFlags.values())));
      map.put(
          "filtertypes",
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getFilterType(),
              UnittypeParameterTypes.valueOf(
                  inputData
                      .getFilterType()
                      .getString(
                          groups.getSelected().getGroupParameters().getGroupParameters().length > 0
                              ? "Configured"
                              : "All")),
              Arrays.asList(UnittypeParameterTypes.values())));
      map.put("filterstring", inputData.getFilterString());
      map.put(
          "groupjobs",
          unittypes.getSelected().getJobs().getGroupJobs(groups.getSelected().getId()));
      map.put("getgroupprofile", new FindProfileMethod());
      map.put("operators", Operator.values());
      map.put("datatypes", ParameterDataType.values());
    } else {
      outputHandler.setDirectToPage(Page.GROUP, "cmd=create");
      return;
    }

    map.put("unittypes", unittypes);
    map.put("groups", groups);
    map.put("parents", parents);
    map.put("profiles", addGroupProfile(xapsDataSource, syslogDataSource));

    if (groups.getSelected() != null) {
      outputHandler.setTemplatePath("group/details");
    } else {
      outputHandler.setTemplatePath("group/create");
    }
  }

  /**
   * Adds the group profile.
   *
   * @return the drop down single select
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException the security exception
   *     <p>the no available connection exception
   * @throws SQLException the sQL exception
   */
  private DropDownSingleSelect<Profile> addGroupProfile(
      DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException {
    if (unittypes.getSelected() == null) {
      return null;
    }

    Group parentGroup = parents.getSelected();

    if (groups.getSelected() != null && groups.getSelected().getParent() != null) {
      parentGroup = groups.getSelected().getParent();
    }

    Profile profile =
        parentGroup != null
            ? findProfile(parentGroup.getName())
            : groups.getSelected() != null
                ? findProfile(groups.getSelected().getName())
                : (inputData.getProfile().getString() != null
                    ? acs.getProfile(
                        unittypes.getSelected().getName(), inputData.getProfile().getString())
                    : null);

    List<Profile> allowedProfiles =
        parentGroup == null
            ? getAllowedProfiles(
                sessionId, unittypes.getSelected(), xapsDataSource, syslogDataSource)
            : new ArrayList<>();

    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getProfile(), profile, allowedProfiles);
  }

  /**
   * Find profile.
   *
   * @param groupName the group name
   * @return the profile
   */
  private Profile findProfile(String groupName) {
    Group group = unittypes.getSelected().getGroups().getByName(groupName);

    Group lastgroup = null;
    Group current = group;
    while ((current = current.getParent()) != null) {
      lastgroup = current;
    }

    Profile profile = null;
    if (lastgroup != null && lastgroup.getProfile() != null) {
      profile = unittypes.getSelected().getProfiles().getById(lastgroup.getProfile().getId());
    } else if (group.getProfile() != null) {
      profile = unittypes.getSelected().getProfiles().getById(group.getProfile().getId());
    }

    return profile;
  }

  /**
   * Calculate possible parents.
   *
   * @param g the g
   * @param allGroups the all groups
   * @return the group[]
   */
  private Group[] calculatePossibleParents(Group g, Group[] allGroups) {
    List<Group> notAllowedGroups = g.getAllChildren();
    List<Group> allowedGroups = new ArrayList<>();
    notAllowedGroups.add(g);
    for (Group g1 : allGroups) {
      boolean match = false;
      for (Group g2 : notAllowedGroups) {
        if (g1.getId() == g2.getId()) {
          match = true;
        }
      }
      if (!match) {
        allowedGroups.add(g1);
      }
    }
    Group[] retGroups = new Group[allowedGroups.size()];
    allowedGroups.toArray(retGroups);
    return retGroups;
  }

  /** The Class FindProfileMethod. */
  public class FindProfileMethod implements TemplateMethodModel {
    /**
     * (non-Javadoc)
     *
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    @SuppressWarnings("rawtypes")
    public TemplateModel exec(List arg0) throws TemplateModelException {
      if (arg0.isEmpty()) {
        throw new TemplateModelException("Specify job name");
      }
      Profile foundProfile = findProfile((String) arg0.get(0));
      return new SimpleScalar(foundProfile != null ? foundProfile.getName() : "All profiles");
    }
  }
}
