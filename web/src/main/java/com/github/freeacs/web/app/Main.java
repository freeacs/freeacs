package com.github.freeacs.web.app;

import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuServlet;
import com.github.freeacs.web.app.page.WebPage;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.WebProperties;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * The front controller in xAPS Web.
 * 
 * @author Jarl Andre Hubenthal
 */
public class Main extends HttpServlet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1009523437560499266L;

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(Main.class);

	/** The Constant version. */
	public static final String version = "2.2.58";

	/** The config. */
	private Configuration config;

	private final DataSource xapsDataSource, syslogDataSource;

	public Main(DataSource xapsDataSource, DataSource syslogDataSource) {
		this.xapsDataSource = xapsDataSource;
		this.syslogDataSource = syslogDataSource;
	}

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
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException {
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
		String username = SessionCache.getSessionData(params.getHttpServletRequest().getSession().getId()).getUser().getUsername();
		String url = params.getHttpServletRequest().getServerName();
		return username + "@" + url;
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

		Output outputHandler = new Output(page, params, res, config, xapsDataSource, syslogDataSource);

		Map<String, Object> templateMap = outputHandler.getTemplateMap();

		templateMap.put("MAIN_MENU", MenuServlet.getMenuHtml(params.getHttpServletRequest()));
		templateMap.put("TOOLS_MENU", new MenuServlet().getToolsMenu(params.getSession().getId(), ""));
		templateMap.put("HELP_PAGE", Page.getHelpPage(pageStr));
		templateMap.put("CONFIRMCHANGES", WebProperties.CONFIRM_CHANGES);
		templateMap.put("REQUESTED_PAGE", pageStr);
		templateMap.put("IXEDIT_DEVELOPER", WebProperties.IX_EDIT_ENABLED);
		templateMap.put("CSS_FILE", WebProperties.PROPERTIES);
		templateMap.put("STATUS_LOGGEDIN", getLoggedInStatusTitle(params));
		templateMap.put("SESSION_TIMEOUT", WebProperties.SESSION_TIMEOUT);
		templateMap.put("JAVASCRIPT_DEBUG", WebProperties.JAVASCRIPT_DEBUG);

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
