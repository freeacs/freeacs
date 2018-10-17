package com.github.freeacs.web.app.page.profile;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class ProfileData. */
public class ProfileData extends InputData {
  /** The profilename. */
  private Input profilename = Input.getStringInput("profilename");

  /** The profile copy. */
  private Input profileCopy = Input.getStringInput("profilecopy");

  /** The cmd. */
  private Input cmd = Input.getStringInput("cmd");

  /**
   * Gets the profile copy.
   *
   * @return the profile copy
   */
  public Input getProfileCopy() {
    return profileCopy;
  }

  /**
   * Sets the profile copy.
   *
   * @param profileCopy the new profile copy
   */
  public void setProfileCopy(Input profileCopy) {
    this.profileCopy = profileCopy;
  }

  /**
   * Gets the profilename.
   *
   * @return the profilename
   */
  public Input getProfilename() {
    return profilename;
  }

  /**
   * Sets the profilename.
   *
   * @param profilename the new profilename
   */
  public void setProfilename(Input profilename) {
    this.profilename = profilename;
  }

  public void setCmd(Input cmd) {
    this.cmd = cmd;
  }

  public Input getCmd() {
    return cmd;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
