package com.github.freeacs.common.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * I would like to retrieve all config/text/xml-files from the classpath, if possible.
 * The main reason is deployment, because if you have to specify path for each file you
 * would like to retrieve, then you would have to supply different configs for different
 * setups. With this approach that won't be necessary.  
 * 
 * This little method is therefore quite useful. 
 */
public class FileUtil {
	public File getFileFromClasspath(String searchName)  throws IOException {
		ClassLoader cl = this.getClass().getClassLoader();
		URL url = null;
		do {
			url = cl.getResource(searchName);
			cl = cl.getParent();
		} while (url == null && cl != null);
		if (url == null)
			throw new IOException("File " + searchName + " was not found in classpath");
		String filename = url.getFile();
		filename = filename.replaceAll("%20", " ");
		return new File(filename);
	}
	
	
	public static String normalizeFilename(String filename) {
		// TODO - Windows cannot allow: \/:?*|<>"
		// TODO - Unix cannot allow /, \ must be escaped with \
		return filename;
	}

	
	
}
