package com.github.freeacs.shell.command;

public interface Substitute {
  /**
   * Public boolean isProcessed(); public void setProcessed(boolean processed); public boolean
   * isChanged(); public void setChanged(boolean changed);.
   */
  void resetToOriginalState();

  String getStringToSubstitute();

  void setSubstitutedString(String s);
}
