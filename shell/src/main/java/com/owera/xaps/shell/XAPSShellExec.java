package com.owera.xaps.shell;

import java.io.InputStreamReader;

import com.owera.common.log.Log;

import jline.ConsoleReader;

public class XAPSShellExec {

	public static void main(String[] args) throws Exception {
		Log.initialize("xaps-shell-logs.properties");
		ConsoleReader reader = new ConsoleReader();
		reader.setBellEnabled(false);
		XAPSShell xapsShell = new XAPSShell();
		XAPSShellReader xapsShellReader = new XAPSShellReader(new InputStreamReader(System.in), reader);
		xapsShellReader.setXapsShell(xapsShell);
		xapsShell.setReader(xapsShellReader);
		xapsShell.mainImpl(args);
	}
}