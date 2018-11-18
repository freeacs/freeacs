package com.github.freeacs.web.routes;

import com.github.freeacs.web.help.HelpServlet;
import spark.Request;
import spark.Response;
import spark.Route;

public class HelpRoute implements Route {
  private final HelpServlet helpServlet = new HelpServlet();

  @Override
  public Object handle(Request request, Response response) throws Exception {
    helpServlet.service(request.raw(), response.raw());
    return null;
  }
}
