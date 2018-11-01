package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.github.freeacs.dbi.report.ReportSyslogGenerator;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import com.github.freeacs.web.app.util.ACSLoader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** The Class SyslogInterface. */
public class SyslogRetriever extends ReportRetriever {
  /** The generator. */
  private ReportSyslogGenerator generator;

  /**
   * Instantiates a new syslog interface.
   *
   * @param inputData the input data
   * @param params the params
   * @param acs the xaps
   * @throws SQLException the sQL exception the no available connection exception
   */
  public SyslogRetriever(ReportData inputData, ParameterParser params, ACS acs)
      throws SQLException {
    super(inputData, params, acs);
    generator =
        new ReportSyslogGenerator(
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
      Group groupSelect)
      throws SQLException, ParseException {
    if (getInputData().getRealtime().getBoolean() || groupSelect != null) {
      return generator.generateFromSyslog(
          periodType, start, end, unittypes, profiles, null, groupSelect);
    } else {
      return generator.generateFromReport(periodType, start, end, unittypes, profiles);
    }
  }

  @Override
  public void applyObjects(Map<String, Object> root) {}

  @Override
  public ReportGenerator getReportGenerator() {
    return generator;
  }
}
