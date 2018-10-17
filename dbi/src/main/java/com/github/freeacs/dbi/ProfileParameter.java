package com.github.freeacs.dbi;

public class ProfileParameter {
  private Integer id;

  private Profile profile;

  private UnittypeParameter unittypeParameter;

  private String value;

  public ProfileParameter(Profile profile, UnittypeParameter unittypeParameter, String value) {
    this.id = unittypeParameter.getId();
    this.profile = profile;
    this.unittypeParameter = unittypeParameter;
    setValue(value);
  }

  @Override
  public String toString() {
    return "[" + unittypeParameter.getName() + "] [" + value + "] [" + profile.getName() + "]";
  }

  public Profile getProfile() {
    return profile;
  }

  public UnittypeParameter getUnittypeParameter() {
    return unittypeParameter;
  }

  public String getValue() {
    return value;
  }

  public Integer getId() {
    return id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public void setValue(String value) {
    if (value == null) {
      value = "";
    }
    this.value = value;
  }
}
