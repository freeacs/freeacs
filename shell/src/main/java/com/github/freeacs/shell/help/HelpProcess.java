package com.github.freeacs.shell.help;

import com.github.freeacs.shell.Context;
import java.util.List;

public class HelpProcess {
  public static String process(Context context, String helpArg) {
    HelpDefinitions helpDefs = new HelpDefinitions();
    HelpGroup hgGen = helpDefs.getHelpGroup(HelpDefinitions.CK_GENERIC);
    HelpGroup hgCon = helpDefs.getHelpGroup(getContextKey(context));
    if (helpArg == null) {
      return hgCon.toString();
    }
    if (helpArg.startsWith("generic")) {
      return hgGen.toString();
    }
    List<String> commands = hgGen.getCommands();
    commands.addAll(hgCon.getCommands());
    int hitCount = 0;
    String commandChosen = null;
    String ambigiousCommands = "";
    for (String command : commands) {
      if (command.equals(helpArg)) {
        commandChosen = command;
        hitCount = 0;
        break;
      } else if (command.startsWith(helpArg)) {
        hitCount++;
        commandChosen = command;
        if ("".equals(ambigiousCommands)) {
          ambigiousCommands = command;
        } else {
          ambigiousCommands += ", " + command;
        }
      }
    }
    if (hitCount > 1) {
      return "Ambigious help argument, matched "
          + hitCount
          + " commands:\n"
          + ambigiousCommands
          + "\n";
    }
    if (commandChosen == null) {
      return "Help argument did not match any command, skip argument to get list of available commands\n";
    }
    if (hgGen.getHelp(commandChosen) != null) {
      return hgGen.getHelp(commandChosen).toString();
    } else {
      return hgCon.getHelp(commandChosen).toString();
    }
  }

  private static String getContextKey(Context context) {
    if (context.getUnit() != null) {
      return HelpDefinitions.CK_UNIT;
    }
    if (context.getProfile() != null) {
      return HelpDefinitions.CK_PROFILE;
    }
    if (context.getUnittypeParameter() != null) {
      return HelpDefinitions.CK_UNITTYPEPARAMETER;
    }
    if (context.getGroup() != null) {
      return HelpDefinitions.CK_GROUP;
    }
    if (context.getJob() != null) {
      return HelpDefinitions.CK_JOB;
    }
    if (context.getUnittype() != null) {
      return HelpDefinitions.CK_UNITTYPE;
    }
    return HelpDefinitions.CK_ROOT;
  }
}
