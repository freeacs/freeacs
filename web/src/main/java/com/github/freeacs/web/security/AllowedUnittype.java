package com.github.freeacs.web.security;

import com.github.freeacs.dbi.Permission;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is mutable!
 *
 * <p>Represents an allowed unittype.
 *
 * @author Jarl Andre Hubenthal
 */
@Getter
public class AllowedUnittype {
  /** The id. */
  private Integer id;

  /** The name. */
  private String name;

  /** The profile. */
  private AllowedProfile profile;

  /**
   * Instantiates a new allowed unittype.
   *
   * @param id the id of the unittype
   */
  public AllowedUnittype(Integer id) {
    this.id = id;
  }

  /**
   * Instantiates a new allowed unittype.
   *
   * @param name the name of the unittype
   */
  public AllowedUnittype(String name) {
    this.name = name;
  }

  /**
   * Sets the profile.
   *
   * @param profileId the profile id
   */
  public void setProfile(Integer profileId) {
    this.profile = new AllowedProfile(profileId);
  }

  public static AllowedUnittype[] retrieveAllowedUnittypes(WebUser usr) {
    List<AllowedUnittype> uts = new ArrayList<>();
    if (usr.getPermissions() != null) {
      if (usr.getPermissions().getPermissions().length == 0) {
        return new AllowedUnittype[] {new AllowedUnittype("*")};
      }
      for (Permission permission : usr.getPermissions().getPermissions()) {
        AllowedUnittype allowed = new AllowedUnittype(permission.getUnittypeId());
        allowed.setProfile(permission.getProfileId());
        uts.add(allowed);
      }
    } else {
      uts.add(new AllowedUnittype("*"));
    }
    return uts.toArray(new AllowedUnittype[] {});
  }
}
