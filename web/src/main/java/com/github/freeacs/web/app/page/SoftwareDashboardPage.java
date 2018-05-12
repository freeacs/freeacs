package com.github.freeacs.web.app.page;

import com.github.freeacs.web.app.input.ParameterParser;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.ParameterParser;


/**
 * The Class SoftwareDashboardPage.
 */
public class SoftwareDashboardPage  extends AbstractWebPage {
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	@Override
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		outputHandler.setTemplatePath("firmware/dashboard");
	}

}
