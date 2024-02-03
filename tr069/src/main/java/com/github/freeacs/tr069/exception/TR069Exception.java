package com.github.freeacs.tr069.exception;

import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletResponse;
import java.io.Serial;

/** SA = Session Aborted. */
@Getter
@Setter
public class TR069Exception extends Exception {
  @Serial
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

  public String getMessage() {
    return errorMsg;
  }
}
