package com.github.freeacs.web.app.page.staging;

import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.security.WebUser;
import com.github.freeacs.web.app.util.SessionCache;

import java.util.List;
import java.util.Map;



/**
 * The Class StagingPage.
 */
public class StagingPage extends AbstractWebPage {
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		Map<String, Object> root = outputHandler.getTemplateMap();
		
		String sessionId = params.getSession().getId();
		WebUser user = SessionCache.getSessionData(sessionId).getUser();
		List<String> allowedPages = user.getAllowedPages(sessionId);
		boolean displayDistributorLink = (user!=null&&user.getAccess().equals(Users.ACCESS_ADMIN))||(user!=null && allowedPages!=null && allowedPages.contains("distributors"));
		root.put("ADMIN", displayDistributorLink);
		
		outputHandler.setTemplatePathWithIndex("staging");
	}

}
