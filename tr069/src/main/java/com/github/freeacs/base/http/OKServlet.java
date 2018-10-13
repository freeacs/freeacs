package com.github.freeacs.base.http;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.dbi.DBI;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OKServlet extends HttpServlet {

  private static final long serialVersionUID = -3217484543967391741L;
  private static Map<String, Long> currentConnectionTmsMap = new HashMap<String, Long>();

  private final DBAccess dbAccess;

  public OKServlet(DBAccess dbAccess) {
    this.dbAccess = dbAccess;
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    doGet(req, res);
  }

  @SuppressWarnings("rawtypes")
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

    PrintWriter out = res.getWriter();
    StringBuilder status = new StringBuilder("FREEACSOK");
    try {
      DBI dbi = dbAccess.getDBI();
      if (dbi != null && dbi.getDbiThrowable() != null) {
        status = new StringBuilder("ERROR: DBI reported error:\n" + dbi.getDbiThrowable() + "\n");
        for (StackTraceElement ste : dbi.getDbiThrowable().getStackTrace())
          status.append(ste.toString());
      }
    } catch (Throwable ignored) {
    }
    if (!status.toString().contains("ERROR") && ThreadCounter.currentSessionsCount() > 0) {
      Map<String, Long> currentSessions = ThreadCounter.cloneCurrentSessions();
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
        if (currentConnectionTmsMap.get(uId) == null) // new process has been added
        currentConnectionTmsMap.put(uId, System.currentTimeMillis());
      }
    } else {
      currentConnectionTmsMap = new HashMap<String, Long>();
    }
    out.print(status);
    out.close();
  }
}
