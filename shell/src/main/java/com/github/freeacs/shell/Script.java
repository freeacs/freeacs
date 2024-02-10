package com.github.freeacs.shell;

import com.github.freeacs.dbi.File;
import com.github.freeacs.shell.util.FileUtil;
import lombok.*;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Script {
  public static final int SCRIPT = 0;
  public static final int WHILE = 1;
  public static final int IF = 2;

  private final int type;
  private Context context;
  private Map<String, Variable> variables = new HashMap<>();
  private List<String> scriptLines;
  private int linePointer;
  private boolean skipOnNextIfElseWord;
  /** Name of script-file (if used). */
  private final String filename;
  /** File-args when iterating over a file. */
  private BufferedReader whileInput;
  /** Sequence of linepointer-numbers going back to root-script. Gives a unique while-id */
  private String whilePath;

  public Script(List<String> scriptLines, Context context, int type) {
    this(scriptLines, context, type, null);
  }

  public Script(
      List<String> scriptLines, Context context, int type, Map<String, Variable> variables) {
    this.scriptLines = scriptLines;
    this.context = context;
    this.type = type;
    if (variables != null) {
      this.variables = variables;
    }
    this.filename = "N/A";
  }

  public Script(String filename, Context context, int type) {
    this(filename, context, type, null);
  }

  public Script(String filename, Context context, int type, Map<String, Variable> variables) {
    try {
      this.filename = filename;
      boolean fileFound = false;
      this.type = type;
      if (context.getUnittype() != null) {
        File script = context.getUnittype().getFiles().getByName(filename);
        if (script != null) {
          scriptLines = new ArrayList<>();
          for (String line : new String(script.getContent()).split("\n")) {
            if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) {
              continue;
            }
            scriptLines.add(line.trim());
          }
          this.context = context;
          fileFound = true;
        }
      }
      if (!fileFound && FileUtil.exists(filename)) {
        this.context = context;
        scriptLines = FileUtil.getLines(filename);
      } else if (!fileFound) {
        throw new IllegalArgumentException("The script file " + filename + " does not exist");
      }
      this.variables = variables;
    } catch (Exception nce) {
      throw new IllegalArgumentException(
          "The script file " + filename + " was not found due to database connection problem");
    }
  }

  private String getCommand(String s) {
    s = s.trim();
    int spacePos = s.indexOf(' ');
    if (spacePos > -1) {
      s = s.substring(0, spacePos);
    }
    return s;
  }

  /**
   * Public String getNextScriptLineWithCommand(String... cmdList) { String s = null; if ((s =
   * getNextScriptLine()) != null) { String word = getCommand(s); for (String w : cmdList) { if
   * (word.equals(w)) { return s; } } } return null; }
   */
  public void moveUpUntilCommand(String... cmdList) {
    String s;
    int skipAhead = 0;
    while ((s = getNextScriptLine()) != null) {
      String word = getCommand(s);
      if ("if".equals(word)) {
        skipAhead++;
      } else if ("fi".equals(word)) {
        skipAhead--;
      }
      if (skipAhead > 0) {
        continue;
      }
      boolean match = false;
      for (String w : cmdList) {
        if (word.equals(w)) {
          match = true;
          break;
        }
      }
      if (match) {
        decLinePointer();
        break;
      }
    }
  }

  public void addScriptLine(String line) {
    scriptLines.add(line);
  }

  public void insertScriptLine(int linePointer, String line) {
    scriptLines.add(linePointer, line);
  }

  public void addVariable(String name, String value) {
    variables.put(name, new Variable(name, value));
  }

  public void removeVariable(String name) {
    variables.remove(name);
  }

  public Variable getVariable(String name) {
    return variables.get(name);
  }

  public boolean endOfScript() {
    return linePointer >= scriptLines.size();
  }

  public String getPreviousScriptLine() {
    if (linePointer == 0 || linePointer > scriptLines.size()) {
      return null;
    } else {
      return scriptLines.get(linePointer - 1);
    }
  }

  public String getNextScriptLine() {
    if (linePointer >= scriptLines.size()) {
      return null;
    } else {
      return scriptLines.get(linePointer++);
    }
  }

  public String getScriptLine(int linePointer) {
    if (linePointer >= scriptLines.size()) {
      return null;
    } else {
      return scriptLines.get(linePointer);
    }
  }

  public void decLinePointer() {
    linePointer--;
  }

  public void incLinePointer() {
    linePointer++;
  }

  public void reset() {
    linePointer = 0;
  }

}
