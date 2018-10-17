package com.github.freeacs.web.app.page.search;

import com.github.freeacs.dbi.Parameter.Operator;
import com.github.freeacs.dbi.Parameter.ParameterDataType;

/**
 * Represents a searchable parameter on the search page.
 *
 * @author Jarl Andre Hubenthal
 */
public class SearchParameter {
  private final String displayText;
  private final String id;
  private final String value;
  private final Operator operator;
  private final ParameterDataType type;
  private boolean enabled;

  public static final String NULL_VALUE = "NULL";
  public static final String EMPTY_VALUE = "EMPTY";

  /**
   * Instantiates a new search parameter.
   *
   * @param theParameterId the key
   * @param theDisplayText the common
   * @param theParameterValue the value
   * @param isEnabled
   */
  public SearchParameter(
      String theParameterId,
      String theDisplayText,
      String theParameterValue,
      Operator operator,
      ParameterDataType type,
      Boolean isEnabled) {
    this.id = theParameterId;
    this.displayText = theDisplayText;
    this.value = theParameterValue;
    this.operator = operator;
    this.type = type;
    this.enabled = isEnabled;
  }

  /**
   * The text that should display in the table row header.
   *
   * @return the display text
   */
  public String getDisplayText() {
    if (displayText != null) {
      return displayText;
    }
    return id;
  }

  /**
   * Two way conversion method.
   *
   * @param id
   * @return the converted parameter id
   */
  public static String convertParameterId(String id) {
    if (id == null) {
      return id;
    }
    if (id.contains("::")) {
      return id.replaceAll("::", "#");
    } else if (id.contains("#")) {
      return id.replaceAll("#", "::");
    }
    return id;
  }

  /**
   * Replaces all * with %.
   *
   * @param value
   * @return the converted parameter value
   */
  public static String convertParameterValue(String value) {
    if (value == null) {
      return value;
    }
    if (value.contains("*")) {
      return value.replaceAll("\\*", "%");
    } else if (value.contains("%")) {
      return value.replaceAll("%", "*");
    }
    return value;
  }

  public Operator getOperator() {
    return operator;
  }

  public ParameterDataType getType() {
    return type;
  }

  public String getId() {
    return convertParameterId(id);
  }

  public String getValue() {
    return value;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
