package com.github.freeacs.shell;

public class Echo {
  /** Default is to echo prompt/command to output. */
  private boolean echo = true;

  private Session session;

  private String input;

  private boolean printPrompt = true;

  private boolean fromKeyboard;

  public Echo(Session session) {
    this.session = session;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public void setEcho(boolean echo) {
    this.echo = echo;
  }

  public void reset() {
    input = null;
    printPrompt = true;
    echo = true;
    fromKeyboard = false;
  }

  public void printInteractiveMode() {
    session.print(session.getScript().getContext().getPrompt());
    printPrompt = false;
    this.input = null;
  }

  public void print() {
    if (echo) {
      if (printPrompt) {
        session.print(session.getScript().getContext().getPrompt());
      }
      if (input != null) {
        if (!fromKeyboard) {
          session.println(input);
        }
        this.input = null;
        printPrompt = true;
      } else {
        printPrompt = false;
      }
    }
  }

  public void setFromKeyboard(boolean fromKeyboard) {
    this.fromKeyboard = fromKeyboard;
  }
}
