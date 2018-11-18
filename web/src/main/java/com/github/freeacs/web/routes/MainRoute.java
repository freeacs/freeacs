package com.github.freeacs.web.routes;

import static spark.Spark.get;
import static spark.Spark.post;

import com.github.freeacs.web.app.Main;
import javax.sql.DataSource;
import spark.RouteGroup;

public class MainRoute implements RouteGroup {
  private final Main main;

  public MainRoute(DataSource mainDs) {
    this.main = new Main(mainDs, mainDs);
    this.main.init();
  }

  @Override
  public void addRoutes() {
    get(
        "",
        (req, res) -> {
          main.doGet(req.raw(), res.raw());
          return null;
        });
    post(
        "",
        (req, res) -> {
          main.doPost(req.raw(), res.raw());
          return null;
        });
  }
}
