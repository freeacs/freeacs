package com.owera.xaps.web.app.security;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Permission;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.login.LoginPage;
import com.owera.xaps.web.app.security.handlers.Authenticator;
import com.owera.xaps.web.app.util.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;

/**
 * The login servlet is responsible for both the security  filtering and to display the login page.
 * 
 * @author Jarl Andre Hubenthal
 */
public class LoginServlet extends HttpServlet implements Filter {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7478533431699190488L;

	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

	/** The config. */
	private FilterConfig config;

	/** The t config. */
	private Configuration tConfig;

	/** The url map. */
	private HashMap<String, List<String>> urlMap;

	/** The freemarker. */
	private Configuration freemarker;

	/** The login handler. */
	private static Authenticator loginHandler;

	/**
	 * Processes a login, with username and password.
	 * 
	 * Delegates the the user to the previous requested url on successful login, or back to the login page if not.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	private void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NoSuchAlgorithmException {
		String sessionId = request.getSession().getId();
		SessionData sessionData = SessionCache.getSessionData(sessionId);
		if (isConfigured()) {
			String name = request.getParameter("username");
			String passwd = request.getParameter("password");
			if (name != null && passwd != null) {
				try {
					if (isUserAuthenticated(name, passwd, sessionId)) {
						String target = sessionData.getLastLoginTarget();
						if (target == null || target.trim().length() == 0) //Idiot safe solution, to avoid any unknown problems
							target = Page.SEARCH.getUrl();
						response.sendRedirect(target.replaceAll("\\s", "%20"));
					} else {
						printLoginPage(response, sessionData);
					}
				} catch (Exception e) {
					sessionData.setErrorMessage("Error while authenticating: " + e.getLocalizedMessage());
					printLoginPage(response, sessionData);
				}
			} else if (isUserLoggedIn(sessionId))
				response.sendRedirect(Page.SEARCH.getUrl());
			else {
				printLoginPage(response, sessionData);
			}
		} else {
			String target = sessionData.getLastLoginTarget();
			if (target == null || target.trim().length() == 0) { //Idiot safe solution, to avoid any unknown problems
				String s = getRequestURL(request);
				SessionCache.getSessionData(request.getSession().getId()).setLastLoginTarget(s);
				target = Page.SEARCH.getUrl();
			}
			response.sendRedirect(target);
		}
	}

	/**
	 * Prints the login page.
	 *
	 * @param response the response
	 * @param sessionData the session data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void printLoginPage(HttpServletResponse response, SessionData sessionData) throws IOException {
		if (tConfig == null)
			tConfig = Freemarker.initFreemarker();

		Template template = tConfig.getTemplate("loginpage.ftl");
		HashMap<String, Object> root = new HashMap<String, Object>();

		root.put("CSS_FILE", WebProperties.getString(WebConstants.DEFAULT_PROPERTIES_KEY, "default"));

		if (loginHandler != null)
			root.put("scramblePassword", loginHandler.scramblePasswordWithMD5());

		String msg = sessionData.getErrorMessage();
		root.put("message", msg);
		sessionData.setErrorMessage(null);

		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");
		PrintWriter writer = response.getWriter();
		try {
			writer.println(Freemarker.parseTemplate(root, template));
		} catch (TemplateException e) {
			logger.error("Error while parsing loginpage.ftl: " + e.getLocalizedMessage(), e);
			writer.println(e);
		} finally {
			writer.flush(); // To avoid any unwritten data being left out + unknown problems
			writer.close();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doRequest(request, response);
		} catch (NoSuchAlgorithmException e) {
			response.getWriter().println(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doRequest(request, response);
		} catch (NoSuchAlgorithmException e) {
			response.getWriter().println(e);
		}
	}

	/**
	 * For every page requested this function is called. It checks wether or not it should check user rights and if the requested page is allowed for the current user.
	 *
	 * @param req the req
	 * @param res the res
	 * @param chain the chain
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServletException the servlet exception
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (config == null)
			return;

		HttpServletRequest sReq = (HttpServletRequest) req;

		if (SessionCache.getXAPSConnectionProperties(sReq.getSession().getId()) == null) {
			retrieveAndSetConnectionProperties(sReq);
		}

		/**
		 * If security is not configured, filter everything through.
		 * Remember the currently accessed page by storing it in sessionData.
		 * If there are no filtered unittypes add wildcard for allowed unittypes (ACCEPT ALL FIRST, THEN RESTRICT)
		 */
		if (!isConfigured()) {
			SessionData sessionData = SessionCache.getSessionData(sReq.getSession().getId());

			/**
			 * If page parameter is not null and "login", and connection properties is not set,
			 * we then retrieve and remember the current request URL.
			 */
			if (req.getParameter("page") != null && !req.getParameter("page").equals("login") && SessionCache.getXAPSConnectionProperties(sReq.getSession().getId()) == null) {
				String lastRequestedPage = getRequestURL(sReq);
				sessionData.setLastLoginTarget(lastRequestedPage);
			}

			if (sessionData.getFilteredUnittypes() == null) {
				sessionData.setFilteredUnittypes(new AllowedUnittype[] { new AllowedUnittype("*") });
			}

			chain.doFilter(req, res);
			return;
		}

