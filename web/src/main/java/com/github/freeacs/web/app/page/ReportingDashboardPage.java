package com.github.freeacs.web.app.page;

import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.ParameterParser;
import javax.sql.DataSource;

/** The Class ReportingDashboardPage. */
public class ReportingDashboardPage extends AbstractWebPage {
  @Override
  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    outputHandler.setTemplatePath("report/dashboard");
  }
}
