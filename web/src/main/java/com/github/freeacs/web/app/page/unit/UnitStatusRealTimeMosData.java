package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class UnitStatusRealTimeMosData. */
@Setter
@Getter
public class UnitStatusRealTimeMosData extends InputData {
  /** The start. */
  private Input start = Input.getDateInput("start", DateUtils.Format.WITH_SECONDS);

  /** The end. */
  private Input end = Input.getDateInput("end", DateUtils.Format.WITH_SECONDS);

  /** The channel. */
  private Input channel = Input.getIntegerInput("channel");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
