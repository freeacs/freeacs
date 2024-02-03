package com.github.freeacs.web.security;

import lombok.Getter;
import lombok.Setter;

/**
 * Always connected to an AllowedUnittype.
 *
 * <p>This class is mutable!
 *
 * <p>Represents an allowed profile.
 *
 * @author Jarl Andre Hubenthal
 */
@Setter
@Getter
public class AllowedProfile {
  /** The id. */
  private Integer id;

  /** The name. */
  private String name;

  /**
   * Instantiates a new allowed profile.
   *
   * @param id the id
   */
  public AllowedProfile(Integer id) {
    this.id = id;
  }

}
