package com.owera.xaps.monitor;

import com.owera.common.log.Logger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MonitorProperties {
    private static Config config = ConfigFactory.parseResources("xaps-monitor.properties");

    /** The log. */
    private static Logger log = new Logger();

    /**
     * Gets the string.
     *
     * @param propertyKey the property key
     * @param defaultValue the default value
     * @return the string
     */
    public static String getString(String propertyKey, String defaultValue) {
        String prop = config.getString(propertyKey);
        if (prop == null) {
            log.debug("The value of " + propertyKey + " was not specified, instead using default value " + defaultValue);
            return defaultValue;
        }
        return prop;
    }
}
