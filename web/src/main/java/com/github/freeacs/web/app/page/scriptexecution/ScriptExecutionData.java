package com.github.freeacs.web.app.page.scriptexecution;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class SyslogEventsData. */
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

  public Input getFileId() {
    return fileId;
  }

  public void setFileId(Input fileId) {
    this.fileId = fileId;
  }

  public Input getAction() {
    return action;
  }

  public void setAction(Input action) {
    this.action = action;
  }

  public Input getId() {
    return id;
  }

  public void setId(Input id) {
    this.id = id;
  }

  public Input getArguments() {
    return arguments;
  }

  public void setArguments(Input arguments) {
    this.arguments = arguments;
  }

  public Input getRequestid() {
    return requestid;
  }

  public void setRequestid(Input requestid) {
    this.requestid = requestid;
  }

  public Input getInfo() {
    return info;
  }

  public void setInfo(Input info) {
    this.info = info;
  }

  public Input getArgument1() {
    return argument1;
  }

  public void setArgument1(Input argument1) {
    this.argument1 = argument1;
  }

  public Input getArgument2() {
    return argument2;
  }

  public void setArgument2(Input argument2) {
    this.argument2 = argument2;
  }

  public Input getArgument3() {
    return argument3;
  }

  public void setArgument3(Input argument3) {
    this.argument3 = argument3;
  }

  public Input getArgument4() {
    return argument4;
  }

  public void setArgument4(Input argument4) {
    this.argument4 = argument4;
  }

  public Input getArgument5() {
    return argument5;
  }

  public void setArgument5(Input argument5) {
    this.argument5 = argument5;
  }

  public Input getArgument6() {
    return argument6;
  }

  public void setArgument6(Input argument6) {
    this.argument6 = argument6;
  }

  public Input getArgument7() {
    return argument7;
  }

  public void setArgument7(Input argument7) {
    this.argument7 = argument7;
  }

  public Input getArgument8() {
    return argument8;
  }

  public void setArgument8(Input argument8) {
    this.argument8 = argument8;
  }

  public Input getArgument9() {
    return argument9;
  }

  public void setArgument9(Input argument9) {
    this.argument9 = argument9;
  }

  public Input getArgument10() {
    return argument10;
  }

  public void setArgument10(Input argument10) {
    this.argument10 = argument10;
  }

  public Input getArgument11() {
    return argument11;
  }

  public void setArgument11(Input argument11) {
    this.argument11 = argument11;
  }

  public Input getArgument12() {
    return argument12;
  }

  public void setArgument12(Input argument12) {
    this.argument12 = argument12;
  }

  public Input getArgument13() {
    return argument13;
  }

  public void setArgument13(Input argument13) {
    this.argument13 = argument13;
  }

  public Input getArgument14() {
    return argument14;
  }

  public void setArgument14(Input argument14) {
    this.argument14 = argument14;
  }

  public Input getArgument15() {
    return argument15;
  }

  public void setArgument15(Input argument15) {
    this.argument15 = argument15;
  }

  public Input getArgument16() {
    return argument16;
  }

  public void setArgument16(Input argument16) {
    this.argument16 = argument16;
  }

  public Input getArgument17() {
    return argument17;
  }

  public void setArgument17(Input argument17) {
    this.argument17 = argument17;
  }

  public Input getArgument18() {
    return argument18;
  }

  public void setArgument18(Input argument18) {
    this.argument18 = argument18;
  }

  public Input getArgument19() {
    return argument19;
  }

  public void setArgument19(Input argument19) {
    this.argument19 = argument19;
  }

  public Input getArgument20() {
    return argument20;
  }

  public void setArgument20(Input argument20) {
    this.argument20 = argument20;
  }
}
