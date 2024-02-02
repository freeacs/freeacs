package com.github.freeacs.web.app.input;

import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
public abstract class InputData {

  /** The Constant ASYNC. */
  public static final String ASYNC = "async";

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

}
