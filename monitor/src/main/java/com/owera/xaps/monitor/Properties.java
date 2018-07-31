package com.owera.xaps.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class Properties {

	public static Long RETRY_SECS = null;
	public static String URL_BASE = null;

	private final Environment environment;

	@Autowired
	public Properties(Environment environment) {
		this.environment = environment;
		URL_BASE = getMonitorURLBase();
		RETRY_SECS = getRetrySeconds();
	}

	public String getMonitorURLBase() {
		String urlBase = environment.getProperty("monitor.urlbase");
		if (urlBase == null) {
			return "http://localhost/";
		}
		if (!urlBase.endsWith("/"))
			urlBase += "/";
		return urlBase;
	}

	public long getRetrySeconds() {
		String prop = environment.getProperty("monitor.retrysec");
		try {
			return Long.parseLong(prop);
		} catch (Throwable t) {
			return 300L;
		}
	}

	public String get(String s) {
		return environment.getProperty(s);
	}
}
