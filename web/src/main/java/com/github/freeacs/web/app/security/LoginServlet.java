package com.github.freeacs.web.app.security;

import com.github.freeacs.dbi.Permission;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.login.LoginPage;
import com.github.freeacs.web.app.security.handlers.Authenticator;
import com.github.freeacs.web.app.security.handlers.DatabaseAuthenticator;
import com.github.freeacs.web.app.util.*;
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
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
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

	private final DataSource xapsDataSource;

	/** The freemarker. */
	private Configuration freemarker;

	/** The login handler. */
	private static Authenticator loginHandler = new DatabaseAuthenticator();
	
	public LoginServlet(DataSource xapsDataSource) {
		this.xapsDataSource = xapsDataSource;
	}

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
	}

	/**
	 * Prints the login page.
	 *
	 * @param response the response
	 * @param sessionData the session data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void printLoginPage(HttpServletResponse response, SessionData sessionData) throws IOException {
		if (freemarker == null)
			freemarker = Freemarker.initFreemarker();

		Template template = freemarker.getTemplate("loginpage.ftl");
		HashMap<String, Object> root = new HashMap<String, Object>();

		root.put("CSS_FILE", WebProperties.PROPERTIES);

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
		if (freemarker == null)
			freemarker = Freemarker.initFreemarker();

		HttpServletRequest request = (HttpServletRequest) req;

		HttpServletResponse response = (HttpServletResponse) res;

		String sessionId = request.getSession().getId();
		SessionData sessionData = SessionCache.getSessionData(sessionId);

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
	 * Checks if is timeout.
	 *
	 * @param req the req
	 * @return true, if is timeout
	 */
	private boolean isTimeout(HttpServletRequest req) {
		int timeoutInMinutes = WebProperties.SESSION_TIMEOUT;

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
		WebUser user = loginHandler.authenticateUser(name, passwd, sessionId, xapsDataSource);
		if (!user.isAuthenticated()) {
			sessionData.setErrorMessage("Your login and password are invalid.");
			return false;
		} else {
			sessionData.setUser(user);
			sessionData.setFilteredUnittypes(retrieveAllowedUnittypes(sessionId));
			return true;
		}
	}

	/**
	 * Retrieve allowed unittypes.
	 *
	 * @param sessionId the session id
	 * @return the allowed unittype[]
	 *  the no available connection exception
	 */
	private AllowedUnittype[] retrieveAllowedUnittypes(String sessionId) {
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
			if (object.equals("index"))
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
	 */
	public void init(FilterConfig filterConfig) {}
}
