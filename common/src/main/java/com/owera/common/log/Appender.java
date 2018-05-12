package com.owera.common.log;

import com.owera.common.util.PropertyReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * There will only be one appender object for
 * each appender specified in the log property
 * file. That is, there can be two Console or
 * Rolling appender objects, but only if the
 * log property file specifies two distinct appender
 * name for these appenders.
 * @author Morten
 *
 */
public abstract class Appender {

	private static Pattern configPattern = Pattern.compile("(%(d|x|m|n|p|c))");

	/**
	 * The core log method.
	 */
	public abstract void log(LogObject lo) throws Exception;

	/**
	 * This method will be the de facto constructor of your appender
	 * class. You MUST therefore make a regular constructor WITHOUT
	 * any arguments (or just don't make ANY regular constructors)
	 * 
	 * In other words..you must make the appender at any time
	 * able to accept new arguments instead of the old ones. That could
	 * be change of file name, resize of files, delete old backup files,
	 * etc..
	 * 
	 * You can retrieve all information you want from the config-property
	 * file, through use of the "propertyReader" object in this super
	 * class. Use "appenderName"+<whatever> to ask for various properties.
	 * The "appenderName" is also found in the super class.
	 */
	public abstract void constructor();

	private String pattern;
	protected String appenderName;
	protected PropertyReader propertyReader;
	private String[] patternArray = new String[100]; // Should be enough with 10, but for good measure..

	protected void preparePatternArray() {
		Matcher m = configPattern.matcher(pattern);
		int pos = 0;
		int count = 0;
		while (m.find(pos)) {
			if (m.start() > pos) { // found a conversion char after the beginning
				patternArray[count++] = pattern.substring(pos, m.start());
			}
			patternArray[count++] = m.group(1);
			pos = m.end();
		}
		if (pos < pattern.length()) {
			patternArray[count++] = pattern.substring(pos);
		}
	}

	public String getPattern() {
		return pattern;
	}

	protected void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * A method to display the private members of the appender 
	 */
	public abstract String toString();

	public String getAppenderName() {
		return appenderName;
	}

	protected void setAppenderName(String appenderName) {
		this.appenderName = appenderName;
	}

	public PropertyReader getPropertyReader() {
		return propertyReader;
	}

	protected void setPropertyReader(PropertyReader propertyReader) {
		this.propertyReader = propertyReader;
	}

	public String[] getPatternArray() {
		return patternArray;
	}

}
