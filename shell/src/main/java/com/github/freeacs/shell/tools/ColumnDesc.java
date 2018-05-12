package com.github.freeacs.shell.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnDesc {
	private boolean toUpperCase;
	private boolean toLowerCase;
	private boolean unitid;
	private int fromIndex;
	private int toIndex;
	private Pattern columnPattern;
	private Pattern descPattern = Pattern.compile("(\\d+)(u|l|unitid)?-(\\d+)(.*)?");

	public ColumnDesc(String columnDesc) {
		Matcher matcher = descPattern.matcher(columnDesc);
		if (matcher.matches()) {
			fromIndex = new Integer(matcher.group(1));
			String tmp = matcher.group(2);
			if (tmp != null) {
				if (tmp.equals("u"))
					toUpperCase = true;
				if (tmp.equals("l"))
					toLowerCase = true;
				if (tmp.equals("unitid"))
					unitid = true;
			}
			toIndex = new Integer(matcher.group(3));
			columnPattern = Pattern.compile(matcher.group(4));
		}
	}

	public boolean isToUpperCase() {
		return toUpperCase;
	}

	public boolean isToLowerCase() {
		return toLowerCase;
	}

	public boolean isUnitid() {
		return unitid;
	}

	public int getFromIndex() {
		return fromIndex;
	}

	public int getToIndex() {
		return toIndex;
	}

	public Pattern getColumnPattern() {
		return columnPattern;
	}

}
