package com.github.freeacs.shell;

import com.github.freeacs.shell.util.FileUtil;
import jline.ArgumentCompletor;
import jline.ConsoleReader;
import jline.SimpleCompletor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ACSShellReader extends BufferedReader {

	private ConsoleReader console;
	private ArgumentCompletor argumentCompletor;
	private ACSShell ACSShell;

	public ACSShellReader(Reader in, ConsoleReader reader) {
		super(in);
		this.console = reader;
	}

	public String readLine() {
		try {
			if (argumentCompletor != null)
				console.removeCompletor(argumentCompletor);
			argumentCompletor = new ArgumentCompletor(new SimpleCompletor(getLines()));
			console.addCompletor(argumentCompletor);
			String line = console.readLine();
			if (line == null) {
				println("\nGoodbye");
				System.exit(0);
			}
			else {
				return line;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	private String[] getLines() throws IOException {
		List<String> completions = new ArrayList<String>();
		if (ACSShell.getSession() != null && ACSShell.getSession().getAcs() != null)
			completions.addAll(FileUtil.getCompletions(ACSShell.getSession()));

		File folder = new File(System.getProperty("user.dir"));
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				completions.add(listOfFiles[i].getName());
			}
		}

		completions.add("show");

		return completions.toArray(new String[] {});
	}

	private void println(String s) {
		ACSShell.println(s);
	}

	public ACSShell getACSShell() {
		return ACSShell;
	}

	public void setACSShell(ACSShell ACSShell) {
		this.ACSShell = ACSShell;
	}

	//	private void listFiles() {
	//		File folder = new File(System.getProperty("user.dir"));
	//		File[] listOfFiles = folder.listFiles();
	//
	//		for (int i = 0; i < listOfFiles.length; i++) {
	//			if (listOfFiles[i].isFile()) {
	//				println(listOfFiles[i].getName());
	//			}
	//		}
	//	}
	//
	//	private void listFile(String string) throws IOException {
	//		BufferedReader br = new BufferedReader(new FileReader(string));
	//		while (br.ready()) {
	//			println(br.readLine());
	//		}
	//		br.close();
	//	}
	//
	//	private String getString(String[] arr, int i) {
	//		String s = "";
	//		for (int x = i; x < arr.length; x++) {
	//			s += arr[x];
	//			if ((x + 1) < arr.length)
	//				s += " ";
	//		}
	//		return s;
	//	}
}
