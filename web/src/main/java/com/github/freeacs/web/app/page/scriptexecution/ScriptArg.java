package com.github.freeacs.web.app.page.scriptexecution;

import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptArg {
  public enum ArgType {
    STRING,
    INTEGER,
    FILE,
    PROFILE,
    GROUP,
    JOB,
    UNIT,
    TRIGGER,
    SYSLOGEVENT,
    HEARTBEAT,
    ENUM
  }

  private int index;
  private String name;
  private ArgType type;
  private String validationRule;
  private String comment;
  private DropDownSingleSelect<Profile> profileDropDown;
  private DropDownSingleSelect<File> fileDropDown;
  private DropDownSingleSelect<Enumeration> enumDropDown;
  private String value;
  private String error;

  /** Private String defValue;. */
  private Input retrieveInput(ScriptExecutionData inputData, int index) throws Exception {
    Method m = ScriptExecutionData.class.getMethod("getArgument" + index);
    return (Input) m.invoke(inputData, (Object[]) null);
  }

  public ScriptArg(
      int index,
      String name,
      String type,
      String comment,
      String value,
      Unittype unittype,
      ScriptExecutionData inputData)
      throws Exception {
    this.index = index;
    this.name = name;
    this.comment = comment;
    this.value = value;
    if (type != null) {
      type = type.toLowerCase();
      if (type.startsWith("profile")) {
        this.type = ArgType.PROFILE;
        profileDropDown =
            InputSelectionFactory.getDropDownSingleSelect(
                retrieveInput(inputData, index),
                null,
                Arrays.asList(unittype.getProfiles().getProfiles()));
      } else if (type.startsWith("file")) {
        this.type = ArgType.FILE;
        fileDropDown =
            InputSelectionFactory.getDropDownSingleSelect(
                retrieveInput(inputData, index),
                null,
                Arrays.asList(unittype.getFiles().getFiles()));
      } else if (type.startsWith("int") || type.startsWith("number")) {
        this.type = ArgType.INTEGER;
        if (type.split(" ").length > 1) {
          this.validationRule = type.split(" ")[1].trim();
        }
      } else if (type.startsWith("string")) {
        this.type = ArgType.STRING;
      } else if (type.startsWith("enum")) {
        this.type = ArgType.ENUM;
        List<String> options = new ArrayList<>(Arrays.asList(type.split(" ")[1].split(",")));
        List<Enumeration> optionsWrapped = new ArrayList<>();
        for (String s : options) {
          optionsWrapped.add(new Enumeration(s, s));
        }
        enumDropDown =
            InputSelectionFactory.getDropDownSingleSelect(
                retrieveInput(inputData, index), null, optionsWrapped);
        //				System.out.println("Enum scriptarg created, options are: " + options);
      }
    }
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public ArgType getType() {
    return type;
  }

  public void setType(ArgType type) {
    this.type = type;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    if (value != null && value.startsWith("\"")) {
      value = value.substring(1);
    }
    if (value != null && value.endsWith("\"")) {
      value = value.substring(0, value.length() - 1);
    }
    this.value = value;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public DropDownSingleSelect<Profile> getProfileDropDown() {
    return profileDropDown;
  }

  public void setProfileDropDown(DropDownSingleSelect<Profile> profileDropDown) {
    this.profileDropDown = profileDropDown;
  }

  public DropDownSingleSelect<File> getFileDropDown() {
    return fileDropDown;
  }

  public void setFileDropDown(DropDownSingleSelect<File> fileDropDown) {
    this.fileDropDown = fileDropDown;
  }

  public DropDownSingleSelect<Enumeration> getEnumDropDown() {
    return enumDropDown;
  }

  public void setEnumDropDown(DropDownSingleSelect<Enumeration> enumDropDown) {
    this.enumDropDown = enumDropDown;
  }

  public String getValidationRule() {
    return validationRule;
  }

  public void setValidationRule(String validationRule) {
    this.validationRule = validationRule;
  }

  public String toString() {
    return type + " at index " + index + ", \"" + comment + "\", value \"" + value + "\"";
  }
}
