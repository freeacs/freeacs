package com.github.freeacs.web.app.page;

import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.ParameterParser;

import javax.sql.DataSource;


/**
 * The Class ReportingDashboardPage.
 */
public class ReportingDashboardPage extends AbstractWebPage {
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	@Override
	public void process(ParameterParser params, Output outputHandler, DataSource xapsDataSource, DataSource syslogDataSource) throws Exception {
		outputHandler.setTemplatePath("report/dashboard");
	}

}
