package com.github.freeacs;

import com.github.freeacs.common.util.PropertyReader;
import com.owera.xaps.base.Log;

public class Properties {

	public enum Module {
		TR069("tr069"), SPP("spp");
		private String module;

		Module(String module) {
			this.module = module;
		}

		public String getModuleName() {
			return module;
		}
	}

	private static PropertyReader pr;

	private static PropertyReader getPr(Module module) {
		if (pr == null)
			pr = new PropertyReader("xaps-" + module.getModuleName() + ".properties");
		return pr;
	}

	private static int getInteger(Module module, String propertyKey, int defaultValue) {
		String prop = getPr(module).getProperty(propertyKey);
		try {
			return Integer.parseInt(prop);
		} catch (Throwable t) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not a number, instead using default value " + defaultValue, t);
			return defaultValue;
		}
	}

	@SuppressWarnings("unused")
	private static String getString(Module module, String propertyKey, String defaultValue) {
		String prop = getPr(module).getProperty(propertyKey);
		if (prop == null) {
			Log.warn(Properties.class, "The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
			return defaultValue;
		}
		return prop;
	}

	public static int concurrentDownloadLimit(Module module) {
		return getInteger(module, "concurrent.download.limit", 50);
	}

	public static int getMaxConn(Module module) {
		Integer maxConn = getInteger(module, "db.max-connections", 10);
		if (maxConn < 5)
			maxConn = 5;
		return maxConn;
	}
	
}
