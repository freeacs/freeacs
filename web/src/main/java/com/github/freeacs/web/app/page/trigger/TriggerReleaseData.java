package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import java.util.Map;

public class TriggerReleaseData extends InputData {
  private Input triggerId = Input.getIntegerInput("triggerId");
  private Input tms = Input.getDateInput("tms", DateUtils.Format.DEFAULT);

  @Override
  protected void bindForm(Map<String, Object> root) {}

  @Override
  protected boolean validateForm() {
    return false;
  }

  public Input getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(Input triggerId) {
    this.triggerId = triggerId;
  }

  public Input getTms() {
    return tms;
  }

  public void setTms(Input tms) {
    this.tms = tms;
  }
}
