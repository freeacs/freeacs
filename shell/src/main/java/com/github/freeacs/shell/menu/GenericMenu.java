package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogFilter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.shell.ACSShell;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Script;
import com.github.freeacs.shell.ScriptMaker;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.Variable;
import com.github.freeacs.shell.command.ContextContainer;
import com.github.freeacs.shell.command.ContextElement;
import com.github.freeacs.shell.output.Heading;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.FileUtil;
import com.github.freeacs.shell.util.StringUtil;
import com.github.freeacs.shell.util.ValidateInteger;
import com.github.freeacs.shell.util.Validation;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sql.DataSource;

public class GenericMenu {
  private Session session;

  private static ScriptEngine scriptEngine =
      new ScriptEngineManager().getEngineByName("JavaScript");

  public GenericMenu(Session session) {
    this.session = session;
  }

  public boolean execute(String[] inputArr, OutputHandler oh) throws Exception {
    String input = inputArr[0];
    if ("cc".equals(input)) {
      cc(inputArr);
      return true;
    } else if ("pausescript".equals(input)) {
      session.print("Press RETURN to continue");
      InputStreamReader isr = new InputStreamReader(System.in);
      char[] cbuf = new char[2];
      isr.read(cbuf);
      String s = new String(cbuf);
      if (!s.contains("\n")) {
        session.println("");
      }
      return true;
    } else if ("syslog".equals(input)) {
      syslog(inputArr, oh);
      return true;
    } else if ("exit".equals(input)) {
      session.exitShell(0);
      return true;
    } else if (input.startsWith("echo")) {
      if (inputArr.length > 1) {
        if (inputArr[1].toLowerCase().matches("off") && inputArr.length == 2) {
          session.getProcessor().getEcho().setEcho(false);
        } else if (inputArr[1].toLowerCase().matches("on") && inputArr.length == 2) {
          session.getProcessor().getEcho().setEcho(true);
        } else {
          Listing listing = oh.getListing();
          Line echoLine = new Line();
          for (int i = 1; i < inputArr.length; i++) {
            echoLine.addValue(inputArr[i]);
          }
          listing.addLine(echoLine);
        }
      }
      return true;
    } else if (input.startsWith("sleep")) {
      sleep(inputArr);
      return true;
    } else if (input.startsWith("help")) {
      session.print(session.getContext().getHelp(inputArr));
      return true;
    } else if (input.startsWith("cat")) {
      cat(inputArr, oh);
      return true;
    } else if (input.startsWith("history")) {
      history(input, oh);
      return true;
    } else if (input.startsWith("ls")) {
      ls(inputArr, oh);
      return true;
    } else if (input.startsWith("logout")) {
      session.getDbi().setLifetimeSec(0); // kill the old DBI
      ACSShell acsShell = session.getACSShell();
      acsShell.setSession(new Session(session.getOriginalOptionArgs(), acsShell));
      DataSource ds = ACSShell.getHikariDataSource("main");
      acsShell.init(ds, ds);
      return true;
    } else if (input.startsWith("userin")) {
      session.println("Logged in as " + session.getVerifiedFusionUser().getUsername());
      return true;
    } else if (input.startsWith("setvar") || input.startsWith("var")) {
      setvar(inputArr, session.getScript());
      return true;
    } else if (input.startsWith("listvar")) {
      listvars(session.getScript(), oh);
      return true;
    } else if (input.startsWith("delvar")) {
      delvar(inputArr, session.getScript());
      return true;
    } else if (input.startsWith("delosf")) {
      delfile(inputArr);
      return true;
    } else if ("unit".equals(input)) {
      unit(inputArr, oh);
      return true;
    } else if ("if".equals(input)
        || "elseif".equals(input)
        || "else".equals(input)
        || "fi".equals(input)) {
      ifelse(inputArr, session);
      return true;
    } else if ("while".equals(input) || "done".equals(input)) {
      whiledone(inputArr, session);
      return true;
    } else if ("break".equals(input) || "continue".equals(input)) {
      breakcontinue(inputArr, session);
      return true;
    } else if ("scriptstatus".equals(input)) {
      scriptstatus();
      return true;
    } else if ("unittypeexport".equals(input) || "unittypeimport".equals(input)) {
      session
          .getScriptStack()
          .push(
              new Script(
                  ScriptMaker.getMigrateScript(0, inputArr), new Context(session), Script.SCRIPT));
      return true;
    } else if ("unittypecompletedelete".equals(input)) {
      session
          .getScriptStack()
          .push(
              new Script(
                  ScriptMaker.getDeleteScript(0, inputArr), new Context(session), Script.SCRIPT));
      return true;
    } else {
      return false;
    }
  }

