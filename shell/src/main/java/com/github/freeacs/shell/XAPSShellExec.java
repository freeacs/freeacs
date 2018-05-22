package com.github.freeacs.shell;

import jline.ConsoleReader;

import java.io.InputStreamReader;

public class XAPSShellExec {

	public static void main(String[] args) throws Exception {
		ConsoleReader reader = new ConsoleReader();
		reader.setBellEnabled(false);
		ACSShell ACSShell = new ACSShell();
		XAPSShellReader xapsShellReader = new XAPSShellReader(new InputStreamReader(System.in), reader);
		xapsShellReader.setACSShell(ACSShell);
		ACSShell.setReader(xapsShellReader);
		ACSShell.mainImpl(args, ACSShell.getHikariDataSource("xaps"), ACSShell.getHikariDataSource("syslog"));
	}
}