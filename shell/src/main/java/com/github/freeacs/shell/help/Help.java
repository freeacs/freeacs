package com.github.freeacs.shell.help;

public class Help {
  private HelpSyntax syntax;
  private HelpComment comment;
  private HelpOptions options;
  private HelpArguments arguments;
  private HelpExamples examples;

  public Help(HelpSyntax syntax) {
    this.syntax = syntax;
  }

  public Help(String syntax) {
    this.syntax = new HelpSyntax(syntax);
  }

  public void addComment(String comment) {
    this.comment = new HelpComment(comment);
  }

  public void addExample(String example) {
    if (examples == null) {
      examples = new HelpExamples();
    }
    examples.addExample(example);
  }

  public void addExamples(String... examples) {
    for (String example : examples) {
      addExample(example);
    }
  }

  public void addArgument(String argument, String comment) {
    if (arguments == null) {
      arguments = new HelpArguments();
    }
    arguments.addArgument(new HelpArgument(argument, comment));
  }

  public void addOption(HelpOption helpOption) {
    if (options == null) {
      options = new HelpOptions();
    }
    options.addOption(helpOption);
  }

  public void addOption(String option, String comment) {
    if (options == null) {
      options = new HelpOptions();
    }
    options.addOption(new HelpOption(option, comment));
  }

  private int findCol1Width() {
    int maxWidth = 9;
    if (arguments != null) {
      for (HelpArgument ha : arguments.getArguments()) {
        if (ha.getArgument().length() > maxWidth) {
          maxWidth = ha.getArgument().length();
        }
      }
    }
    return maxWidth;
  }

  private void print(StringBuffer sb, String col1, int col1Width, String col2, int col2Width) {
    if (col2 == null) {
      col2 = "";
    }
    String[] col2Sentences = col2.split("\n");
    printImpl(sb, col1, col1Width, col2Sentences[0], col2Width);
    for (int i = 1; i < col2Sentences.length; i++) {
      printImpl(sb, "", col1Width, col2Sentences[i], col2Width);
    }
  }

  private void printImpl(StringBuffer sb, String col1, int col1Width, String col2, int col2Width) {
    if (col2 == null) {
      col2 = "";
    }
    String[] col2Words = col2.split(" ");
    String col2Sentence = "";
    String lastCol2Sentence = null;
    boolean col1printed = false;
    for (String col2Word : col2Words) {
      lastCol2Sentence = col2Sentence;
      col2Sentence += col2Word + " ";
      if (col2Sentence.length() > col2Width) {
        if (!col1printed) {
          sb.append(
              String.format("%" + col1Width + "s  %-" + col2Width + "s\n", col1, lastCol2Sentence));
          col1printed = true;
        } else {
          sb.append(
              String.format("%" + col1Width + "s  %-" + col2Width + "s\n", "", lastCol2Sentence));
        }
        col2Sentence = col2Word + " ";
      }
    }
    if (!col1printed) {
      sb.append(String.format("%" + col1Width + "s  %-" + col2Width + "s\n", col1, col2Sentence));
    } else {
      sb.append(String.format("%" + col1Width + "s  %-" + col2Width + "s\n", "", col2Sentence));
    }
  }

  public StringBuffer toStringBuffer() {
    StringBuffer sb = new StringBuffer();
    int maxWidth = 120;
    int col1Width = findCol1Width();
    int col2Width = maxWidth - col1Width;

    String syntaxStr = syntax.getSyntax();
    if (getOptions() != null && !getOptions().getOptions().isEmpty()) {
      int firstSpace = syntaxStr.trim().indexOf(' ');
      if (firstSpace < 0) {
        syntaxStr += " <options>";
      } else {
        syntaxStr =
            syntaxStr.substring(0, firstSpace)
                + " <options> "
                + syntaxStr.substring(firstSpace + 1);
      }
    }
    print(sb, "Syntax", col1Width, syntaxStr, col2Width);
    sb.append("\n");
    if (comment != null) {
      print(sb, "Comment", col1Width, comment.getComment(), col2Width);
      sb.append("\n");
    }
    if (options != null) {
      print(sb, "Options", col1Width, "", col2Width);
      for (HelpOption ho : options.getOptions()) {
        print(sb, ho.getOption(), col1Width, ho.getComment(), col2Width);
      }
      sb.append("\n");
    }
    if (arguments != null) {
      print(sb, "Arguments", col1Width, "", col2Width);
      for (HelpArgument ha : arguments.getArguments()) {
        print(sb, ha.getArgument(), col1Width, ha.getComment(), col2Width);
      }
      sb.append("\n");
    }
    if (examples != null) {
      print(sb, "Examples", col1Width, "", col2Width);
      for (String example : examples.getExamples()) {
        print(sb, "", col1Width, example, col2Width);
      }
      sb.append("\n");
    }
    return sb;
  }

  public String toString() {
    return toStringBuffer().toString();
  }

  public HelpSyntax getSyntax() {
    return syntax;
  }

  public String getCommand() {
    return syntax.getCommand();
  }

  public String getComment() {
    return comment.getComment();
  }

  public HelpArguments getArguments() {
    return arguments;
  }

  public HelpOptions getOptions() {
    return options;
  }

  public HelpExamples getExamples() {
    return examples;
  }
}
