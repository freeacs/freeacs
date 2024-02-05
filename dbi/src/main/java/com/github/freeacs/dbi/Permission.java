package com.github.freeacs.dbi;

import lombok.Data;

@Data
public class Permission {
  private Integer id;
  private final User user;
  private final Integer unittypeId;
  private final Integer profileId;

  public Permission(User user, Integer unittypeId, Integer profileId) {
    this.user = user;
    this.unittypeId = unittypeId;
    this.profileId = profileId;
  }

  public boolean isUnittypeAdmin() {
    return profileId == null;
  }
}
