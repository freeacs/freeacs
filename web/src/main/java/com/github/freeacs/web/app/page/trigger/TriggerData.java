package com.github.freeacs.web.app.page.trigger;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TriggerData extends InputData {
  private Input triggerId = Input.getIntegerInput("triggerId");
  private Input unitTypeId = Input.getIntegerInput("unitTypeId");
  private Input profileId = Input.getIntegerInput("profileId");
  private Input name = Input.getStringInput("name");
  private Input description = Input.getStringInput("description");
  private Input syslogEventId = Input.getIntegerInput("syslogEventId");
  private Input toList = Input.getStringInput("toList");
  private Input evalPeriodMinutes = Input.getIntegerInput("evalPeriodMinutes");
  private Input numberTotal = Input.getIntegerInput("noTotal");
  private Input numberPerUnit = Input.getIntegerInput("noPrUnit");
  private Input numberOfUnits = Input.getIntegerInput("noUnits");
  private Input notifyIntervalHours = Input.getIntegerInput("notifyIntervalHours");
  private Input triggerType = Input.getIntegerInput("typeTrigger");
  private Input parentTrigger = Input.getIntegerInput("parentTrigger");
  private Input notifyType = Input.getIntegerInput("notifyType");
  private Input action = Input.getStringInput("action");
  private Input scriptFileId = Input.getIntegerInput("scriptFileId");
  private Input active = Input.getBooleanInput("active");

  @Override
  protected void bindForm(Map<String, Object> root) {}

  @Override
  protected boolean validateForm() {
    return false;
  }
}
