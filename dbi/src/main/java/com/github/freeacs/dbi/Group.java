package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.MapWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {
	private Integer id;

	private String name;

	private String oldName;

	private String description;

	private Group parent;

	private Unittype unittype;

	private List<Group> children;

	private Profile profile;

	private Integer count;

	//	private UnittypeParameter timeParameter;
	//
	//	private String timeRollingRule;

	private GroupParameters parameters;

	protected Group(Integer id) {
		this.id = id;
	}

	public Group(String name, String description, Group parent, Unittype unittype, Profile profile) {
		this.name = name;
		this.description = description;
		this.unittype = unittype;
		setParent(parent);
		setProfile(profile);
	}

	public Integer getId() {
		return id;
	}

	protected void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!name.equals(this.name))
			this.oldName = this.name;
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Group getParent() {
		return parent;
	}

	public Group getTopParent() {
		Group tmp = this;
		while (tmp.getParent() != null) {
			tmp = tmp.getParent();
		}
		return tmp;
	}

	protected void setParentFromDelete(Group newParent) {
		this.parent = newParent;
	}

	/* 
	 * Logic to prevent a group to reference itself, 
	 * through one or many parents.
	 */
	public void setParent(Group newParent) {
		if (this.parent != null)
			parent.removeChild(this);
		if (newParent == null) {
			this.parent = newParent;
			return;
		}
		Group tmpParent = newParent;
		while (tmpParent != null) {
			if (tmpParent.getId().equals(this.getId())) {
				throw new IllegalArgumentException("Group parent reference loop occurred for " + tmpParent.getName() + " (" + tmpParent.getId() + ")");
			}
			tmpParent = tmpParent.getParent();
		}
		this.parent = newParent;
		this.parent.addChild(this);
	}

	public List<Group> getAllChildren() {
		return getAllChildrenRec(this);
	}

	private List<Group> getAllChildrenRec(Group g) {
		List<Group> groups = new ArrayList<Group>();
		for (Group childrenGroup : g.getChildren()) {
			groups.add(childrenGroup);
			groups.addAll(getAllChildrenRec(childrenGroup));
		}
		return groups;
	}

	@Override
	public String toString() {
		if (parent == null)
			return "[" + id + "] [" + name + "] [" + description + "] ";
		return "[" + id + "] [" + name + "] [" + description + "] [" + parent.getId() + "]";

	}

	protected String getOldName() {
		return oldName;
	}

	public List<Group> getChildren() {
		if (children == null)
			children = new ArrayList<Group>();
		return children;
	}

	protected void addChild(Group child) {
		if (children == null)
			children = new ArrayList<Group>();
		if (!this.children.contains(child))
			this.children.add(child);
	}

	protected void removeChild(Group child) {
		if (children != null)
			children.remove(child);
	}

	public Unittype getUnittype() {
		return unittype;
	}

	protected void setUnittype(Unittype unittype) {
		this.unittype = unittype;
	}

	protected void setProfileFromDelete(Profile profile) {
		this.profile = profile;
	}

	/*
	 * If current profile has a value already, or the new value is set to null, then perform
	 * change. 
	 * 
	 * If current profile is null, but the new profile has a value, then perform
	 * a check to see that it does not violates the rules for profile setting:
	 * 
	 * 	All groups should inherit the profile from the top level group.
	 */
	public void setProfile(Profile profile) {
		if (profile == null) {
			this.profile = profile;
		} else {
			if (this.getParent() != null) {
				Group topGroup = this.getTopParent();
				Profile topProfile = topGroup.getProfile();
				if (topProfile != null && topProfile.getId().equals(profile.getId())) {
					// profile should only be set on top-level group, but we don't need to throw exception, just silently accept the value
					profile = null;
				} else {
					throw new IllegalArgumentException("Cannot set group profile to something different than the top level group profile");
				}
			}
			this.profile = profile;
		}
	}

	public Profile getProfile() {
		return profile;
	}

	public void setGroupParameters(GroupParameters parameters) {
		this.parameters = parameters;
	}

	public GroupParameters getGroupParameters() {
		if (parameters == null) {
			Map<Integer, GroupParameter> idMap = new HashMap<Integer, GroupParameter>();
			MapWrapper<GroupParameter> mw = new MapWrapper<GroupParameter>(ACS.isStrictOrder());
			Map<String, GroupParameter> nameMap = mw.getMap();
			parameters = new GroupParameters(nameMap, idMap, this);
		}
		return parameters;
	}

	protected void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	//	public List<SyslogEvent> getGroupSyncEvents() {
	//		SyslogEvent[] syslogEvents = unittype.getSyslogEvents().getSyslogEvents();
	//		List<SyslogEvent> syslogEventList = new ArrayList<SyslogEvent>();
	//		for (SyslogEvent se : syslogEvents) {
	//			if (se.getTask().getTaskType() == SyslogEventTaskType.GROUPSYNC && se.getTask().getSyncGroup().getName().equals(name)) {
	//				syslogEventList.add(se);
	//			}
	//		}
	//		return syslogEventList;
	//	}
	//
	//	public UnittypeParameter getTimeParameter() {
	//		return timeParameter;
	//	}

	//	public void setTimeParameter(UnittypeParameter timeParameter) {
	//		this.timeParameter = timeParameter;
	//	}
	//
	//	public String getTimeRollingRule() {
	//		return timeRollingRule;
	//	}
	//
	//	public Integer getTimeRollingOffset() {
	//		if (timeRollingRule != null) {
	//			String[] arr = timeRollingRule.split("#");
	//			if (arr.length < 2)
	//				return 0;
	//			else {
	//				try {
	//					return new Integer(arr[1]);
	//				} catch (NumberFormatException nfe) {
	//					return 0;
	//				}
	//			}
	//		} else
	//			return null;
	//	}
	//
	//	public String getTimeRollingFormat() {
	//		if (timeRollingRule != null) {
	//			String[] arr = timeRollingRule.split("#");
	//			if (arr[0].trim().equals(""))
	//				return "yyyyMMdd";
	//			else
	//				return arr[0];
	//		} else
	//			return null;
	//	}
	//
	//	public void setTimeRollingRule(String timeRollingRule) {
	//		this.timeRollingRule = timeRollingRule;
	//	}
	//
	//	public void setTimeRollingFormat(String timeFormat) {
	//		if (timeFormat == null)
	//			timeFormat = "yyyyMMdd";
	//		if (this.timeRollingRule != null) {
	//			String[] arr = timeRollingRule.split("#");
	//			if (arr.length < 2)
	//				this.timeRollingRule = timeFormat + "#0";
	//			else
	//				this.timeRollingRule = timeFormat + "#" + arr[1];
	//		} else {
	//			this.timeRollingRule = timeFormat + "#0";
	//		}
	//	}
	//
	//	public void setTimeRollingOffset(int seconds) {
	//		if (this.timeRollingRule != null) {
	//			String[] arr = timeRollingRule.split("#");
	//			if (arr.length < 2)
	//				this.timeRollingRule += ("#" + seconds);
	//			else
	//				this.timeRollingRule = arr[0] + "#" + seconds;
	//		} else {
	//			this.timeRollingRule = "yyyyMMdd#" + seconds;
	//		}
	//	}

	public boolean match(Unit unit) {
		Map<String, String> upMap = unit.getParameters();
		Profile groupProfile = this.getTopParent().getProfile();
		boolean match = true;
		if (groupProfile != null && groupProfile.getId().intValue() != unit.getProfile().getId())
			return false;
		Group g = this;
		GroupParameter[] gpArr = this.getGroupParameters().getGroupParameters();
		while (match && gpArr != null) {
			if (!matchGroupParameters(upMap, gpArr)) {
				match = false;
				break;
			}
			g = g.getParent();
			if (g != null) {
				gpArr = g.getGroupParameters().getGroupParameters();
			} else {
				gpArr = null;
			}
		}
		return match;
	}

	private boolean matchGroupParameters(Map<String, String> upMap, GroupParameter[] gpArr) {
		for (GroupParameter gp : gpArr) {
			String upValue = upMap.get(gp.getParameter().getUnittypeParameter().getName());
			Parameter gpParam = gp.getParameter();
			String gpValue = null;
			if (!gpParam.valueWasNull())
				gpValue = gpParam.getValue();

			if (!UnitQueryWithinUnittype.match(upValue, gpValue, gpParam.getOp(), gpParam.getType()))
				return false;
		}
		return true;
	}

}
