package com.github.freeacs.shell;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.shell.help.HelpProcess;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Context {
  private static Pattern commandPattern = Pattern.compile("\\t([^\\s\\n]+)");

  private Session session;
  private Group group;
  private Job job;
  private Profile profile;
  private Unit unit;
  private Unittype unittype;
  private UnittypeParameter unittypeParameter;
  private String[] commandCounterContext = new String[6];

  public Context(Session session) {
    this.session = session;
    commandCounterContext[0] = "";
    commandCounterContext[1] = "";
    commandCounterContext[2] = "";
    commandCounterContext[3] = "";
    commandCounterContext[4] = "";
    commandCounterContext[5] = "";
  }

  public void println(String s) {
    getSession().getACSShell().println(s);
  }

  public void print(String s) {
    getSession().getACSShell().print(s);
  }

  public UnittypeParameter getUnittypeParameter() {
    return unittypeParameter;
  }

  public void setUnittypeParameter(UnittypeParameter unittypeParameter) {
    this.unittypeParameter = unittypeParameter;
    if (unittypeParameter != null) {
      this.commandCounterContext[5] = "U";
    } else {
      this.commandCounterContext[5] = "";
    }
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
    if (group != null) {
      this.commandCounterContext[3] = "G";
    } else {
      this.commandCounterContext[3] = "";
    }
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
    if (job != null) {
      this.commandCounterContext[4] = "J";
    } else {
      this.commandCounterContext[4] = "";
    }
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
    if (profile != null) {
      this.commandCounterContext[1] = "P";
    } else {
      this.commandCounterContext[1] = "";
    }
  }

  public Unit getUnit() {
    return unit;
  }

  public void setUnit(Unit unit) {
    this.unit = unit;
    if (unit != null) {
      this.commandCounterContext[2] = "U";
    } else {
      this.commandCounterContext[2] = "";
    }
  }

  public Unittype getUnittype() {
    return unittype;
  }

  public void setUnittype(Unittype unittype) {
    this.unittype = unittype;
    if (unittype != null) {
      this.commandCounterContext[0] = "U";
    } else {
      this.commandCounterContext[0] = "";
    }
  }

  public List<String> getCommands() {
    List<String> commands = new ArrayList<>();
    String help = getHelp(null);
    Matcher m = commandPattern.matcher(help);
    int findPos = 0;
    while (m.find(findPos)) {
      String command = m.group(1);
      if (command.charAt(1) != '-' && command.charAt(0) != '<') {
        commands.add(m.group(1));
      }
      findPos = m.end();
    }
    return commands;
  }

  public String getHelp(String[] inputArr) {
    String input = null;
    if (inputArr != null && inputArr.length > 1) {
      input = inputArr[1].toLowerCase();
    }
    return HelpProcess.process(this, input);
  }

  public String toString() {
    return getPrompt();
  }

  public String getPrompt() {
    String prompt = "/";
    if (unittype != null) {
      prompt += "ut:" + unittype.getName() + "/";
    }
    if (profile != null) {
      prompt += "pr:" + profile.getName() + "/";
    }
    if (unit != null) {
      prompt += "un:" + unit.getId() + "/";
    }
    if (group != null) {
      prompt += "gr:" + group.getName() + "/";
    }
    if (job != null) {
      prompt += "jo:" + job.getName() + "/";
    }
    if (unittypeParameter != null) {
      prompt += "up:" + unittypeParameter.getName() + "/";
    }

    prompt += ">";
    return prompt;
  }

  public Context clone() {
    Context clone = new Context(session);
    clone.setUnittype(getUnittype());
    clone.setProfile(getProfile());
    clone.setUnit(getUnit());
    clone.setGroup(getGroup());
    clone.setJob(getJob());
    clone.setUnittypeParameter(getUnittypeParameter());
    return clone;
  }

  public void copyFrom(Context context) {
    setUnittype(context.getUnittype());
    setProfile(context.getProfile());
    // The unit object may be renewed (not updated) in one of the commands, but it may still be
    // the same unit. So if unit-id is the same, we do not reset the context with the old unit,
    // since we then might miss the changes (like setparam/delparam) on the unit.
    if (this.unit == null
        || this.unit.getId() == null
        || context.getUnit() == null
        || context.getUnit().getId() == null
        || !getUnit().getId().equals(context.getUnit().getId())) {
      setUnit(context.getUnit());
    }
    setGroup(context.getGroup());
    setJob(context.getJob());
    setUnittypeParameter(context.getUnittypeParameter());
  }

  public void resetToNull() {
    setUnittype(null);
    setProfile(null);
    setUnit(null);
    setGroup(null);
    setJob(null);
    setUnittypeParameter(null);
  }

  public void resetXAPS(ACS acs) {
    if (getUnittype() != null) {
      setUnittype(acs.getUnittype(getUnittype().getId()));
    }
    if (getProfile() != null) {
      setProfile(getProfile().getUnittype().getProfiles().getById(getProfile().getId()));
    }
    if (getGroup() != null) {
      setGroup(getUnittype().getGroups().getById(getGroup().getId()));
    }
    if (getUnittypeParameter() != null) {
      setUnittypeParameter(
          getUnittype().getUnittypeParameters().getById(getUnittypeParameter().getId()));
    }
  }

  public boolean equals(Context context) {
    return context.getUnittype() == getUnittype()
        && context.getProfile() == getProfile()
        && context.getUnit() == getUnit()
        && context.getGroup() == getGroup()
        && context.getJob() == getJob()
        && context.getUnittypeParameter() == getUnittypeParameter();
  }

  public String getCommandCounterContext() {
    return "["
        + commandCounterContext[0]
        + commandCounterContext[1]
        + commandCounterContext[2]
        + commandCounterContext[3]
        + commandCounterContext[4]
        + commandCounterContext[5]
        + "]";
  }

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }
}
