package com.github.freeacs.web.app.page.job;

import com.github.freeacs.dbi.JobFlag.JobType;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

public class JobData extends InputData {
  /** Code-order: unty, id, name, flag, desc, group, unct, rules, file, dep, repc, repi */
  private Input name = Input.getStringInput("name");

  private Input type = Input.getStringInput("type");
  private Input serviceWindow = Input.getStringInput("servicewindow");
  private Input description = Input.getStringInput("description");
  private Input groupId = Input.getIntegerInput("groupId");
  private Input unconfirmedTimeout = Input.getIntegerInput("unconfirmedtimeout");
  private Input stoprules = Input.getStringInput("stoprules");
  private Input fileId = Input.getIntegerInput("fileId");
  private Input dependency = Input.getStringInput("dependency");
  private Input repeatCount = Input.getIntegerInput("repeatcount");
  private Input repeatInterval = Input.getIntegerInput("repeatinterval");

  private Input statusSubmit = Input.getStringInput("statuschange");

  @Override
  public void bindForm(Map<String, Object> root) {}

  // code-order: unty, id, name, flag, desc, group, unct, rules, file, dep, repc, repi
  /** This method is only called in the event of add/change job-details. */
  @Override
  public boolean validateForm() {
    boolean valid = true;
    if (getFormSubmit().getValue() != null) {
      // Action is either add/change-details/params/status
      if (name.getString() == null) {
        name.setError("Name must be specified");
        valid = false;
      }
      if (groupId.getInteger() == null) {
        groupId.setError("Group must be specified");
        valid = false;
      }
      if (unconfirmedTimeout.getInteger() == null) {
        unconfirmedTimeout.setError("Unconfirmed timeout must be a number");
        valid = false;
      } else if (unconfirmedTimeout.getInteger() < 60) {
        unconfirmedTimeout.setError("Unconfirmed timeout cannot be less than 60 (seconds)");
        valid = false;
      }
      if (JobType.valueOf(type.getString()).requireFile() && fileId.getInteger() == null) {
        fileId.setError("Software/script file must be specified");
        valid = false;
      }
      if (!"".equals(repeatCount.getString())) {
        if (repeatCount.getInteger() == null) {
          repeatCount.setError("Repeat count must, if specified, be a number");
          valid = false;
        } else if (repeatCount.getInteger() < 0) {
          repeatCount.setError("Repeat count cannot, if specified, be less than 0 (times)");
          valid = false;
        }
      } else {
        repeatCount.setError(null);
      }
      if (!"".equals(repeatInterval.getString())) {
        if (repeatInterval.getInteger() == null) {
          repeatInterval.setError("Repeat interval must, if specified, be a number");
          valid = false;
        } else if (repeatInterval.getInteger() < 0) {
          repeatInterval.setError("Repeat interval cannot, if specified, be less than 0 (seconds)");
          valid = false;
        }
      } else {
        repeatInterval.setError(null);
      }
    }
    return valid;
  }

  public Input getDescription() {
    return description;
  }

  public Input getName() {
    return name;
  }

  public Input getDependency() {
    return dependency;
  }

  public Input getServiceWindow() {
    return serviceWindow;
  }

  public Input getStatusSubmit() {
    return statusSubmit;
  }

  public Input getType() {
    return type;
  }

  public Input getRepeatCount() {
    return repeatCount;
  }

  public Input getRepeatInterval() {
    return repeatInterval;
  }

  public Input getStoprules() {
    return stoprules;
  }

  public Input getUnconfirmedTimeout() {
    return unconfirmedTimeout;
  }

  public void setDescription(Input description) {
    this.description = description;
  }

  public void setName(Input jobcreatename) {
    this.name = jobcreatename;
  }

  public void setDependency(Input jobdependency) {
    this.dependency = jobdependency;
  }

  public void setServiceWindow(Input jobSW) {
    this.serviceWindow = jobSW;
  }

  public void setStatusSubmit(Input jobStatusSubmit) {
    this.statusSubmit = jobStatusSubmit;
  }

  public void setType(Input jobType) {
    this.type = jobType;
  }

  public void setRepeatCount(Input repeatCount) {
    this.repeatCount = repeatCount;
  }

  public void setRepeatInterval(Input repeatInterval) {
    this.repeatInterval = repeatInterval;
  }

  public void setStoprules(Input stoprules) {
    this.stoprules = stoprules;
  }

  public void setUnconfirmedTimeout(Input timeoutsetting) {
    this.unconfirmedTimeout = timeoutsetting;
  }

  public Input getFileId() {
    return fileId;
  }

  public void setFileId(Input fileId) {
    this.fileId = fileId;
  }

  public Input getGroupId() {
    return groupId;
  }

  public void setGroupId(Input groupId) {
    this.groupId = groupId;
  }
}
