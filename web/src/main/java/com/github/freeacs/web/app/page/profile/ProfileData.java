package com.github.freeacs.web.app.page.profile;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class ProfileData. */
@Setter
@Getter
public class ProfileData extends InputData {
  /** The profilename. */
  private Input profilename = Input.getStringInput("profilename");

  /** The profile copy. */
  private Input profileCopy = Input.getStringInput("profilecopy");

  /** The cmd. */
  private Input cmd = Input.getStringInput("cmd");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
