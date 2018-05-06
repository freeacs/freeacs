package com.owera.xaps.web.app.page.group;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.GroupParameter;
import com.owera.xaps.dbi.GroupParameters;
import com.owera.xaps.dbi.Groups;
import com.owera.xaps.dbi.Parameter;
import com.owera.xaps.dbi.Parameter.Operator;
import com.owera.xaps.dbi.Parameter.ParameterDataType;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.InputDataIntegrity;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.menu.MenuItem;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.page.search.SearchParameter;
import com.owera.xaps.web.app.page.unittype.UnittypeParameterFlags;
import com.owera.xaps.web.app.page.unittype.UnittypeParameterTypes;
import com.owera.xaps.web.app.table.TableElement;
import com.owera.xaps.web.app.table.TableElementMaker;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.SessionData;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * A page for Group Configuration and Group Create.
 * 
 * @author Jarl Andre Hubenthal
 */
public class GroupPage extends AbstractWebPage {

	/** The xaps. */
	private XAPS xaps;

	/** The xaps unit. */
	private XAPSUnit xapsUnit;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owera.xaps.web.app.page.AbstractWebPage#getTitle(java.lang.String)
	 */
	public String getTitle(String page) {
		return super.getTitle(page) + (groups.getSelected() != null ? " | " + groups.getSelected().getName() : "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owera.xaps.web.app.page.AbstractWebPage#getShortcutItems(com.owera
	 * .xaps.web.app.util.SessionData)
	 */
	public List<MenuItem> getShortcutItems(SessionData sessionData) {
		List<MenuItem> list = new ArrayList<MenuItem>();
		list.addAll(super.getShortcutItems(sessionData));
		list.add(new MenuItem("Group overview", Page.GROUPSOVERVIEW));
		if (groups.getSelected() != null) {
			MenuItem unitsThatMatches = new MenuItem("Search for units that matches", Page.SEARCH);
			unitsThatMatches.addCommand("auto");
			unitsThatMatches.addParameter("advancedView", "true");
			unitsThatMatches.addParameter("formsubmit", "Search");
			unitsThatMatches.addParameter("unittype", groups.getSelected().getUnittype().getName());
			unitsThatMatches.addParameter("profile", (groups.getSelected().getTopParent().getProfile() != null ? groups.getSelected().getTopParent().getProfile().getName()
					: WebConstants.ALL_ITEMS_OR_DEFAULT));
			unitsThatMatches.addParameter("group", groups.getSelected().getName());
			list.add(unitsThatMatches);
		}
		return list;
	}

	/**
	 * Action delete group.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void actionDeleteGroup() throws Exception {
		if (groups.getSelected() != null) {
			unittypes.getSelected().getGroups().deleteGroup(groups.getSelected(), xaps);
			inputData.getGroup().setValue(null);
			SessionCache.getSessionData(sessionId).setGroup(null);
		}
	}

	/**
	 * Action cud parameters.
	 * 
	 * @param req
	 *            the req
	 * @throws Exception
	 *             the exception
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
			if (key.length() > 8)
				gpName = key.substring(8);
			String convertedGroupParameterName = SearchParameter.convertParameterId(gpName);
			GroupParameter groupParameter = gParams.getByName(convertedGroupParameterName);

			// First we process updating and creating
			if (key.startsWith("update::") || key.startsWith("create::")) {
				String opStr = req.getParameter("operator::" + gpName);
				Operator operator = Operator.EQ;
				if (opStr != null && isOperatorValid(opStr))
					operator = Operator.getOperator(opStr);

				String typeStr = req.getParameter("datatype::" + gpName);
				ParameterDataType type = ParameterDataType.TEXT;
				if (typeStr != null && isParameterDataTypeValid(typeStr))
					type = ParameterDataType.getDataType(typeStr);

				// if no group parameter exists and this is a create key
				if (groupParameter == null && key.startsWith("create::")) {
					String newValue = req.getParameter("update::" + gpName);
					if (newValue != null && newValue.equals("NULL"))
						newValue = null;
					newValue = SearchParameter.convertParameterValue(newValue);
					UnittypeParameter utp = groups.getSelected().getUnittype().getUnittypeParameters().getByName(gpName);
					Parameter param = new Parameter(utp, newValue, operator, type);
					GroupParameter newGP = new GroupParameter(param, groups.getSelected());
					gParams.addOrChangeGroupParameter(newGP, xaps);
				}

				// If a group parameter exists and there is a value to replace
				// the current value
				// The last part in this if test is to check if this is not a
				// previously created group parameter or a parameter that should
				// be created
				// We only want to process normal updating of existing group
				// parameters
				if (groupParameter != null && req.getParameter("update::" + gpName) != null && req.getParameter("create::" + gpName) == null) {
					String updatedValue = req.getParameter("update::" + gpName);
					if (updatedValue != null && updatedValue.equals("NULL"))
						updatedValue = null;
					updatedValue = SearchParameter.convertParameterValue(updatedValue);
					if ((!groupParameter.getParameter().getValue().equals(updatedValue) || groupParameter.getParameter().getOp() != operator) || groupParameter.getParameter().getType() != type) {
						Parameter param = new Parameter(groupParameter.getParameter().getUnittypeParameter(), updatedValue, operator, type);
						groupParameter.setParameter(param);
						gParams.addOrChangeGroupParameter(groupParameter, xaps);
					}
				}
			}

			// Then we delete those that should be deleted
			if (key.startsWith("delete::") && groupParameter != null) {
				gParams.deleteGroupParameter(groupParameter, xaps);
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
	 * @throws Exception
	 *             the exception
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

				if (inputData.getProfile().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT))
					groups.getSelected().setProfile(unittypes.getSelected().getProfiles().getByName(inputData.getProfile().getString()));
				else
					groups.getSelected().setProfile(null);
			} catch (IllegalArgumentException ie) {
				groups.getSelected().setParent(oldParent != null ? oldParent : null);
				groups.getSelected().setProfile(oldProfile != null ? oldProfile : null);
				groups.getSelected().setDescription(oldDescription != null ? oldDescription : null);
				throw ie;
			}

			allGroups.addOrChangeGroup(groups.getSelected(), xaps);

			inputData.getGroup().setValue(groups.getSelected().getName());
		}
	}

	/**
	 * Action create group.
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	private String actionCreateGroup() throws Exception {
		String gName = inputData.getGroupname().getStringWithoutTags();
		String desc = inputData.getDescription().getString();

		Group parent = parents.getSelected();
		Profile profile = null;

		Groups allGroups = unittypes.getSelected().getGroups();

		if (inputData.getProfile().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT))
			profile = unittypes.getSelected().getProfiles().getByName(inputData.getProfile().getString());
		else if (isProfilesLimited(unittypes.getSelected(), sessionId))
			throw new Exception("You are not allowed to create groups!");

		if (gName != null && desc != null) {
			if (allGroups.getByName(gName) != null)
				return "The group " + gName + " is already created";
			groups.setSelected(new Group(gName, desc, parent, unittypes.getSelected(), profile));
			allGroups.addOrChangeGroup(groups.getSelected(), xaps);
			SessionCache.getSessionData(sessionId).setGroup(gName);
			return "OK";
		} else {
			return "Name and description is required";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input
	 * .ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		// important object var
		inputData = (GroupData) InputDataRetriever.parseInto(new GroupData(), params);

		sessionId = params.getSession().getId();

		xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		xapsUnit = XAPSLoader.getXAPSUnit(sessionId);

		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype(), inputData.getGroup());

		if (inputData.getCmd().hasValue("create"))
			inputData.getGroup().setValue(null);

		unittypes = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		groups = InputSelectionFactory.getGroupSelection(inputData.getGroup(), unittypes.getSelected(), xaps);
		Group selectedParent = (unittypes.getSelected() != null && inputData.getParentgroup().getString() != null ? unittypes.getSelected().getGroups()
				.getByName(inputData.getParentgroup().getString()) : (groups.getSelected() != null ? groups.getSelected().getParent() : null));
		List<Group> possibleParents = (groups.getSelected() != null ? Arrays.asList(calculatePossibleParents(groups.getSelected(), unittypes.getSelected().getGroups().getGroups())) : (unittypes
				.getSelected() != null ? Arrays.asList(unittypes.getSelected().getGroups().getGroups()) : new ArrayList<Group>()));
		parents = InputSelectionFactory.getDropDownSingleSelect(inputData.getParentgroup(), selectedParent, possibleParents);

		String createMessage = null;

		// action
		if (inputData.getFormSubmit().isValue(WebConstants.DELETE)) {
			actionDeleteGroup();
			outputHandler.setDirectToPage(Page.GROUPSOVERVIEW);
			return;
		} else if (inputData.getFormSubmit().isValue(WebConstants.UPDATE))
			actionUpdateGroup();
		else if (inputData.getFormSubmit().isValue("Create group")) {
			createMessage = actionCreateGroup();
		} else if (inputData.getFormSubmit().isValue(WebConstants.UPDATE_PARAMS))
			actionCUDParameters(params);

		Map<String, Object> map = outputHandler.getTemplateMap();

		if (inputData.getCmd().hasValue("create")) {
			if (createMessage != null && createMessage.equals("OK") && groups.getSelected() != null) {
				outputHandler.setDirectToPage(Page.GROUP);
				return;
			} else {
				groups.setSelected(null);
				map.put("message", createMessage);
			}
			map.put("name", inputData.getGroupname().getString());
			map.put("description", inputData.getDescription().getString());
		} else if (unittypes.getSelected() != null && groups.getSelected() != null) {
			List<Parameter> gParams = groups.getSelected().getGroupParameters().getAllParameters(groups.getSelected());
			int unitCount = xapsUnit.getUnitCount(unittypes.getSelected(), findProfile(groups.getSelected().getName()), gParams);
			Groups allGroups = unittypes.getSelected().getGroups();
			groups.getSelected().setCount(unitCount);
			allGroups.addOrChangeGroup(groups.getSelected(), xaps);
			map.put("count", unitCount);

			GroupParameter[] groupParameters = groups.getSelected().getGroupParameters().getGroupParameters();
			List<TableElement> parameters = new TableElementMaker().getParameters(unittypes.getSelected().getUnittypeParameters().getUnittypeParameters(), groupParameters);
			map.put("params", parameters);
			map.put("filterflags",
					InputSelectionFactory.getDropDownSingleSelect(inputData.getFilterFlag(), UnittypeParameterFlags.getByValue(inputData.getFilterFlag().getString()),
							Arrays.asList(UnittypeParameterFlags.values())));
			map.put("filtertypes", InputSelectionFactory.getDropDownSingleSelect(inputData.getFilterType(),
					UnittypeParameterTypes.valueOf(inputData.getFilterType().getString(groups.getSelected().getGroupParameters().getGroupParameters().length > 0 ? "Configured" : "All")),
					Arrays.asList(UnittypeParameterTypes.values())));
			map.put("filterstring", inputData.getFilterString());
			map.put("groupjobs", unittypes.getSelected().getJobs().getGroupJobs(groups.getSelected().getId()));
			map.put("getgroupprofile", new FindProfileMethod());
			map.put("operators", Parameter.Operator.values());
			map.put("datatypes", Parameter.ParameterDataType.values());
		} else {
			outputHandler.setDirectToPage(Page.GROUP, "cmd=create");
			return;
		}

		map.put("unittypes", unittypes);
		map.put("groups", groups);
		map.put("parents", parents);
		map.put("profiles", addGroupProfile());

		if (groups.getSelected() == null)
			outputHandler.setTemplatePath("group/create");
		else
			outputHandler.setTemplatePath("group/details");
	}

	/**
	 * Adds the group profile.
	 * 
	 * @return the drop down single select
	 * @throws IllegalArgumentException
	 *             the illegal argument exception
	 * @throws SecurityException
	 *             the security exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws NoAvailableConnectionException
	 *             the no available connection exception
	 * @throws SQLException
	 *             the sQL exception
	 */
	private DropDownSingleSelect<Profile> addGroupProfile() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			NoAvailableConnectionException, SQLException {
		if (unittypes.getSelected() == null)
			return null;

		Group parentGroup = parents.getSelected();

		if (groups.getSelected() != null && groups.getSelected().getParent() != null)
			parentGroup = groups.getSelected().getParent();

		Profile profile = parentGroup != null ? findProfile(parentGroup.getName()) : groups.getSelected() != null ? findProfile(groups.getSelected().getName())
				: (inputData.getProfile().getString() != null ? xaps.getProfile(unittypes.getSelected().getName(), inputData.getProfile().getString()) : null);

		List<Profile> allowedProfiles = parentGroup == null ? getAllowedProfiles(sessionId, unittypes.getSelected()) : new ArrayList<Profile>();

		DropDownSingleSelect<Profile> profiles = InputSelectionFactory.getDropDownSingleSelect(inputData.getProfile(), profile, allowedProfiles);

		return profiles;
	}

	/**
	 * Find profile.
	 * 
	 * @param groupName
	 *            the group name
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
		if (lastgroup != null && lastgroup.getProfile() != null)
			profile = unittypes.getSelected().getProfiles().getById(lastgroup.getProfile().getId());
		else if (group.getProfile() != null)
			profile = unittypes.getSelected().getProfiles().getById(group.getProfile().getId());

		if (profile != null)
			return profile;
		return null;
	}

	/**
	 * Calculate possible parents.
	 * 
	 * @param g
	 *            the g
	 * @param allGroups
	 *            the all groups
	 * @return the group[]
	 */
	private Group[] calculatePossibleParents(Group g, Group[] allGroups) {
		List<Group> notAllowedGroups = g.getAllChildren();
		List<Group> allowedGroups = new ArrayList<Group>();
		notAllowedGroups.add(g);
		for (Group g1 : allGroups) {
			boolean match = false;
			for (Group g2 : notAllowedGroups) {
				if (g1.getId() == g2.getId())
					match = true;
			}
			if (!match)
				allowedGroups.add(g1);
		}
		Group[] retGroups = new Group[allowedGroups.size()];
		allowedGroups.toArray(retGroups);
		return retGroups;
	}

	/**
	 * The Class FindProfileMethod.
	 */
	public class FindProfileMethod implements TemplateMethodModel {

		/*
		 * (non-Javadoc)
		 * 
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public TemplateModel exec(List arg0) throws TemplateModelException {
			if (arg0.size() < 1)
				throw new TemplateModelException("Specify job name");
			Profile foundProfile = findProfile((String) arg0.get(0));
			return new SimpleScalar(foundProfile != null ? foundProfile.getName() : "All profiles");
		}
	}
}