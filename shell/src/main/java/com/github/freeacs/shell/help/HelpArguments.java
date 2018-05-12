package com.github.freeacs.shell.help;

import java.util.ArrayList;
import java.util.List;

public class HelpArguments {
	private List<HelpArgument> arguments = new ArrayList<HelpArgument>();

	public void addArgument(HelpArgument helpArgument) {
		arguments.add(helpArgument);
	}

	public List<HelpArgument> getArguments() {
		return arguments;
	}

}
