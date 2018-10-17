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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This class represents a report retriever.
 *
 * <p>Each type of report will implement this differently.
 *
 * @author Jarl Andre Hubenthal
 */
public abstract class ReportRetriever {
  /**
   * Gets the input data.
   *
   * @return the input data
   */
  public ReportData getInputData() {
    return inputData;
  }

  /**
   * Gets the params.
   *
   * @return the params
   */
  public ParameterParser getParams() {
    return params;
  }

  /**
   * Gets the xaps.
   *
   * @return the xaps
   */
  public ACS getAcs() {
    return acs;
  }

  /** The input data. */
  private final ReportData inputData;

  /** The params. */
  private final ParameterParser params;

  /** The xaps. */
  private final ACS acs;

  /**
   * Instantiates a new report retriever.
   *
   * @param inputData the input data
   * @param params the params
   * @param acs the xaps
   */
  public ReportRetriever(ReportData inputData, ParameterParser params, ACS acs) {
    this.inputData = inputData;
    this.params = params;
    this.acs = acs;
  }

  /**
   * Apply objects.
   *
   * @param root the root
   */
  public abstract void applyObjects(Map<String, Object> root);

  /**
   * Generate report.
   *
   * @param periodType the period type
   * @param start the start
   * @param end the end
   * @param unittypes the unittypes
   * @param profiles the profiles
   * @return the report
   * @throws Exception the exception
   */
  public abstract Report<?> generateReport(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> unittypes,
      List<Profile> profiles,
      Group groupSelect)
      throws Exception;

  public abstract ReportGenerator getReportGenerator();
}
