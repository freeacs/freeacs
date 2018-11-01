package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.github.freeacs.dbi.report.ReportHardwareGenerator;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import com.github.freeacs.web.app.util.ACSLoader;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** The Class HardwareInterface. */
public class HardwareRetriever extends ReportRetriever {
  /** The generator. */
  private ReportHardwareGenerator generator;

  /**
   * Instantiates a new hardware interface.
   *
   * @param inputData the input data
   * @param params the params
   * @param acs the xaps
   * @throws SQLException the sQL exception the no available connection exception
   */
  public HardwareRetriever(ReportData inputData, ParameterParser params, ACS acs)
      throws SQLException {
    super(inputData, params, acs);
    generator =
        new ReportHardwareGenerator(
            acs.getDataSource(),
            acs,
            null,
            ACSLoader.getIdentity(params.getSession().getId(), acs.getDataSource()));
  }

  @Override
  public Report<?> generateReport(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> unittypes,
      List<Profile> profiles,
      Group group)
      throws SQLException {
    if (group != null) {
      return generator.generateFromSyslog(periodType, start, end, unittypes, profiles, null, group);
    }
    return generator.generateFromReport(periodType, start, end, unittypes, profiles);
  }

  @Override
  public void applyObjects(Map<String, Object> root) {}

  @Override
  public ReportGenerator getReportGenerator() {
    return generator;
  }
}
