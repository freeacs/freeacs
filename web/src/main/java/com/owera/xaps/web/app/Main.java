package com.owera.xaps.web.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.log.Logger;
import com.owera.common.util.Sleep;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.menu.MenuServlet;
import com.owera.xaps.web.app.page.WebPage;
import com.owera.xaps.web.app.security.WebUser;
import com.owera.xaps.web.app.util.Freemarker;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.WebProperties;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * The front controller in xAPS Web.
 * 
 * @author Jarl Andre Hubenthal
 */
public class Main extends HttpServlet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1009523437560499266L;

	/** The logger. */
	private static Logger logger = new Logger();

	/** The Constant version. */
	public static final String version = "2.2.58";

	/** The config. */
	private Configuration config;

	/**
	 * This is the main magic of this web application.
	 * All pages are initialized in this method, reflectively.
	 * 
	 * @param page the page request parameter
	 * @return The initialized WebPage instance
	 */
	private WebPage getWebPage(String page) {
		if (page != null) {
			try {
				Page p = Page.getById(page);
				if (p.getClazz() != null) {
					if (p.getClazz().getConstructors().length > 0 && p.getClazz().getConstructors()[0].getParameterTypes().length == 0)
						return (WebPage) p.getClazz().getConstructors()[0].newInstance();
					throw new RuntimeException("Page " + page + " cannot be instantiated because it has no default constructor.");
				}
				return null;
			} catch (InstantiationException ex) {
				logger.error("Could not instantiate class for page " + page, ex);
			} catch (IllegalAccessException ex) {
				logger.error("Illegal access exception occured while retrieving page instance", ex);
			} catch (IllegalArgumentException ex) {
				logger.error("Illegal argument exception occured while retrieving page instance", ex);
			} catch (SecurityException ex) {
				logger.error("Security exception occured while retrieving page instance", ex);
			} catch (InvocationTargetException ex) {
				logger.error("Invocation target exception occured while retrieving page instance", ex);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String pageStr = req.getParameter("page");
		try {
			doImpl(new ParameterParser(req, getServletContext()), res, pageStr);
		} catch (Exception e) {
			logger.error("An error occured while instantiating ParameterParser", e);
			throw new ServletException(e);
		}
	}

	/**
	 * Usually returns:
	 * 
	 * user@xaps.domain.com
	 *
	 * @param params the params
	 * @return the logged in status title
	 */
	private String getLoggedInStatusTitle(ParameterParser params) {
		try {
			if (params.getHttpServletRequest().getSession() != null) {
				ConnectionProperties cp = SessionCache.getXAPSConnectionProperties(params.getHttpServletRequest().getSession().getId());

				String status = "";
				if (cp != null) {
					String username = null;
					WebUser user = SessionCache.getSessionData(params.getHttpServletRequest().getSession().getId()).getUser();
					if (user != null)
						username = user.getUsername();
					else
						username = "anonymous";
					String url = params.getHttpServletRequest().getServerName();
					status += username + "@" + url;
				} else {
					status = "Not connected to any xAPS database";
				}
				return status;
			}
		} catch (IllegalStateException e) {
		}
		return "Not connected to any xAPS database";
	}

	/**
	 * Overrides the default init
	 * 
	 * Creates a new FreeMarker Configuration instance. Should only be one instance.
	 * 
	 * It is thread safe as long as no changes is done to the Configuration after it has been initialized.
	 */
	public void init() {
		config = Freemarker.initFreemarker();
	}

	/**
	 * The main implementation for the Front Controller
	 * 
	 * <ol>
	 * <li>set and remove the needed syslog connection properties in Context</li>
	 * <li>create the response handler to be used for this response</li>
	 * <li>add menus to the response handler template map</li>
	 * <li>add other variables that instruct the main template< how to function</li>
	 * </ol>.
	 *
	 * @param params the params
	 * @param res the res
	 * @param pageStr the page str
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TemplateException the template exception
	 * @throws ServletException the servlet exception
	 */
	public void doImpl(ParameterParser params, HttpServletResponse res, String pageStr) throws IOException, TemplateException, ServletException {

		WebPage page = getWebPage(pageStr);

		Output outputHandler = new Output(page, params, res, config);

		Map<String, Object> templateMap = outputHandler.getTemplateMap();

		templateMap.put("MAIN_MENU", MenuServlet.getMenuHtml(params.getHttpServletRequest()));
		//		templateMap.put("TOOLS_MENU", new MenuServlet().getToolsMenu(params.getSession().getId(), Page.getSelectedMenuPage(pageStr)));
		templateMap.put("TOOLS_MENU", new MenuServlet().getToolsMenu(params.getSession().getId(), ""));
		//		templateMap.put("SELECTED_MENU_PAGE", Page.getSelectedMenuPage(pageStr));
		templateMap.put("HELP_PAGE", Page.getHelpPage(pageStr));
		templateMap.put("CONFIRMCHANGES", WebProperties.getBoolean("confirmchanges", false));
		templateMap.put("REQUESTED_PAGE", pageStr);
		templateMap.put("IXEDIT_DEVELOPER", WebProperties.getBoolean("ixedit.enabled"));
		templateMap.put("CSS_FILE", WebProperties.getString(WebConstants.DEFAULT_PROPERTIES_KEY, "default"));
		templateMap.put("STATUS_LOGGEDIN", getLoggedInStatusTitle(params));
		templateMap.put("SESSION_TIMEOUT", WebProperties.getSessionTimeout());
		templateMap.put("JAVASCRIPT_DEBUG", Boolean.parseBoolean(WebProperties.getString("javascript.debug", "false")));

		outputHandler.deliverResponse();

	}

	/**
	 * Overrides default destroy
	 * 
	 * Terminates the sleep technique used by owera-common library.
	 */
	public void destroy() {
		Sleep.terminateApplication();
	}
}
