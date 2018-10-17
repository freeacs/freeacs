package com.github.freeacs.shell;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.shell.command.Command;
import com.github.freeacs.shell.command.CommandAndArgument;
import com.github.freeacs.shell.command.ContextContainer;
import com.github.freeacs.shell.command.ContextElement;
import com.github.freeacs.shell.command.Option;
import com.github.freeacs.shell.menu.GenericMenu;
import com.github.freeacs.shell.menu.GroupMenu;
import com.github.freeacs.shell.menu.JobMenu;
import com.github.freeacs.shell.menu.ProfileMenu;
import com.github.freeacs.shell.menu.RootMenu;
import com.github.freeacs.shell.menu.UnitMenu;
import com.github.freeacs.shell.menu.UnittypeMenu;
import com.github.freeacs.shell.menu.UnittypeParameterMenu;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.StringUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Processor {
  private Logger logger = LoggerFactory.getLogger(Processor.class);

  private String logPrefix = "";

  private Session session;

  private Echo echo;

  private List<String> daemonCommands = new LinkedList<>();
  private List<String> processedCommands = new ArrayList<>();

  /**
   * Used to send notify between Shell-thread and Core script execution thread when 1.
   * Daemon-command is added to the shell-thread 2. Daemon-command is processed in the shell-thread
   * Used to grant exclusive right to add/remove from daemon-commands
   */
  private final Object monitor = new Object();

  public Processor(Session session) {
    this.session = session;
    this.echo = new Echo(session);
  }

  /** THE high level process function. */
  public void promptProcessing() throws Exception {
    String input = retrieveInput();
    processInput(input);
    if (processedCommands.size() > 1000) {
      processedCommands.remove(0);
    }
    processedCommands.add(input);
  }

  private IllegalArgumentException makeContextSwitchException(ContextElement ce) {
    return new IllegalArgumentException("The context switch to " + ce + " was not possible");
  }

  public void changeContext(ContextContainer cc, Context context, Session session)
      throws SQLException {
    for (ContextElement ce : cc.getContextList()) {
      if (ce.getType().equals(ContextElement.TYPE_ROOT)) {
        context.resetToNull();
      } else if (ce.getType().equals(ContextElement.TYPE_UNITTYPE)) {
        String ceName = ce.getStringToSubstitute();
        Unittype unittype = session.getAcs().getUnittype(ceName);
        if (unittype == null) {
          throw makeContextSwitchException(ce);
        }
        context.resetToNull();
        context.setUnittype(unittype);
      } else if (ce.getType().equals(ContextElement.TYPE_PROFILE)) {
        String ceName = ce.getStringToSubstitute();
        Unittype unittype = context.getUnittype();
        if (unittype == null) {
          throw makeContextSwitchException(ce);
        }
        Profile profile = unittype.getProfiles().getByName(ceName);
        if (profile == null) {
          throw makeContextSwitchException(ce);
        }
        if (context.getUnit() != null) {
          context.setUnit(null);
        }
        if (context.getGroup() != null) {
          context.setGroup(null);
        }
        if (context.getJob() != null) {
          context.setJob(null);
        }
        context.setProfile(profile);
      } else if (ce.getType().equals(ContextElement.TYPE_UNIT)) {
        String ceName = ce.getStringToSubstitute();
        Unittype unittype = context.getUnittype();
        if (unittype == null) {
          throw makeContextSwitchException(ce);
        }
        Profile profile = context.getProfile();
        if (profile == null) {
          throw makeContextSwitchException(ce);
        }
        Unit unit = session.getAcsUnit().getUnitById(ceName, unittype, profile);
        if (unit == null) {
          throw new IllegalArgumentException("The context switch to " + ce + " was not possible");
        }
        if (context.getGroup() != null) {
          context.setGroup(null);
        }
        if (context.getJob() != null) {
          context.setJob(null);
        }
        context.setUnit(unit);
      } else if (ce.getType().equals(ContextElement.TYPE_GROUP)) {
        String ceName = ce.getStringToSubstitute();
        Unittype unittype = context.getUnittype();
        if (unittype == null) {
          throw makeContextSwitchException(ce);
        }
        Group group = unittype.getGroups().getByName(ceName);
        if (group == null) {
          throw makeContextSwitchException(ce);
        }
        if (context.getUnit() != null) {
          context.setUnit(null);
        }
        if (context.getProfile() != null) {
          context.setProfile(null);
        }
        if (context.getJob() != null) {
          context.setJob(null);
        }
        context.setGroup(group);
      } else if (ce.getType().equals(ContextElement.TYPE_JOB)) {
        String ceName = ce.getStringToSubstitute();
        Unittype unittype = context.getUnittype();
        if (unittype == null) {
          throw makeContextSwitchException(ce);
        }
        Job job = unittype.getJobs().getByName(ceName);
        if (job == null) {
          throw makeContextSwitchException(ce);
        }
        if (context.getUnit() != null) {
          context.setUnit(null);
        }
        if (context.getProfile() != null) {
          context.setProfile(null);
        }
        context.setJob(job);
      } else if (ce.getType().equals(ContextElement.TYPE_UNITTYPE_PARAMS)) {
        String ceName = ce.getStringToSubstitute();
        Unittype unittype = context.getUnittype();
        if (unittype == null) {
          throw makeContextSwitchException(ce);
        }
        UnittypeParameter unittypeParameter = unittype.getUnittypeParameters().getByName(ceName);
        if (unittypeParameter == null) {
          throw makeContextSwitchException(ce);
        }
        context.setUnittypeParameter(unittypeParameter);
      } else if (ce.getType().equals(ContextElement.TYPE_BACK)) {
        if (context.getUnittypeParameter() != null) {
          context.setUnittypeParameter(null);
        } else if (context.getJob() != null) {
          context.setJob(null);
        } else if (context.getGroup() != null) {
          context.setGroup(null);
        } else if (context.getUnit() != null) {
          context.setUnit(null);
        } else if (context.getProfile() != null) {
          context.setProfile(null);
        } else if (context.getUnittype() != null) {
          context.resetToNull();
        }
      }
    }
  }

  private void debugException(Exception e) {
    logger.error(logPrefix + "Output: " + session.getContext() + e.getMessage());
  }

  private void debugCommand(Command command) {
    if (logger.isDebugEnabled()) {
      logger.debug(logPrefix + "Command: " + session.getContext() + command);
    }
  }

  private void returnCommand(Command command) {
    String retVal = "";
    for (CommandAndArgument caa : command.getCommandAndArguments()) {
      if ("return".equals(caa.getStringToSubstitute())) {
        continue;
      }
      retVal += caa.getStringToSubstitute() + " ";
    }
    retVal = retVal.trim();
    if (!"".equals(retVal)) {
      CommandAndArgument caa = new CommandAndArgument(retVal);
      Command.varArgSubst(caa, session);
      retVal = caa.getStringToSubstitute();
      retVal = GenericMenu.eval(retVal);
    }
    while (!session.getScriptStack().isEmpty()) {
      Script s = session.getScriptStack().pop();
      // pop off inner IF/WHILE-scripts
      if (s.getType() == Script.SCRIPT) {
        break;
      }
    }
    session.getScript().addVariable("_return", retVal);
  }

  private void errorCommand(Command command) {
    String retVal = "";
    for (CommandAndArgument caa : command.getCommandAndArguments()) {
      if ("error".equals(caa.getStringToSubstitute())) {
        continue;
      }
      retVal += caa.getStringToSubstitute() + " ";
    }
    retVal = retVal.trim();
    if (!"".equals(retVal)) {
      CommandAndArgument caa = new CommandAndArgument(retVal);
      Command.varArgSubst(caa, session);
      retVal = caa.getStringToSubstitute();
    }
    while (!session.getScriptStack().isEmpty()) {
      Script s = session.getScriptStack().pop();
      // pop off inner IF/WHILE-scripts
      if (s.getType() == Script.SCRIPT) {
        break;
      }
    }
    throw new IllegalArgumentException(retVal);
  }

  /**
   * Depending upon the context, retrieve the appropriate Menu-object and run the command in that
   * context.
   */
  private void processCommand(Context context, Command command, OutputHandler oh) throws Exception {
    String input = command.getCommandAndArguments().get(0).toString();
    if (input.startsWith("return")) {
      returnCommand(command);
      return;
    } else if (input.startsWith("call")) {
      call(command);
      return;
    } else if (input.startsWith("error")) {
      errorCommand(command);
    }
    String[] commandAndArgumentsArr = new String[command.getCommandAndArguments().size()];
    for (int i = 0; i < command.getCommandAndArguments().size(); i++) {
      CommandAndArgument caa = command.getCommandAndArguments().get(i);
      commandAndArgumentsArr[i] = caa.getStringToSubstitute();
    }
    GenericMenu hlc = new GenericMenu(context.getSession());
    if (hlc.execute(commandAndArgumentsArr, oh)) {
      return;
    }
    if (context.getUnit() != null) {
      UnitMenu unitMenu = new UnitMenu(context.getSession());
      unitMenu.execute(commandAndArgumentsArr, oh);
    } else if (context.getProfile() != null) {
      ProfileMenu profileMenu = new ProfileMenu(context.getSession());
      profileMenu.execute(commandAndArgumentsArr, oh);
    } else if (context.getGroup() != null) {
      GroupMenu groupMenu = new GroupMenu(context.getSession());
      groupMenu.execute(commandAndArgumentsArr, oh);
    } else if (context.getJob() != null) {
      JobMenu jobMenu = new JobMenu(context.getSession());
      jobMenu.execute(commandAndArgumentsArr, oh);
    } else if (context.getUnittypeParameter() != null) {
      UnittypeParameterMenu unittypeParameterMenu = new UnittypeParameterMenu(context.getSession());
      unittypeParameterMenu.execute(commandAndArgumentsArr, oh);
    } else if (context.getUnittype() != null) {
      UnittypeMenu unittypeMenu = new UnittypeMenu(context.getSession());
      unittypeMenu.execute(commandAndArgumentsArr, oh);
    } else {
      RootMenu rootMenu = new RootMenu(context.getSession());
      rootMenu.execute(commandAndArgumentsArr, oh);
    }
  }

  private void singleCommandExecution(Context context, Command command, OutputHandler oh)
      throws Exception {
    // expand command with variable arguments
    command.processVarArgs(session);
    // print the command to log before any context-change or command action
    debugCommand(command);
    // The context is updated after this context, the correct menu might be invoked
    changeContext(command.getContextContainer(), context, session);
    // The command is executed in this step
    processCommand(context, command, oh);
  }

  private void multipleCommandExecution(
      Context context, Command command, OutputHandler oh, InputHandler ih) throws Exception {
    String[] fileArgs = null;
    Context initialContext = context.clone();
    while ((fileArgs = ih.read()) != null) {
      // Set context to initalContext before every run in this loop
      context.copyFrom(initialContext);
      // expand command with fileArgs
      command.processFileArgs(fileArgs);
      // expand command with variable arguments
      command.processVarArgs(session);
      if (!command.getCommandAndArguments().isEmpty()
          && "call".equals(command.getCommandAndArguments().get(0).getCommandAndArgument())) {
        // expand call commands into a single-execution command line and put it back into the script
        session
            .getScript()
            .insertScriptLine(session.getScript().getLinePointer(), command.toString());
      } else {
        // print the command to log before any context-change or command action
        debugCommand(command);
        // The context is updated after this context, the correct menu might be invoked
        changeContext(command.getContextContainer(), context, session);
        // The command is executed in this step
        processCommand(context, command, oh);
      }
      command.reset();
    }
  }

  /**
   * Add command to history. Delete old history. If command is a history command, exchange input
   * with historical command.
   */
  private String historyProcessing(String input) {
    if (input.trim().isEmpty()) {
      return input;
    }
    String command = null;
    if (input.startsWith("!")) { // history-command
      if (input.length() == 1) {
        return "history";
      } else {
        try {
          int number = Integer.parseInt(input.substring(1));
          List<String> commandHistory = session.getCommandHistory();
          if (number > 0 && commandHistory.size() >= number) {
            command = commandHistory.get(number - 1);
          } else {
            throw new IllegalArgumentException(
                "The command history cannot retrieve number " + number);
          }
        } catch (NumberFormatException nfe) {
          //					for (String cmd : session.getCommandHistory()) {
          for (int i = session.getCommandHistory().size() - 1; i >= 0; i--) {
            String cmd = session.getCommandHistory().get(i);
            if (cmd.contains(input.substring(1))) {
              command = cmd;
            }
          }
          if (command == null) {
            throw new IllegalArgumentException("The history search did not match any command");
          }
        }
      }
    } else {
      command = input;
    }
    session.getCommandHistory().add(0, command);
    return command;
  }

  private void processInput(String input) throws Exception {
    String[] inputPipeDividedArr = StringUtil.splitOnPipe(input);
    Script currentScript = session.getScript();
    Context orgContext = currentScript.getContext().clone();
    OutputHandler previousOh = null;
    OutputHandler oh = null;
    InputHandler ih = null;
    try {
      for (int i = 0; i < inputPipeDividedArr.length; i++) {
        session.resetCounter();
        String commandStr = inputPipeDividedArr[i];
        if (commandStr == null || commandStr.isEmpty() || "".equals(commandStr)) {
          continue;
        }
        Command command = new Command(commandStr, session.getContext());
        oh = new OutputHandler(command, session.getContext());
        ih =
            new InputHandler(
                command.getInputFilename(), previousOh, session.getContext().getUnittype());
        if (ih.isInput()) {
          multipleCommandExecution(session.getContext(), command, oh, ih);
        } else {
          singleCommandExecution(session.getContext(), command, oh);
        }
        ih.close();
        new RootMenu(session).executescript();
        if (!command.contextChangeOnly()) {
          currentScript.getContext().copyFrom(orgContext);
        }
        // At the last command, print output to file/shell
        previousOh = oh;
        if (i == inputPipeDividedArr.length - 1
            && previousOh.getListing() != null
            && !previousOh.getListing().getLines().isEmpty()) {
          previousOh.getListing().printListing(previousOh);
        }
        oh.close();
      }
    } catch (Exception e) { // lots of cleanup to reset state in the event of an error
      if (ih != null) {
        ih.close();
      }
      if (oh != null) {
        oh.close();
      }
      debugException(e);
      while (!session.getScriptStack().isEmpty()) {
        session.getScriptStack().pop();
      }
      session.getContext().copyFrom(orgContext);
      session.getProcessor().getEcho().reset();
      throw e;
    }
  }

  protected String retrieveInput() throws IOException {
    do {
      Script script = session.getScript();
      if (script.endOfScript()) {
        if (session.getScriptStack().size() > 1) {
          // If end-of-script, check if there's more scripts in the stack - and read on
          session.getScriptStack().pop();
          continue;
        } else if (session.getMode() == SessionMode.SCRIPT) {
          session.exitShell(0);
        } else if (session.getMode() == SessionMode.DAEMON) {
          script.addScriptLine(getDaemonCommand());
        } else if (session.getMode() == SessionMode.INTERACTIVE) {
          echo.printInteractiveMode();
          BufferedReader br =
              session.getACSShell().getReader(); // In interactive-mode, wait for keyboard-input
          script.addScriptLine(br.readLine());
          echo.setFromKeyboard(true); // set keyboard flag
        }
      }
      if (session.getDbi() != null && session.getDbi().isACSUpdated()) {
        session.setAcs(session.getDbi().getAcs());
        session.getContext().resetXAPS(session.getAcs());
        session.println("# XAPS data was refreshed due to changes by another XAPS module");
      }
      String input = script.getNextScriptLine();
      echo.setInput(input);
      echo.print();
      echo.setFromKeyboard(false); // reset flag
      return historyProcessing(input);
    } while (true);
  }

  /**
   * Retrieve a daemon command to feed into the shell. Daemon command come from Core Server Script
   * Execution Task. The daemon commands are fed into the shell using addDaemonCommands(String).
   *
   * @return daemonCommand
   */
  private String getDaemonCommand() {
    /*
     * The idea of putting a notify here, is that the shell is ready to perform a new
     * daemon command each time this method is called. At the same time this means
     * that the shell has completed (except the very first time this method is being
     * called) the previous command. This seems to be the "safest" place to send
     * the notify to any listeners (most likely the Core server running script executions)
     */
    synchronized (monitor) {
      monitor.notifyAll();
    }
    do {
      // Will wait forever if no daemon command is added to the shell
      synchronized (monitor) {
        if (!daemonCommands.isEmpty()) {
          return daemonCommands.remove(0);
        }
        // Wait one second each loop. If a notify has been sent
        // from the TR-069 Server/Syslog server (indicating a command
        // has been added), then the wait() will return immediately
        // and the command will be returned from the method.
        try {
          monitor.wait(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } while (true);
  }

  /**
   * This function is always called from another thread than the shell/processor-thread. Thus the
   * need for synchronizing.
   */
  public void addDaemonCommand(String command) {
    synchronized (monitor) {
      daemonCommands.add(command);
      monitor.notifyAll();
    }
  }

  public int getDaemonCommandSize() {
    return daemonCommands.size();
  }

  private void call(Command command) throws SQLException {
    List<CommandAndArgument> caaList = command.getCommandAndArguments();
    if (caaList.size() < 2) {
      throw new IllegalArgumentException("call expects a filename as the first argumnet");
    }
    String filename = caaList.get(1).toString();
    Context scriptContext = session.getContext().clone();
    Option conOption = command.getOptions().get(Option.OPTION_USE_CONTEXT);
    if (conOption != null && conOption.getOptionArgs() != null) {
      ContextContainer optionCC = ContextElement.parseContextElements(conOption.getOptionArgs());
      session.getProcessor().changeContext(optionCC, scriptContext, session);
    }
    Option varOption = command.getOptions().get(Option.OPTION_VARIABLES);
    Map<String, Variable> variables = new HashMap<>();
    if (varOption != null && varOption.getOptionArgs() != null) {
      String varOptionArgs = varOption.getStringToSubstitute();
      String[] varNameArr = varOptionArgs.split(",");
      int varCounter = 1;
      for (String varName : varNameArr) {
        Variable var = session.getScript().getVariable(varName);
        if (var == null) {
          variables.put("_" + varCounter, new Variable("_" + varCounter, varName));
          varCounter++;
        } else {
          variables.put(varName, var);
        }
      }
    }
    session.getScriptStack().push(new Script(filename, scriptContext, Script.SCRIPT, variables));
  }

  public Echo getEcho() {
    return echo;
  }

  public Object getMonitor() {
    return monitor;
  }

  public Logger getLogger() {
    return logger;
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public void setLogPrefix(String logPrefix) {
    if (logPrefix == null) {
      logPrefix = "";
    }
    this.logPrefix = logPrefix;
  }
}
