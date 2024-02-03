package com.github.freeacs.tr069.exception;

import java.io.Serial;

public class TR069DatabaseException extends TR069ServerException {
  @Serial
  private static final long serialVersionUID = -8002456201010264031L;

  public TR069DatabaseException(String errorMsg, Throwable t) {
    super(errorMsg, TR069ExceptionShortMessage.DATABASE, t);
  }

  public TR069DatabaseException(Throwable t) {
    super("Database not responding or exception occurred", TR069ExceptionShortMessage.DATABASE, t);
  }
}
