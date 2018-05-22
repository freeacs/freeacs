package com.github.freeacs.shell;

import com.github.freeacs.dbi.*;
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
		getSession().getFreeacsShell().println(s);
	}

	public void print(String s) {
		getSession().getFreeacsShell().print(s);
	}

	public UnittypeParameter getUnittypeParameter() {
		return unittypeParameter;
	}

	public void setUnittypeParameter(UnittypeParameter unittypeParameter) {
		this.unittypeParameter = unittypeParameter;
		if (unittypeParameter == null)
			this.commandCounterContext[5] = "";
		else
			this.commandCounterContext[5] = "U";
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
		if (group == null)
			this.commandCounterContext[3] = "";
		else
			this.commandCounterContext[3] = "G";
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
		if (job == null)
			this.commandCounterContext[4] = "";
		else
			this.commandCounterContext[4] = "J";
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		if (profile == null)
			this.commandCounterContext[1] = "";
		else
			this.commandCounterContext[1] = "P";
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
		if (unit == null)
			this.commandCounterContext[2] = "";
		else
			this.commandCounterContext[2] = "U";
	}

	public Unittype getUnittype() {
		return unittype;
	}

	public void setUnittype(Unittype unittype) {
		this.unittype = unittype;
		if (unittype == null)
			this.commandCounterContext[0] = "";
		else
			this.commandCounterContext[0] = "U";
	}

	public List<String> getCommands() {
		List<String> commands = new ArrayList<String>();
		String help = getHelp(null);
		Matcher m = commandPattern.matcher(help);
		int findPos = 0;
		while (m.find(findPos)) {
			String command = m.group(1);
			if (command.charAt(1) != '-' && command.charAt(0) != '<')
				commands.add(m.group(1));
			findPos = m.end();
		}
		return commands;
	}

	public String getHelp(String[] inputArr) {
		String input = null;
		if (inputArr != null && inputArr.length > 1)
			input = inputArr[1].toLowerCase();
		return HelpProcess.process(this, input);
	}

	public String toString() {
		return getPrompt();
	}

	public String getPrompt() {
		String prompt = "/";
		if (unittype != null)
			prompt += "ut:" + unittype.getName() + "/";
		if (profile != null)
			prompt += "pr:" + profile.getName() + "/";
		if (unit != null)
			prompt += "un:" + unit.getId() + "/";
		if (group != null)
			prompt += "gr:" + group.getName() + "/";
		if (job != null)
			prompt += "jo:" + job.getName() + "/";
		if (unittypeParameter != null)
			prompt += "up:" + unittypeParameter.getName() + "/";

		prompt += ">";
		return prompt;
	}

	public Context clone() {
		Context clone = new Context(session);
		clone.setUnittype(this.getUnittype());
		clone.setProfile(this.getProfile());
		clone.setUnit(this.getUnit());
		clone.setGroup(this.getGroup());
		clone.setJob(this.getJob());
		clone.setUnittypeParameter(this.getUnittypeParameter());
		return clone;
	}

	public void copyFrom(Context context) {
		this.setUnittype(context.getUnittype());
		this.setProfile(context.getProfile());
		// The unit object may be renewed (not updated) in one of the commands, but it may still be
		// the same unit. So if unit-id is the same, we do not reset the context with the old unit,
		// since we then might miss the changes (like setparam/delparam) on the unit.
		if (this.unit != null && this.unit.getId() != null && context.getUnit() != null && context.getUnit().getId() != null && this.getUnit().getId().equals(context.getUnit().getId())) {
			// skip reset
		} else {
			this.setUnit(context.getUnit());
		}
		this.setGroup(context.getGroup());
		this.setJob(context.getJob());
		this.setUnittypeParameter(context.getUnittypeParameter());
	}

	public void resetToNull() {
		this.setUnittype(null);
		this.setProfile(null);
		this.setUnit(null);
		this.setGroup(null);
		this.setJob(null);
		this.setUnittypeParameter(null);
	}

	public void resetXAPS(ACS acs) {

		if (this.getUnittype() != null)
			this.setUnittype(acs.getUnittype(this.getUnittype().getId()));
		if (this.getProfile() != null)
			this.setProfile(this.getProfile().getUnittype().getProfiles().getById(this.getProfile().getId()));
		if (this.getGroup() != null)
			this.setGroup(this.getUnittype().getGroups().getById(this.getGroup().getId()));
		if (this.getUnittypeParameter() != null)
			this.setUnittypeParameter(this.getUnittype().getUnittypeParameters().getById(this.getUnittypeParameter().getId()));
	}

	public boolean equals(Context context) {
		if (context.getUnittype() != this.getUnittype())
			return false;
		if (context.getProfile() != this.getProfile())
			return false;
		if (context.getUnit() != this.getUnit())
			return false;
		if (context.getGroup() != this.getGroup())
			return false;
		if (context.getJob() != this.getJob())
			return false;
		if (context.getUnittypeParameter() != this.getUnittypeParameter())
			return false;
		return true;
	}

	public String getCommandCounterContext() {
		return "[" + commandCounterContext[0] + commandCounterContext[1] + commandCounterContext[2] + commandCounterContext[3] + commandCounterContext[4] + commandCounterContext[5] + "]";
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
