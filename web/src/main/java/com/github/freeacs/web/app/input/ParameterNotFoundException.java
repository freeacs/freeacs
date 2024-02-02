package com.github.freeacs.web.app.input;

/**
 * Parameter is not found exception.
 *
 * @author Jarl Andre Hubenthal
 */
public class ParameterNotFoundException extends Exception {

  /**
   * Instantiates a new parameter not found exception.
   *
   * @param s the s
   */
  public ParameterNotFoundException(String s) {
    super(s);
  }
}
