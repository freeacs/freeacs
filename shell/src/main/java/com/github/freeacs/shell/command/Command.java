package com.github.freeacs.shell.command;

import com.github.freeacs.dbi.GroupParameter;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.util.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represent one command. A full command is presented to the shell like this:
 * (ContextElement)*Command (Option)* (CommandArgument)* (< INPUTFILE)? (> OUTPUTFILE)?
 *
 * <p>The most important task for this class is to parse the input from the prompt, and make it into
 * various objects for retrieval upon processing.
 */
public class Command {
  private static Pattern commandPattern = Pattern.compile("([^<>]+)(<([^>]+))?((>+)(.*))?");
  private static Pattern varPattern = Pattern.compile("(\\$\\{([^\\}]+)\\})");
  private static Pattern fileArgPattern = Pattern.compile("(\\$\\{(\\d+)\\})");
  private static Pattern contextPattern =
      Pattern.compile(
          "(\\.\\./)|(ut:[^/]+/)|(pr:[^/]+/)|(un:[^/]+/)|(up:[^/]+/)|(gr:[^/]+/)|(jo:[^/]+/)");

  /** Private Map<String, ContextElement> contextMap = new HashMap<String, ContextElement>();. */
  private ContextContainer contextContainer = new ContextContainer();

  private List<CommandAndArgument> commandAndArguments = new ArrayList<>();
  private Map<Character, Option> options = new HashMap<>();

  private String inputFilename;
  private String outputFilename;
  private boolean appendToOutput;
  private Context context;

  /**
   * In order to process the prompt input correctly, we need to know where the Context ends and
   * where the Command begins. When we know this boundary, we can easily parse each part.
   *
   * @param commandEFR
   * @return
   */
  private int getContextEndPos(String commandEFR) {
    Matcher m = contextPattern.matcher(commandEFR);
    int endPos = 0;
    int startPos = 0;
    while (m.find()) {
      startPos = m.start();
      if (startPos - 2 > endPos) {
        break;
      }
      endPos = m.end();
    }
    while (commandEFR.length() >= endPos + 1
        && "/".equals(commandEFR.substring(endPos, endPos + 1))) {
      endPos += 1;
    }
    return endPos;
  }

  public Command(String command, Context context) {
    this.context = context;
    Matcher m = commandPattern.matcher(command);
    String commandExclusiveFileRedirection = "";
    if (m.matches()) {
      commandExclusiveFileRedirection = m.group(1);
      inputFilename = m.group(3);
      if (inputFilename != null) {
        inputFilename = inputFilename.trim();
      }
      String outputRedirect = m.group(5);
      if (">>".equals(outputRedirect)) {
        appendToOutput = true;
      }
      outputFilename = m.group(6);
      if (outputFilename != null) {
        outputFilename = outputFilename.trim();
      }
    }
    int contextEndPos = getContextEndPos(commandExclusiveFileRedirection);
    String contextStr = "";
    if (contextEndPos > 0) {
      contextStr = commandExclusiveFileRedirection.substring(0, contextEndPos);
    }
    if (commandExclusiveFileRedirection.length() > contextEndPos) {
      commandExclusiveFileRedirection = commandExclusiveFileRedirection.substring(contextEndPos);
    }

    String[] strArr = StringUtil.split(commandExclusiveFileRedirection);
    for (String s : strArr) {
      Option o = Option.parseOption(s);
      if (o != null) {
        options.put(o.getType(), o);
      } else {
        commandAndArguments.add(new CommandAndArgument(s));
      }
    }
    Option uOption = options.get(Option.OPTION_USE_CONTEXT);
    if (uOption != null && uOption.getOptionArgs() != null) {
      contextContainer = ContextElement.parseContextElements(uOption.getOptionArgs());
    }
    // The u-option context takes precendce over command-context. The main idea is
    // to allowe the command-context to be appended to u-option context, but in case
    // there is an overlap, the command-context-element is skipped
    contextContainer.skipOrAppend(ContextElement.parseContextElements(contextStr));
  }

