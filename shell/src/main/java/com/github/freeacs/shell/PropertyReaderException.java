package com.github.freeacs.shell;

import java.io.Serial;

public class PropertyReaderException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1464507105516835591L;

  public PropertyReaderException(String propertyfile) {
    super("The propertyfile " + propertyfile + " could not be found");
  }
}
