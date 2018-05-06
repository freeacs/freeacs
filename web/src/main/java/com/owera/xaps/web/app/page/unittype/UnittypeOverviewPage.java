package com.owera.xaps.web.app.page.unittype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.InputDataIntegrity;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.menu.MenuItem;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.SessionData;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

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
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		UnittypeData inputData = (UnittypeData) InputDataRetriever.parseInto(new UnittypeData(), params);

		String sessionId = params.getSession().getId();

		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}
		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile(), inputData.getUnit());

		List<Unittype> unittypes = Arrays.asList(xaps.getUnittypes().getUnittypes());
		outputHandler.getTemplateMap().put("unittypes", unittypes);
		outputHandler.getTemplateMap().put("urltodetails", Page.UNITTYPE.getUrl());
		outputHandler.setTemplatePath("unit-type/list.ftl");
	}
}
