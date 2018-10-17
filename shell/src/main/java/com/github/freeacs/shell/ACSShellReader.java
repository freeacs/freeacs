package com.github.freeacs.shell;

import com.github.freeacs.shell.util.FileUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import jline.ArgumentCompletor;
import jline.ConsoleReader;
import jline.SimpleCompletor;

public class ACSShellReader extends BufferedReader {
  private ConsoleReader console;
  private ArgumentCompletor argumentCompletor;
  private ACSShell ACSShell;

  public ACSShellReader(Reader in, ConsoleReader reader) {
    super(in);
    this.console = reader;
  }

  public String readLine() {
    try {
      if (argumentCompletor != null) {
        console.removeCompletor(argumentCompletor);
      }
      argumentCompletor = new ArgumentCompletor(new SimpleCompletor(getLines()));
      console.addCompletor(argumentCompletor);
      String line = console.readLine();
      if (line == null) {
        println("\nGoodbye");
        System.exit(0);
      } else {
        return line;
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
    return null;
  }

  private String[] getLines() {
    List<String> completions = new ArrayList<>();
    if (ACSShell.getSession() != null && ACSShell.getSession().getAcs() != null) {
      completions.addAll(FileUtil.getCompletions(ACSShell.getSession()));
    }

    File folder = new File(System.getProperty("user.dir"));
    File[] listOfFiles = folder.listFiles();
    if (listOfFiles != null) {
      for (File listOfFile : listOfFiles) {
        if (listOfFile.isFile()) {
          completions.add(listOfFile.getName());
        }
      }
    }

    completions.add("show");

    return completions.toArray(new String[] {});
  }

  private void println(String s) {
    ACSShell.println(s);
  }

  public void setACSShell(ACSShell ACSShell) {
    this.ACSShell = ACSShell;
  }
}
