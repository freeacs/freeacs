package com.github.freeacs.base.http;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.dbi.DBI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

@RestController
public class OKServlet {
  private static Map<String, Long> currentConnectionTmsMap = new HashMap<>();

  private final DBAccess dbAccess;
  private final ThreadCounter threadCounter;

  public OKServlet(DBAccess dbAccess, ThreadCounter threadCounter) {
    this.dbAccess = dbAccess;
    this.threadCounter = threadCounter;
  }

  @GetMapping("${context-path}/ok")
  public void doGet(HttpServletResponse res) throws IOException {
    PrintWriter out = res.getWriter();
    StringBuilder status = new StringBuilder("FREEACSOK");
    try {
      DBI dbi = dbAccess.getDbi();
      if (dbi != null && dbi.getDbiThrowable() != null) {
        status =
            new StringBuilder("ERROR: DBI reported error:\n")
                .append(dbi.getDbiThrowable())
                .append("\n");
        for (StackTraceElement ste : dbi.getDbiThrowable().getStackTrace()) {
          status.append(ste);
        }
      }
    } catch (Throwable ignored) {
    }
    if (!status.toString().contains("ERROR") && threadCounter.currentSessionsCount() > 0) {
      Map<String, Long> currentSessions = threadCounter.cloneCurrentSessions();
      Iterator<String> cctmIterator = currentConnectionTmsMap.keySet().iterator();
      while (cctmIterator.hasNext()) {
        String uId = cctmIterator.next();
        if (currentSessions.get(uId) == null) { // the process has been completed
          cctmIterator.remove();
        } else {
          long sessionTime = (System.currentTimeMillis() - currentConnectionTmsMap.get(uId)) / 1000;
          if (sessionTime > 600) { // if a process has not been completed in 600 sec -> problem
            status =
                new StringBuilder(
                    "ERROR: A session may not have been completed for more than 600 seconds. May indicate a hang-situation. Consider restart Tomcat on Fusion server");
            Log.fatal(OKServlet.class, status.toString());
            break;
          }
        }
      }
      for (String uId : currentSessions.keySet()) {
        if (currentConnectionTmsMap.get(uId) == null) {
          currentConnectionTmsMap.put(uId, System.currentTimeMillis());
        }
      }
    } else {
      currentConnectionTmsMap = new HashMap<>();
    }
    out.print(status);
    out.close();
  }
}
