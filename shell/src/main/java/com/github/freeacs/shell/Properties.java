package com.github.freeacs.shell;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Properties {
  private static String propertyfile =
      Optional.ofNullable(System.getProperty("config.name")).orElse("application") + ".properties";

  public static PropertyReader pr;

  static {
    try {
      pr = new PropertyReader(propertyfile);
    } catch (PropertyReaderException pre) {
      // If propertyfile is not present, use deault settings. Important for shell-as-deamon
    }
  }

  private static Logger logger = LoggerFactory.getLogger(Properties.class);

  private static String getString(String propertyKey, String defaultValue) {
    try {
      String prop = pr.getProperty(propertyKey);
      if (prop == null) {
        logger.warn(
            "The value of "
                + propertyKey
                + " was not specified, instead using default value "
                + defaultValue);
        return defaultValue;
      }
      return prop;
    } catch (Throwable t) {
      return defaultValue;
    }
  }

  public static boolean isRestricted() {
    return "true".equals(getString("restricted", "false"));
  }
}
