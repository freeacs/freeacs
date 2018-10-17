package com.github.freeacs.web.app.input;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * The InputData definition, with shared inputs, constants and methods.
 *
 * @author Jarl Andre Hubenthal
 */
public abstract class InputData {
  /** Constants. */
  public static final String INFO = "info";

  /** The Constant ERROR. */
  public static final String ERROR = "ERROR";

  /** The Constant ASYNC. */
  public static final String ASYNC = "async";

  /** The Constant EXPORT. */
  public static final String EXPORT = "export";
  /** Submit button values. */
  public String CMD_ADD = "Add";

  /** The CM d_ update. */
  public String CMD_UPDATE = "Update";

  /** The CM d_ delete. */
  public String CMD_DELETE = "Delete";

  /** Shared inputs. */
  private Input group = Input.getStringInput("group");

  /** The unittype. */
  private Input unittype = Input.getStringInput("unittype");

  /** The form submit. */
  private Input formSubmit = Input.getStringInput("formsubmit");

  /** The profile. */
  private Input profile = Input.getStringInput("profile");

  /** The unit. */
  private Input unit = Input.getStringInput("unit");

  /** The filter string. */
  private Input filterString = Input.getStringInput("filterstring");

  /** The filter flag. */
  private Input filterFlag = Input.getStringInput("filterflag");

  /** The filter type. */
  private Input filterType = Input.getStringInput("filtertype");

  /** The cmd. */
  private Input cmd = Input.getStringInput("cmd");

  /** The export. */
  private Input export = Input.getStringInput("export");

  /** The job. */
  private Input job = Input.getStringInput("job");

  /** The async. */
  private Input async = Input.getStringInput(ASYNC);

  /**
   * Checks if is delete operation.
   *
   * @return true, if is delete operation
   */
  public boolean isDeleteOperation() {
    return getFormSubmit().isValue(CMD_DELETE);
  }

  /**
   * Checks if is update operation.
   *
   * @return true, if is update operation
   */
  public boolean isUpdateOperation() {
    return getFormSubmit().isValue(CMD_UPDATE);
  }

  /**
   * Checks if is adds the operation.
   *
   * @return true, if is adds the operation
   */
  public boolean isAddOperation() {
    return getFormSubmit().isValue(CMD_ADD);
  }

  /**
   * Collects errors from all Input objects.
   *
   * @return The map of errors
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  public final Map<String, String> getErrors()
      throws IllegalAccessException, InvocationTargetException {
    Map<String, String> map = new HashMap<>();
    Method[] methods = getClass().getMethods();
    for (Method m : methods) {
      if (m.getReturnType() == Input.class && Modifier.isPublic(m.getModifiers())) {
        Input in = (Input) m.invoke(this, (Object[]) null);
        if (in.getError() != null) {
          map.put(in.getKey(), in.getError());
        }
      }
    }
    return map;
  }

  /**
   * Binds form input to the given root map
   *
   * <p>Override it in the implementing class.
   *
   * @param root the root
   */
  protected abstract void bindForm(Map<String, Object> root);

  /**
   * Validates the form input and returns the result as a boolean
   *
   * <p>Override it in the implementing class.
   *
   * @return True if not overridden
   */
  protected abstract boolean validateForm();

  /**
   * 1. Binds the form input to the given root map<br>
   * 2. Validates the form input<br>
   * - If not validated the errors will be added to the map
   *
   * @param root the root
   * @return True if valid
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  public final boolean bindAndValidate(Map<String, Object> root)
      throws IllegalAccessException, InvocationTargetException {
    bindForm(root);
    if (!validateForm()) {
      root.put("errors", getErrors());
      return false;
    }
    return true;
  }

  /**
   * Gets the form submit.
   *
   * @return the form submit
   */
  public Input getFormSubmit() {
    return formSubmit;
  }

  /**
   * Gets the job.
   *
   * @return the job
   */
  public Input getJob() {
    return job;
  }

  /**
   * Gets the unittype.
   *
   * @return the unittype
   */
  public Input getUnittype() {
    return unittype;
  }

  /**
   * Gets the profile.
   *
   * @return the profile
   */
  public Input getProfile() {
    return profile;
  }

  /**
   * Gets the filter type.
   *
   * @return the filter type
   */
  public Input getFilterType() {
    return filterType;
  }

  /**
   * Gets the filter string.
   *
   * @return the filter string
   */
  public Input getFilterString() {
    return filterString;
  }

  /**
   * Gets the filter flag.
   *
   * @return the filter flag
   */
  public Input getFilterFlag() {
    return filterFlag;
  }

  /**
   * Gets the group.
   *
   * @return the group
   */
  public Input getGroup() {
    return group;
  }

  /**
   * Gets the unit.
   *
   * @return the unit
   */
  public Input getUnit() {
    return unit;
  }

  /**
   * Gets the cmd.
   *
   * @return the cmd
   */
  public Input getCmd() {
    return cmd;
  }

  /**
   * Gets the export.
   *
   * @return the export
   */
  public Input getExport() {
    return export;
  }

  /**
   * Sets the job.
   *
   * @param jobname the new job
   */
  public void setJob(Input jobname) {
    this.job = jobname;
  }

  /**
   * Sets the unittype.
   *
   * @param unittype the new unittype
   */
  public void setUnittype(Input unittype) {
    this.unittype = unittype;
  }

  /**
   * Sets the form submit.
   *
   * @param formSubmit the new form submit
   */
  public void setFormSubmit(Input formSubmit) {
    this.formSubmit = formSubmit;
  }

  /**
   * Sets the profile.
   *
   * @param profile the new profile
   */
  public void setProfile(Input profile) {
    this.profile = profile;
  }

  /**
   * Sets the filter type.
   *
   * @param filterType the new filter type
   */
  public void setFilterType(Input filterType) {
    this.filterType = filterType;
  }

  /**
   * Sets the filter string.
   *
   * @param filterString the new filter string
   */
  public void setFilterString(Input filterString) {
    this.filterString = filterString;
  }

  /**
   * Sets the filter flag.
   *
   * @param filterFlag the new filter flag
   */
  public void setFilterFlag(Input filterFlag) {
    this.filterFlag = filterFlag;
  }

  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(Input group) {
    this.group = group;
  }

  /**
   * Sets the unit.
   *
   * @param unit the new unit
   */
  public void setUnit(Input unit) {
    this.unit = unit;
  }

  /**
   * Sets the cmd.
   *
   * @param cmd the new cmd
   */
  public void setCmd(Input cmd) {
    this.cmd = cmd;
  }

  /**
   * Sets the export.
   *
   * @param export the new export
   */
  public void setExport(Input export) {
    this.export = export;
  }

  /**
   * Sets the async.
   *
   * @param async the new async
   */
  public void setAsync(Input async) {
    this.async = async;
  }

  /**
   * Gets the async.
   *
   * @return the async
   */
  public Input getAsync() {
    return async;
  }
}
