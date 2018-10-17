package com.github.freeacs.shell.help;

import java.util.ArrayList;
import java.util.List;

public class HelpGroup {
  private List<Help> helpList = new ArrayList<>();

  private String menu;

  public HelpGroup(String menu) {
    this.menu = menu;
  }

  public void addHelp(Help help) {
    helpList.add(help);
  }

  public List<Help> getHelpList() {
    return helpList;
  }

  public Help getHelp(String command) {
    for (Help help : helpList) {
      if (command.equals(help.getCommand())) {
        return help;
      }
    }
    return null;
  }

  public StringBuffer toStringBuffer() {
    StringBuffer sb = new StringBuffer();
    sb.append(
        "\nShows the list of available commands.\nType 'help generic' to get generic commands.\n");
    sb.append("Type 'help <command>' to get detailed help on each command.\n\n");
    for (Help help : helpList) {
      sb.append("\t").append(help.getSyntax().getSyntax()).append("\n");
    }
    sb.append("\n");
    return sb;
  }

  public String toString() {
    return toStringBuffer().toString();
  }

  public List<String> getCommands() {
    List<String> commands = new ArrayList<>();
    for (Help help : helpList) {
      commands.add(help.getCommand());
    }
    return commands;
  }

  public static String encode(String s) {
    s = s.replaceAll("<", "&lt;");
    s = s.replaceAll(">", "&gt;");
    s = s.replaceAll("\n", "<br>");
    return s.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
  }

  public String toHTML() {
    StringBuilder sb = new StringBuilder();
    sb.append("<h2>").append(menu).append(" menu</h2>\n");
    sb.append("List of commands available:<br>\n");
    sb.append("<ul><br>\n");
    for (Help help : helpList) {
      sb.append("<li>").append(help.getCommand()).append("\n");
    }
    sb.append("</ul><p>\n");
    for (Help help : helpList) {
      sb.append("<h3>")
          .append(menu)
          .append(".")
          .append(encode(help.getCommand()))
          .append("</h3>\n");
      sb.append("<b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Syntax:</b> ")
          .append(encode(help.getSyntax().getSyntax()))
          .append("<p>\n");
      sb.append("<b>&nbsp;&nbsp;&nbsp;Comment:</b> ")
          .append(encode(help.getComment()))
          .append("<p>\n");
      HelpOptions opts = help.getOptions();
      if (opts != null) {
        sb.append("<b>Arguments:</b><br>\n<ul>\n");
        for (HelpOption opt : opts.getOptions()) {
          sb.append("<li><b>")
              .append(encode(opt.getOption()))
              .append("</b>: ")
              .append(encode(opt.getComment()))
              .append("\n");
        }
        sb.append("</ul>\n");
      }

      HelpArguments args = help.getArguments();
      if (args != null) {
        sb.append("<b>Arguments:</b><br>\n<ul>\n");
        for (HelpArgument arg : args.getArguments()) {
          sb.append("<li><b>")
              .append(encode(arg.getArgument()))
              .append("</b>: ")
              .append(encode(arg.getComment()))
              .append("\n");
        }
        sb.append("</ul>\n");
      }
      HelpExamples examples = help.getExamples();
      if (examples != null) {
        sb.append("<b>&nbsp;&nbsp;Examples:</b><br>\n<ul>\n");
        for (String example : examples.getExamples()) {
          sb.append("<li>").append(encode(example)).append("\n");
        }
        sb.append("</ul>\n");
      }
    }
    return sb.toString();
  }
}
