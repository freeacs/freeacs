package com.github.freeacs.shell.util;

public class ValidateInteger implements ValidateInput {
  private Integer min;

  private Integer max;

  public ValidateInteger(Integer minInc, Integer maxExc) {
    this.min = minInc;
    this.max = maxExc;
  }

  public boolean validate(String input) {
    try {
      int i = Integer.parseInt(input);
      return (min == null || i >= min) && (max == null || i < max);
    } catch (NumberFormatException nfe) {
      return false;
    }
  }
}