  public boolean contextChangeOnly() {
    if (contextContainer.size() > 0 && commandAndArguments.isEmpty()) {
      return true;
    }
    if (!commandAndArguments.isEmpty()) {
      CommandAndArgument caa = commandAndArguments.get(0);
      return "unit".equals(caa.getCommandAndArgument()) || "cc".equals(caa.getCommandAndArgument());
    }
    return false;
  }

  /** Public Map<String, ContextElement> getContextElementMap() { return contextMap; }. */
  public ContextContainer getContextContainer() {
    return contextContainer;
  }

  public void setContextContainer(ContextContainer cc) {
    this.contextContainer = cc;
  }

  public List<CommandAndArgument> getCommandAndArguments() {
    return commandAndArguments;
  }

  public Map<Character, Option> getOptions() {
    return options;
  }

  public String getInputFilename() {
    if (inputFilename == null) {
      return null;
    }

    String orgStr = inputFilename;
    Matcher m = varPattern.matcher(orgStr);
    Session session = context.getSession();
    String modStr = "";
    int previousEnd = 0;
    while (m.find()) {
      String varName = m.group(2);
      modStr += orgStr.substring(previousEnd, m.start());
      if (session.getScript().getVariable(varName) != null) {
        modStr +=
            varArgSubstParam(session.getScript().getVariable(varName).getValue(), session, true);
      }
      previousEnd = m.end();
    }
    if (previousEnd < orgStr.length()) {
      modStr += orgStr.substring(previousEnd);
    }
    return modStr;
  }

  public String getOutputFilename() {
    if (outputFilename == null) {
      return null;
    }

    String orgStr = outputFilename;
    Matcher m = varPattern.matcher(orgStr);
    Session session = context.getSession();
    String modStr = "";
    int previousEnd = 0;
    while (m.find()) {
      String varName = m.group(2);
      modStr += orgStr.substring(previousEnd, m.start());
      if (session.getScript().getVariable(varName) != null) {
        modStr +=
            varArgSubstParam(session.getScript().getVariable(varName).getValue(), session, true);
      }
      previousEnd = m.end();
    }
    if (previousEnd < orgStr.length()) {
      modStr += orgStr.substring(previousEnd);
    }
    return modStr;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (ContextElement ce : contextContainer.getContextList()) {
      sb.append(ce);
    }
    if (sb.length() > 0) {
      sb.append("/");
    }
    if (!commandAndArguments.isEmpty()) {
      sb.append(commandAndArguments.get(0)).append(" ");
    }
    for (Option o : options.values()) {
      sb.append(o).append(" ");
    }
    if (commandAndArguments.size() > 1) {
      for (int i = 1; i < commandAndArguments.size(); i++) {
        CommandAndArgument caa = commandAndArguments.get(i);
        if (caa.toString().indexOf(' ') > -1 || caa.toString().indexOf('\t') > -1) {
          sb.append("\"").append(caa).append("\" ");
        } else {
          sb.append(caa).append(" ");
        }
      }
    }
    return sb.toString();
  }

  public boolean appendToOutput() {
    return appendToOutput;
  }

  public void processVarArgs(Session session) {
    // Then substitute parts of context with fileArg (if necessary)
    for (ContextElement ce : getContextContainer().getContextList()) {
      varArgSubst(ce, session);
    }

    // Then substitute parts of commandAndArguments with fileArg (if necessary)
    for (CommandAndArgument caa : getCommandAndArguments()) {
      varArgSubst(caa, session);
    }

    // Then substitute parts of variable options with fileArg (if necessary)
    if (getOptions().containsKey(Option.OPTION_VARIABLES)) {
      varArgSubst(getOptions().get(Option.OPTION_VARIABLES), session);
    }
  }

  /** Reset substitutions and remove appended file-args. */
  public void reset() {
    for (ContextElement ce : getContextContainer().getContextList()) {
      ce.resetToOriginalState();
    }
    Iterator<CommandAndArgument> iterator = getCommandAndArguments().iterator();
    while (iterator.hasNext()) {
      CommandAndArgument caa = iterator.next();
      if (caa.isAppendedFromFile()) {
        iterator.remove();
      } else {
        caa.resetToOriginalState();
      }
    }

    for (Option o : getOptions().values()) {
      o.resetToOriginalState();
    }
  }

