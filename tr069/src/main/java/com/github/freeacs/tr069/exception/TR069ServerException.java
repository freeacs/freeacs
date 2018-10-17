package com.github.freeacs.tr069.exception;

public class TR069ServerException extends TR069Exception {
  private static final long serialVersionUID = -3867523704885196657L;

  public TR069ServerException(String errorMsg, TR069ExceptionShortMessage shortMsg, Throwable t) {
    super(errorMsg, shortMsg, t);
  }
}
