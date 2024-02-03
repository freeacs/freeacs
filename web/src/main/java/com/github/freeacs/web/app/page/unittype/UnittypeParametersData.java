package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;

import java.util.Map;

/** The Class UnittypeParametersData. */
@Getter
public class UnittypeParametersData extends InputData {
  /** The form submit. */
  private final Input formSubmit = Input.getStringInput("addparameters");

    @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
