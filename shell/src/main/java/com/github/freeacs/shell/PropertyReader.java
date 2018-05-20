package com.github.freeacs.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PropertyReader {

	private static final String RELOAD_INTERVAL_MIN = "reload.interval-minutes";
	private static final String RELOAD_INTERVAL_SEC = "reload.interval-seconds";
	private final static long RELOAD_INTERVAL_DEFAULT = 30 * 1000;

	private static Map<String, Object> properties = new HashMap<String, Object>();
	private static Logger logger = LoggerFactory.getLogger(PropertyReader.class);
	private String propertyfile;

	public PropertyReader(String propertyfile) throws PropertyReaderException {
		this.propertyfile = propertyfile;
		if (properties.get(propertyfile) == null)
			readProperties();
	}

	private synchronized void readProperties() throws PropertyReaderException {
		String searchName = propertyfile;
		InputStream stream = null;
		try {
			ClassLoader cl = PropertyReader.class.getClassLoader();
			stream = cl.getResourceAsStream(searchName);
			while (stream == null) {
				cl = cl.getParent();
				if (cl == null)
					break;
				stream = cl.getResourceAsStream(searchName);
			}
			if (stream == null) {
				File f = new File(".");
				for (String filename : f.list()) {
					if (filename.equals(searchName)) {
						stream = new FileInputStream(filename);
					}
				}
			}
			Map<String, String> keys = new TreeMap<String, String>();
			if (stream != null) {
				stream = new java.io.BufferedInputStream(stream);
				InputStreamReader is = new InputStreamReader(stream);
				BufferedReader br = new BufferedReader((Reader) is);
				String line;
				while (true) {
					try {
						line = br.readLine();
					} catch (IOException ioe) {
						break;
					}
					if (line == null)
						break;
					if (line.startsWith("#"))
						continue;
					if (line.indexOf('=') > 0) {
						String key = line.substring(0, line.indexOf('='));
						key = key.trim();
						String value = line.substring(line.indexOf('=') + 1);
						value = value.trim();
						if (key.length() > 0 && !key.equals(""))
							keys.put(key, (value.equals("") ? null : value));
					} else {
						String key = line.trim();
						keys.put(key, null);
					}
				}
				properties.put(propertyfile + "REFRESH", new Long(System.currentTimeMillis()));
				properties.put(propertyfile, keys);
			} else {
				properties.remove(propertyfile);
				properties.remove(propertyfile + "REFRESH");
				throw new PropertyReaderException(searchName);
			}
		} catch (Throwable t) {
			properties.put(propertyfile + "REFRESH", new Long(System.currentTimeMillis()));
			if (t instanceof PropertyReaderException) {
				throw (PropertyReaderException) t;
			} else {
				throw new PropertyReaderException(searchName);
			}
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public static boolean isPropertyFileLoaded(String propertyfile) {
		if (properties.get(propertyfile) == null) {
			return false;
		} else {
			return true;
		}
	}

	public static synchronized void refreshCache() {
		Object[] propertyfiles = properties.keySet().toArray();
		for (int i = 0; propertyfiles != null && i < propertyfiles.length; i++) {
			String propertyfile = (String) propertyfiles[i];
			if (!propertyfile.endsWith("REFRESH"))
				refreshCache(propertyfile);
		}
	}

	public static synchronized void refreshCache(String propertyfile) {
		try {
			PropertyReader pr = new PropertyReader(propertyfile);
			pr.readProperties();
		} catch (PropertyReaderException pre) {
			if (properties.size() > 0)
				logger.warn("Could not refresh " + propertyfile + ", possibly the file was deleted. Continue using old data");
			else
				logger.error("Could not refresh " + propertyfile);
		}

	}

	@SuppressWarnings("rawtypes")
	public String getProperty(String propertykey) {
		if (!(propertykey.equals(RELOAD_INTERVAL_MIN) || propertykey.equals(RELOAD_INTERVAL_SEC))) {
			if (isRefreshRequired())
				refreshCache(propertyfile);
		}
		Map hm = (Map) properties.get(propertyfile);
		String prop = (String) hm.get(propertykey);
		if (prop != null && !prop.equals(""))
			return prop;
		else
			return prop;
	}

	public String[] getPropertyArray(String propertykey, String delimiter) {
		String propertyValue = getProperty(propertykey);
		if (propertyValue != null)
			return propertyValue.split(delimiter);
		else
			return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getPropertyMap() {
		if (properties.get(propertyfile) != null) {
			TreeMap hm = (TreeMap) properties.get(propertyfile);
			// Kloner objektet for å sikre at ingen kan endre properties ved et
			// "uhell"
			return (Map) hm.clone();
		} else
			return null;
	}

	// Should return ms interval
	private Long getReloadInterval() {
		Long reloadIntervalL = RELOAD_INTERVAL_DEFAULT;
		String reloadInterval = getProperty(RELOAD_INTERVAL_MIN);
		if (reloadInterval != null) {
			try {
				reloadIntervalL = Long.parseLong(reloadInterval) * 60 * 1000;
			} catch (NumberFormatException nfe) {
				reloadIntervalL = RELOAD_INTERVAL_DEFAULT;
			}
		} else {
			reloadInterval = getProperty(RELOAD_INTERVAL_SEC);
			if (reloadInterval != null) {
				try {
					reloadIntervalL = Long.parseLong(reloadInterval) * 1000;
				} catch (NumberFormatException nfe) {
					reloadIntervalL = RELOAD_INTERVAL_DEFAULT;
				}
			}
		}
		return reloadIntervalL;
	}

	public boolean isRefreshRequired() {
		if (properties.get(propertyfile) != null) {
			Long reloadIntervalL = getReloadInterval();
			Long tmsL = (Long) properties.get(propertyfile + "REFRESH");
			if (tmsL != null && tmsL.longValue() + reloadIntervalL < System.currentTimeMillis())
				return true;
		}
		return false;
	}

	public String getPropertyfile() {
		return propertyfile;
	}
}