  private void scriptstatus() {
    for (int i = 0; i < session.getScriptStack().size(); i++) {
      Script s = session.getScriptStack().get(i);
      session.println(
          "Script ("
              + (i + 1)
              + ") of type "
              + s.getType()
              + ", linepointer: "
              + s.getLinePointer());
    }
  }

  private void history(String input, OutputHandler oh) {
    List<String> history = session.getCommandHistory();
    Listing listing = oh.getListing();
    int numberOfEntriesToList = 100;
    if (history.size() < numberOfEntriesToList) {
      numberOfEntriesToList = history.size();
    }
    for (int i = numberOfEntriesToList - 1; i > -1; i--) {
      listing.addLine(String.valueOf(i + 1), history.get(i));
    }
  }

  private void breakcontinue(String[] args, Session session) {
    while (session.getScript().getType() != Script.WHILE) {
      if (session.getScriptStack().size() > 1) {
        session.getScriptStack().pop();
      } else {
        throw new IllegalArgumentException(
            "Script terminated because of '" + args[0] + "' not within a while-done loop");
      }
    }
    if ("break".equals(args[0])) {
      session.getScriptStack().pop();
    }
    if ("continue".equals(args[0])) {
      session.getScript().reset();
    }
  }

  private static final String IF = "if";
  private static final String ELSEIF = "elseif";
  private static final String ELSE = "else";
  private static final String FI = "fi";
  private static final String WHILE = "while";
  private static final String DONE = "done";

  private String getCommand(String s) {
    s = s.trim().toLowerCase();
    int spacePos = s.indexOf(' ');
    if (spacePos > -1) {
      s = s.substring(0, spacePos);
    }
    return s;
  }

  private String getWhilePath(Session session) {
    StringBuilder path = new StringBuilder();
    for (Script script : session.getScriptStack()) {
      path.append(script.getLinePointer()).append("-");
    }
    return path.substring(0, path.length() - 1);
  }

  private void whiledone(String[] args, Session session) throws IOException, SQLException {
    String command = getCommand(args[0]);
    if (WHILE.equals(command)) {
      String whilePath = getWhilePath(session);
      if (session.getScript().getWhilePath() == null
          || !session.getScript().getWhilePath().equals(whilePath)) {
        Script parentScript = session.getScript();
        List<String> scriptLines = new ArrayList<>();
        scriptLines.add(parentScript.getPreviousScriptLine()); // add the While-statement
        int whileCounter = 0;
        do {
          if (parentScript.endOfScript()) {
            throw new IllegalArgumentException(
                "Cannot find matching 'done' for the 'while' statement");
          }
          String scriptLine =
              parentScript.getNextScriptLine(); // moves the linepointer of the parent-script
          scriptLines.add(scriptLine);
          String c = getCommand(scriptLine);
          if (WHILE.equals(c)) {
            whileCounter++;
          } else if (DONE.equals(c)) {
            if (whileCounter == 0) {
              Script whileScript =
                  new Script(
                      scriptLines,
                      parentScript.getContext(),
                      Script.WHILE,
                      parentScript.getVariables());
              whileScript.incLinePointer(); // skip the first while-statement, since that is being
              // processed in now
              session.getScriptStack().push(whileScript);
              whileScript.setWhilePath(getWhilePath(session));
              break;
            } else {
              whileCounter--;
            }
          }
        } while (true);
      }
    }

    Script whileScript = session.getScript(); // Will now retrieve the IF-script
    if (whileScript.getType() != Script.WHILE) {
      throw new IllegalArgumentException(
          "Not possible to put done here, since a preceding while was not found");
    }

    if (WHILE.equals(command)) {
      String whileArg = "";
      for (int i = 1; i < args.length; i++) {
        whileArg += args[i] + " ";
      }
      whileArg = whileArg.trim();

      boolean eval = true;
      if (session.getContext().getUnittype() != null) {
        com.github.freeacs.dbi.File fusionFile =
            session.getContext().getUnittype().getFiles().getByName(whileArg);
        if (fusionFile != null && whileScript.getWhileInput() == null) {
          whileScript.setWhileInput(
              new BufferedReader(
                  new InputStreamReader(new ByteArrayInputStream(fusionFile.getContent()))));
        }
      }
      if (whileScript.getWhileInput() == null
          && FileUtil.exists(whileArg)
          && FileUtil.allowed("while", new File(whileArg))) {
        whileScript.setWhileInput(new BufferedReader(new FileReader(whileArg)));
      }
      if (whileScript.getWhileInput() != null) { // did find a Fusion-file or an OS-file
        // remove file-variables from last iteration
        int varCounter = 1;
        while (whileScript.getVariable(String.valueOf(varCounter)) != null) {
          whileScript.getVariables().remove(String.valueOf(varCounter));
          varCounter++;
        }
        String line = whileScript.getWhileInput().readLine();
        if (line == null) {
          eval = false;
        } else {
          // add file-variables from this iteration
          String fileArgs[] = StringUtil.split(line);
          varCounter = 1;
          for (String fileArg : fileArgs) {
            whileScript
                .getVariables()
                .put(String.valueOf(varCounter), new Variable(String.valueOf(varCounter), fileArg));
            varCounter++;
          }
        }
      } else {
        eval = evalBoolean(whileArg);
      }
      if (!eval) {
        BufferedReader br = session.getScript().getWhileInput();
        if (br != null) {
          br.close();
        }
        session.getScriptStack().pop();
      }
    } else if (DONE.equals(command)) {
      whileScript.reset();
    }
  }

