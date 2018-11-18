package com.github.freeacs.web.routes;

import com.github.freeacs.web.app.Monitor;
import spark.Request;
import spark.Response;
import spark.Route;

public class HealthRoute implements Route {
  private final Monitor monitorServlet = new Monitor();

  @Override
  public Object handle(Request request, Response response) throws Exception {
    monitorServlet.service(request.raw(), response.raw());
    return null;
  }
}
