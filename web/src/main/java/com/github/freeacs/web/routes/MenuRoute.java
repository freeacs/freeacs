package com.github.freeacs.web.routes;

import com.github.freeacs.web.app.menu.MenuServlet;
import freemarker.template.Configuration;
import spark.Request;
import spark.Response;
import spark.Route;

public class MenuRoute implements Route {
  private final MenuServlet menuServlet;

  public MenuRoute(Configuration configuration) {
    menuServlet = new MenuServlet(configuration);
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    menuServlet.service(request.raw(), response.raw());
    return null;
  }
}
