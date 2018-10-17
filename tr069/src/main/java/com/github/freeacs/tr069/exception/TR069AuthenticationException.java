package com.github.freeacs.tr069.exception;

public class TR069AuthenticationException extends TR069Exception {
  private static final long serialVersionUID = 4703474182491184668L;

  public TR069AuthenticationException(String errorMsg, Throwable t, Integer HTTPErrorCode) {
    super(errorMsg, TR069ExceptionShortMessage.AUTHENTICATION, t);
    setHTTPErrorCode(HTTPErrorCode);
  }
}
