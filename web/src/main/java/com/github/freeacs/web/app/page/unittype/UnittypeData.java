package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class UnittypeData. */
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
   * Sets the newparameter flag.
   *
   * @param newparameterFlag the new newparameter flag
   */
  public void setNewparameterFlag(Input newparameterFlag) {
    this.newparameterFlag = newparameterFlag;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public Input getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(Input description) {
    this.description = description;
  }

  /**
   * Gets the vendor.
   *
   * @return the vendor
   */
  public Input getVendor() {
    return vendor;
  }

  /**
   * Sets the vendor.
   *
   * @param vendor the new vendor
   */
  public void setVendor(Input vendor) {
    this.vendor = vendor;
  }

  /**
   * Gets the newparameter flag.
   *
   * @return the newparameter flag
   */
  public Input getNewparameterFlag() {
    return newparameterFlag;
  }

  /**
   * Sets the newparameter flag i.
   *
   * @param newparameterFlag the new newparameter flag i
   */
  public void setNewparameterFlagI(Input newparameterFlag) {
    this.newparameterFlag = newparameterFlag;
  }

  /**
   * Gets the newparameter name.
   *
   * @return the newparameter name
   */
  public Input getNewparameterName() {
    return newparameterName;
  }

  /**
   * Sets the newparameter name.
   *
   * @param newparameterName the new newparameter name
   */
  public void setNewparameterName(Input newparameterName) {
    this.newparameterName = newparameterName;
  }

  /**
   * Gets the unittype name.
   *
   * @return the unittype name
   */
  public Input getUnittypeName() {
    return unittypeName;
  }

  /**
   * Sets the unittype name.
   *
   * @param unittypeName the new unittype name
   */
  public void setUnittypeName(Input unittypeName) {
    this.unittypeName = unittypeName;
  }

  /**
   * Gets the matcher id.
   *
   * @return the matcher id
   */
  public Input getMatcherId() {
    return matcherId;
  }

  /**
   * Sets the matcher id.
   *
   * @param matcherId the new matcher id
   */
  public void setMatcherId(Input matcherId) {
    this.matcherId = matcherId;
  }

  public void setCmd(Input cmd) {
    this.cmd = cmd;
  }

  public Input getCmd() {
    return cmd;
  }

  /**
   * Gets the protocol.
   *
   * @return the protocol
   */
  public Input getProtocol() {
    return protocol;
  }

  /**
   * Sets the protocol.
   *
   * @param protocol the new protocol
   */
  public void setProtocol(Input protocol) {
    this.protocol = protocol;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
