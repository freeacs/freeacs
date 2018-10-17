package com.owera.xaps.monitor.task;

import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import com.owera.xaps.monitor.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for monitoring a set of URLs.
 *
 * @author Morten
 */
public class ModuleMonitorTask extends TaskDefaultImpl {
  private static Logger log = LoggerFactory.getLogger(ModuleMonitorTask.class);

  /**
   * Contains a list of MonitorInfo. This list is used to 1. Run monitoring of the various URLs in
   * MonitorInfo 2. Read the list of monitored URLs 3. Read the status for each monitored URL.
   */
  private static Set<MonitorInfo> monitorInfoSet = new TreeSet<>();

  /**
   * All modules are defined here - we assume that every module will be monitored using this URL:
   * monitor-urlbase+xaps+<module>+/ok Example: https://localhost:8443/xapsspp/ok
   */
  static {
    for (String module : new String[] {"core", "stun", "syslog", "tr069", "web", "webservice"}) {
      monitorInfoSet.add(new MonitorInfo(module));
    }
  }

  private final Properties properties;

  public ModuleMonitorTask(String taskName, Properties properties) {
    super(taskName);
    this.properties = properties;
  }

  @Override
  public void runImpl() throws Throwable {
    /* Update urlBase from config for every time we run monitoring */
    String urlBase = Properties.URL_BASE;

    /* Iterate over the monitorInfoMap and monitor each URL, update status/errormessage in MonitorInfo */
    Map<MonitorInfo, MonitorExecution> mapInfo2Execution = new HashMap<>();

    for (MonitorInfo mi : monitorInfoSet) {
      String moduleUrl = properties.get("monitor.url." + mi.getModule());
      if (moduleUrl == null) {
        moduleUrl = urlBase + mi.getModule() + "/ok";
      }
      MonitorExecution me = new MonitorExecution(moduleUrl);
      mapInfo2Execution.put(mi, me);
      new Thread(me).start();
    }

    /* Check that mapInfo2Execution is updated with results from all MonitorExecutions */
    do {
      boolean allResultsReady = true;
      for (Entry<MonitorInfo, MonitorExecution> entry : mapInfo2Execution.entrySet()) {
        if (entry.getValue().getStatus() == null) {
          Thread.sleep(Properties.RETRY_SECS * 1000 / 10);
          allResultsReady = false;
        } else {
          entry.getKey().setErrorMessage(entry.getValue().getErrorMessage());
          entry.getKey().setStatus(entry.getValue().getStatus());
          entry.getKey().setVersion(entry.getValue().getVersion());
          entry.getKey().setUrl(entry.getValue().getUrl());
          if (entry.getKey().getErrorMessage() != null && "OK".equals(entry.getKey().getStatus())) {
            log.warn("Monitoring: ModuleMonitorTask: ErrorMessage is not null and status is OK!!");
          }
        }
      }
      if (allResultsReady) {
        break;
      }
    } while (true);
  }

  @Override
  public Logger getLogger() {
    return log;
  }

  public static Set<MonitorInfo> getMonitorInfoSet() {
    return monitorInfoSet;
  }
}
