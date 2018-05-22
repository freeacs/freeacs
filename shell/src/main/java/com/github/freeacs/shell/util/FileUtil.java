package com.github.freeacs.shell.util;

import com.github.freeacs.dbi.*;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Properties;
import com.github.freeacs.shell.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

	public static boolean exists(String filename) {
		File f = new File(filename);
		return f.exists();
	}

	private static String[] protectedFilePatterns = new String[] { "xaps-shell.properties", "xaps-shell-logs.properties", "xaps-shell.*log", "xapsshell.sh", "fusion-shell.*log", "fusionshell.sh" };

	public static boolean allowed(String command, File f) {

		if (Properties.isRestricted()) {
			String msg = null;
			String absPath = f.getAbsolutePath();
			if (absPath.contains(".."))
				msg = "Error: Not allowed to do '" + command + "' since it contains '..'. This incident will be reported.";
			String thisDir = new File(".").getAbsolutePath();
			thisDir = thisDir.substring(0, thisDir.length() - 1);
			if (!absPath.contains(thisDir))
				msg = "Error: Not allowed to do '" + command + "' since it access outside your direcetory tree. This incident will be reported.";
			for (String protectedFilePattern : protectedFilePatterns) {
				if (absPath.matches(".*" + protectedFilePattern))
					msg = "Error: Not allowed to do '" + command + "' to this specially protected file. This incident will be reported";
			}
			if (msg != null) {
				System.out.println(msg);
				logger.error(msg);
				return false;
			} else
				return true;
		}
		return true;

	}

	public static void store(List<String> lines, String filename) throws IOException {
		// try {
		FileWriter fw = new FileWriter(new File(filename));
		for (int i = 0; i < lines.size(); i++) {
			fw.write(lines.get(i) + "\n");
		}
		fw.close();
		// } catch (Throwable t) {
		// throw new IOException("Could not write to file " + filename);
		// }
	}

	public static List<String> getLines(String filename) {
		List<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (line.trim().length() == 0)
					continue;
				if (line.trim().startsWith("#"))
					continue;
				lines.add(line);
			}
			return lines;
		} catch (FileNotFoundException fnfe) {
			return null;
		} catch (IOException ioe) {
			if (lines.size() > 0)
				return lines;
			return null;
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	public static List<String> getCompletions(Session session) throws IOException {
		Context context = session.getContext();
		List<String> completions = context.getCommands();
		if (context.getUnit() != null) {
			Unittype unittype = context.getUnittype();
			for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
				completions.add(utp.getName());
			}
		} else if (context.getProfile() != null) {
			Unittype unittype = context.getUnittype();
			for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
				completions.add(utp.getName());
			}
		} else if (context.getGroup() != null) {
			Unittype unittype = context.getUnittype();
			for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
				completions.add(utp.getName());
			}
		} else if (context.getJob() != null) {
			Unittype unittype = context.getUnittype();
			for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
				completions.add(utp.getName());
			}
		} else if (context.getUnittypeParameter() != null) {
			// add nothing
		} else if (context.getUnittype() != null) {
			Unittype unittype = context.getUnittype();
			for (Profile p : unittype.getProfiles().getProfiles()) {
				completions.add(p.getName());
			}
			for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
				completions.add(utp.getName());
				completions.add("V-" + utp.getName());
			}
			for (Job job : unittype.getJobs().getJobs()) {
				completions.add("J-" + job.getName());
			}
			for (Group group : unittype.getGroups().getGroups()) {
				completions.add("G-" + group.getName());
			}
		} else {
			for (Unittype unittype : session.getAcs().getUnittypes().getUnittypes()) {
				completions.add(unittype.getName());
			}
		}
		return completions;
	}

}
