package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.github.freeacs.dbi.report.ReportVoipCallGenerator;
import com.github.freeacs.dbi.report.ReportVoipGenerator;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import com.github.freeacs.web.app.util.ACSLoader;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** The Class VoipInterface. */
public class VoipRetriever extends ReportRetriever {
  /** The generator voip. */
  private ReportVoipGenerator generatorVoip;

  /** The generator voip call. */
  private ReportVoipCallGenerator generatorVoipCall;

  /**
   * Instantiates a new voip interface.
   *
   * @param inputData the input data
   * @param params the params
   * @param acs the xaps
   * @throws SQLException the sQL exception the no available connection exception
   */
  public VoipRetriever(ReportData inputData, ParameterParser params, ACS acs) throws SQLException {
    super(inputData, params, acs);
    generatorVoip =
        new ReportVoipGenerator(
            acs.getDataSource(),
            acs,
            null,
            ACSLoader.getIdentity(params.getSession().getId(), acs.getDataSource()));
    generatorVoipCall =
        new ReportVoipCallGenerator(
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
      throws SQLException {
    Report<?> report;
    if (getInputData().getRealtime().getBoolean() || groupSelect != null) {
      if (getInputData().getMethod().hasValue("MosAvg")) {
        report =
            generatorVoipCall.generateFromSyslog(
                periodType, start, end, unittypes, profiles, null, null, groupSelect);
      } else {
        report =
            generatorVoip.generateFromSyslog(
                periodType, start, end, unittypes, profiles, null, groupSelect);
      }
    } else {
      report = generatorVoip.generateFromReport(periodType, start, end, unittypes, profiles);
    }
    return report;
  }

  @Override
  public void applyObjects(Map<String, Object> root) {}

  @Override
  public ReportGenerator getReportGenerator() {
    return generatorVoip;
  }
}
