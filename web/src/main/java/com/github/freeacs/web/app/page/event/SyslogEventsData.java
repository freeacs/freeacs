package com.github.freeacs.web.app.page.event;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class SyslogEventsData. */
public class SyslogEventsData extends InputData {
  private Input action = Input.getStringInput("action");

  private Input eventId = Input.getIntegerInput("eventid");
  private Input name = Input.getStringInput("name");
  private Input description = Input.getStringInput("description");
  private Input groupId = Input.getIntegerInput("groupId");
  private Input expression = Input.getStringInput("expression");
  private Input storePolicy = Input.getStringInput("storepolicy");
  private Input script = Input.getIntegerInput("scriptId");
  private Input limit = Input.getIntegerInput("limit");

  public Input getScript() {
    return script;
  }

  public void setScript(Input script) {
    this.script = script;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    boolean valid = true;
    if (eventId.getValue() != null && eventId.getInteger() == null) {
      eventId.setError("Event Id must be a number");
      valid = false;
    } else if (eventId.getInteger() == null) {
      eventId.setError("Event Id is required");
      valid = false;
    } else if (eventId.getInteger() < 1000) {
      eventId.setError("Event Id must be higher than 1000");
      valid = false;
    }
    if (name.getString() == null || name.getString().isEmpty()) {
      name.setError("Name is required");
      valid = false;
    }
    if (expression.getString() == null || expression.getString().isEmpty()) {
      expression.setError("Expression is required");
      valid = false;
    }
    if (storePolicy.getString() == null || storePolicy.getString().isEmpty()) {
      storePolicy.setError("StorePolicy is required");
      valid = false;
    }
    if (!"".equals(limit.getString()) && (limit.getInteger() == null || limit.getInteger() < 0)) {
      limit.setError("Limit must be a number higher or equal to 0");
      valid = false;
    } else { // if input was empty and an attempt to parse to Integer failed, we suppress that error
      limit.setError(null);
    }

    return valid;
  }

  public Input getEventId() {
    return eventId;
  }

  public void setEventId(Input id) {
    this.eventId = id;
  }

  public Input getName() {
    return name;
  }

  public void setName(Input name) {
    this.name = name;
  }

  public Input getDescription() {
    return description;
  }

  public void setDescription(Input description) {
    this.description = description;
  }

  public Input getExpression() {
    return expression;
  }

  public void setExpression(Input expression) {
    this.expression = expression;
  }

  public Input getLimit() {
    return limit;
  }

  public void setLimit(Input limit) {
    this.limit = limit;
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

  public Input getStorePolicy() {
    return storePolicy;
  }

  public void setStorePolicy(Input storePolicy) {
    this.storePolicy = storePolicy;
  }
}
