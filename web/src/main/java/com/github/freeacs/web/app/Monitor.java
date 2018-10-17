package com.github.freeacs.web.app;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.web.app.util.SessionCache;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Responds to incoming requests from xAPS Monitor Server.
 *
 * <p>Just a (very) simple servlet that returns FREEACSOK and the version number.
 *
 * @author Jarl Andre Hubenthal
 */
@SuppressWarnings("serial")
public class Monitor extends HttpServlet {
  private static Throwable lastDBILogin;

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    PrintWriter out = res.getWriter();
    Cache cache = SessionCache.getCache();
    Map<Object, CacheValue> map = cache.getMap();
    StringBuilder status = new StringBuilder("FREEACSOK");
    for (Map.Entry<Object, CacheValue> entry : map.entrySet()) {
      Object o = entry.getKey();
      if (o instanceof String) {
        String s = (String) o;
        if (s.endsWith("dbi")) {
          // We've found a DBI-object in the cache
          DBI dbi = (DBI) entry.getValue().getObject();
          if (dbi != null && dbi.getDbiThrowable() != null) {
            status =
                new StringBuilder("ERROR: DBI reported error:\n")
                    .append(dbi.getDbiThrowable())
                    .append("\n");
            for (StackTraceElement ste : dbi.getDbiThrowable().getStackTrace()) {
              status.append(ste);
            }
          }
        }
      }
    }
    if (lastDBILogin != null) {
      status = new StringBuilder("ERROR: DBI reported error:\n").append(lastDBILogin).append("\n");
      for (StackTraceElement ste : lastDBILogin.getStackTrace()) {
        status.append(ste);
      }
    }
    out.println(status.toString());
    out.close();
  }

  public static void setLastDBILogin(Throwable lastDBILogin) {
    Monitor.lastDBILogin = lastDBILogin;
  }
}
