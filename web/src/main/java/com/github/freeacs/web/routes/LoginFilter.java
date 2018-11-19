package com.github.freeacs.web.routes;

import static com.github.freeacs.web.app.util.WebConstants.LOGIN_URI;
import static spark.Spark.halt;

import com.github.freeacs.web.security.ThreadUser;
import java.util.Optional;
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
    if (!request.url().endsWith(LOGIN_URI)
        && !request.url().endsWith("/ok")
        && (session == null || session.attribute("loggedIn") == null)) {
      request
          .session()
          .attribute(
              "redirect",
              request.uri()
                  + Optional.ofNullable(request.queryString()).map(qs -> "?" + qs).orElse(""));
      response.redirect(ctxPath + LOGIN_URI);
      halt();
    } else if (session != null && session.attribute("loggedIn") != null) {
      ThreadUser.setUserDetails(session.attribute("loggedIn"));
    }
  }
}
