package com.github.freeacs.dbi.exceptions;

public class AcsException extends RuntimeException {
  public AcsException(String message, Object ...args) {
      super(args.length == 0 ? message : String.format(message, args));
  }

  public AcsException(String message, Throwable cause, Object ...args) {
    super(args.length == 0 ? message : String.format(message, args), cause);
  }
}
