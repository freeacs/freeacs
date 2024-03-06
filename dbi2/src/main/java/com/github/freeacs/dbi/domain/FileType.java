package com.github.freeacs.dbi.domain;

public enum FileType {
  SOFTWARE,
  TR069_SCRIPT,
  SHELL_SCRIPT,
  TELNET_SCRIPT,
  UNITS,
  MISC;

  public static FileType fromString(String typeStr) {
    FileType ft = null;
    try {
      ft = valueOf(typeStr);
    } catch (Throwable t) { // Convert from old types
      if ("SCRIPT".equals(typeStr)) {
        ft = SHELL_SCRIPT;
      }
      if ("CONFIG".equals(typeStr)) {
        ft = TR069_SCRIPT;
      }
    }
    return ft;
  }
}
