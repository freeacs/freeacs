package com.github.freeacs.shell;

import jline.ConsoleReader;

import java.io.InputStreamReader;

public class XAPSShellExec {

	public static void main(String[] args) throws Exception {
		ConsoleReader reader = new ConsoleReader();
		reader.setBellEnabled(false);
		XAPSShell xapsShell = new XAPSShell();
		XAPSShellReader xapsShellReader = new XAPSShellReader(new InputStreamReader(System.in), reader);
		xapsShellReader.setXapsShell(xapsShell);
		xapsShell.setReader(xapsShellReader);
		xapsShell.mainImpl(args, XAPSShell.getHikariDataSource("xaps"), XAPSShell.getHikariDataSource("syslog"));
	}
}