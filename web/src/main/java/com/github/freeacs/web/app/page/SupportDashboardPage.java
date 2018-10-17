package com.github.freeacs.web.app.page;

import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.ParameterParser;
import javax.sql.DataSource;

/** The Class SupportDashboardPage. */
public class SupportDashboardPage extends AbstractWebPage {
  @Override
  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    outputHandler.setDirectToPage(Page.SEARCH);
  }
}