  private static String varArgSubstParam(
      String varValue, Session session, boolean variableValueFound) {
    Unittype unittype = session.getContext().getUnittype();
    String substStr = varValue; // default is that varValue is used
    if (varValue != null && varValue.startsWith("_") && varValue.length() > 1) {
      varValue = varValue.substring(1);
      if (unittype != null) {
        // The variable value is a unittype parameter name
        // check to see if there is a parameter value to used, otherwise NULL
        if (unittype.getUnittypeParameters().getByName(varValue) != null) {
          if (session.getContext().getUnit() != null) {
            UnitParameter up = session.getContext().getUnit().getUnitParameters().get(varValue);
            if (up != null && up.getValue() != null) {
              substStr = up.getValue();
            } else {
              substStr = "NULL";
            }
          } else if (session.getContext().getProfile() != null) {
            ProfileParameter pp =
                session.getContext().getProfile().getProfileParameters().getByName(varValue);
            if (pp != null && pp.getValue() != null) {
              substStr = pp.getValue();
            } else {
              substStr = "NULL";
            }
          } else if (session.getContext().getGroup() != null) {
            GroupParameter gp =
                session.getContext().getGroup().getGroupParameters().getByName(varValue);
            if (gp != null && gp.getParameter() != null && gp.getParameter().getValue() != null) {
              substStr = gp.getParameter().getValue();
            } else {
              substStr = "NULL";
            }
          } else if (session.getContext().getJob() != null) {
            JobParameter jp = session.getContext().getJob().getDefaultParameters().get(varValue);
            if (jp != null && jp.getParameter() != null && jp.getParameter().getValue() != null) {
              substStr = jp.getParameter().getValue();
            } else {
              substStr = "NULL";
            }
          } else { // Unittype
            substStr = unittype.getUnittypeParameters().getByName(varValue).getFlag().getFlag();
          }
        } else if (!variableValueFound) {
          substStr = "NULL";
        }
      } else if (!variableValueFound) {
        substStr = "NULL";
      }
    }

    return substStr;
  }

  public static void varArgSubst(Substitute subst, Session session) {
    String orgStr = subst.getStringToSubstitute();
    Matcher m = varPattern.matcher(orgStr);
    String modStr = "";
    int previousEnd = 0;
    while (m.find()) {
      String varName = m.group(2);
      modStr += orgStr.substring(previousEnd, m.start());
      if (session.getScript().getVariable(varName) != null) { // Variable exists
        // Check if variable value is to be used directly - or interpreted as a parameter
        modStr +=
            varArgSubstParam(session.getScript().getVariable(varName).getValue(), session, true);
      } else if (varName.startsWith("_")) { // Special variable name is used
        if (varName.length() > 1) {
          String type = varName.substring(1);
          if (ContextElement.types.contains(type)) {
            if (type.equals(ContextElement.TYPE_UNITTYPE)
                && session.getContext().getUnittype() != null) {
              modStr += session.getContext().getUnittype().getName();
            } else if (type.equals(ContextElement.TYPE_UNITTYPE_PARAMS)
                && session.getContext().getUnittypeParameter() != null) {
              modStr += session.getContext().getUnittypeParameter().getName();
            } else if (type.equals(ContextElement.TYPE_PROFILE)
                && session.getContext().getProfile() != null) {
              modStr += session.getContext().getProfile().getName();
            } else if (type.equals(ContextElement.TYPE_GROUP)
                && session.getContext().getGroup() != null) {
              modStr += session.getContext().getGroup().getName();
            } else if (type.equals(ContextElement.TYPE_JOB)
                && session.getContext().getJob() != null) {
              modStr += session.getContext().getJob().getName();
            } else if (type.equals(ContextElement.TYPE_UNIT)
                && session.getContext().getUnit() != null) {
              modStr += session.getContext().getUnit().getId();
            }
          } else {
            modStr += varArgSubstParam(varName, session, false);
          }
        } else {
          throw new IllegalArgumentException("The variable name '_' is not allowed");
        }
      } else { // Check if variable name can be interpreted as a parameter
        modStr += varArgSubstParam(varName, session, false);
      }
      previousEnd = m.end();
    }
    if (previousEnd < orgStr.length()) {
      modStr += orgStr.substring(previousEnd);
    }
    subst.setSubstitutedString(modStr);
  }

