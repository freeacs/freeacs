package com.github.freeacs.tr069.base;

import java.io.Serial;

public class BaseCacheException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 6365246367824984258L;

  public BaseCacheException(String key) {
    super("The cache did not contain the information for key " + key);
  }
}
