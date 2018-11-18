package com.github.freeacs.web.routes;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.security.ThreadUser;
import javax.servlet.http.HttpSession;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutRoute implements Route {
  private final String ctxPath;

  public LogoutRoute(String ctxPath) {
    this.ctxPath = ctxPath;
  }

  @Override
  public Object handle(Request request, Response response) {
    HttpSession session = request.raw().getSession(false);
    if (session != null) {
      DBI dbi = SessionCache.getDBI(session.getId());
      if (dbi != null) {
        dbi.setRunning(false);
      }
      SessionCache.removeSession(session.getId());
    }
    response.redirect(ctxPath + "/index");
    request.session().removeAttribute("loggedIn");
    ThreadUser.setUserDetails(null);
    return null;
  }
}
