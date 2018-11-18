package com.github.freeacs.web.routes;

import static spark.Spark.halt;

import com.github.freeacs.web.security.ThreadUser;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Session;

public class LoginFilter implements Filter {
  private final String ctxPath;

  public LoginFilter(String ctxPath) {
    this.ctxPath = ctxPath;
  }

  @Override
  public void handle(Request request, Response response) {
    ThreadUser.setUserDetails(null);
    Session session = request.session(false);
    if (!request.url().endsWith("/login")
        && !request.url().endsWith("/ok")
        && (session == null || session.attribute("loggedIn") == null)) {
      response.redirect(ctxPath + "/login");
      halt();
    } else if (session != null && session.attribute("loggedIn") != null) {
      ThreadUser.setUserDetails(session.attribute("loggedIn"));
    }
  }
}