		if (freemarker == null)
			freemarker = Freemarker.initFreemarker();

		HttpServletRequest request = (HttpServletRequest) req;

		HttpServletResponse response = (HttpServletResponse) res;

		String sessionId = request.getSession().getId();
		SessionData sessionData = SessionCache.getSessionData(sessionId);

		sessionData.setUrlMap(urlMap);

		WebUser user = sessionData.getUser();

		String page = request.getParameter("page");

		if (user != null && page != null) {
			List<String> allowedPages = user.getAllowedPages(sessionId);
			if (allowedPages.contains(page) || (allowedPages.size() == 1 && allowedPages.contains(WebConstants.ALL_PAGES))) {
				if (checkTimeoutAndReturn(request, response))
					return;
				chain.doFilter(req, res);
			} else {
				sessionData.setLastLoginTarget(null);
				Template t = freemarker.getTemplate("templates/"+ "errorpage.ftl");
				Map<String, String> root = new HashMap<String, String>();
				root.put("message", "You are not allowed to access<br />the " + page + " page." + "<br /><a href='" + Page.SEARCH.getUrl()
						+ "'>Go to the search page</a><br />or hit the browser back button.");
				PrintWriter out = response.getWriter();
				try {
					out.println(Freemarker.parseTemplate(root, t));
				} catch (Exception e) {
					out.println(e);
				} finally {
					out.flush();
					out.close();
				}
				return;
			}
		} else {
			if (request.getParameter("username") == null && request.getParameter("password") == null) {
				if (request.getParameter("return") != null) {
					String target = request.getParameter("return");
					if (!target.startsWith("/"))
						target = "/" + target;
					sessionData.setLastLoginTarget(target);
				} else {
					String target = getRequestURL(request);
					sessionData.setLastLoginTarget(target);
				}
			}
			response.sendRedirect("login");
			return;
		}
	}

	/**
	 * Retrieve and set connection properties.
	 *
	 * @param req the req
	 */
	private static void retrieveAndSetConnectionProperties(HttpServletRequest req) {
		ConnectionProperties xapsCp = LoginPage.getXAPSConnectionProperties();
		ConnectionProperties syslogCp = LoginPage.getSyslogConnectionProperties();
		if (xapsCp != null && xapsCp.getUrl() != null)
			SessionCache.putXAPSConnectionProperties(req.getSession().getId(), xapsCp);

		if (syslogCp != null && syslogCp.getUrl() != null)
			SessionCache.putSyslogConnectionProperties(req.getSession().getId(), syslogCp);
		else
			SessionCache.putSyslogConnectionProperties(req.getSession().getId(), xapsCp);
	}

	/**
	 * Checks if is timeout.
	 *
	 * @param req the req
	 * @return true, if is timeout
	 */
	private boolean isTimeout(HttpServletRequest req) {
		int timeoutInMinutes = WebProperties.getSessionTimeout();

		Date lastAccessed = SessionCache.getSessionData(req.getSession().getId()).getLastAccessed();
		if (lastAccessed == null)
			return false;

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(lastAccessed.getTime());
		cal.add(Calendar.MINUTE, timeoutInMinutes);
		lastAccessed = new Date(cal.getTimeInMillis());

		Date current = new Date(System.currentTimeMillis());

		return current.after(lastAccessed);
	}

	/**
	 * Update timeout.
	 *
	 * @param req the req
	 */
	private void updateTimeout(HttpServletRequest req) {
		SessionData sessionData = SessionCache.getSessionData(req.getSession().getId());
		sessionData.setLastAccessed(new Date(System.currentTimeMillis()));
	}

	/**
	 * Checks if is user logged in.
	 *
	 * @param sessionId the session id
	 * @return true, if is user logged in
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private boolean isUserLoggedIn(String sessionId) throws IOException {
		SessionData sessionData = SessionCache.getSessionData(sessionId);
		if (sessionData.getUser() == null)
			return false;
		return true;
	}

	/**
	 * Checks if is user authenticated.
	 *
	 * @param name the name
	 * @param passwd the passwd
	 * @param sessionId the session id
	 * @return true, if is user authenticated
	 * @throws Exception the exception
	 */
	private boolean isUserAuthenticated(String name, String passwd, String sessionId) throws Exception {
		SessionData sessionData = SessionCache.getSessionData(sessionId);
		name = name.toLowerCase();
		logger.debug("Will check if " + name + "/" + passwd + " is authenticated using " + loginHandler.getClass() + " class");
		WebUser user = loginHandler.authenticateUser(name, passwd, sessionId);

		if (!user.isAuthenticated()) {
			sessionData.setErrorMessage("Your login and password are invalid.");
			return false;
		} else {
			sessionData.setUser(user);
			sessionData.setFilteredUnittypes(retrieveAllowedUnittypes(name, sessionId));
			return true;
		}
	}

	/**
	 * Retrieve allowed unittypes.
	 *
	 * @param user the user
	 * @param sessionId the session id
	 * @return the allowed unittype[]
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	private AllowedUnittype[] retrieveAllowedUnittypes(String user, String sessionId) throws NoAvailableConnectionException, SQLException {
		WebUser usr = SessionCache.getSessionData(sessionId).getUser();
		List<AllowedUnittype> uts = new ArrayList<AllowedUnittype>();
		if (usr.getPermissions() != null) {
			if (usr.getPermissions().getPermissions().length == 0)
				return new AllowedUnittype[] { new AllowedUnittype("*") };
			for (Permission permission : usr.getPermissions().getPermissions()) {
				AllowedUnittype allowed = new AllowedUnittype(permission.getUnittypeId());
				allowed.setProfile(permission.getProfileId());
				uts.add(allowed);
			}
			return uts.toArray(new AllowedUnittype[] {});
		} else {
			uts.add(new AllowedUnittype("*"));
			return uts.toArray(new AllowedUnittype[] {});
		}
	}

	/**
	 * Is xAPS Web configured to use a login handler?.
	 *
	 * @return A boolean saying if a login handler is set.
	 */
	public boolean isConfigured() {
		if (loginHandler != null)
			return true;

		try {
			ClassLoader classLoader = LoginServlet.class.getClassLoader();
			Class<?> handler = classLoader.loadClass(getCamelCasedHandler("Database"));
			loginHandler = (Authenticator) handler.newInstance();
			return true;
		} catch (Exception e) {
			logger.error("Could not initialize login handler", e);
			return false;
		}
	}

	/**
	 * Gets the camel cased handler.
	 *
	 * @param loginAuth the login auth
	 * @return the camel cased handler
	 */
	private String getCamelCasedHandler(String loginAuth) {
		String handlerString = "com.owera.xaps.web.app.security.handlers.%HANDLER%Authenticator";
		if (loginAuth == null || loginAuth.length() < 2)
			return null;
		loginAuth = loginAuth.substring(0, 1).toUpperCase() + loginAuth.substring(1).toLowerCase();
		handlerString = handlerString.replace("%HANDLER%", loginAuth);
		return handlerString;
	}

	/**
	 * Checks if the session has timed out. Redirects to login page if so.
	 *
	 * @param request the request
	 * @param response the response
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private boolean checkTimeoutAndReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
		boolean redirectAndReturn = false;
		if (isTimeout(request)) {
			try {
				LoginPage.clearSession(new ParameterParser(request));
			} catch (FileUploadException e) {
				logger.warn("An error occured while instantiating ParameterParser", e);
			}
			redirectAndReturn = true;
		}
		if (redirectAndReturn)
			response.sendRedirect(Page.SEARCH.getUrl());
		else
			updateTimeout(request);
		return redirectAndReturn;
	}

	/**
	 * Gets the request url.
	 *
	 * @param request the request
	 * @return the request url
	 */
	private String getRequestURL(HttpServletRequest request) {
		StringBuffer target = request.getRequestURL();
		Enumeration<?> parms = request.getParameterNames();
		if (parms.hasMoreElements())
			target.append("?");
		while (parms.hasMoreElements()) {
			String object = (String) parms.nextElement();
			if (((String) object).equals("index"))
				continue;
			target.append(object).append("=").append(request.getParameter(object));
			if (parms.hasMoreElements())
				target.append("&");
		}
		return target.toString();
	}

	/**
	 * Filter init method.
	 *
	 * @param filterConfig the filter config
	 * @throws ServletException the servlet exception
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		SessionCache.CONTEXT_PATH = "";
		config = filterConfig;
	}
}
