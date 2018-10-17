package com.github.freeacs.shell.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
  public static Map<String, String> getOptionMap(String[] args) {
    Map<String, String> options = new HashMap<>();
    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      sb.append(arg);
      sb.append(" ");
    }
    sb.trimToSize();
    String input = sb.toString().trim();

    Pattern p = Pattern.compile("((\"[^\"]+\")|(\\S+))" + "\\s*=\\s*" + "((\"[^\"]+\")|(\\S+))");
    Matcher m = p.matcher(input);
    while (m.find()) {
      String[] parts = m.group().split("=", 2);
      for (int i = 0; i < parts.length; i++) {
        parts[i] = parts[i].trim();
        if (parts[i].charAt(0) == '"') {
          parts[i] = parts[i].substring(1, parts[i].length() - 1);
        }
      }
      options.put(parts[0].toLowerCase(), parts[1]);
    }
    return options;
  }

  private static Pattern pattern = Pattern.compile("(\"([^\"]*)\")|([^ \"\t]+)");

  /**
   * Replaces the old split function, since the old function did not handle strings which had quotes
   * in the middle of a string. This was a problem when using the special root-char :. For example
   * this string would not be properly split: call script.xss :"Testperf" TestperfP;
   *
   * @param s
   * @return
   */
  public static String[] split(String s) {
    Matcher m = pattern.matcher(s);
    int pos = 0;
    List<String> commands = new ArrayList<>();
    while (m.find(pos)) {
      String group1 = m.group(2);
      String group2 = m.group(3);
      if (group1 != null) {
        //				System.out.println("G1-with quotes   : " + group1);
        commands.add(group1);
      } else if (group2 != null) {
        //				System.out.println("G2-without quotes: " + group2);
        commands.add(group2);
      }
      pos = m.end();
    }
    String[] strArray = new String[commands.size()];
    commands.toArray(strArray);
    return strArray;
  }

  /**
   * A command string must be splitted on |, because it signals that the output of the first command
   * should go into the next command. However, if the | is within quotes, it must be ignored an no
   * splitting should happen.
   *
   * @param s
   * @return
   */
  public static String[] splitOnPipe(String s) {
    Matcher m = pattern.matcher(s);
    int pos = 0;
    List<String> commands = new ArrayList<>();
    String command = "";
    while (m.find(pos)) {
      String group1 = m.group(2);
      String group2 = m.group(3);
      if (group1 != null) {
        //				System.out.println("G1-with quotes   : " + group1);
        command += "\"" + group1 + "\" ";
      } else if (group2 != null) {
        //				System.out.println("G2-without quotes: " + group2);
        // May contain |
        int pipePos = group2.indexOf('|');
        while (pipePos > -1) {
          String untilPipeStr = group2.substring(0, pipePos);
          command += untilPipeStr + " ";
          commands.add(command.trim());
          command = "";
          group2 = group2.substring(pipePos + 1); // make group2 shorter
          pipePos = group2.indexOf('|');
        }
        command += group2 + " ";
      }
      pos = m.end();
    }
    commands.add(command.trim());
    String[] strArray = new String[commands.size()];
    commands.toArray(strArray);
    return strArray;
  }
}
