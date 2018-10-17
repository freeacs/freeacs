package com.github.freeacs.shell.command;

public class CommandAndArgument implements Substitute {
  private String commandAndArgument;
  private String substitute;
  private boolean appendedFromFile;

  public CommandAndArgument(String s) {
    this.commandAndArgument = s;
  }

  public CommandAndArgument(String s, boolean appendedFromFile) {
    this.commandAndArgument = s;
    this.appendedFromFile = appendedFromFile;
  }

  public String getCommandAndArgument() {
    return commandAndArgument;
  }

  public void setCommandAndArgument(String commandAndArgument) {
    this.commandAndArgument = commandAndArgument;
  }

  public String toString() {
    return getStringToSubstitute();
  }

  @Override
  public void resetToOriginalState() {
    substitute = commandAndArgument;
  }

  @Override
  public String getStringToSubstitute() {
    if (substitute != null) {
      return substitute;
    }
    return commandAndArgument;
  }

  @Override
  public void setSubstitutedString(String s) {
    substitute = s;
  }

  public boolean isAppendedFromFile() {
    return appendedFromFile;
  }
}
