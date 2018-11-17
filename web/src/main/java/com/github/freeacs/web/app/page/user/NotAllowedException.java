package com.github.freeacs.web.app.page.user;

/** The Class NotAllowedException. */
// @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class NotAllowedException extends RuntimeException {
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new not allowed exception.
   *
   * @param s the s
   */
  public NotAllowedException(String s) {
    super(s);
  }
}
