package com.github.freeacs.web.app.page.scriptexecution;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class SyslogEventsData. */
@Getter
@Setter
public class ScriptExecutionData extends InputData {
  /** Typically to indicate edit/delete/add/etc. */
  private Input action = Input.getStringInput("action");

  /** Id to reference a particular script execution. */
  private Input id = Input.getIntegerInput("id");

  /** The script file to be executed. */
  private Input fileId = Input.getIntegerInput("fileId");

  /** Arguments which goes with the script file. */
  private Input arguments = Input.getStringInput("arguments");

  /** Request id, to identify the script execution. */
  private Input requestid = Input.getStringInput("requestid");

  private Input info = Input.getStringInput("info");

  private Input argument1 = Input.getStringInput("argument1");
  private Input argument2 = Input.getStringInput("argument2");
  private Input argument3 = Input.getStringInput("argument3");
  private Input argument4 = Input.getStringInput("argument4");
  private Input argument5 = Input.getStringInput("argument5");
  private Input argument6 = Input.getStringInput("argument6");
  private Input argument7 = Input.getStringInput("argument7");
  private Input argument8 = Input.getStringInput("argument8");
  private Input argument9 = Input.getStringInput("argument9");
  private Input argument10 = Input.getStringInput("argument10");
  private Input argument11 = Input.getStringInput("argument11");
  private Input argument12 = Input.getStringInput("argument12");
  private Input argument13 = Input.getStringInput("argument13");
  private Input argument14 = Input.getStringInput("argument14");
  private Input argument15 = Input.getStringInput("argument15");
  private Input argument16 = Input.getStringInput("argument16");
  private Input argument17 = Input.getStringInput("argument17");
  private Input argument18 = Input.getStringInput("argument18");
  private Input argument19 = Input.getStringInput("argument19");
  private Input argument20 = Input.getStringInput("argument20");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return true;
  }
}
