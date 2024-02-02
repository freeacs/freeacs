package com.github.freeacs.web.app.page.group;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Group Configuration and Group Create input definition.
 *
 * @author Jarl Andre Hubenthal
 */
@Getter
@Setter
public class GroupData extends InputData {

  /** The groupname. */
  private Input groupname = Input.getStringInput("groupname");

  /** The description. */
  private Input description = Input.getStringInput("description");

  /** The parentgroup. */
  private Input parentgroup = Input.getStringInput("parentgroup");

  /** The cmd. */
  private Input cmd = Input.getStringInput("cmd");

  /** The time rolling parameter. */
  private Input timeRollingParameter = Input.getStringInput("timerollingparameter");

  /** The time rolling format. */
  private Input timeRollingFormat = Input.getStringInput("timerollingformat");

  /** The time rolling offset. */
  private Input timeRollingOffset = Input.getIntegerInput("timerollingoffset");

  /** The time rolling enabled. */
  private Input timeRollingEnabled = Input.getBooleanInput("timerollingenabled");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
