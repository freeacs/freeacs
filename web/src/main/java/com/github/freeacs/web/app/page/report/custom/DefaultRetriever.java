package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;

/** The Class DefaultInterface. */
public class DefaultRetriever extends ReportRetriever {
  /**
   * Instantiates a new default interface.
   *
   * @param inputData the input data
   * @param params the params
   * @param acs the xaps
   */
  public DefaultRetriever(ReportData inputData, ParameterParser params, ACS acs) {
    super(inputData, params, acs);
  }

  public DefaultRetriever() {
    super(null, null, null);
  }

  @Override
  public Report<?> generateReport(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> unittypes,
      List<Profile> profiles,
      Group group)
      throws SQLException, IOException {
    throw new NotImplementedException("The report is not implemented correctly.");
  }

  @Override
  public void applyObjects(Map<String, Object> root) {}

  @Override
  public ReportGenerator getReportGenerator() {
    return null;
  }
}
