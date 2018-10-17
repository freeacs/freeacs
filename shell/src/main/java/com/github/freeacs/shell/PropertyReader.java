package com.github.freeacs.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyReader {
  private static final String RELOAD_INTERVAL_MIN = "reload.interval-minutes";
  private static final String RELOAD_INTERVAL_SEC = "reload.interval-seconds";
  private static final long RELOAD_INTERVAL_DEFAULT = 30 * 1000;

  private static Map<String, Object> properties = new HashMap<>();
  private static Logger logger = LoggerFactory.getLogger(PropertyReader.class);
  private String propertyfile;

  public PropertyReader(String propertyfile) {
    this.propertyfile = propertyfile;
    if (properties.get(propertyfile) == null) {
      readProperties();
    }
  }

  private synchronized void readProperties() {
    String searchName = propertyfile;
    InputStream stream = null;
    try {
      File f = new File("config");
      String[] files = f.list();
      if (files != null) {
        for (String filename : files) {
          if (filename.equals(searchName)) {
            stream = new FileInputStream("config/" + filename);
          }
        }
      }
      if (stream == null) {
        f = new File(".");
        files = f.list();
        if (files != null) {
          for (String filename : files) {
            if (filename.equals(searchName)) {
              stream = new FileInputStream(filename);
            }
          }
        }
      }
      if (stream == null) {
        ClassLoader cl = PropertyReader.class.getClassLoader();
        stream = cl.getResourceAsStream(searchName);
        while (stream == null) {
          cl = cl.getParent();
          if (cl == null) {
            break;
          }
          stream = cl.getResourceAsStream(searchName);
        }
      }
      Map<String, String> keys = new TreeMap<>();
      if (stream != null) {
        stream = new java.io.BufferedInputStream(stream);
        InputStreamReader is = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(is);
        String line;
        do {
          try {
            line = br.readLine();
          } catch (IOException ioe) {
            break;
          }
          if (line == null) {
            break;
          }
          if (line.startsWith("#")) {
            continue;
          }
          if (line.indexOf('=') > 0) {
            String key = line.substring(0, line.indexOf('='));
            key = key.trim();
            String value = line.substring(line.indexOf('=') + 1);
            value = value.trim();
            if (!key.isEmpty()) {
              keys.put(key, "".equals(value) ? null : value);
            }
          } else {
            String key = line.trim();
            keys.put(key, null);
          }
        } while (true);
        properties.put(propertyfile + "REFRESH", System.currentTimeMillis());
        properties.put(propertyfile, keys);
      } else {
        properties.remove(propertyfile);
        properties.remove(propertyfile + "REFRESH");
        throw new PropertyReaderException(searchName);
      }
    } catch (Throwable t) {
      properties.put(propertyfile + "REFRESH", System.currentTimeMillis());
      if (t instanceof PropertyReaderException) {
        throw (PropertyReaderException) t;
      } else {
        throw new PropertyReaderException(searchName);
      }
    } finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (Throwable t) {
        // do nothing
      }
    }
  }

  public static synchronized void refreshCache(String propertyfile) {
    try {
      PropertyReader pr = new PropertyReader(propertyfile);
      pr.readProperties();
    } catch (PropertyReaderException pre) {
      if (!properties.isEmpty()) {
        logger.warn(
            "Could not refresh "
                + propertyfile
                + ", possibly the file was deleted. Continue using old data");
      } else {
        logger.error("Could not refresh " + propertyfile);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  public String getProperty(String propertykey) {
    if (!RELOAD_INTERVAL_MIN.equals(propertykey)
        && !RELOAD_INTERVAL_SEC.equals(propertykey)
        && isRefreshRequired()) {
      refreshCache(propertyfile);
    }
    Map hm = (Map) properties.get(propertyfile);
    String prop = (String) hm.get(propertykey);
    if (prop != null && !"".equals(prop)) {
      return prop;
    }
    return prop;
  }

  /** Should return ms interval. */
  private Long getReloadInterval() {
    long reloadIntervalL = RELOAD_INTERVAL_DEFAULT;
    String reloadInterval = getProperty(RELOAD_INTERVAL_MIN);
    if (reloadInterval != null) {
      try {
        reloadIntervalL = Long.parseLong(reloadInterval) * 60 * 1000;
      } catch (NumberFormatException ignored) {
      }
    } else {
      reloadInterval = getProperty(RELOAD_INTERVAL_SEC);
      if (reloadInterval != null) {
        try {
          reloadIntervalL = Long.parseLong(reloadInterval) * 1000;
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return reloadIntervalL;
  }

  public boolean isRefreshRequired() {
    if (properties.get(propertyfile) != null) {
      Long reloadIntervalL = getReloadInterval();
      Long tmsL = (Long) properties.get(propertyfile + "REFRESH");
      return tmsL != null && tmsL + reloadIntervalL < System.currentTimeMillis();
    }
    return false;
  }
}
