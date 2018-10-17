package com.github.freeacs.dbi;

public class Permission {
  private Integer id;
  private User user;
  private Integer unittypeId;
  private Integer profileId;

  public Permission(User user, Integer unittypeId, Integer profileId) {
    this.user = user;
    this.unittypeId = unittypeId;
    this.profileId = profileId;
  }

  public User getUser() {
    return user;
  }

  public Integer getId() {
    return id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public Integer getUnittypeId() {
    return unittypeId;
  }

  public Integer getProfileId() {
    return profileId;
  }

  public boolean isUnittypeAdmin() {
    return profileId == null;
  }
}