  private void ifelse(String[] args, Session session) {
    String command = getCommand(args[0]);

    /* 1. Make a new IF-script, containing everything from IF to (matching) FI.
     * 2. Move the line pointer in the parent script till the line after FI.
     * 3. Validate the if-elseif-else-fi structure. */
    if (IF.equals(command)) {
      Script parentScript = session.getScript();
      List<String> scriptLines = new ArrayList<>();
      scriptLines.add(parentScript.getPreviousScriptLine()); // add the if-statement
      int ifCounter = 0;
      String ifLevel = IF;
      do {
        if (parentScript.endOfScript()) {
          throw new IllegalArgumentException("Cannot find matching 'fi' for the 'if' statement");
        }
        String scriptLine = parentScript.getNextScriptLine();
        scriptLines.add(scriptLine);
        String c = getCommand(scriptLine);
        if (IF.equals(c)) {
          ifCounter++;
        } else if (ELSEIF.equals(c) && ifCounter == 0) {
          if (IF.equals(ifLevel)) {
            ifLevel = ELSEIF;
          } else {
            throw new IllegalArgumentException(
                "Not possible to put elseif here, since previous if/else word was " + ifLevel);
          }
        } else if (ELSE.equals(c) && ifCounter == 0) {
          if (IF.equals(ifLevel) || ELSEIF.equals(ifLevel)) {
            ifLevel = ELSE;
          } else {
            throw new IllegalArgumentException(
                "Not possible to put else here, since previous if/else word was " + ifLevel);
          }
        } else if (FI.equals(c)) {
          if (ifCounter == 0) {
            Script ifScript =
                new Script(
                    scriptLines, parentScript.getContext(), Script.IF, parentScript.getVariables());
            ifScript
                .incLinePointer(); // skip the first if-statement, since that is being processed in
            // now
            session.getScriptStack().push(ifScript);
            break;
          } else {
            ifCounter--;
          }
        }
      } while (true);
    }
    Script ifScript = session.getScript(); // Will now retrieve the IF-script
    if (ifScript.getType() != Script.IF) {
      throw new IllegalArgumentException(
          "Not possible to put " + command + " here, since a preceding 'if' was not found");
    }

    /* Evaluate IF/ELSEIF if necessary - move linepointer accordingly */
    if (IF.equals(command) || ELSEIF.equals(command)) {
      if (ifScript.isSkipOnNextIfElseWord()) {
        ifScript.moveUpUntilCommand(ELSEIF, ELSE, FI);
      } else {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
          sb.append(args[i]).append(" ");
        }
        boolean eval = evalBoolean(sb.toString());
        if (!eval) {
          ifScript.moveUpUntilCommand(ELSEIF, ELSE, FI);
        } else {
          ifScript.setSkipOnNextIfElseWord(true);
        }
      }
    } else if (ELSE.equals(command)) {
      if (ifScript.isSkipOnNextIfElseWord()) {
        ifScript.moveUpUntilCommand(FI);
      } else {
        ifScript.setSkipOnNextIfElseWord(true);
      }
    } else if (FI.equals(command)) {
      session.getScriptStack().pop(); // pop of ifScript
    }
  }

  private boolean evalBoolean(String evalStr) {
    return "true".equals(eval(evalStr));
  }

  public static String eval(String evalStr) {
    evalStr = evalStr.replaceAll(" LT ", " < ");
    evalStr = evalStr.replaceAll(" lt ", " < ");
    evalStr = evalStr.replaceAll(" LE ", " <= ");
    evalStr = evalStr.replaceAll(" le ", " <= ");
    evalStr = evalStr.replaceAll(" EQ ", " == ");
    evalStr = evalStr.replaceAll(" eq ", " == ");
    evalStr = evalStr.replaceAll(" NE ", " != ");
    evalStr = evalStr.replaceAll(" ne ", " != ");
    evalStr = evalStr.replaceAll(" GE ", " >= ");
    evalStr = evalStr.replaceAll(" ge ", " >= ");
    evalStr = evalStr.replaceAll(" GT ", " > ");
    evalStr = evalStr.replaceAll(" gt ", " > ");
    evalStr = evalStr.replaceAll("NULL", "null");
    evalStr = evalStr.replaceAll(" or ", " || ");
    evalStr = evalStr.replaceAll(" OR ", " || ");
    try {
      return scriptEngine.eval(evalStr).toString();
    } catch (ScriptException se) {
      throw new IllegalArgumentException(
          "The evaluation of " + evalStr + " failed: " + se.getMessage());
    }
  }

  private void cat(String[] args, OutputHandler oh) throws IOException, SQLException {
    Validation.numberOfArgs(args, 2);

    String filename = args[1];
    if (!FileUtil.allowed("cat " + filename, new File(filename))) {
      return;
    }
    if (session.getContext().getUnittype() != null) {
      com.github.freeacs.dbi.File f =
          session.getContext().getUnittype().getFiles().getByName(filename);
      if (f != null) {
        Listing listing = oh.getListing();
        for (String line : new String(f.getContent()).split("\n")) {
          listing.addLineRaw(line);
        }
        return;
      }
    }
    if (!FileUtil.exists(filename)) {
      throw new IllegalArgumentException("The file " + filename + " does not exist");
    }
    BufferedReader br = new BufferedReader(new FileReader(filename));
    Listing listing = oh.getListing();
    while (br.ready()) {
      listing.addLineRaw(br.readLine());
    }
    br.close();
  }

  private void delvar(String[] args, Script script) {
    Validation.numberOfArgs(args, 2);
    script.removeVariable(args[1]);
  }

  private void listvars(Script script, OutputHandler oh) {
    Listing listing = oh.getListing();
    listing.setHeading("Name", "Value");
    for (Variable var : script.getVariables().values()) {
      listing.addLine(var.getName(), var.getValue());
    }
    //			session.println("${" + var.getName() + "} : " + var.getValue());
  }

  private void ls(String[] args, OutputHandler oh) {
    String filename = ".";
    if (args.length > 1) {
      filename = args[1];
    }
    File f = new File(filename);
    if (!FileUtil.allowed("ls " + filename, f)) {
      return;
    }
    if (f.exists()) {
      if (f.isDirectory()) {
        File[] files = f.listFiles();
        if (files != null) {
          for (File f1 : files) {
            printFileInfo(f1, oh);
          }
        }
      } else {
        printFileInfo(f, oh);
      }
    } else {
      Listing listing = oh.getListing();
      listing.addLineRaw("Error: The file/directory " + filename + " does not exist");
    }
  }

  private void setvar(String[] args, Script script) {
    Validation.numberOfArgs(args, 3);
    if (Character.isDigit(args[1].charAt(0))) {
      throw new IllegalArgumentException("The variable name cannot start with a digit.");
    }
    if (args[1].indexOf(' ') > 0 || args[1].indexOf(',') > 0 || args[1].indexOf('\t') > 0) {
      throw new IllegalArgumentException("The variable name cannot include space, tabs or commas");
    }
    if (args[1].startsWith("_")) {
      throw new IllegalArgumentException("The variable name cannot start with an underscore.");
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 2; i < args.length; i++) {
      sb.append(args[i]).append(" ");
    }
    script.addVariable(args[1], eval(sb.toString()));
  }

  private void sleep(String[] sleepTime) {
    try {
      long sleepTimeLong = 60000;
      if (sleepTime.length >= 2) {
        ValidateInteger valInteger = new ValidateInteger(1, null);
        if (valInteger.validate(sleepTime[1])) {
          sleepTimeLong = Long.parseLong(sleepTime[1]) * 1000;
        }
      }
      RootMenu rm = new RootMenu(session);
      try {
        rm.executescript();
        Thread.sleep(sleepTimeLong);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      // cannot happen
    }
  }

  private void printFileInfo(File f, OutputHandler oh) {
    Listing listing = oh.getListing();
    String dirStr = "";
    if (f.isDirectory()) {
      dirStr = "<DIR>";
    }
    listing.addLine(dirStr, String.valueOf(f.length()), f.getName());
  }

  private Map<String, Unit> getUnits(String[] args, Context context) throws Exception {
    Map<String, Unit> units = null;
    if (args.length > 1) {
      units =
          session
              .getAcsUnit()
              .getUnits("%" + args[1] + "%", context.getUnittype(), context.getProfile(), null);
    } else {
      units =
          session.getAcsUnit().getUnits(null, context.getUnittype(), context.getProfile(), null);
    }
    return units;
  }

  private void cc(String[] args) throws Exception {
    Context context = session.getContext();
    Validation.numberOfArgs(args, 2);
    String ccArg = args[1].trim();
    ContextContainer cc = ContextElement.parseContextElements(ccArg);
    if (cc.size() == 0) {
      throw new IllegalArgumentException(
          "The argument " + cc + " could not be parsed into a context switch");
    }
    session.getProcessor().changeContext(cc, context, session);
  }

  private void syslog(String[] args, OutputHandler oh) throws Exception {
    SyslogFilter sf = new SyslogFilter();
    sf.setMaxRows(100);
    sf.setCollectorTmsStart(new Date(System.currentTimeMillis() - 86400 * 1000));
    Context context = session.getContext();
    if (context.getUnit() != null) {
      sf.setUnitId(context.getUnit().getId());
    }
    if (context.getProfile() != null) {
      sf.setProfiles(Collections.singletonList(context.getProfile()));
    }
    if (context.getUnittype() != null) {
      sf.setUnittypes(Collections.singletonList(context.getUnittype()));
    }
    String listOptionStr = "t,s,fn,ev,m,ip,un,pr,ut";
    for (int i = 1; i < 3; i++) {
      if (args.length > i && args[i].startsWith("s") && args[i].length() > 1) {
        sf = alterSyslogFilter(sf, args[i].substring(1));
      }
      if (args.length > i && args[i].startsWith("l") && args[i].length() > 1) {
        listOptionStr = args[i].substring(1);
        int rowOptStart = listOptionStr.indexOf("r=");
        if (rowOptStart > -1) {
          int rowOptEnd = listOptionStr.length();
          int nextCommaPos = listOptionStr.indexOf(',', rowOptStart);
          if (nextCommaPos > -1 && nextCommaPos < rowOptEnd) {
            rowOptEnd = listOptionStr.indexOf(',', rowOptStart);
          }
          String rowOption = listOptionStr.substring(rowOptStart, rowOptEnd);
          try {
            sf.setMaxRows(Integer.parseInt(rowOption.substring(2)));
          } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                "List option " + rowOption + " was not specified correctly");
          }
        }
      }
    }
    Syslog syslog = session.getDbi().getSyslog();
    String[] listOptions = listOptionStr.split(",");
    List<SyslogEntry> entries = syslog.read(sf, session.getAcs());
    Listing listing = oh.getListing();
    Line headingLine = new Line();
    for (String listOption : listOptions) {
      if (listOption.trim().isEmpty()) {
        continue;
      }
      if ("t".equals(listOption)) {
        headingLine.addValue("Timestamp");
      }
      if ("ev".equals(listOption)) {
        headingLine.addValue("Event Id");
      }
      if ("fn".equals(listOption)) {
        headingLine.addValue("Facility");
      }
      if ("fv".equals(listOption)) {
        headingLine.addValue("Facility-ver.");
      }
      if ("ip".equals(listOption)) {
        headingLine.addValue("Ip address");
      }
      if ("m".equals(listOption)) {
        headingLine.addValue("Message");
      }
      if ("s".equals(listOption)) {
        headingLine.addValue("Severity");
      }
      if ("us".equals(listOption)) {
        headingLine.addValue("User");
      }
      if ("un".equals(listOption)) {
        headingLine.addValue("Unit Id");
      }
      if ("pr".equals(listOption)) {
        headingLine.addValue("Profile");
      }
      if ("ut".equals(listOption)) {
        headingLine.addValue("Unit Type");
      }
      if (headingLine.getValues().isEmpty()) {
        throw new IllegalArgumentException(
            "List option " + listOption + " was not specified correctly");
      }
    }
    listing.setHeading(new Heading(headingLine));
    for (SyslogEntry entry : entries) {
      Line line = new Line();
      for (String listOption : listOptions) {
        if (listOption.trim().isEmpty()) {
          continue;
        }
        if ("t".equals(listOption)) {
          line.addValue(Util.outputFormatExtended.format(entry.getCollectorTimestamp()));
        }
        if ("ev".equals(listOption)) {
          line.addValue(String.valueOf(entry.getEventId()));
        }
        if ("fn".equals(listOption)) {
          line.addValue(SyslogConstants.getFacilityName(entry.getFacility()));
        }
        if ("fv".equals(listOption)) {
          line.addValue(entry.getFacilityVersion());
        }
        if ("ip".equals(listOption)) {
          line.addValue(entry.getIpAddress());
        }
        if ("m".equals(listOption)) {
          line.addValue(entry.getContent());
        }
        if ("s".equals(listOption)) {
          line.addValue(SyslogConstants.getSeverityName(entry.getSeverity()));
        }
        if ("us".equals(listOption)) {
          line.addValue(entry.getUserId());
        }
        if ("un".equals(listOption)) {
          line.addValue(entry.getUnitId());
        }
        if ("pr".equals(listOption)) {
          line.addValue(entry.getProfileName());
        }
        if ("ut".equals(listOption)) {
          line.addValue(entry.getUnittypeName());
        }
      }
      listing.addLine(line);
    }
  }

  private SyslogFilter alterSyslogFilter(SyslogFilter sf, String searchOptionStr) {
    String[] searchOptions = searchOptionStr.split(",");
    for (String searchOption : searchOptions) {
      if (searchOption.trim().isEmpty()) {
        continue;
      } else if (searchOption.startsWith("ts=") && searchOption.length() > 3) {
        String optionValue = searchOption.substring(3);
        Date d = Util.getDateFromOption(optionValue);
        if (d == null) {
          throw new IllegalArgumentException(
              "Search option " + searchOption + " was not specified correctly");
        }
        sf.setCollectorTmsStart(d);
      } else if (searchOption.startsWith("te=") && searchOption.length() > 3) {
        String optionValue = searchOption.substring(3);
        Date d = Util.getDateFromOption(optionValue);
        if (d == null) {
          throw new IllegalArgumentException(
              "Search option " + searchOption + " was not specified correctly");
        }
        sf.setCollectorTmsEnd(d);
      } else if (searchOption.startsWith("ev=") && searchOption.length() > 3) {
        String optionValue = searchOption.substring(3);
        try {
          sf.setEventId(Integer.parseInt(optionValue));
        } catch (NumberFormatException nfe) {
          throw new IllegalArgumentException(
              "Search option " + searchOption + " was not specified correctly");
        }
      } else if (searchOption.startsWith("fn=") && searchOption.length() > 3) {
        String optionValue = searchOption.substring(3);
        for (Entry<Integer, String> entry : SyslogConstants.facilityMap.entrySet()) {
          if (entry.getValue().equalsIgnoreCase(optionValue)) {
            sf.setFacility(entry.getKey());
          }
        }
        if (sf.getFacility() == null) {
          throw new IllegalArgumentException(
              "Search option " + searchOption + " was not specified correctly");
        }
      } else if (searchOption.startsWith("fv=") && searchOption.length() > 3) {
        sf.setFacilityVersion(searchOption.substring(3));
      } else if (searchOption.startsWith("ip=") && searchOption.length() > 3) {
        sf.setIpAddress(searchOption.substring(3));
      } else if (searchOption.startsWith("m=") && searchOption.length() > 2) {
        sf.setMessage(searchOption.substring(2));
      } else if (searchOption.startsWith("s=") && searchOption.length() > 2) {
        String optionValue = searchOption.substring(2);
        try {
          List<Integer> severityList = new ArrayList<>();
          for (int i = 0; i < optionValue.length(); i++) {
            severityList.add(Integer.parseInt(String.valueOf(optionValue.charAt(i))));
          }
          sf.setSeverity(severityList.toArray(new Integer[] {}));
        } catch (NumberFormatException nfe) {
          throw new IllegalArgumentException(
              "Search option " + searchOption + " was not specified correctly");
        }
        if (sf.getSeverity() == null || sf.getSeverity().length == 0) {
          throw new IllegalArgumentException(
              "Search option " + searchOption + " was not specified correctly");
        }
      } else if (searchOption.startsWith("us=") && searchOption.length() > 3) {
        sf.setUserId(searchOption.substring(3));
      } else if (searchOption.startsWith("r=") && searchOption.length() > 2) {
        try {
          sf.setMaxRows(Integer.parseInt(searchOption.substring(2)));
        } catch (NumberFormatException nfe) {
          throw new IllegalArgumentException(
              "List option " + searchOption + " was not specified correctly");
        }
      } else {
        throw new IllegalArgumentException(
            "Search option " + searchOption + " was not specified correctly");
      }
    }
    return sf;
  }

  private void unit(String[] args, OutputHandler oh) throws Exception {
    Context context = session.getContext();
    Map<String, Unit> units = getUnits(args, context);
    if (units.isEmpty()) {
      context.println("No units found - context change not possible");
    } else if (units.size() > 1) {
      if (context.getUnittype() != null) {
        context.println(units.size() + " units found - context change not possible");
        Listing listing = oh.getListing();
        Line headingLine = new Line("Unit Type", "Profile", "Unit Id");
        Map<String, String> displayableMap =
            context.getUnittype().getUnittypeParameters().getDisplayableNameMap();
        for (String heading : displayableMap.values()) {
          headingLine.addValue(heading);
        }
        listing.setHeading(new Heading(headingLine));
        for (Unit u : units.values()) {
          Unit unit = session.getAcsUnit().getUnitById(u.getId());
          Line line = new Line();
          line.addValue(u.getUnittype().getName());
          line.addValue(u.getProfile().getName());
          line.addValue(u.getId());
          for (String utpName : displayableMap.keySet()) {
            String value = unit.getParameters().get(utpName);
            if (value != null) {
              line.addValue(value);
            } else {
              line.addValue(" ");
            }
          }
          listing.addLine(line);
        }
      } else {
        session.getContext().println(units.size() + " units found - context change not possible");
        Listing listing = oh.getListing();
        Line headingLine = new Line("Unit Type");
        headingLine.addValue("Profile");
        headingLine.addValue("Unit Id");
        listing.setHeading(new Heading(headingLine));
        for (Unit unit : units.values()) {
          listing.addLine(unit.getUnittype().getName(), unit.getProfile().getName(), unit.getId());
        }
      }
    } else { // units.size() == 1
      Unit unit = session.getAcsUnit().getUnitById((String) units.keySet().toArray()[0]);
      //			Unit unit = (Unit) units.values().toArray()[0];
      context.resetToNull();
      context.setUnittype(unit.getUnittype());
      context.setProfile(unit.getProfile());
      context.setUnit(unit);
    }
  }

  private void delfile(String[] args) throws IOException {
    Validation.numberOfArgs(args, 2);
    String filename = args[1];
    File f = new File(filename);
    if (!FileUtil.allowed("delosfile " + filename, f)) {
      return;
    }
    if (f.exists()) {
      boolean success = f.delete();
      if (success) {
        session.println("The file " + filename + " was deleted");
      } else {
        throw new IOException("The file " + filename + " could not be deleted");
      }
    }
  }
}
