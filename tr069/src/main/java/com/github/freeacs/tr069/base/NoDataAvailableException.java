package com.github.freeacs.tr069.base;

import java.io.Serial;

public class NoDataAvailableException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 3593855904817603230L;

  public NoDataAvailableException() {
    super("Desired data is not available in the database");
  }
}
