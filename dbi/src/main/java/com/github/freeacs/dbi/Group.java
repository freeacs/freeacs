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
    if (!name.equals(this.name)) {
      this.oldName = this.name;
    }
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

  /** Logic to prevent a group to reference itself, through one or many parents. */
  public void setParent(Group newParent) {
    if (this.parent != null) {
      parent.removeChild(this);
    }
    if (newParent == null) {
      this.parent = newParent;
      return;
    }
    Group tmpParent = newParent;
    while (tmpParent != null) {
      if (tmpParent.getId().equals(getId())) {
        throw new IllegalArgumentException(
            "Group parent reference loop occurred for "
                + tmpParent.getName()
                + " ("
                + tmpParent.getId()
                + ")");
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
    List<Group> groups = new ArrayList<>();
    for (Group childrenGroup : g.getChildren()) {
      groups.add(childrenGroup);
      groups.addAll(getAllChildrenRec(childrenGroup));
    }
    return groups;
  }

  @Override
  public String toString() {
    if (parent != null) {
      return "[" + id + "] [" + name + "] [" + description + "] [" + parent.getId() + "]";
    }
    return "[" + id + "] [" + name + "] [" + description + "] ";
  }

  protected String getOldName() {
    return oldName;
  }

  public List<Group> getChildren() {
    if (children == null) {
      children = new ArrayList<>();
    }
    return children;
  }

  protected void addChild(Group child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    if (!this.children.contains(child)) {
      this.children.add(child);
    }
  }

  protected void removeChild(Group child) {
    if (children != null) {
      children.remove(child);
    }
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

  /**
   * If current profile has a value already, or the new value is set to null, then perform change.
   *
   * <p>If current profile is null, but the new profile has a value, then perform a check to see
   * that it does not violates the rules for profile setting:
   *
   * <p>All groups should inherit the profile from the top level group.
   */
  public void setProfile(Profile profile) {
    if (profile != null && getParent() != null) {
      Group topGroup = getTopParent();
      Profile topProfile = topGroup.getProfile();
      if (topProfile != null && topProfile.getId().equals(profile.getId())) {
        // profile should only be set on top-level group, but we don't need to throw exception,
        // just silently accept the value
        profile = null;
      } else {
        throw new IllegalArgumentException(
            "Cannot set group profile to something different than the top level group profile");
      }
    }
    this.profile = profile;
  }

  public Profile getProfile() {
    return profile;
  }

  public GroupParameters getGroupParameters() {
    if (parameters == null) {
      Map<Integer, GroupParameter> idMap = new HashMap<>();
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

  public boolean match(Unit unit) {
    Map<String, String> upMap = unit.getParameters();
    Profile groupProfile = getTopParent().getProfile();
    boolean match = true;
    if (groupProfile != null && groupProfile.getId().intValue() != unit.getProfile().getId()) {
      return false;
    }
    Group g = this;
    GroupParameter[] gpArr = getGroupParameters().getGroupParameters();
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
      if (!gpParam.valueWasNull()) {
        gpValue = gpParam.getValue();
      }

      if (!UnitQueryWithinUnittype.match(upValue, gpValue, gpParam.getOp(), gpParam.getType())) {
        return false;
      }
    }
    return true;
  }
}
