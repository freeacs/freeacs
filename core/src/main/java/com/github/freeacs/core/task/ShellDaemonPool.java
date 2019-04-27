package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.shell.ACSShell;
import com.github.freeacs.shell.ACSShellDaemon;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a container for all the shell daemons available within TR-069 server.
 *
 * @author morten
 */
public class ShellDaemonPool {
  private static Logger logger = LoggerFactory.getLogger(ShellDaemonPool.class);

  private static Map<String, List<ACSShellDaemon>> shellDaemonPoolMap = new HashMap<>();

  private static ACSShellDaemon createNewShellDaemon(
      DataSource mainDataSource, DataSource syslogDataSource, int index, String fusionUser)
      throws Throwable {
    ACSShellDaemon acsShellDaemon =
        new ACSShellDaemon(mainDataSource, syslogDataSource, fusionUser);
    acsShellDaemon.setIndex(index);
    ACSShell ACSShell = acsShellDaemon.getACSShell();
    try {
      ACSShell.setPrinter(
          new PrintWriter(
              new FileWriter("fusion-core-shell-daemon-for-" + fusionUser + "-" + index + ".log")));
    } catch (IOException e) {
      logger.error(
          "ScriptExecutor: Cannot log freeacs-shell output til fusion-core-shell-daemon-for-"
              + fusionUser
              + "-"
              + index
              + ".log file",
          e);
    }
    Thread thread = new Thread(acsShellDaemon);
    thread.setName("Core Shell Daemon " + index);
    thread.setDaemon(true);
    thread.start();
    while (!acsShellDaemon.isInitialized()) {
      List<Throwable> throwables = acsShellDaemon.getAndResetThrowables();
      if (throwables != null && !throwables.isEmpty()) {
        throw throwables.get(0);
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return acsShellDaemon;
  }

  private static List<ACSShellDaemon> getShellDaemonPool(String fusionUser) {
    return shellDaemonPoolMap.computeIfAbsent(fusionUser, k -> new ArrayList<>());
  }

  static synchronized ACSShellDaemon getShellDaemon(
          DataSource mainDataSource,
          DataSource syslogDataSource,
          String fusionUser,
          Properties properties)
      throws Throwable {
    List<ACSShellDaemon> shellDaemonPool = getShellDaemonPool(fusionUser);
    int poolsize = properties.getShellScriptPoolSize();
    ACSShellDaemon acsShellDaemon = null;
    // Check if any shell daemon is available. If not create a new one within poolsize-limit
    for (int i = 0; i < poolsize; i++) {
      if (shellDaemonPool.size() > i) {
        acsShellDaemon = shellDaemonPool.get(i);
        if (acsShellDaemon.isIdle()) {
          break;
        } else {
          acsShellDaemon = null;
        }
      } else {
        acsShellDaemon = createNewShellDaemon(mainDataSource, syslogDataSource, i, fusionUser);
        shellDaemonPool.add(acsShellDaemon);
        break;
      }
    }
    if (acsShellDaemon != null) {
      acsShellDaemon.setIdle(false);
    }
    return acsShellDaemon;
  }
}
