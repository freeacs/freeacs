package com.github.freeacs.shell.help;

public class HelpSyntax {
  private String syntax;
  private String command;

  public HelpSyntax(String syntax) {
    this.syntax = syntax;
    int spacePos = syntax.indexOf(' ');
    command = syntax;
    if (spacePos > -1) {
      command = syntax.substring(0, spacePos);
    }
  }

  public String getSyntax() {
    return syntax;
  }

  public String getCommand() {
    return command;
  }
}
