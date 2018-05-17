package com.github.freeacs.shell;

import com.github.freeacs.dbi.File;
import com.github.freeacs.shell.util.FileUtil;

import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Script {
	public final static int SCRIPT = 0;
	public final static int WHILE = 1;
	public final static int IF = 2;

	private int type;
	private Context context;
	private Map<String, Variable> variables = new HashMap<String, Variable>();
	private List<String> scriptLines;
	private int linePointer = 0;
	private boolean skipOnNextIfElseWord = false;
	private String filename; // Name of script-file (if used)
	private BufferedReader whileInput; // File-args when iterating over a file
	// Sequence of linepointer-numbers going back to root-script. Gives a unique while-id
	private String whilePath;

	public Script(List<String> scriptLines, Context context, int type) {
		this(scriptLines, context, type, null);
	}

	public Script(List<String> scriptLines, Context context, int type, Map<String, Variable> variables) {
		this.scriptLines = scriptLines;
		this.context = context;
		this.type = type;
		if (variables != null)
			this.variables = variables;
		this.filename = "N/A";
	}

	public Script(String filename, Context context, int type) {
		this(filename, context, type, null);
	}

	public Script(String filename, Context context, int type, Map<String, Variable> variables) {
		try {
			this.filename = filename;
			boolean fileFound = false;
			this.type = type;
			if (context.getUnittype() != null) {
				File script = context.getUnittype().getFiles().getByName(filename);
				if (script != null) {
					scriptLines = new ArrayList<String>();
					for (String line : new String(script.getContent()).split("\n")) {
						if (line == null || line.trim().length() == 0 || line.trim().startsWith("#"))
							continue;
						scriptLines.add(line.trim());
					}
					this.context = context;
					fileFound = true;
				}
			}
			if (!fileFound && FileUtil.exists(filename)) {
				this.context = context;
				scriptLines = FileUtil.getLines(filename);
				fileFound = true;
			} else if (!fileFound)
				throw new IllegalArgumentException("The script file " + filename + " does not exist");
			this.variables = variables;
		} catch (SQLException nce) {
			throw new IllegalArgumentException("The script file " + filename + " was not found due to database connection problem");
		}
	}

	private String getCommand(String s) {
		s = s.trim();
		int spacePos = s.indexOf(" ");
		if (spacePos > -1)
			s = s.substring(0, spacePos);
		return s;
	}

	//	public String getNextScriptLineWithCommand(String... cmdList) {
	//		String s = null;
	//		if ((s = getNextScriptLine()) != null) {
	//			String word = getCommand(s);
	//			for (String w : cmdList) {
	//				if (word.equals(w)) {
	//					return s;
	//				}
	//			}
	//		}
	//		return null;
	//	}

	public void moveUpUntilCommand(String... cmdList) {
		String s = null;
		int skipAhead = 0;
		while ((s = getNextScriptLine()) != null) {
			String word = getCommand(s);
			if (word.equals("if"))
				skipAhead++;
			else if (word.equals("fi"))
				skipAhead--;
			if (skipAhead > 0)
				continue;
			boolean match = false;
			for (String w : cmdList) {
				if (word.equals(w)) {
					match = true;
				}
			}
			if (match) {
				decLinePointer();
				break;
			}
		}
	}

	public void addScriptLine(String line) {
		scriptLines.add(line);
	}

	public void insertScriptLine(int linePointer, String line) {
		scriptLines.add(linePointer, line);
	}

	public void addVariable(String name, String value) {
		variables.put(name, new Variable(name, value));
	}

	public void removeVariable(String name) {
		variables.remove(name);
	}

	public Variable getVariable(String name) {
		return variables.get(name);
	}

	public Map<String, Variable> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, Variable> variables) {
		this.variables = variables;
	}

	public boolean endOfScript() {
		if (linePointer >= scriptLines.size())
			return true;
		else
			return false;
	}

	public String getPreviousScriptLine() {
		if (linePointer == 0 || linePointer > scriptLines.size()) {
			return null;
		} else {
			return scriptLines.get(linePointer - 1);
		}
	}

	public String getNextScriptLine() {
		if (linePointer >= scriptLines.size()) {
			return null;
		} else {
			return scriptLines.get(linePointer++);
		}
	}

	public String getScriptLine(int linePointer) {
		if (linePointer >= scriptLines.size()) {
			return null;
		} else {
			return scriptLines.get(linePointer);
		}
	}

	//	public String getLastRetrievedScriptLine() {
	//		if (linePointer == 0 || linePointer >= scriptLines.size() - 1) {
	//			return null;
	//		} else {
	//			return scriptLines.get(linePointer - 1);
	//		}
	//	}

	public Context getContext() {
		return context;
	}

	public int getLinePointer() {
		return linePointer;
	}

	public void decLinePointer() {
		linePointer--;
	}

	public void incLinePointer() {
		linePointer++;
	}

	public int getType() {
		return type;
	}

	public void reset() {
		linePointer = 0;
	}

	public void setSkipOnNextIfElseWord(boolean skipOnNextIfElseWord) {
		this.skipOnNextIfElseWord = skipOnNextIfElseWord;
	}

	public boolean isSkipOnNextIfElseWord() {
		return skipOnNextIfElseWord;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		String typeStr = "SCRIPT";
		if (type == IF)
			typeStr = "IF";
		else if (type == WHILE)
			typeStr = "WHILE";
		sb.append("Context: " + context.toString() + ", Type: " + typeStr + ", Size: " + scriptLines.size() + ", Position: " + linePointer + ", Filename: " + filename + "\n");
		for (int i = 0; i < scriptLines.size(); i++) {
			if (linePointer == i)
				sb.append(" ====> " + scriptLines.get(i) + "\n");
			else
				sb.append("       " + scriptLines.get(i) + "\n");
		}
		return sb.toString();
	}

	public BufferedReader getWhileInput() {
		return whileInput;
	}

	public void setWhileInput(BufferedReader whileInput) {
		this.whileInput = whileInput;
	}

	public String getWhilePath() {
		return whilePath;
	}

	public void setWhilePath(String whilePath) {
		this.whilePath = whilePath;
	}
}
