package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
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

}
