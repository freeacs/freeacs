package com.github.freeacs.web.routes;

import static spark.Spark.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.web.app.page.unit.UnitStatusPage;
import freemarker.template.Configuration;
import java.util.Map;
import javax.sql.DataSource;
import spark.ModelAndView;
import spark.RouteGroup;
import spark.template.freemarker.FreeMarkerEngine;

public class UnitDashboardRoute implements RouteGroup {
  private final Configuration configuration;
  private final ObjectMapper objectMapper;
  private final UnitStatusPage unitStatusPage;

  public UnitDashboardRoute(
      Configuration configuration, ObjectMapper objectMapper, DataSource mainDs) {
    this.unitStatusPage = new UnitStatusPage();
    this.unitStatusPage.setMainDataSource(mainDs);
    this.configuration = configuration;
    this.objectMapper = objectMapper;
  }

  @Override
  public void addRoutes() {
    get(
        "/linesup",
        (req, res) -> {
          String unitId = req.raw().getParameter("unitId");
          Map<String, Boolean> result =
              unitStatusPage.getLineStatus(unitId, req.raw().getSession());
          return objectMapper.writeValueAsString(result);
        });
    get(
        "/chartimage",
        (req, res) -> {
          String pageType = req.raw().getParameter("type");
          String periodType = req.raw().getParameter("period");
          String method = req.raw().getParameter("method");
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          String syslogFilter = req.raw().getParameter("syslogFilter");
          String[] aggregations = req.raw().getParameterValues("aggregate");
          unitStatusPage.getChartImage(
              pageType,
              periodType,
              method,
              startTms,
              endTms,
              unitId,
              syslogFilter,
              aggregations,
              res.raw(),
              req.raw().getSession());
          return null;
        });
    get(
        "/charttable",
        (req, res) -> {
          String pageType = req.raw().getParameter("type");
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          String syslogFilter = req.raw().getParameter("syslogFilter");
          ModelAndView modelAndView =
              unitStatusPage.getChartTable(
                  pageType,
                  startTms,
                  endTms,
                  unitId,
                  syslogFilter,
                  req.raw(),
                  req.raw().getSession());
          return new FreeMarkerEngine(configuration).render(modelAndView);
        });
    get(
        "/totalscore-effect",
        (req, res) -> {
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          Map<String, Object> result =
              unitStatusPage.getTotalScoreEffect(startTms, endTms, unitId, req.raw().getSession());
          return objectMapper.writeValueAsString(result);
        });
    get(
        "/totalscore-number",
        (req, res) -> {
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          return unitStatusPage.getTotalScoreNumber(
              startTms, endTms, unitId, req.raw().getSession());
        });
    get(
        "/overallstatus",
        (req, res) -> {
          String startTms = req.raw().getParameter("start");
          String endTms = req.raw().getParameter("end");
          String unitId = req.raw().getParameter("unitId");
          unitStatusPage.getOverallStatusSpeedometer(
              startTms, endTms, unitId, res.raw(), req.raw().getSession());
          return null;
        });
  }
}
