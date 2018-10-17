package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.MapWrapper;
import java.util.HashMap;
import java.util.Map;

public class Profile {
  private Integer id;
  private String name;
  private String oldName;
  private Unittype unittype;
  private ProfileParameters profileParameters;

  public Profile(String name, Unittype unittype) {
    if (name == null || "".equals(name.trim())) {
      throw new IllegalArgumentException("Profile name cannot be null or an empty string");
    }
    this.name = name;
    this.unittype = unittype;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Unittype getUnittype() {
    return unittype;
  }

  @Override
  public String toString() {
    return "[" + id + "] [" + name + "]";
  }

  public ProfileParameters getProfileParameters() {
    if (profileParameters == null) {
      Map<Integer, ProfileParameter> idMap = new HashMap<>();
      MapWrapper<ProfileParameter> mw = new MapWrapper<ProfileParameter>(ACS.isStrictOrder());
      Map<String, ProfileParameter> nameMap = mw.getMap();
      profileParameters = new ProfileParameters(idMap, nameMap, this);
    }
    return profileParameters;
  }

  protected void setProfileParameters(ProfileParameters profileParameters) {
    this.profileParameters = profileParameters;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  protected String getOldName() {
    return oldName;
  }

  public void setName(String name) {
    if (!name.equals(this.name)) {
      this.oldName = this.name;
    }
    this.name = name;
  }

  protected void setOldName(String oldName) {
    this.oldName = oldName;
  }
}
