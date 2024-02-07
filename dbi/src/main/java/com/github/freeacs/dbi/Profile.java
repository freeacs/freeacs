package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.MapWrapper;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Profile {
  private Integer id;
  private String name;
  private String oldName;
  private final Unittype unittype;
  private ProfileParameters profileParameters;

  public Profile(String name, Unittype unittype) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Profile name cannot be null or an empty string");
    }
    this.name = name;
    this.unittype = unittype;
  }

  public ProfileParameters getProfileParameters() {
    if (profileParameters == null) {
      Map<Integer, ProfileParameter> idMap = new HashMap<>();
      MapWrapper<ProfileParameter> mw = new MapWrapper<>(ACS.isStrictOrder());
      Map<String, ProfileParameter> nameMap = mw.getMap();
      profileParameters = new ProfileParameters(idMap, nameMap, this);
    }
    return profileParameters;
  }

  public void setName(String name) {
    if (!name.equals(this.name)) {
      this.oldName = this.name;
    }
    this.name = name;
  }
}
