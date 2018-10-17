package com.github.freeacs.web.app.page.permissions;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class PermissionsData. */
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

  /**
   * Gets the username.
   *
   * @return the username
   */
  public Input getUsername() {
    return username;
  }

  /**
   * Sets the username.
   *
   * @param username the new username
   */
  public void setUsername(Input username) {
    this.username = username;
  }

  /**
   * Gets the web access.
   *
   * @return the web access
   */
  public Input getWebAccess() {
    return webAccess;
  }

  /**
   * Sets the web access.
   *
   * @param access the new web access
   */
  public void setWebAccess(Input access) {
    this.webAccess = access;
  }

  /**
   * Gets the password.
   *
   * @return the password
   */
  public Input getPassword() {
    return password;
  }

  /**
   * Sets the password.
   *
   * @param password the new password
   */
  public void setPassword(Input password) {
    this.password = password;
  }

  /**
   * Sets the user.
   *
   * @param user the new user
   */
  public void setUser(Input user) {
    this.user = user;
  }

  /**
   * Gets the user.
   *
   * @return the user
   */
  public Input getUser() {
    return user;
  }

  /**
   * Sets the fullname.
   *
   * @param fullname the new fullname
   */
  public void setFullname(Input fullname) {
    this.fullname = fullname;
  }

  /**
   * Gets the fullname.
   *
   * @return the fullname
   */
  public Input getFullname() {
    return fullname;
  }

  /**
   * Sets the permission.
   *
   * @param permission the new permission
   */
  public void setPermission(Input permission) {
    this.permission = permission;
  }

  /**
   * Gets the permission.
   *
   * @return the permission
   */
  public Input getPermission() {
    return permission;
  }

  /**
   * Sets the detail submit.
   *
   * @param detailSubmit the new detail submit
   */
  public void setDetailSubmit(Input detailSubmit) {
    this.detailSubmit = detailSubmit;
  }

  /**
   * Gets the detail submit.
   *
   * @return the detail submit
   */
  public Input getDetailSubmit() {
    return detailSubmit;
  }

  /**
   * Sets the shell access.
   *
   * @param shellAccess the new shell access
   */
  public void setShellAccess(Input shellAccess) {
    this.shellAccess = shellAccess;
  }

  /**
   * Gets the shell access.
   *
   * @return the shell access
   */
  public Input getShellAccess() {
    return shellAccess;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

  public Input getAdmin() {
    return admin;
  }

  public void setAdmin(Input admin) {
    this.admin = admin;
  }
}
