package com.github.freeacs.web.app.page.report.custom;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.RecordGroup;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportGenerator;
import com.github.freeacs.dbi.report.ReportGroupGenerator;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.report.ReportData;
import com.github.freeacs.web.app.util.ACSLoader;
import lombok.Getter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
// The Class GroupInterface.
public class GroupRetriever extends ReportRetriever {
  /** The generator. */
  private final ReportGroupGenerator generator;

  /** The groups. */
  private final DropDownSingleSelect<Group> groups;

  /**
   * Instantiates a new group interface.
   *
   * @param inputData the input data
   * @param params the params
   * @param acs the xaps
   * @throws SQLException the sQL exception the no available connection exception
   */
  public GroupRetriever(ReportData inputData, ParameterParser params, ACS acs) throws SQLException {
    super(inputData, params, acs);

    generator = generateGroupGenerator();

    Unittype unittype = null;
    if (inputData.getUnittype().notNullNorValue("")) {
      unittype = acs.getUnittype(inputData.getUnittype().getString());
    }

    List<Group> groupList = new ArrayList<>();
    if (unittype != null) {
      List<Group> _all = Arrays.asList(unittype.getGroups().getGroups());
      groupList.addAll(_all);
    }

    Group group = null;
    if (unittype != null && inputData.getGroup().notNullNorValue("")) {
      group = unittype.getGroups().getByName(inputData.getGroup().getString());
    }

    groups = InputSelectionFactory.getDropDownSingleSelect(inputData.getGroup(), group, groupList);
  }

  /**
   * Generate group generator.
   *
   * @return the report group generator
   * @throws SQLException the sQL exception the no available connection exception
   */
  private ReportGroupGenerator generateGroupGenerator() throws SQLException {
    ACS acs = getAcs();
    return new ReportGroupGenerator(
        acs.getDataSource(),
        acs,
        null,
        ACSLoader.getIdentity(getParams().getSession().getId(), acs.getDataSource()));
  }

  @Override
  public Report<RecordGroup> generateReport(
      PeriodType periodType,
      Date start,
      Date end,
      List<Unittype> unittypes,
      List<Profile> profiles,
      Group group)
      throws SQLException {
    return generator.generateGroupReport(periodType, start, end, unittypes, groups.getSelected());
  }

  @Override
  public void applyObjects(Map<String, Object> root) {
    root.put("groups", groups);
    //		root.put("types", types);
  }

  @Override
  public ReportGenerator getReportGenerator() {
    return generator;
  }
}
