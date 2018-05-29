package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.dao.UnittypeDao;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.vo.UnittypeVO;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.security.AllowedUnittype;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Class UnittypeOverviewPage.
 */
public class UnittypeOverviewPage extends AbstractWebPage {

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#getShortcutItems(com.owera.xaps.web.app.util.SessionData)
	 */
	public List<MenuItem> getShortcutItems(SessionData sessionData) {
		List<MenuItem> list = super.getShortcutItems(sessionData);
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

		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile(), inputData.getUnit());

		List<UnittypeVO> unitTypes = getJdbi().onDemand(UnittypeDao.class).get(getUnitTypeNames(sessionId));
		outputHandler.getTemplateMap().put("unittypes", unitTypes);
		outputHandler.getTemplateMap().put("urltodetails", Page.UNITTYPE.getUrl());
		outputHandler.setTemplatePath("unit-type/list.ftl");
	}

	private List<String> getUnitTypeNames(String sessionId) {
		return Arrays.stream(SessionCache.getSessionData(sessionId).getFilteredUnittypes())
				.map(AllowedUnittype::getName)
				.collect(Collectors.toList());
	}
}
