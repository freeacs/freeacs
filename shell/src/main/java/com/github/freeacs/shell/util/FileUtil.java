package com.github.freeacs.shell.util;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Properties;
import com.github.freeacs.shell.Session;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
  private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

  public static boolean exists(String filename) {
    File f = new File(filename);
    return f.exists();
  }

  private static String[] protectedFilePatterns =
      new String[] {
        "application.properties",
        "xaps-shell-logs.properties",
        "xaps-shell.*log",
        "xapsshell.sh",
        "fusion-shell.*log",
        "fusionshell.sh"
      };

  public static boolean allowed(String command, File f) {
    if (Properties.isRestricted()) {
      String msg = null;
      String absPath = f.getAbsolutePath();
      if (absPath.contains("..")) {
        msg =
            "Error: Not allowed to do '"
                + command
                + "' since it contains '..'. This incident will be reported.";
      }
      String thisDir = new File(".").getAbsolutePath();
      thisDir = thisDir.substring(0, thisDir.length() - 1);
      if (!absPath.contains(thisDir)) {
        msg =
            "Error: Not allowed to do '"
                + command
                + "' since it access outside your direcetory tree. This incident will be reported.";
      }
      for (String protectedFilePattern : protectedFilePatterns) {
        if (absPath.matches(".*" + protectedFilePattern)) {
          msg =
              "Error: Not allowed to do '"
                  + command
                  + "' to this specially protected file. This incident will be reported";
        }
      }
      if (msg != null) {
        System.out.println(msg);
        logger.error(msg);
        return false;
      } else {
        return true;
      }
    }
    return true;
  }

  public static List<String> getLines(String filename) {
    List<String> lines = new ArrayList<>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(filename));
      do {
        String line = br.readLine();
        if (line == null) {
          break;
        }
        if (line.trim().isEmpty() || line.trim().startsWith("#")) {
          continue;
        }
        lines.add(line);
      } while (true);
      return lines;
    } catch (FileNotFoundException fnfe) {
      return null;
    } catch (IOException ioe) {
      if (!lines.isEmpty()) {
        return lines;
      }
      return null;
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static List<String> getCompletions(Session session) {
    Context context = session.getContext();
    List<String> completions = context.getCommands();
    if (context.getUnit() != null
        || context.getProfile() != null
        || context.getGroup() != null
        || context.getJob() != null) {
      Unittype unittype = context.getUnittype();
      for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
        completions.add(utp.getName());
      }
    } else if (context.getUnittypeParameter() == null) {
      if (context.getUnittype() != null) {
        Unittype unittype = context.getUnittype();
        for (Profile p : unittype.getProfiles().getProfiles()) {
          completions.add(p.getName());
        }
        for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
          completions.add(utp.getName());
          completions.add("V-" + utp.getName());
        }
        for (Job job : unittype.getJobs().getJobs()) {
          completions.add("J-" + job.getName());
        }
        for (Group group : unittype.getGroups().getGroups()) {
          completions.add("G-" + group.getName());
        }
      } else {
        for (Unittype unittype : session.getAcs().getUnittypes().getUnittypes()) {
          completions.add(unittype.getName());
        }
      }
    }
    return completions;
  }
}
