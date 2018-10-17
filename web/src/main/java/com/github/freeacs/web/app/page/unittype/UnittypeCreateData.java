package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

public class UnittypeCreateData extends InputData {
  /** The new description. */
  private Input newDescription = Input.getStringInput("new_description");

  /** The new modelname. */
  private Input newModelname = Input.getStringInput("new_modelname");

  /** The new protocol. */
  private Input newProtocol = Input.getStringInput("new_protocol");

  /** The new vendor. */
  private Input newVendor = Input.getStringInput("new_vendor");

  /** The new matcherid. */
  private Input newMatcherid = Input.getStringInput("new_matcherid");

  /** The unittype to copy from. */
  private Input unittypeToCopyFrom = Input.getStringInput("unittypeToCopyFrom");

  @Override
  protected void bindForm(Map<String, Object> root) {}

  @Override
  protected boolean validateForm() {
    return false;
  }

  /**
   * Gets the new description.
   *
   * @return the new description
   */
  public Input getNewDescription() {
    return newDescription;
  }

  /**
   * Sets the new description.
   *
   * @param newDescription the new new description
   */
  public void setNewDescription(Input newDescription) {
    this.newDescription = newDescription;
  }

  /**
   * Gets the new modelname.
   *
   * @return the new modelname
   */
  public Input getNewModelname() {
    return newModelname;
  }

  /**
   * Sets the new modelname.
   *
   * @param newModelname the new new modelname
   */
  public void setNewModelname(Input newModelname) {
    this.newModelname = newModelname;
  }

  /**
   * Gets the new protocol.
   *
   * @return the new protocol
   */
  public Input getNewProtocol() {
    return newProtocol;
  }

  /**
   * Sets the new protocol.
   *
   * @param newProtocol the new new protocol
   */
  public void setNewProtocol(Input newProtocol) {
    this.newProtocol = newProtocol;
  }

  /**
   * Gets the new vendor.
   *
   * @return the new vendor
   */
  public Input getNewVendor() {
    return newVendor;
  }

  /**
   * Sets the new vendor.
   *
   * @param newVendor the new new vendor
   */
  public void setNewVendor(Input newVendor) {
    this.newVendor = newVendor;
  }

  /**
   * Gets the new matcherid.
   *
   * @return the new matcherid
   */
  public Input getNewMatcherid() {
    return newMatcherid;
  }

  /**
   * Sets the new matcherid.
   *
   * @param newMatcherid the new new matcherid
   */
  public void setNewMatcherid(Input newMatcherid) {
    this.newMatcherid = newMatcherid;
  }

  /**
   * Gets the unittype to copy from.
   *
   * @return the unittype to copy from
   */
  public Input getUnittypeToCopyFrom() {
    return unittypeToCopyFrom;
  }

  /**
   * Sets the unittype to copy from.
   *
   * @param unittypeToCopyFrom the new unittype to copy from
   */
  public void setUnittypeToCopyFrom(Input unittypeToCopyFrom) {
    this.unittypeToCopyFrom = unittypeToCopyFrom;
  }
}
