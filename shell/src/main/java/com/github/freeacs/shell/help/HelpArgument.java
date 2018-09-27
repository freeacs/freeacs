package com.github.freeacs.shell.help;

public class HelpArgument {
  private String argument;
  private String comment;

  public HelpArgument(String argument, String comment) {
    this.argument = argument;
    this.comment = comment;
  }

  public String getArgument() {
    return argument;
  }

  public String getComment() {
    return comment;
  }
}