  public void processFileArgs(String[] fileArgs) {
    // If option -u is set: process parts of the fileArgs that is direct context-change
    int i = 0;
    Option uOption = getOptions().get(Option.OPTION_USE_CONTEXT);
    if (uOption != null) {
      ContextContainer fileargCC = new ContextContainer();
      for (i = 0; i < fileArgs.length; i++) {
        ContextContainer oneArgCC = ContextElement.parseContextElements(fileArgs[i]);
        if (oneArgCC.size() > 0) {
          // We read the file-args context in absolute order,
          // thus appending context to the container is suitable
          fileargCC.skipOrAppend(oneArgCC);
          String uOptArg = uOption.getStringToSubstitute();
          if (uOptArg != null) {
            uOption.setSubstitutedString(uOptArg + fileArgs[i]);
          } else {
            uOption.setSubstitutedString(fileArgs[i]);
          }
        } else {
          break;
        }
      }
      // getContextContainer() contains already parsed/read context
      // File args context will overwrite/insert the already read context
      getContextContainer().overwriteOrInsert(fileargCC);
      //			fileargCC.addContextContainer(getContextContainer());
      //			setContextContainer(fileargCC);
    }
    String[] fileArgsWithoutContext = new String[fileArgs.length - i];
    System.arraycopy(fileArgs, i, fileArgsWithoutContext, 0, fileArgsWithoutContext.length);

    boolean change = false;
    // Then substitute parts of context with fileArg (if necessary)
    for (ContextElement ce : getContextContainer().getContextList()) {
      if (fileArgSubst(ce, fileArgsWithoutContext)) {
        change = true;
      }
    }

    // Then substitute parts of commandAndArguments with fileArg (if necessary)
    for (CommandAndArgument caa : getCommandAndArguments()) {
      if (fileArgSubst(caa, fileArgsWithoutContext)) {
        change = true;
      }
    }

    // Then substitute parts of variable options with fileArg (if necessary)
    if (getOptions().containsKey(Option.OPTION_VARIABLES)) {
      Option varOption = getOptions().get(Option.OPTION_VARIABLES);
      if (fileArgSubst(varOption, fileArgsWithoutContext)) {
        change = true;
      }
    }

    // if no change using substitution, append
    // whole fileArgs to commandAndArguments
    if (!change) {
      for (String fileArg : fileArgsWithoutContext) {
        CommandAndArgument caa = new CommandAndArgument(fileArg, true);
        getCommandAndArguments().add(caa);
      }
    } else {
      // Some part of the command may be context-elements (this must have happend in fileArgSubgs())
      // We must then "move" the command to the context (and remove it from the
      // command/argument-list)
      Iterator<CommandAndArgument> iterator = getCommandAndArguments().iterator();
      while (iterator.hasNext()) {
        CommandAndArgument caa = iterator.next();
        ContextContainer cc = ContextElement.parseContextElements(caa.getStringToSubstitute());
        if (!"".equals(cc.toString())) {
          getContextContainer().overwriteOrInsert(cc);
          caa.setSubstitutedString(
              caa.getStringToSubstitute()
                  .substring(caa.getStringToSubstitute().lastIndexOf('/') + 1));
        }
      }
    }
  }

  private static boolean fileArgSubst(Substitute subst, String[] fileArgs) {
    String orgStr = subst.getStringToSubstitute();
    Matcher m = fileArgPattern.matcher(orgStr);
    String modStr = "";
    int previousEnd = 0;
    boolean changed = false;
    while (m.find()) {
      int fileArgIndex = Integer.parseInt(m.group(2));
      modStr += orgStr.substring(previousEnd, m.start());
      if (fileArgIndex <= 0 || fileArgIndex > fileArgs.length) {
        throw new IllegalArgumentException(
            "The file argument index "
                + fileArgIndex
                + " does not match any column in the file input");
      } else {
        modStr += fileArgs[fileArgIndex - 1];
      }
      previousEnd = m.end();
      changed = true;
    }
    if (previousEnd < orgStr.length()) {
      modStr += orgStr.substring(previousEnd);
    }
    subst.setSubstitutedString(modStr);
    return changed;
  }
}
