package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TriggerReleaseData extends InputData {
  private Input triggerId = Input.getIntegerInput("triggerId");
  private Input tms = Input.getDateInput("tms", DateUtils.Format.DEFAULT);

  @Override
  protected void bindForm(Map<String, Object> root) {}

  @Override
  protected boolean validateForm() {
    return false;
  }
}
