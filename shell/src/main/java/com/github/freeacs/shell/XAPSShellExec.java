package com.github.freeacs.shell;

import jline.ConsoleReader;

import java.io.InputStreamReader;

public class XAPSShellExec {

	public static void main(String[] args) throws Exception {
		ConsoleReader reader = new ConsoleReader();
		reader.setBellEnabled(false);
		FreeacsShell freeacsShell = new FreeacsShell();
		XAPSShellReader xapsShellReader = new XAPSShellReader(new InputStreamReader(System.in), reader);
		xapsShellReader.setFreeacsShell(freeacsShell);
		freeacsShell.setReader(xapsShellReader);
		freeacsShell.mainImpl(args, FreeacsShell.getHikariDataSource("xaps"), FreeacsShell.getHikariDataSource("syslog"));
	}
}