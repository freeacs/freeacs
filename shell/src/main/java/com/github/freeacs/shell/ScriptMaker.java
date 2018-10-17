package com.github.freeacs.shell;

import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.github.freeacs.shell.util.FileUtil;
import com.github.freeacs.shell.util.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScriptMaker {
  public static List<String> getDeleteScript(int i, String[] args) {
    String unittypeName = getUnittypeFromArg(i, args);
    List<String> script = new ArrayList<>();
    script.add("echo off");
    if ("ALL".equals(unittypeName)) {
      script.add("echo WARNING: All unittypes will be deleted, press CTRL-C to abort");
    } else {
      script.add(
          "echo WARNING: The unittype \""
              + unittypeName
              + "\" will be deleted, press CTRL-C to abort");
    }
    script.add("pausescript");
    if ("ALL".equals(unittypeName)) {
      script.add("listunittypes > unittypes_sd.txt");
    } else {
      script.add("listunittypes \"^" + unittypeName + "$\" > unittypes_sd.txt");
    }
    script.add("/ut:${1}/ listprofiles -c < unittypes_sd.txt > profile_sd.txt");
    script.add("/ut:${1}/ listsyslogevents -c < unittypes_sd.txt > syslog_events_sd.txt");
    script.add("pr:${1}/ listunits -u -c < profile_sd.txt > units_sd.txt");
    if (ACSVersionCheck.triggerSupported) {
      script.add("/ut:${1}/ listtriggers -c parent-last < unittypes_sd.txt > triggers_sd.txt");
    }
    if (ACSVersionCheck.heartbeatSupported) {
      script.add("/ut:${1}/ listheartbeats -c < unittypes_sd.txt > heartbeats_sd.txt");
    }
    script.add("/ut:${1}/ listfiles -c < unittypes_sd.txt > files_sd.txt");
    script.add("/ut:${1}/ listgroups -c parent-last < unittypes_sd.txt > groups_sd.txt");
    script.add("/ut:${1}/ listjobs -c dep-first < unittypes_sd.txt > jobs_sd.txt");
    script.add("jo:${1}/ delfailedunits -c -u < jobs_sd.txt");
    if ("ALL".equals(unittypeName)) {
      script.add("listperms > perms_sd.txt");
    } else {
      script.add("listperms \"^" + unittypeName + "$\" > perms_sd.txt");
    }
    script.add("delperm -u < perms_sd.txt");
    if (ACSVersionCheck.heartbeatSupported) {
      script.add("delheartbeat -u < heartbeats_sd.txt");
    }
    if (ACSVersionCheck.triggerSupported) {
      script.add("deltrigger -u < triggers_sd.txt");
    }
    script.add("delsyslogevent -u < syslog_events_sd.txt");
    script.add("deljob -u < jobs_sd.txt");
    script.add("delgroup -u < groups_sd.txt");
    script.add("delfile -u < files_sd.txt");
    script.add("delunit -u < units_sd.txt");
    script.add("delprofile -u < profile_sd.txt");
    script.add("/ut:${1}/deltc < unittypes_sd.txt");
    script.add("/ut:${1}/deltesthistory < unittypes_sd.txt");
    script.add("delunittype -u < unittypes_sd.txt");
    script.add("delosfile perms_sd.txt");
    script.add("delosfile syslog_events_sd.txt");
    script.add("delosfile jobs_sd.txt");
    script.add("delosfile triggers_sd.txt");
    script.add("delosfile heartbeats_sd.txt");
    script.add("delosfile groups_sd.txt");
    script.add("delosfile files_sd.txt");
    script.add("delosfile units_sd.txt");
    script.add("delosfile profile_sd.txt");
    script.add("delosfile unittypes_sd.txt");
    script.add("return");
    script.add("echo on");
    return script;
  }

  private static List<String> getExportScript(String unittypeName, String path) {
    List<String> scriptLines = new ArrayList<>();
    scriptLines.add("listcertificates > " + path + "certificates.txt");
    if ("ALL".equals(unittypeName)) {
      scriptLines.add("listunittypes -a > " + path + "unittypes.txt");
    } else {
      scriptLines.add("listunittypes -a \"^" + unittypeName + "$\" > " + path + "unittypes.txt");
    }
    scriptLines.add(
        "/ut:${1}/ listparams -c < " + path + "unittypes.txt > " + path + "unittypeparams.txt");
    scriptLines.add(
        "/ut:${1}/ listsyslogevents -c < " + path + "unittypes.txt > " + path + "syslogevents.txt");
    if (ACSVersionCheck.heartbeatSupported) {
      scriptLines.add(
          "/ut:${1}/listheartbeats -c < " + path + "unittypes.txt > " + path + "heartbeats.txt");
    }
    scriptLines.add(
        "up:${1}/ listvalues -c -u < "
            + path
            + "unittypeparams.txt > "
            + path
            + "unittypeparamvalues.txt");
    scriptLines.add(
        "/ut:${1}/ listprofiles -c < " + path + "unittypes.txt > " + path + "profiles.txt");
    scriptLines.add(
        "pr:${1}/ listparams -c -u < " + path + "profiles.txt > " + path + "profileparams.txt");
    scriptLines.add("pr:${1}/ listunits -c -u < " + path + "profiles.txt > " + path + "units.txt");
    scriptLines.add(
        "un:${1}/ listunitparams -c -u < " + path + "units.txt > " + path + "unitparams.txt");
    scriptLines.add("/ut:${1}/ listfiles -c < " + path + "unittypes.txt > " + path + "files.txt");
    scriptLines.add(
        "/ut:${1}/ listgroups -c parent-first < "
            + path
            + "unittypes.txt > "
            + path
            + "groups.txt");
    scriptLines.add(
        "/ut:${1}/ listjobs -c dep-first < " + path + "unittypes.txt > " + path + "jobs.txt");
    scriptLines.add("jo:${1}/ listparams -c -u < " + path + "jobs.txt > " + path + "jobparams.txt");
    scriptLines.add("jo:${1}/ status -c -u < " + path + "jobs.txt > " + path + "jobstatus.txt");
    scriptLines.add(
        "gr:${1}/ listparamsforexport -c -u < "
            + path
            + "groups.txt > "
            + path
            + "groupparams.txt");
    scriptLines.add(
        "jo:${1}/ listfailedunits -c -u < " + path + "jobs.txt > " + path + "unitjobs.txt");
    if (ACSVersionCheck.triggerSupported) {
      scriptLines.add(
          "/ut:${1}/ listtriggers -c parent-first < "
              + path
              + "unittypes.txt > "
              + path
              + "triggers.txt");
    }
    scriptLines.add("listusers -c > " + path + "users.txt");
    if ("ALL".equals(unittypeName)) {
      scriptLines.add("listperms -c > " + path + "perms.txt");
    } else {
      scriptLines.add("listperms -c \"^" + unittypeName + "$\" > " + path + "perms.txt");
    }
    scriptLines.add("exportfile -c -u \"" + path + "${1}\" < " + path + "files.txt");
    scriptLines.add("return");
    return scriptLines;
  }

  private static List<String> getImportScript(String path) {
    List<String> scriptLines = new ArrayList<>();
    if (FileUtil.exists(path + "certificates.txt")) {
      scriptLines.add("setcertificate < " + path + "certificates.txt");
    }
    scriptLines.add("setunittype < " + path + "unittypes.txt");
    scriptLines.add("setparam -u < " + path + "unittypeparams.txt");
    scriptLines.add("setvalues -u < " + path + "unittypeparamvalues.txt");
    scriptLines.add("setprofile -u < " + path + "profiles.txt");
    scriptLines.add("setparam -u < " + path + "profileparams.txt");
    if (FileUtil.exists(path + "users.txt")) {
      scriptLines.add("setuser < " + path + "users.txt");
    }
    if (FileUtil.exists(path + "perms.txt")) {
      scriptLines.add("setperm < " + path + "perms.txt");
    }
    scriptLines.add("setunit -u < " + path + "units.txt");
    scriptLines.add("setparam -u < " + path + "unitparams.txt");
    List<String> fileLines = FileUtil.getLines(path + "files.txt");
    if (!fileLines.isEmpty() && StringUtil.split(fileLines.get(0)).length == 6) {
      scriptLines.add("importfile -u " + path + "${1} ${2} ${3} ${4} ${5} < " + path + "files.txt");
    } else {
      scriptLines.add(
          "importfile -u " + path + "${1} ${2} ${3} ${4} ${5} ${6} ${7} < " + path + "files.txt");
    }
    scriptLines.add("setgroup -u < " + path + "groups.txt");
    scriptLines.add("setparam -u < " + path + "groupparams.txt");
    scriptLines.add("setsyslogevent -u < " + path + "syslogevents.txt");
    if (ACSVersionCheck.heartbeatSupported && new File(path + "heartbeats.txt").exists()) {
      scriptLines.add("setheartbeat -u < " + path + "heartbeats.txt");
    }
    scriptLines.add("setjob -u < " + path + "jobs.txt");
    scriptLines.add("setparam -u < " + path + "jobparams.txt");
    scriptLines.add("${1} -u < " + path + "jobstatus.txt");
    scriptLines.add("setfailedunits -u < " + path + "unitjobs.txt");
    if (ACSVersionCheck.triggerSupported && new File(path + "triggers.txt").exists()) {
      scriptLines.add("settrigger -u < " + path + "triggers.txt");
    }
    scriptLines.add("return");
    return scriptLines;
  }

  public static List<String> getMigrateScript(int i, String[] args) {
    String type = args[i];
    String unittypeName = getUnittypeFromArg(i, args);
    String folder = null;
    for (int j = 0; j < args.length; j++) {
      if ("-dir".equals(args[j])) {
        try {
          folder = args[j + 1];
        } catch (Throwable t) {
          //					XAPSShell.println("No directory specified, using working directory to place the " +
          // unittypeName + "directory");
        }
      }
    }
    if (folder != null) {
      File f1 = new File(folder);
      if (f1.exists() && !f1.isDirectory()) {
        throw new IllegalArgumentException(
            "Cannot import/export since " + unittypeName + " is not a directory");
      } else {
        f1.mkdir();
      }
    }
    File dir;
    if (folder != null) {
      folder = folder + File.separator + unittypeName;
    } else {
      folder = unittypeName;
    }

    dir = new File(folder);
    if (dir.exists() && !dir.isDirectory()) {
      throw new IllegalArgumentException(
          "Cannot import/export since " + unittypeName + " is not a directory");
    } else if (!dir.exists()) {
      dir.mkdir();
    }
    if (!folder.endsWith(File.separator)) {
      folder += File.separator;
    }
    if (type.indexOf("export") > -1) {
      return getExportScript(unittypeName, folder);
    } else { // -import
      return getImportScript(folder);
    }
  }

  private static String getUnittypeFromArg(int i, String[] args) {
    String unittypeName = null;
    if (args.length >= i + 2) {
      unittypeName = args[i + 1];
    }
    if (unittypeName == null) {
      throw new IllegalArgumentException(
          args[i] + " must be followed by an argument specifying unittype name");
    }
    //		unittypeNameFromArg = unittypeName;
    return unittypeName;
  }

  public static List<String> getUpgradeSystemparametersScript(int i, String[] args) {
    List<String> scriptLines = new ArrayList<>();
    scriptLines.add("listunittypes > UNITTYPES");
    scriptLines.add("setunittype $ < UNITTYPES");
    scriptLines.add("${1} systemparameterscleanup < UNITTYPES");
    scriptLines.add("exit");
    return scriptLines;
  }
}
