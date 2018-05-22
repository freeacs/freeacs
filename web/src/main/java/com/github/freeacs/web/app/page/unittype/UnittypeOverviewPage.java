package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.XAPSLoader;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class UnittypeOverviewPage.
 */
public class UnittypeOverviewPage extends AbstractWebPage {

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#getShortcutItems(com.owera.xaps.web.app.util.SessionData)
	 */
	public List<MenuItem> getShortcutItems(SessionData sessionData) {
		List<MenuItem> list = new ArrayList<MenuItem>();
		list.addAll(super.getShortcutItems(sessionData));
		list.add(new MenuItem("Create new Unit Type", Page.UNITTYPECREATE));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	@Override
	public void process(ParameterParser params, Output outputHandler, DataSource xapsDataSource, DataSource syslogDataSource) throws Exception {
		UnittypeData inputData = (UnittypeData) InputDataRetriever.parseInto(new UnittypeData(), params);

		String sessionId = params.getSession().getId();

		ACS acs = XAPSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
		if (acs == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}
		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile(), inputData.getUnit());

		List<Unittype> unittypes = Arrays.asList(acs.getUnittypes().getUnittypes());
		outputHandler.getTemplateMap().put("unittypes", unittypes);
		outputHandler.getTemplateMap().put("urltodetails", Page.UNITTYPE.getUrl());
		outputHandler.setTemplatePath("unit-type/list.ftl");
	}
}
