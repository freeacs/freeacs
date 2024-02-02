package com.github.freeacs.web.app.page.search;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class SearchData. */
@Getter
@Setter
public class SearchData extends InputData {
  /** The unit param value. */
  private Input unitParamValue = Input.getStringInput("unitparamvalue");

  /** The limit. */
  private Input limit = Input.getIntegerInput("limit");

  /** The mark box. */
  private Input markBox = Input.getStringInput("markbox");

  /** The cmd. */
  private Input cmd = Input.getStringInput("cmd");

  /** The url. */
  private Input url = Input.getStringInput("url");

  /** The advanced. */
  private Input advanced = Input.getBooleanInput("advancedView");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

}
