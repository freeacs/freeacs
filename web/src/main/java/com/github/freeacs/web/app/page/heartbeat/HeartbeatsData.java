package com.github.freeacs.web.app.page.heartbeat;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class SyslogEventsData. */
public class HeartbeatsData extends InputData {
  /** Typically to indicate edit/delete/add/etc. */
  private Input action = Input.getStringInput("action");

  /** Id to reference heartbeat. */
  private Input id = Input.getIntegerInput("id");

  /** Input form data. */
  private Input name = Input.getStringInput("name");

  private Input groupId = Input.getIntegerInput("groupId");
  private Input expression = Input.getStringInput("expression");
  private Input timeout = Input.getIntegerInput("timeout");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    boolean valid = true;
    if (name.getString() == null || name.getString().isEmpty()) {
      name.setError("Name is required");
      valid = false;
    }
    if (expression.getString() == null || expression.getString().isEmpty()) {
      expression.setError("Expression is required");
      valid = false;
    }
    if (groupId.getString() == null || groupId.getString().isEmpty()) {
      groupId.setError("Group is required");
      valid = false;
    }
    if (timeout.getInteger() == null || timeout.getInteger() < 1 || timeout.getInteger() > 48) {
      timeout.setError("Limit must be a number from 1 to 48");
      valid = false;
    }

    return valid;
  }

  public Input getName() {
    return name;
  }

  public void setName(Input name) {
    this.name = name;
  }

  public Input getExpression() {
    return expression;
  }

  public void setExpression(Input expression) {
    this.expression = expression;
  }

  public Input getAction() {
    return action;
  }

  public void setAction(Input action) {
    this.action = action;
  }

  public Input getGroupId() {
    return groupId;
  }

  public void setGroupId(Input groupId) {
    this.groupId = groupId;
  }

  public Input getTimeout() {
    return timeout;
  }

  public void setTimeout(Input timeout) {
    this.timeout = timeout;
  }

  public Input getId() {
    return id;
  }

  public void setId(Input id) {
    this.id = id;
  }
}
