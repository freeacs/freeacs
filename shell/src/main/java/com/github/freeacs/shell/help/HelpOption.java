package com.github.freeacs.shell.help;

public class HelpOption {
  private String option;
  private String comment;

  public HelpOption(String option, String comment) {
    this.option = option;
    this.comment = comment;
  }

  public String getOption() {
    return option;
  }

  public String getComment() {
    return comment;
  }
}
