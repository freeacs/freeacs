package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class UnittypeParametersData. */
public class UnittypeParametersData extends InputData {
  /** The form submit. */
  private Input formSubmit = Input.getStringInput("addparameters");

  public Input getFormSubmit() {
    return formSubmit;
  }

  public void setFormSubmit(Input formSubmit) {
    this.formSubmit = formSubmit;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
