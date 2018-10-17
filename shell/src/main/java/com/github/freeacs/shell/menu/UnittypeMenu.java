package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Groups;
import com.github.freeacs.dbi.Heartbeat;
import com.github.freeacs.dbi.Heartbeats;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobFlag;
import com.github.freeacs.dbi.JobFlag.JobServiceWindow;
import com.github.freeacs.dbi.JobFlag.JobType;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ScriptExecution;
import com.github.freeacs.dbi.ScriptExecutions;
import com.github.freeacs.dbi.SyslogEvent;
import com.github.freeacs.dbi.SyslogEvent.StorePolicy;
import com.github.freeacs.dbi.SyslogEvents;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.TriggerComparator;
import com.github.freeacs.dbi.Triggers;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.command.Option;
import com.github.freeacs.shell.output.Heading;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.Validation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UnittypeMenu {
  private Session session;
  private Context context;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

  public UnittypeMenu(Session session) {
    this.session = session;
    this.context = session.getContext();
  }

  /**
   * Returns true : has processed a cd-command Returns false : has processed another command
   * (everything else)
   */
  public boolean execute(String[] args, OutputHandler oh) throws Exception {
    if (args[0].startsWith("delf")) {
      delfile(args);
    } else if (args[0].startsWith("delg")) {
      delgroup(args);
    } else if (args[0].startsWith("delh")) {
      delheartbeat(args);
    } else if (args[0].startsWith("delj")) {
      deljob(args);
    } else if (args[0].startsWith("delpa")) {
      delparam(args);
    } else if (args[0].startsWith("delpr")) {
      delprofile(args);
    } else if (args[0].startsWith("delsy")) {
      delsyslogevent(args);
    } else if (args[0].startsWith("deltr")) {
      deltrigger(args);
    } else if ("exportfile".equals(args[0])) {
      exportfile(args);
    } else if ("importfile".equals(args[0])) {
      importfile(args);
    } else if (args[0].startsWith("listex")) {
      listexecutions(args, oh);
    } else if (args[0].startsWith("listf")) {
      listfiles(args, oh);
    } else if (args[0].startsWith("listg")) {
      listgroups(args, oh);
    } else if (args[0].startsWith("listh")) {
      listheartbeats(args, oh);
    } else if (args[0].startsWith("listj")) {
      listjobs(args, oh);
    } else if (args[0].startsWith("listpa")) {
      listparams(args, oh);
    } else if (args[0].startsWith("listpr")) {
      listprofiles(args, oh);
    } else if (args[0].startsWith("listsy")) {
      listsyslogevents(args, oh);
    } else if (args[0].startsWith("listtr")) {
      listtriggers(args, oh);
    } else if (args[0].startsWith("listu")) {
      listunits(args, oh);
    } else if (args[0].startsWith("move")) {
      moveunit(args, oh);
    } else if (args[0].startsWith("sete")) {
      setexecution(args);
    } else if (args[0].startsWith("setg")) {
      setgroup(args);
    } else if (args[0].startsWith("seth")) {
      setheartbeat(args);
    } else if (args[0].startsWith("setj")) {
      setjob(args);
    } else if (args[0].startsWith("setpa")) {
      setparam(args);
    } else if (args[0].startsWith("setpr")) {
      setprofile(args);
    } else if (args[0].startsWith("setsy")) {
      setsyslogevent(args);
    } else if (args[0].startsWith("settr")) {
      settrigger(args);
    } else if ("systemparameterscleanup".equals(args[0])) {
      systemparameterscleanup(args);
    } else {
      throw new IllegalArgumentException("The command " + args[0] + " was not recognized.");
    }
    return false;
  }

  private void listprofiles(String[] args, OutputHandler oh) throws Exception {
    Profile[] profiles = context.getUnittype().getProfiles().getProfiles();
    Listing listing = oh.getListing();
    listing.setHeading(Listing.HEADER_PROFILE, "Parameter-Count");
    for (Profile profile : profiles) {
      if (!Validation.matches(args.length > 1 ? args[1] : null, profile.getName())) {
        continue;
      }
      listing.addLine(
          profile.getName(),
          String.valueOf(profile.getProfileParameters().getProfileParameters().length));
    }
  }

  private void setprofile(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    String profileName = args[1];
    Profile profile = context.getUnittype().getProfiles().getByName(profileName);
    if (profile == null) {
      profile = new Profile(profileName, context.getUnittype());
      context.getUnittype().getProfiles().addOrChangeProfile(profile, session.getAcs());
      context.println("[" + session.getCounter() + "] The profile " + profileName + " is added.");
    } else {
      context.println(
          "[" + session.getCounter() + "] The profile " + profileName + " already exists");
    }
    session.incCounter();
  }

  private void delprofile(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    String profileName = args[1];
    Profile profile = context.getUnittype().getProfiles().getByName(profileName);
    if (profile == null) {
      context.println("The profile " + profileName + " does not exist");
    } else {
      context.getUnittype().getProfiles().deleteProfile(profile, session.getAcs(), true);
      context.println("[" + session.getCounter() + "] The profile " + profileName + " is deleted.");
      session.incCounter();
    }
  }

  private void listparams(String[] args, OutputHandler oh) throws Exception {
    UnittypeParameter[] unittypeParameters =
        context.getUnittype().getUnittypeParameters().getUnittypeParameters();
    Listing listing = oh.getListing();
    listing.setHeading(Listing.HEADER_UNITTYPE_PARAMETER, "Flag");
    String flagArg = null;
    String matchArg = null;
    int flagArgIndex = 0;
    for (int i = 1; i < args.length; i++) {
      String input = args[i];
      if ("-flag".equals(input) && i < args.length - 1) {
        flagArgIndex = i + 1;
        flagArg = args[flagArgIndex];
      } else if (i != flagArgIndex) {
        matchArg = args[i];
      }
    }
    int i = 0;
    while (i < unittypeParameters.length) {
      if (flagArg != null) {
        UnittypeParameterFlag utpFlag = unittypeParameters[i].getFlag();
        if ((flagArg.contains("R") && flagArg.contains("W") && !utpFlag.isReadWrite())
            || (flagArg.contains("R") && !flagArg.contains("W") && !utpFlag.isReadOnly())
            || (flagArg.contains("C") && !utpFlag.isConfidential())
            || (flagArg.contains("A") && !utpFlag.isAlwaysRead())
            || (flagArg.contains("B") && !utpFlag.isBootRequired())
            || (flagArg.contains("D") && !utpFlag.isDisplayable())
            || (flagArg.contains("S") && !utpFlag.isSearchable())
            || (flagArg.contains("X") && !utpFlag.isSystem())) {
          i++;
          continue;
        }
      }
      if (!Validation.matches(matchArg, unittypeParameters[i].getName())) {
        i++;
        continue;
      }
      listing.addLine(unittypeParameters[i].getName(), unittypeParameters[i].getFlag().toString());
      i++;
    }
  }

  private void setparam(String[] args) throws Exception {
    Validation.numberOfArgs(args, 3);
    String unittypeParameterName = args[1];
    String unittypeParameterFlagStr = args[2];
    UnittypeParameters unittypeParameters = context.getUnittype().getUnittypeParameters();
    UnittypeParameterFlag upf = new UnittypeParameterFlag(unittypeParameterFlagStr);
    UnittypeParameter unittypeParameter = unittypeParameters.getByName(unittypeParameterName);
    String action = "added";
    if (unittypeParameter != null) {
      unittypeParameter.setFlag(upf);
      action = "changed";
    } else {
      unittypeParameter = new UnittypeParameter(context.getUnittype(), unittypeParameterName, upf);
    }
    List<UnittypeParameter> utpList = session.getBatchStorage().getAddChangeUnittypeParameters();
    if (!utpList.isEmpty()) {
      UnittypeParameter previousUtp = utpList.get(utpList.size() - 1);
      if (previousUtp.getUnittype().getId().intValue() != unittypeParameter.getUnittype().getId()) {
        Unittype ut = previousUtp.getUnittype();
        ut.getUnittypeParameters().addOrChangeUnittypeParameters(utpList, session.getAcs());
        session.getBatchStorage().setAddChangeUnittypeParameters(null);
        utpList = session.getBatchStorage().getAddChangeUnittypeParameters();
      }
    }
    utpList.add(unittypeParameter);
    context.println(
        "["
            + session.getCounter()
            + "] The unit type parameter "
            + unittypeParameter.getName()
            + " is "
            + action
            + ".");
    if (session.getBatchStorage().getAddChangeUnittypeParameters().size() == 1000) {
      unittypeParameters.addOrChangeUnittypeParameters(utpList, session.getAcs());
      session.getBatchStorage().setAddChangeUnittypeParameters(null);
    }
    session.incCounter();
  }

  private void delparam(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    String unittypeParameterName = args[1];
    UnittypeParameters unittypeParameters = context.getUnittype().getUnittypeParameters();
    if (unittypeParameters.getByName(unittypeParameterName) != null) {
      UnittypeParameter up = unittypeParameters.getByName(unittypeParameterName);
      List<UnittypeParameter> utpList = session.getBatchStorage().getDeleteUnittypeParameters();
      if (!utpList.isEmpty()) {
        UnittypeParameter previousUtp = utpList.get(utpList.size() - 1);
        if (previousUtp.getUnittype().getId().intValue() != up.getUnittype().getId()) {
          Unittype ut = previousUtp.getUnittype();
          ut.getUnittypeParameters().deleteUnittypeParameters(utpList, session.getAcs());
          session.getBatchStorage().setDeleteUnittypeParameters(null);
          utpList = session.getBatchStorage().getDeleteUnittypeParameters();
        }
      }
      utpList.add(up);
      context.println(
          "["
              + session.getCounter()
              + "] The unit type parameter "
              + unittypeParameterName
              + " is scheduled for deletion.");
      if (utpList.size() == 1000) {
        unittypeParameters.deleteUnittypeParameters(utpList, session.getAcs());
        session.getBatchStorage().setDeleteUnittypeParameters(null);
        context.println("The unit type parameters scheduled for deletion are deleted");
      }
    } else {
      context.println(
          "["
              + session.getCounter()
              + "] The unittype parameter "
              + unittypeParameterName
              + " does not exist.");
      session.incCounter();
    }
  }

  private void listexecutions(String[] args, OutputHandler oh) throws Exception {
    ScriptExecutions executions = new ScriptExecutions(context.getSession().getXapsProps());
    List<ScriptExecution> executionList =
        executions.getExecutions(context.getUnittype(), null, null);
    Listing listing = oh.getListing();
    listing.setHeading(
        "Name",
        "Arguments",
        "Request-Tms",
        "Start-Tms",
        "End-Tms",
        "Exit-Status",
        "Error-Message",
        "Request-Id");
    for (ScriptExecution se : executionList) {
      if (!Validation.matches(
          args.length > 1 ? args[1] : null,
          se.getScriptFile().getName(),
          se.getArguments(),
          se.getErrorMessage(),
          se.getRequestId())) {
        continue;
      }
      String exitStatus = "NULL";
      if (se.getExitStatus() != null) {
        exitStatus = se.getExitStatus() ? "ERROR" : "OK";
      }
      listing.addLine(
          se.getScriptFile().getName(),
          se.getArguments(),
          String.valueOf(se.getRequestTms()),
          String.valueOf(se.getStartTms()),
          String.valueOf(se.getEndTms()),
          exitStatus,
          se.getErrorMessage(),
          se.getRequestId());
    }
  }

  private void listfiles(String[] args, OutputHandler oh) throws Exception {
    com.github.freeacs.dbi.File[] files = context.getUnittype().getFiles().getFiles();
    Listing listing = oh.getListing();
    listing.setHeading("Name", "Type", "Version", "Date", "TargetName", "Owner", "Description");

    for (com.github.freeacs.dbi.File f : files) {
      String owner = f.getOwner() == null ? "NULL" : f.getOwner().getUsername();
      if (!Validation.matches(
          args.length > 1 ? args[1] : null,
          f.getName(),
          f.getType().toString(),
          f.getVersion(),
          f.getTargetName(),
          owner,
          f.getDescription())) {
        continue;
      }
      listing.addLine(
          f.getName(),
          f.getType().toString(),
          f.getVersion(),
          dateFormat.format(f.getTimestamp()),
          f.getTargetName(),
          owner,
          f.getDescription());
    }
  }

  private void exportfile(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    String name = args[1];
    String filename = name;
    if (name.contains(File.separator)) {
      name = name.substring(name.lastIndexOf(File.separator) + 1);
    }
    com.github.freeacs.dbi.File f = context.getUnittype().getFiles().getByName(name);
    if (f == null) {
      throw new IllegalArgumentException("The file does not exist");
    }
    File file = new File(filename);
    if (file.exists()) {
      context.println(
          "INFO: Name conflict when exporting "
              + f.getName()
              + ", renaming to "
              + f.getName()
              + "-"
              + context.getUnittype().getName()
              + ", the importfile command will find this file even without the "
              + context.getUnittype().getName()
              + " prefix if run within the same unittype.");
      file = new File(filename + "-" + context.getUnittype().getName());
    }
    FileOutputStream fos = new FileOutputStream(file);
    fos.write(f.getContent());
    fos.flush();
    fos.close();
  }

  /**
   * Index: 1 2 3 4 5 6 7 OLD : Name Type Description Version Date NEW : Name Type Version Date
   * TargetName Owner Description
   */
  private void importfile(String[] args) throws Exception {
    Validation.numberOfArgs(args, 6);
    String name = args[1];
    String filename = args[1];
    if (name.contains("/")) {
      name = name.substring(name.lastIndexOf('/') + 1);
    }
    if (name.contains("\\")) {
      name = name.substring(name.lastIndexOf('\\') + 1);
    }
    FileType type = FileType.valueOf(args[2]);
    String desc = null, ver = null, targetName = null, ownerName = null;
    Date date = null;
    User owner;
    boolean fusion2012 = args.length == 6;
    if (fusion2012) { // This logic is created to read 2012R1 files output and import into into
      // 2013R1
      desc = args[3];
      ver = args[4];
      String dateStr = args[5];
      if (!"NULL".equals(dateStr)) {
        date = dateFormat.parse(dateStr);
      }
    } else {
      ver = args[3];
      String dateStr = args[4];
      if (!"NULL".equals(dateStr)) {
        date = dateFormat.parse(dateStr);
      }
      targetName = autoboxString(args[5]);
      ownerName = autoboxString(args[6]);
      desc = args[7];
    }

    User loggedInUser = session.getAcs().getUser();
    owner = loggedInUser;
    if (loggedInUser.isAdmin() && ownerName != null) {
      owner = session.getAcs().getUsers().getUnprotected(ownerName);
    }
    byte[] b = null;
    if (filename.startsWith("DUMMY")) {
      b = "Test".getBytes();
    } else {
      File file = new File(filename + "-" + context.getUnittype().getName());
      if (!file.exists()) {
        file = new File(filename);
        if (!file.exists()) {
          throw new IllegalArgumentException("The file " + filename + " does not exist");
        }
      }
      b = new byte[(int) file.length()];
      FileInputStream fs = new FileInputStream(file);
      fs.read(b);
      fs.close();
    }
    com.github.freeacs.dbi.File f = context.getUnittype().getFiles().getByVersionType(ver, type);
    if (f != null && !f.getName().equals(name)) {
      throw new IllegalArgumentException(
          "Cannot add/change file, beacuse version + file type is the same as for " + f.getName());
    }
    String action = "added";
    if (f != null) {
      f.setName(name);
      f.setType(type);
      f.setUnittype(context.getUnittype());
      f.setDescription(desc);
      f.setVersion(ver);
      f.setTimestamp(date);
      f.setTargetName(targetName);
      f.setBytes(b);
      action = "changed";
      //			context.getUnittype().getFiles().deleteFile(f, session.getXaps());
      //			context.println("Found an existing file with same name (" + name + "), deleted it");
    } else {
      f =
          new com.github.freeacs.dbi.File(
              context.getUnittype(), name, type, desc, ver, date, targetName, owner);
      f.setBytes(b);
    }
    context.getUnittype().getFiles().addOrChangeFile(f, session.getAcs());
    f.resetContentToNull();
    context.println("File: " + name + " was " + action);
  }

  private void delfile(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    String filename = args[1];
    com.github.freeacs.dbi.File f = context.getUnittype().getFiles().getByName(filename);
    if (f == null) {
      throw new IllegalArgumentException("The file " + args[1] + " does not exist");
    }
    context.getUnittype().getFiles().deleteFile(f, session.getAcs());
    context.println("[" + session.getCounter() + "] File " + args[1] + " is deleted");
    session.incCounter();
  }

  private void setheartbeat(String[] args) throws Exception {
    Validation.numberOfArgs(args, 5);
    Unittype unittype = context.getUnittype();
    Heartbeats heartbeats = unittype.getHeartbeats();
    String name = args[1];
    Heartbeat heartbeat = heartbeats.getByName(name);
    String groupName = args[2];
    Group group = unittype.getGroups().getByName(groupName);
    if (group == null) {
      throw new IllegalArgumentException("The group name " + groupName + " is unknown");
    }
    String expression = args[3];
    if (expression.contains("%") || expression.contains("&") || expression.contains(";")) {
      throw new IllegalArgumentException(
          "The expression contains characters like %, & and ;, which are not legal");
    }
    Integer timeoutHours = autoboxInteger(args[4]);
    if (timeoutHours == null || timeoutHours < 1 || timeoutHours > Heartbeat.MAX_TIMEOUT_HOURS) {
      throw new IllegalArgumentException(
          "The timeout hours argument must be between 1 and " + Heartbeat.MAX_TIMEOUT_HOURS);
    }
    String action = "changed";
    if (heartbeat == null) {
      action = "added";
      heartbeat = new Heartbeat();
      heartbeat.setUnittype(unittype);
      heartbeat.setName(name);
    }
    heartbeat.setExpression(expression);
    heartbeat.setGroup(group);
    heartbeat.setTimeoutHours(timeoutHours);
    heartbeats.addOrChangeHeartbeat(heartbeat, session.getAcs());
    context.println(
        "[" + session.getCounter() + "] The heartbeat " + heartbeat.getName() + " is " + action);
    session.incCounter();
  }

  /**
   * Listing.setHeading("Id", "Name", "Group", "Expression", "StorePolicy", "Script", "DeleteLim",
   * "Description");
   */
  private void setsyslogevent(String[] args) throws Exception {
    Validation.numberOfArgs(args, 9);
    Unittype unittype = context.getUnittype();
    SyslogEvents syslogEvents = unittype.getSyslogEvents();
    SyslogEvent syslogEvent;
    Integer eventId = null;
    try {
      eventId = Integer.valueOf(args[1]);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException(
          "The syslogevent id must be a number (instead of " + args[1] + ")");
    }
    String name = args[2];
    Group group = unittype.getGroups().getByName(autoboxString(args[3]));
    String expression = args[4];
    if (eventId < 1000) {
      expression = null;
    }
    if (expression.contains("%")) {
      expression = expression.replaceAll("#%#", "@@@@@@@");
      expression = expression.replaceAll("%", ".*");
      expression = expression.replaceAll("@@@@@@@", "%");
      context.println(
          "WARNING: '%' (SQL wildchar) is converted to '.*' (Regex equivalent). If you really want to match the character '%', use '#%#'");
    }
    if (expression.contains("_")) {
      expression = expression.replaceAll("#_#", "@@@@@@@");
      expression = expression.replaceAll("_", ".{1}");
      expression = expression.replaceAll("@@@@@@@", "_");
      context.println(
          "WARNING: '_' (SQL wildchar) is converted to '.{1}' (Regex equivalent). If you really want to match the character '_', use '#_#'");
    }
    StorePolicy storePolicy = StorePolicy.valueOf(args[5]);
    com.github.freeacs.dbi.File script = unittype.getFiles().getByName(autoboxString(args[6]));
    Integer deleteLimit = autoboxInteger(args[7]);
    String desc = args[8];
    String action = "changed";
    syslogEvent = syslogEvents.getByEventId(Integer.valueOf(args[1]));
    if (syslogEvent == null) {
      action = "added";
      syslogEvent = new SyslogEvent();
      syslogEvent.setUnittype(unittype);
      syslogEvent.setEventId(eventId);
    }
    syslogEvent.setName(name);
    syslogEvent.setDescription(desc);
    syslogEvent.setGroup(group);
    syslogEvent.setExpression(expression);
    syslogEvent.setStorePolicy(storePolicy);
    syslogEvent.setScript(script);
    syslogEvent.setDeleteLimit(deleteLimit);
    syslogEvents.addOrChangeSyslogEvent(syslogEvent, session.getAcs());
    context.println("[" + session.getCounter() + "] The syslog event " + eventId + " is " + action);
    session.incCounter();
  }

  /** Delsyslogevent <id>. */
  private void delsyslogevent(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    Unittype unittype = context.getUnittype();
    SyslogEvents syslogEvents = unittype.getSyslogEvents();
    SyslogEvent syslogEvent;
    Integer eventId = null;
    try {
      eventId = Integer.valueOf(args[1]);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("The syslogevent id must be a number");
    }
    syslogEvent = syslogEvents.getByEventId(eventId);
    if (syslogEvent == null) {
      context.println("The syslogevent " + args[1] + " does not exist");
      return;
    }
    syslogEvents.deleteSyslogEvent(syslogEvent, session.getAcs());
    context.println("[" + session.getCounter() + "] The syslog event " + args[1] + " is deleted");
    session.incCounter();
  }

  /** Deltrigger <id>. */
  private void deltrigger(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    Unittype unittype = context.getUnittype();
    Triggers triggers = unittype.getTriggers();
    Trigger trigger = triggers.getByName(args[1]);
    if (trigger == null) {
      context.println("The trigger " + args[1] + " does not exist");
      return;
    }
    triggers.deleteTrigger(trigger, session.getAcs());
    context.println("[" + session.getCounter() + "] The trigger " + args[1] + " is deleted");
    session.incCounter();
  }

  /** Delheartbeat <id>. */
  private void delheartbeat(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    Unittype unittype = context.getUnittype();
    Heartbeats heartbeats = unittype.getHeartbeats();
    Heartbeat heartbeat = heartbeats.getByName(args[1]);
    if (heartbeat == null) {
      context.println("The heartbeat " + args[1] + " does not exist");
      return;
    }
    heartbeats.deleteHeartbeat(heartbeat, session.getAcs());
    context.println("[" + session.getCounter() + "] The hearbeat " + args[1] + " is deleted");
    session.incCounter();
  }

  /** Listsyslogevents. */
  private void listsyslogevents(String[] args, OutputHandler oh) throws Exception {
    Unittype unittype = context.getUnittype();
    SyslogEvent[] syslogEvents = unittype.getSyslogEvents().getSyslogEvents();
    Listing listing = oh.getListing();
    listing.setHeading(
        "Id", "Name", "Group", "Expression", "StorePolicy", "Script", "DeleteLim", "Description");
    for (SyslogEvent se : syslogEvents) {
      Line line = new Line(String.valueOf(se.getEventId()), se.getName());
      if (se.getGroup() != null) {
        line.addValue(se.getGroup().getName());
      } else {
        line.addValue("NULL");
      }
      if (se.getExpression() != null) {
        line.addValue(se.getExpression());
      } else {
        line.addValue("NULL");
      }
      line.addValue(se.getStorePolicy().toString());
      if (se.getScript() != null) {
        line.addValue(se.getScript().getName());
      } else {
        line.addValue("NULL");
      }
      if (se.getDeleteLimit() != null) {
        line.addValue(String.valueOf(se.getDeleteLimit()));
      } else {
        line.addValue("NULL");
      }
      line.addValue(se.getDescription());
      listing.addLine(line);
    }
  }

  private void listheartbeats(String[] args, OutputHandler oh) throws Exception {
    Unittype unittype = context.getUnittype();
    Heartbeat[] heartbeats = unittype.getHeartbeats().getHeartbeats();
    Listing listing = oh.getListing();
    listing.setHeading("Name", "Group", "Expression", "TimeoutHours");
    for (Heartbeat heartbeat : heartbeats) {
      Line line =
          new Line(
              heartbeat.getName(),
              heartbeat.getGroup().getName(),
              heartbeat.getExpression(),
              String.valueOf(heartbeat.getTimeoutHours()));
      listing.addLine(line);
    }
  }

  /**
   * Setjob <jobname> <jobtype> REGULAR|DISRUPTIVE <groupname> <parent-jobname>|NULL <description>
   * <software-version> <unconfirmed-timeout> <move-to-profile>|NULL <stop-rules>|NULL
   * [<repeat-count>|NULL <repeat-interval-sec>|NULL]\n.
   */
  private void setjob(String[] args) throws Exception {
    Validation.numberOfArgs(args, 11);

    String jobName = args[1];

    JobType jobType = JobType.valueOf(args[2]);

    JobServiceWindow serviceWindow = JobServiceWindow.valueOf(args[3]);

    Group group = context.getUnittype().getGroups().getByName(args[4]);
    if (group == null) {
      throw new IllegalArgumentException("Group " + args[4] + " does not exist");
    }
    String parent = args[5];
    Job dependency = null;
    if (!"NULL".equals(parent)) {
      dependency = context.getUnittype().getJobs().getByName(parent);
    }

    String desc = args[6];

    String fileVersion = args[7];
    com.github.freeacs.dbi.File file = null;
    if (jobType.requireFile()) {
      if ("NULL".equals(fileVersion)) {
        throw new IllegalArgumentException(
            "The jobtype " + jobType + " requires a file to execute/download");
      } else {
        file =
            context
                .getUnittype()
                .getFiles()
                .getByVersionType(fileVersion, jobType.getCorrelatedFileType());
        if (file == null) {
          throw new IllegalArgumentException(
              "No file found with version number: "
                  + args[7]
                  + " of file type "
                  + jobType.getCorrelatedFileType());
        }
      }
    } else if (!jobType.requireFile() && !"NULL".equals(fileVersion)) {
      throw new IllegalArgumentException(
          "The jobtype " + jobType + " cannot be accompanied with a file");
    }

    int unconfirmedTimeout;
    try {
      unconfirmedTimeout = Integer.parseInt(args[8]);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException(
          "Unconfirmed timeout must be a number (instead of " + args[8] + ")");
    }

    String stopRules = null;
    if (!"NULL".equals(args[9])) {
      stopRules = args[9];
    }

    Integer repeat = null;
    Integer repeatInterval = null;
    if (args.length > 11) {
      try {
        if (!"NULL".equals(args[10])) {
          repeat = Integer.parseInt(args[10]);
          if (!"NULL".equals(args[11])) {
            repeatInterval = Integer.parseInt(args[11]);
          } else {
            repeatInterval = 86400;
          }
        }
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException("Repeat-count and repeat-interval-sec must be numbers");
      }
    }

    Job job = context.getUnittype().getJobs().getByName(jobName);

    if (job != null) {
      job.setDescription(desc);
      job.setUnconfirmedTimeout(unconfirmedTimeout);
      job.setStopRules(stopRules);
      job.setFile(file);
      job.setDependency(dependency);
      //			job.setMoveToProfile(moveToProfile);
      job.setRepeatCount(repeat);
      job.setRepeatInterval(repeatInterval);
      context.getUnittype().getJobs().changeFromUI(job, session.getAcs());
      context.println(
          "["
              + session.getCounter()
              + "] The job "
              + job.getName()
              + " is changed, but not all fields are changed since the job is already created.");
    } else {
      job =
          new Job(
              context.getUnittype(),
              jobName,
              new JobFlag(jobType, serviceWindow),
              desc,
              group,
              unconfirmedTimeout,
              stopRules,
              file,
              dependency,
              repeat,
              repeatInterval);
      context.getUnittype().getJobs().add(job, session.getAcs());
      context.println("[" + session.getCounter() + "] The job " + job.getName() + " is added");
    }
    session.incCounter();
  }

  private void deljob(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    Job j = context.getUnittype().getJobs().getByName(args[1]);
    if (j == null) {
      context.println("The job " + args[1] + " does not exist");
      return;
    }
    context.getUnittype().getJobs().delete(j, session.getAcs());
    context.println("[" + session.getCounter() + "] Job " + j.getName() + " is deleted");
    session.incCounter();
  }

  private void listjobs(String[] args, OutputHandler oh) throws Exception {
    String jobname = null;
    Job[] jobs = context.getUnittype().getJobs().getJobs();
    List<Job> filteredJobs = new ArrayList<>();
    for (Job job : jobs) {
      if (job.getGroup().getUnittype().getId().intValue()
          == context.getUnittype().getId().intValue()) {
        filteredJobs.add(job);
      }
    }
    if (args.length > 2) {
      jobname = args[1];
      if ("DEP-FIRST".equalsIgnoreCase(args[2])) {
        filteredJobs.sort(new JobComparator(true));
      } else if ("DEP-LAST".equalsIgnoreCase(args[2])) {
        filteredJobs.sort(new JobComparator(false));
      }
    } else if (args.length > 1) {
      if ("DEP-FIRST".equalsIgnoreCase(args[1])) {
        filteredJobs.sort(new JobComparator(true));
      } else if ("DEP-LAST".equalsIgnoreCase(args[1])) {
        filteredJobs.sort(new JobComparator(false));
      } else {
        jobname = args[1];
      }
    }

    Listing listing = oh.getListing();
    listing.setHeading(
        Listing.HEADER_JOB,
        "Type",
        "ServiceW.",
        "Group",
        "Parent",
        "Description",
        "Software/Script",
        "UC-timeout",
        "Stop-rules",
        "Repeat-counter",
        "Repeat-interval");
    for (Job job : filteredJobs) {
      if (jobname != null
          && !Validation.matches(
              jobname,
              job.getName(),
              job.getFlags().getType().toString(),
              job.getFlags().getServiceWindow().toString(),
              job.getDescription(),
              job.getFile().getVersion())) {
        continue;
      }
      Line line = new Line();

      line.addValue(job.getName());
      line.addValue(job.getFlags().getType().toString());
      line.addValue(job.getFlags().getServiceWindow().toString());
      line.addValue(job.getGroup().getName());
      if (job.getDependency() != null) {
        line.addValue(job.getDependency().getName());
      } else {
        line.addValue("NULL");
      }
      line.addValue(job.getDescription());
      if (job.getFile() != null) {
        line.addValue(job.getFile().getVersion());
      } else {
        line.addValue("NULL");
      }
      line.addValue(String.valueOf(job.getUnconfirmedTimeout()));
      if (job.getStopRules() == null || job.getStopRules().isEmpty()) {
        line.addValue("NULL");
      } else {
        line.addValue(job.getStopRulesSerialized());
      }
      if (job.getRepeatCount() != null) {
        line.addValue(String.valueOf(job.getRepeatCount()));
      } else {
        line.addValue("NULL");
      }
      if (job.getRepeatInterval() != null) {
        line.addValue(String.valueOf(job.getRepeatInterval()));
      } else {
        line.addValue("NULL");
      }
      listing.addLine(line);
    }
  }

  /**
   * Settrigger <name> <description> <type> <action> <active> <group-name>
   * <evaluation-period-minutes> <notify-interval-hours> <script> <parent-triggername> <to-list>
   * <syslog-event-id> <no-events-total> <no-events-pr-unit> <no-units>.
   */
  private void listtriggers(String[] args, OutputHandler oh) throws Exception {
    Trigger[] triggers = context.getUnittype().getTriggers().getTriggers();
    String searchStr = null;
    if (args.length > 2) {
      searchStr = args[1];
      if ("PARENT-FIRST".equalsIgnoreCase(args[2])) {
        Arrays.sort(triggers, new TriggerComparator(true));
      } else if ("PARENT-LAST".equalsIgnoreCase(args[2])) {
        Arrays.sort(triggers, new TriggerComparator(false));
      }
    } else if (args.length > 1) {
      if ("PARENT-FIRST".equalsIgnoreCase(args[1])) {
        Arrays.sort(triggers, new TriggerComparator(true));
      } else if ("PARENT-LAST".equalsIgnoreCase(args[1])) {
        Arrays.sort(triggers, new TriggerComparator(false));
      } else {
        searchStr = args[1];
      }
    }
    Listing listing = oh.getListing();
    listing.setHeading(
        "Name",
        "Description",
        "Type",
        "Action",
        "Active",
        "EvalPeriod(min)",
        "NotifyInterval(hour)",
        "Script",
        "Parent-Trigger",
        "Subscriptions",
        "SyslogEvent",
        "NoEvents",
        "NoEventsPrUnit",
        "NoUnits");
    for (Trigger trigger : triggers) {
      Line line = new Line();
      String scriptName = null;
      if (trigger.getScript() != null) {
        scriptName = trigger.getScript().getName();
      }
      String syslogEventId = null;
      if (trigger.getSyslogEvent() != null) {
        syslogEventId = String.valueOf(trigger.getSyslogEvent().getEventId());
      }
      String parentTriggerName = null;
      if (trigger.getParent() != null) {
        parentTriggerName = trigger.getParent().getName();
      }
      if (!Validation.matches(
          searchStr,
          trigger.getName(),
          trigger.getDescription(),
          trigger.getTriggerTypeStr(),
          trigger.getNotifyTypeAsStr(),
          String.valueOf(trigger.isActive()),
          scriptName,
          syslogEventId,
          parentTriggerName)) {
        continue;
      }
      line.addValue(trigger.getName());
      line.addValue(trigger.getDescription());
      line.addValue(trigger.getTriggerTypeStr());
      line.addValue(trigger.getNotifyTypeAsStr());
      line.addValue(String.valueOf(trigger.isActive()));
      //			line.addValue(groupName);
      line.addValue(trigger.getEvalPeriodMinutes());
      line.addValue(trigger.getNotifyIntervalHours());
      line.addValue(scriptName);
      line.addValue(parentTriggerName);
      if (trigger.getToList() != null) {
        line.addValue(trigger.getToList());
      } else {
        line.addValue("NULL");
      }
      line.addValue(syslogEventId);
      line.addValue(trigger.getNoEvents());
      line.addValue(trigger.getNoEventsPrUnit());
      line.addValue(trigger.getNoUnits());
      listing.addLine(line);
    }
  }

  private void listgroups(String[] args, OutputHandler oh) throws Exception {
    Group[] groups = context.getUnittype().getGroups().getGroups();
    String searchStr = null;
    if (args.length > 2) {
      searchStr = args[1];
      if ("PARENT-FIRST".equalsIgnoreCase(args[2])) {
        Arrays.sort(groups, new GroupComparator(true));
      } else if ("PARENT-LAST".equalsIgnoreCase(args[2])) {
        Arrays.sort(groups, new GroupComparator(false));
      }
    } else if (args.length > 1) {
      if ("PARENT-FIRST".equalsIgnoreCase(args[1])) {
        Arrays.sort(groups, new GroupComparator(true));
      } else if ("PARENT-LAST".equalsIgnoreCase(args[1])) {
        Arrays.sort(groups, new GroupComparator(false));
      } else {
        searchStr = args[1];
      }
    }
    Listing listing = oh.getListing();
    listing.setHeading(Listing.HEADER_GROUP, "Parent", "Description", "Profile", "Last Count");
    for (Group group : groups) {
      Line line = new Line();
      if (searchStr != null
          && !Validation.matches(searchStr, group.getName(), group.getDescription())) {
        continue;
      }
      line.addValue(group.getName());
      if (group.getParent() != null) {
        line.addValue(group.getParent().getName());
      } else {
        line.addValue("NULL");
      }
      line.addValue(group.getDescription());
      if (group.getTopParent().getProfile() != null) {
        line.addValue(group.getTopParent().getProfile().getName());
      } else {
        line.addValue("NULL");
      }
      if (group.getCount() != null) {
        line.addValue(String.valueOf(group.getCount()));
      } else {
        line.addValue("Not counted yet");
      }
      listing.addLine(line);
    }
  }

  private String autoboxString(String arg) {
    if ("NULL".equals(arg)) {
      return null;
    } else {
      return arg;
    }
  }

  private Integer autoboxInteger(String arg) {
    try {
      if ("NULL".equals(arg)) {
        return null;
      } else {
        return arg != null ? Integer.valueOf(arg) : null;
      }
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("The argument " + arg + " was not a number (as expected)");
    }
  }

  /**
   * Settrigger <name> <description> <type> <action> <active> <group-name>
   * <evaluation-period-minutes> <notify-interval-hours> <script> <parent-triggername> <to-list>
   * <syslog-event-id> <no-events-total> <no-events-pr-unit> <no-units>.
   */
  private void settrigger(String[] args) throws Exception {
    Unittype unittype = context.getUnittype();
    Triggers triggers = unittype.getTriggers();
    Validation.numberOfArgs(args, 15);
    Trigger thisTrigger = triggers.getByName(args[1]);
    String action = null;
    if (thisTrigger == null) {
      action = "added";
      thisTrigger = new Trigger(Trigger.getTriggerType(args[3]), Trigger.getNotifyType(args[4]));
      thisTrigger.setName(args[1]);
      thisTrigger.setUnittype(unittype);
    } else {
      action = "changed";
      thisTrigger.setTriggerType(Trigger.getTriggerType(args[3]));
      thisTrigger.setNotifyType(Trigger.getNotifyType(args[4]));
    }
    thisTrigger.setDescription(autoboxString(args[2]));
    thisTrigger.setActive("true".equalsIgnoreCase(args[5]));
    thisTrigger.setEvalPeriodMinutes(autoboxInteger(args[6]));
    thisTrigger.setNotifyIntervalHours(autoboxInteger(args[7]));
    thisTrigger.setScript(unittype.getFiles().getByName(args[8]));
    thisTrigger.setParent(triggers.getByName(args[9]));
    thisTrigger.setToList(autoboxString(args[10]));
    if ("NULL".equals(args[11])) {
      thisTrigger.setSyslogEvent(null);
    } else {
      thisTrigger.setSyslogEvent(unittype.getSyslogEvents().getByEventId(autoboxInteger(args[11])));
    }
    thisTrigger.setNoEvents(autoboxInteger(args[12]));
    thisTrigger.setNoEventPrUnit(autoboxInteger(args[13]));
    thisTrigger.setNoUnits(autoboxInteger(args[14]));
    triggers.addOrChangeTrigger(thisTrigger, session.getAcs());
    context.println(
        "[" + session.getCounter() + "] Trigger " + thisTrigger.getName() + " is " + action);
    session.incCounter();
  }

  private void setexecution(String[] args) throws Exception {
    ScriptExecutions scriptExecutions = session.getAcs().getScriptExecutions();
    Validation.numberOfArgs(args, 3);
    String args1Str = args[1].trim();
    int spacePos = args1Str.indexOf(' ');
    String scriptFileStr = null;
    String scriptArgs = null;
    if (spacePos == -1) {
      scriptFileStr = args1Str;
    } else {
      scriptFileStr = args1Str.substring(0, spacePos);
      scriptArgs = args1Str.substring(spacePos + 1);
    }
    com.github.freeacs.dbi.File scriptFile =
        context.getUnittype().getFiles().getByName(scriptFileStr);
    scriptExecutions.requestExecution(scriptFile, scriptArgs, autoboxString(args[2]));
  }

  private void setgroup(String[] args) throws Exception {
    Groups groups = context.getUnittype().getGroups();
    Validation.numberOfArgs(args, 5);
    Group thisGroup = groups.getByName(args[1]);
    String action = "changed";
    if (thisGroup == null) {
      action = "added";
      thisGroup = new Group(args[1], null, null, context.getUnittype(), null);
    }
    if ("NULL".equals(args[2])) {
      thisGroup.setParent(null);
    } else {
      Group parent = groups.getByName(args[2]);
      if (parent == null) {
        throw new IllegalArgumentException(
            "["
                + session.getCounter()
                + "] Group "
                + thisGroup.getName()
                + " has a parent which is not a group");
      }
      thisGroup.setParent(parent);
    }
    thisGroup.setDescription(args[3]);
    if ("NULL".equals(args[4])) {
      thisGroup.setProfile(null);
    } else {
      Profile profile = context.getUnittype().getProfiles().getByName(args[4]);
      if (profile == null) {
        throw new IllegalArgumentException(
            "["
                + session.getCounter()
                + "] Group "
                + thisGroup.getName()
                + " has a profile which is not a profile");
      }
      thisGroup.setProfile(profile);
    }

    groups.addOrChangeGroup(thisGroup, session.getAcs());
    context.println(
        "[" + session.getCounter() + "] Group " + thisGroup.getName() + " is " + action);
    session.incCounter();
  }

  private void delgroup(String[] args) throws Exception {
    Groups groups = context.getUnittype().getGroups();
    Validation.numberOfArgs(args, 2);
    Group group = groups.getByName(args[1]);
    if (group == null) {
      context.println("The group " + args[1] + " does not exist");
      return;
    }
    groups.deleteGroup(group, session.getAcs());
    context.println("[" + session.getCounter() + "] Group " + group.getName() + " is deleted");
    session.incCounter();
  }

  private void moveunit(String[] args, OutputHandler oh) throws Exception {
    Validation.numberOfArgs(args, 4);
    Unittype unittype = session.getContext().getUnittype();
    ACSUnit acsUnit = session.getAcsUnit();
    Unit u = acsUnit.getUnitById(args[1], unittype, null);
    if (u == null) {
      throw new IllegalArgumentException(
          "The unitid " + args[1] + " was not found in this unittype.");
    }
    ACS acs = session.getAcs();
    Unittype targetUnittype = acs.getUnittype(args[2]);
    if (targetUnittype == null) {
      throw new IllegalArgumentException("The target unittype " + args[2] + " was not found.");
    }
    if (!args[2].equals(targetUnittype.getName())) {
      throw new IllegalArgumentException("The target unittype is the same as this unittype.");
    }

    Profile targetProfile = targetUnittype.getProfiles().getByName(args[3]);
    if (targetProfile == null) {
      throw new IllegalArgumentException("The target profile " + args[3] + " was not found.");
    }
    Map<String, UnitParameter> unitParams = u.getUnitParameters();
    List<UnitParameter> targetUnitParams = new ArrayList<>();
    for (UnitParameter up : unitParams.values()) {
      String utpName = up.getParameter().getUnittypeParameter().getName();
      UnittypeParameter targetUtp = targetUnittype.getUnittypeParameters().getByName(utpName);
      if (targetUtp == null) {
        throw new IllegalArgumentException(
            "The unittype parameter " + utpName + " was not found in target unittype");
      }
      UnitParameter targetUnitParameter =
          new UnitParameter(targetUtp, u.getId(), up.getValue(), targetProfile);
      targetUnitParams.add(targetUnitParameter);
    }
    List<String> unitList = new ArrayList<>();
    unitList.add(u.getId());
    acsUnit.deleteUnit(u);
    acsUnit.addUnits(unitList, targetProfile);
    acsUnit.addOrChangeUnitParameters(targetUnitParams, targetProfile);
    context.println(
        "The unit " + args[1] + " is moved to unittype " + args[2] + ", profile " + args[3]);
  }

  private Map<String, Unit> getUnitMap(String[] args) throws Exception {
    Map<String, Unit> units = null;
    if (args.length == 1) {
      units = session.getAcsUnit().getUnits((String) null, context.getUnittype(), null, null);
    } else if (args.length == 2) {
      units = session.getAcsUnit().getUnits("%" + args[1] + "%", context.getUnittype(), null, null);
    } else if (args.length > 2) {
      List<Parameter> params = ParameterParser.parse(context, args);
      units = session.getAcsUnit().getUnits(context.getUnittype(), (Profile) null, params, null);
    }
    return units;
  }

  private void listunits(String[] args, OutputHandler oh) throws Exception {
    Listing listing = oh.getListing();
    Line headingLine = new Line("Unit Id");
    if (oh.getCommand().getOptions().containsKey(Option.OPTION_LIST_ALL_COLUMNS)) {
      Map<String, String> displayableMap =
          context.getUnittype().getUnittypeParameters().getDisplayableNameMap();
      for (String shortName : displayableMap.values()) {
        headingLine.addValue(shortName);
      }
    }
    listing.setHeading(new Heading(headingLine), true);
    Map<String, Unit> units = getUnitMap(args);
    for (Map.Entry<String, Unit> entry : units.entrySet()) {
      String unitId = entry.getKey();
      Line line = new Line(unitId);
      if (oh.getCommand().getOptions().containsKey(Option.OPTION_LIST_ALL_COLUMNS)) {
        Map<String, String> displayableMap =
            context.getUnittype().getUnittypeParameters().getDisplayableNameMap();
        Unit unit = session.getAcsUnit().getUnitById(unitId);
        for (String utpName : displayableMap.keySet()) {
          String value = unit.getParameters().get(utpName);
          if (value != null) {
            line.addValue(value);
          } else {
            line.addValue("NULL");
          }
        }
      }
      listing.addLine(line, entry.getValue());
    }
  }

  private void systemparameterscleanup(String[] args) throws Exception {
    UnittypeParameters utps = context.getUnittype().getUnittypeParameters();
    List<UnittypeParameter> deleteList = new ArrayList<>();
    for (UnittypeParameter utp : utps.getUnittypeParameters()) {
      if (utp.getName().startsWith("System.X_FREEACS-COM.")) {
        if (SystemParameters.commonParameters.get(utp.getName()) != null
            || SystemParameters.stagingParameters.get(utp.getName()) != null) {
          continue;
        }
        deleteList.add(utp);
      }
    }
    for (UnittypeParameter utp : deleteList) {
      try {
        utps.deleteUnittypeParameter(utp, session.getAcs());
        context.println(
            "The system parameter "
                + utp.getName()
                + " (unittype: "
                + context.getUnittype().getName()
                + ") is obsolete and has been deleted (it was not in use).");
      } catch (Throwable t) {
        Unittype unittype = session.getAcs().getUnittype(context.getUnittype().getId());
        context.setUnittype(unittype);
        utps = context.getUnittype().getUnittypeParameters();
        context.println(
            "The system parameter "
                + utp.getName()
                + " (unittype: "
                + context.getUnittype().getName()
                + ") is obsolete, but could not be deleted since it was in use.");
      }
    }
  }
}
