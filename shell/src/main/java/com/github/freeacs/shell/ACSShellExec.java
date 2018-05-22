package com.github.freeacs.shell;

import jline.ConsoleReader;

import java.io.InputStreamReader;

public class ACSShellExec {

	public static void main(String[] args) throws Exception {
		ConsoleReader reader = new ConsoleReader();
		reader.setBellEnabled(false);
		ACSShell acsShell = new ACSShell();
		ACSShellReader acsShellReader = new ACSShellReader(new InputStreamReader(System.in), reader);
		acsShellReader.setACSShell(acsShell);
		acsShell.setReader(acsShellReader);
		acsShell.mainImpl(args, acsShell.getHikariDataSource("xaps"), acsShell.getHikariDataSource("syslog"));
	}
}