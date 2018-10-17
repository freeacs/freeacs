package com.github.freeacs.shell.command;

import java.util.HashSet;
import java.util.Set;

/**
 * An option have this syntax: -<option-char><option-args> If option-args contains whitespace,
 * enclose using quotes
 */
public class Option implements Substitute {
  public static char OPTION_LIST_CONTEXT = 'c';
  public static char OPTION_LIST_ALL_COLUMNS = 'a';
  /**
   * Option-args are have this syntax: (<column-index>(a|n)(a|d))+ The column-index tells which
   * column to sort a|n tells to sort using alphabetical or numerical a|d tells to sort ascending or
   * descending Examples: -o1aa -o2aa1nd -o3na8ad1aa
   */
  public static char OPTION_ORDER = 'o';
  /**
   * Option-args can be context-string. Examples -u/ut:NPA201E-Pingcom/
   * "-u/ut:NPA201E-Pingcom/pr:Test Profile/"
   */
  public static char OPTION_USE_CONTEXT = 'u';

  /**
   * Variable option to use in call-command -v(<var-name>)(,<var-name>)* Examples -va_var -va,b,c.
   */
  public static char OPTION_VARIABLES = 'v';

  public static Set<Character> optionSet = new HashSet<>();

  static {
    optionSet.add(OPTION_LIST_CONTEXT);
    optionSet.add(OPTION_LIST_ALL_COLUMNS);
    optionSet.add(OPTION_ORDER);
    optionSet.add(OPTION_USE_CONTEXT);
    optionSet.add(OPTION_VARIABLES);
  }

  private char type;
  private String optionArgs;
  private String substitute;

  public char getType() {
    return type;
  }

  public void setType(char type) {
    this.type = type;
  }

  public String getOptionArgs() {
    return optionArgs;
  }

  public void setOptionArgs(String optionArgs) {
    this.optionArgs = optionArgs;
  }

  public String toString() {
    String s = "-" + type;
    if (getStringToSubstitute() != null) {
      return s + getStringToSubstitute();
    }
    if (optionArgs != null) {
      return s + optionArgs;
    } else {
      return s;
    }
  }

  /**
   * An option starts with a space+dash and is followed by one or more character. It ends with a
   * space" Valid options are: -a (list all information) -c (list context) -o<orderoptions> (order
   * list according to orderoptions) -u<context> (use context from file or from option-argument)
   */
  public static Option parseOption(String s) {
    if (s == null) {
      return null;
    }
    s = s.trim();
    if (s.startsWith("-") && s.length() > 1) { // matches option pattern
      Option o = new Option();
      char c = s.charAt(1);
      if (optionSet.contains(c)) {
        o.setType(c);
        if (s.length() > 2) {
          o.setOptionArgs(s.substring(2));
        }
        return o;
      }
    }
    return null;
  }

  @Override
  public void resetToOriginalState() {
    substitute = optionArgs;
  }

  @Override
  public String getStringToSubstitute() {
    if (substitute != null) {
      return substitute;
    }
    return optionArgs;
  }

  @Override
  public void setSubstitutedString(String s) {
    substitute = s;
  }
}
