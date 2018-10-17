package com.github.freeacs.tr069.exception;

import javax.servlet.http.HttpServletResponse;

/** SA = Session Aborted. */
public class TR069Exception extends Exception {
  private static final long serialVersionUID = 7288005181389170348L;

  /** Default is 200 OK. */
  private Integer HTTPErrorCode = HttpServletResponse.SC_OK;

  private String errorMsg;
  private TR069ExceptionShortMessage shortMsg;
  private Throwable cause;

  public TR069Exception(String errorMsg, TR069ExceptionShortMessage shortMsg, Throwable t) {
    this.errorMsg = errorMsg;
    this.shortMsg = shortMsg;
    this.cause = t;
  }

  public TR069Exception(String errorMsg, TR069ExceptionShortMessage shortMsg) {
    this.errorMsg = errorMsg;
    this.shortMsg = shortMsg;
  }

  public void setHTTPErrorCode(int HTTPErrorCode) {
    this.HTTPErrorCode = HTTPErrorCode;
  }

  public Integer getHTTPErrorCode() {
    return this.HTTPErrorCode;
  }

  public Throwable getCause() {
    return cause;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public TR069ExceptionShortMessage getShortMsg() {
    return shortMsg;
  }

  public void setCause(Throwable cause) {
    this.cause = cause;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }

  public void setShortMsg(TR069ExceptionShortMessage shortMsg) {
    this.shortMsg = shortMsg;
  }

  public String getMessage() {
    return errorMsg;
  }
}
