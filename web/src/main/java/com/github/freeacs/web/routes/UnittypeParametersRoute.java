package com.github.freeacs.web.routes;

import com.github.freeacs.web.app.page.unittype.UnittypeParametersPage;
import javax.sql.DataSource;
import spark.Request;
import spark.Response;
import spark.Route;

public class UnittypeParametersRoute implements Route {
  private final UnittypeParametersPage unittypeParametersPage;

  public UnittypeParametersRoute(DataSource mainDs) {
    this.unittypeParametersPage = new UnittypeParametersPage();
    this.unittypeParametersPage.setMainDataSource(mainDs);
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    String unittype = request.raw().getParameter("unittype");
    String term = request.raw().getParameter("term");
    return unittypeParametersPage.getUnittypeParameters(unittype, term, request.raw().getSession());
  }
}
