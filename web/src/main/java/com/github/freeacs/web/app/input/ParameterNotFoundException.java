package com.github.freeacs.web.app.input;

/**
 * Parameter is not found exception.
 *
 * @author Jarl Andre Hubenthal
 */
public class ParameterNotFoundException extends Exception {
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Instantiates a new parameter not found exception. */
  public ParameterNotFoundException() {}

  /**
   * Instantiates a new parameter not found exception.
   *
   * @param s the s
   */
  public ParameterNotFoundException(String s) {
    super(s);
  }
}
