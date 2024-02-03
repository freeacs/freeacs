package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class UnitData extends InputData {
  private Input unitParameter = Input.getStringInput("unitparameter");
  private Input unitParameterValue = Input.getStringInput("unitparametervalue");
  private Input filter = Input.getStringInput("filter");
  private Input cmd = Input.getStringInput("cmd");
  private Input initProvisioning = Input.getStringInput("init_provisioning");
  private Input initReadAll = Input.getStringInput("init_readall");
  private Input initRestart = Input.getStringInput("init_restart");
  private Input initReset = Input.getStringInput("init_reset");
  private Input initRefreshPage = Input.getStringInput("init_refreshpage");
  private Input frequency = Input.getStringInput("frequency");
  private Input spread = Input.getStringInput("spread");
  private Input changeFreqSpread = Input.getStringInput("change_freqspread");
  private Input unitUpgrade = Input.getStringArrayInput("unitupgrade");
  private Input showConfidential = Input.getBooleanInput("show_confidential");
  /**
   * Private Input modeSelect = Input.getStringInput("modeselect"); private Input inspectAction =
   * Input.getStringInput("inspectaction");
   */
  private Input unitDelete = Input.getStringInput("unitdelete");

  private Input unitMove = Input.getStringInput("unitmove");
  private Input unitReset = Input.getStringInput("unitreset");
  private Input newUnit = Input.getStringInput("new_unit");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

}
