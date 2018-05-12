package com.github.freeacs.shell.help;

import java.util.ArrayList;
import java.util.List;

public class HelpGroup {
	private List<Help> helpList = new ArrayList<Help>();

	private String menu;

	public HelpGroup(String menu) {
		this.menu = menu;
	}

	public void addHelp(Help help) {
		helpList.add(help);
	}

	public List<Help> getHelpList() {
		return helpList;
	}

	public Help getHelp(String command) {
		for (Help help : helpList) {
			if (command.equals(help.getCommand()))
				return help;
		}
		return null;
	}

	public StringBuffer toStringBuffer() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nShows the list of available commands.\nType 'help generic' to get generic commands.\n");
		sb.append("Type 'help <command>' to get detailed help on each command.\n\n");
		for (Help help : helpList) {
			sb.append("\t" + help.getSyntax().getSyntax() + "\n");
		}
		sb.append("\n");
		return sb;
	}

	public String toString() {
		return toStringBuffer().toString();
	}

	public List<String> getCommands() {
		List<String> commands = new ArrayList<String>();
		for (Help help : helpList) {
			commands.add(help.getCommand());
		}
		return commands;
	}

	public static String encode(String s) {
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		s = s.replaceAll("\n", "<br>");
		s = s.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		return s;
	}

	public String toHTML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<h2>" + menu + " menu</h2>\n");
		sb.append("List of commands available:<br>\n");
		sb.append("<ul><br>\n");
		for (Help help : helpList) {
			sb.append("<li>" + help.getCommand() + "\n");
		}
		sb.append("</ul><p>\n");
		for (Help help : helpList) {
			sb.append("<h3>" + menu + "." + encode(help.getCommand()) + "</h3>\n");
			sb.append("<b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Syntax:</b> " + encode(help.getSyntax().getSyntax()) + "<p>\n");
			sb.append("<b>&nbsp;&nbsp;&nbsp;Comment:</b> " + encode(help.getComment()) + "<p>\n");
			HelpOptions opts = help.getOptions();
			if (opts != null) {
				sb.append("<b>Arguments:</b><br>\n<ul>\n");
				for (HelpOption opt : opts.getOptions()) {
					sb.append("<li><b>" + encode(opt.getOption()) + "</b>: " + encode(opt.getComment()) + "\n");
				}
				sb.append("</ul>\n");
			}

			HelpArguments args = help.getArguments();
			if (args != null) {
				sb.append("<b>Arguments:</b><br>\n<ul>\n");
				for (HelpArgument arg : args.getArguments()) {
					sb.append("<li><b>" + encode(arg.getArgument()) + "</b>: " + encode(arg.getComment()) + "\n");
				}
				sb.append("</ul>\n");
			}
			HelpExamples examples = help.getExamples();
			if (examples != null) {
				sb.append("<b>&nbsp;&nbsp;Examples:</b><br>\n<ul>\n");
				for (String example : examples.getExamples()) {
					sb.append("<li>" + encode(example) + "\n");
				}
				sb.append("</ul>\n");
			}

		}
		return sb.toString();
	}
}
