package com.github.freeacs.web.app.page.permissions;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class PermissionsData. */
@Setter
@Getter
public class PermissionsData extends InputData {
  /** The user. */
  private Input user = Input.getStringInput("user");

  /** The username. */
  private Input username = Input.getStringInput("user_name");

  /** The fullname. */
  private Input fullname = Input.getStringInput("user_fullname");

  /** The web access. */
  private Input webAccess = Input.getStringArrayInput("web_access");

  /** The admin flag. */
  private Input admin = Input.getBooleanInput("user_admin");

  /** The shell access. */
  private Input shellAccess = Input.getStringInput("shell_access");

  /** The permission. */
  private Input permission = Input.getStringArrayInput("permission");

  /** The password. */
  private Input password = Input.getStringInput("user_pass");

  /** The detail submit. */
  private Input detailSubmit = Input.getStringInput("detailsubmit");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

}
