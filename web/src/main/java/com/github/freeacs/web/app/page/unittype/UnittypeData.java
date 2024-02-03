package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class UnittypeData. */
@Setter
@Getter
public class UnittypeData extends InputData {
  /** The vendor. */
  private Input vendor = Input.getStringInput("vendor");

  /** The description. */
  private Input description = Input.getStringInput("description");

  /** The unittype name. */
  private Input unittypeName = Input.getStringInput("modelname");

  /** The matcher id. */
  private Input matcherId = Input.getStringInput("matcherid");

  /** The newparameter name. */
  private Input newparameterName = Input.getStringInput("newparametername");

  /** The newparameter flag. */
  private Input newparameterFlag = Input.getStringInput("newparameterflag");

  /** The protocol. */
  private Input protocol = Input.getStringInput("protocol");

  /** The cmd. */
  private Input cmd = Input.getStringInput("cmd");

  /**
   * Sets the newparameter flag i.
   *
   * @param newparameterFlag the new newparameter flag i
   */
  public void setNewparameterFlagI(Input newparameterFlag) {
    this.newparameterFlag = newparameterFlag;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
