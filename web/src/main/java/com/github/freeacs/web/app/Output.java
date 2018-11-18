package com.github.freeacs.web.app;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.context.ContextItem;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.WebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.StackTraceFormatter;
import com.github.freeacs.web.app.util.WebProperties;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for redirecting from and writing data to the servlet response. <br>
 * This logic was earlier located in the {@link Main} servlet. <br>
 * Was moved here because the Main servlet was too cluttered and difficult to read.
 *
 * @author Jarl Andre Hubenthal
 */
public class Output {
  private static final String INCLUDE_TEMPLATE_KEY = "INCLUDED_TEMPLATE";
  private final DataSource xapsDataSource;
  private final DataSource syslogDataSource;
  private HttpServletResponse servletResponseChannel;
  private Configuration freemarkerConfig;
  private static final String defaultTemplatePath = "/index.ftl";
  private String templatePathString;
  private Map<String, Object> templateMap = new HashMap<>();
  private WebPage currentPage;
  private ParameterParser inputParameters;
  private static final Logger logger = LoggerFactory.getLogger(Output.class);
  private String redirectToUrl;
  private String directResponseString;
  private String contentType = "text/html";
  private Boolean responseCommitted = false;
  private Boolean delivered = false;
  private ContextItem trailPoint;

  /**
   * Instantiates a new output handler.
   *
   * @param page the page
   * @param params the params
   * @param res the res
   * @param config the config
   * @param xapsDataSource
   * @param syslogDataSource
   */
  public Output(
      WebPage page,
      ParameterParser params,
      HttpServletResponse res,
      Configuration config,
      DataSource xapsDataSource,
      DataSource syslogDataSource) {
    this.servletResponseChannel = res;
    this.freemarkerConfig = config;
    this.inputParameters = params;
    this.currentPage = page;
    this.xapsDataSource = xapsDataSource;
    this.syslogDataSource = syslogDataSource;
  }

  /**
   * Deliver the response. To avoid thread problems we synchronize the use of this object within
   * this method.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  public void deliverResponse() throws IOException, ServletException {
    synchronized (this) {
      if (this.delivered) {
        return;
      }

      this.delivered = true;

      String pageContents = null;

      try {
        if (currentPage == null) {
          redirect(Page.SEARCH.getUrl(), servletResponseChannel);
          return;
        }

        processContextBarUpdates();

        currentPage.process(inputParameters, this, xapsDataSource, syslogDataSource);

        if (isResponseCommitted()) {
          return;
        }

        if (redirectToUrl != null) {
          redirect(redirectToUrl, servletResponseChannel);
          return;
        }
        if (currentPage.requiresNoCache()) {
          addNoCacheToResponse();
        }

        if (getDirectResponseString() != null) {
          printPartialPage(
              getDirectResponseString(), currentPage, inputParameters, servletResponseChannel);
          return;
        }

        populateTemplateMapWithContextBar();

        // If async parameter is true, only parse the page template, and then deliver the partial
        // response
        if (inputParameters.getBoolean("async")) {
          deliverPartially();
          return;
        }

        Template mainTemplate = freemarkerConfig.getTemplate(getDefaultTemplatePath());

        pageContents = Freemarker.parseTemplate(getTemplateMap(), mainTemplate);
      } catch (Throwable allPossibleExceptions) {
        allPossibleExceptions.printStackTrace();
        pageContents =
            processExceptionTemplate(
                new Exception(allPossibleExceptions), WebProperties.getInstance().isDebug());
      } finally {
        deliverHTML(pageContents, inputParameters, servletResponseChannel);
      }
    }
  }

  private void deliverPartially() throws TemplateException, IOException, ServletException {
    getTemplateMap().put("async", true);
    String content =
        Freemarker.parseTemplate(templateMap, freemarkerConfig.getTemplate(getTemplatePath()));
    printPartialPage(content, currentPage, inputParameters, servletResponseChannel);
  }

  private void populateTemplateMapWithContextBar() throws SQLException {
    ACS acs =
        ACSLoader.getXAPS(inputParameters.getSession().getId(), xapsDataSource, syslogDataSource);
    Unittype currentUnittype =
        acs.getUnittype(trailPoint != null ? trailPoint.getUnitTypeName() : null);
    Input utInput = Input.getStringInput("unittype");
    if (currentUnittype != null) {
      utInput.setValue(currentUnittype.getName());
    }
    templateMap.put("UNITTYPE_DROPDOWN", InputSelectionFactory.getUnittypeSelection(utInput, acs));
    Input prInput = Input.getStringInput("profile");
    if (currentUnittype != null && trailPoint != null && trailPoint.getProfileName() != null) {
      Profile currentProfile = currentUnittype.getProfiles().getByName(trailPoint.getProfileName());
      if (currentProfile != null) {
        prInput.setValue(currentProfile.getName());
      }
    }
    templateMap.put(
        "PROFILE_DROPDOWN", InputSelectionFactory.getProfileSelection(prInput, utInput, acs));
    Input grInput = Input.getStringInput("group");
    templateMap.put(
        "GROUP_DROPDOWN", InputSelectionFactory.getGroupSelection(grInput, currentUnittype, acs));
    Input jInput = Input.getStringInput("job");
    templateMap.put(
        "JOB_DROPDOWN",
        InputSelectionFactory.getDropDownSingleSelect(
            jInput,
            null,
            Arrays.asList(
                currentUnittype != null ? currentUnittype.getJobs().getJobs() : new Job[] {})));

    templateMap.put("CONTEXT_ITEM", trailPoint);
    boolean isSpecific = ContextItem.isSpecific(trailPoint);
    templateMap.put("CONTEXT_SPECIFIC", isSpecific);
    templateMap.put(INCLUDE_TEMPLATE_KEY, getTemplatePath());
    List<MenuItem> shortcutList = currentPage.getShortcutItems(inputParameters.getSessionData());
    templateMap.put("SHORTCUTS", shortcutList);
    String title = currentPage.getTitle(inputParameters.getParameter("page"));
    templateMap.put("TITLE", title);
    Page current = Page.getById(inputParameters.getParameter("page"));
    templateMap.put("CURRENT_PAGE", current);
    Map<String, Page> pageMap = Page.getPageURLMap();
    templateMap.put("URL_MAP", pageMap);
  }

  /**
   * Processes the requested changes from the context bar.
   *
   * @throws IOException
   * @throws SQLException
   */
  private void processContextBarUpdates() throws IOException, SQLException {
    String cUT = inputParameters.getStringParameter("contextunittype");
    if (cUT != null) {
      if ("All".equals(cUT)) {
        cUT = null;
      }
      inputParameters.getSessionData().setUnittypeName(cUT);
      inputParameters.getSessionData().setProfileName(null);
      inputParameters.getHttpServletRequest().ignoreParameter("unittype");
    }
    String cPR = inputParameters.getStringParameter("contextprofile");
    if (cPR != null) {
      if ("All".equals(cPR)) {
        cPR = null;
      }
      inputParameters.getSessionData().setProfileName(cPR);
      inputParameters.getHttpServletRequest().ignoreParameter("profile");
    }
    String cU = inputParameters.getStringParameter("contextunit");
    if (cU != null) {
      inputParameters.getSessionData().setUnitId(cU);
      inputParameters.getHttpServletRequest().ignoreParameter("unit");
      String newValue = inputParameters.getSessionData().getUnitId();
      ACSUnit xaps =
          ACSLoader.getACSUnit(
              inputParameters.getSession().getId(), xapsDataSource, syslogDataSource);
      Unit unit = xaps.getUnitById(newValue);
      if (unit != null) {
        redirect(
            Page.UNITSTATUS.getUrl(
                "unit="
                    + unit.getId()
                    + "&unittype="
                    + unit.getUnittype().getName()
                    + "&profile="
                    + unit.getProfile().getName()),
            servletResponseChannel);
      } else {
        redirect(
            Page.SEARCH.getUrl("unitparamvalue=" + cU + "&formsubmit=Search"),
            servletResponseChannel);
      }
      return;
    }
    if (inputParameters.getStringParameter("contextgroup") != null) {
      String cGR = inputParameters.getStringParameter("contextgroup");
      if ("All".equals(cGR)) {
        cGR = null;
      }
      inputParameters.getSessionData().setGroup(cGR);
      inputParameters.getHttpServletRequest().ignoreParameter("group");
    }
    if (inputParameters.getStringParameter("contextjob") != null) {
      String cJB = inputParameters.getStringParameter("contextjob");
      if ("All".equals(cJB)) {
        cJB = null;
      }
      inputParameters.getSessionData().setJobname(cJB);
      inputParameters.getHttpServletRequest().ignoreParameter("job");
    }
  }

  /**
   * <meta http-equiv="PRAGMA" content="NO-CACHE" /> <meta http-equiv="CACHE-CONTROL"
   * content="NO-STORE, NO-CACHE, MUST-REVALIDATE, POST-CHECK=0, PRE-CHECK=0" /> <meta
   * http-equiv="EXPIRES" content="01 Jan 1970 00:00:00 GMT" /> <meta http-equiv="Last-Modified"
   * content="01 Jan 1970 00:00:00 GMT" /> <meta http-equiv="If-Modified-Since" content="01 Jan 1970
   * 00:00:00 GMT" />.
   */
  public void addNoCacheToResponse() {
    servletResponseChannel.setHeader("Pragma", "no-cache");
    servletResponseChannel.setHeader(
        "Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
    servletResponseChannel.setDateHeader("Expires", 0);
    servletResponseChannel.setDateHeader("Last-Modified", 0);
    servletResponseChannel.setDateHeader("If-Modified-Since", 0);
  }

  /**
   * Sets the redirect target.
   *
   * @param url the new redirect target
   */
  public void setRedirectTarget(String url) {
    this.redirectToUrl = url;
  }

  /**
   * Sets the direct response.
   *
   * @param response the new direct response
   */
  public void setDirectResponse(String response) {
    this.directResponseString = response;
  }

  /**
   * Compile template.
   *
   * @param template the template
   * @return the string
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String compileTemplate(String template) throws TemplateException, IOException {
    return Freemarker.parseTemplate(templateMap, freemarkerConfig.getTemplate(template));
  }

  /**
   * Gets the servlet response.
   *
   * @return the servlet response
   */
  public HttpServletResponse getServletResponse() {
    return servletResponseChannel;
  }

  /**
   * Write image bytes to response.
   *
   * @param image the image
   * @param servletResponseChannel the servlet response channel
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void writeImageBytesToResponse(
      byte[] image, HttpServletResponse servletResponseChannel) throws IOException {
    servletResponseChannel.setContentType("image/png");
    appendNoCache(servletResponseChannel);
    writeBytesToResponse(image, servletResponseChannel);
  }

  /**
   * Write bytes to response.
   *
   * @param image the image
   * @param servletResponseChannel the servlet response channel
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void writeBytesToResponse(byte[] image, HttpServletResponse servletResponseChannel)
      throws IOException {
    OutputStream out = servletResponseChannel.getOutputStream();
    out.write(image);
    out.flush();
    out.close();
  }

  public void writeImageBytesToResponse(byte[] image) throws IOException {
    servletResponseChannel.setContentType("image/png");
    appendNoCache(servletResponseChannel);
    writeBytesToResponse(image);
  }

  private static void appendNoCache(HttpServletResponse servletResponseChannel) {
    servletResponseChannel.setHeader("Cache-Control", "no-cache");
    servletResponseChannel.setHeader("Pragma", "no-cache");
    servletResponseChannel.setDateHeader("Expires", 0);
  }

  /**
   * Write bytes to response.
   *
   * @param image the image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void writeBytesToResponse(byte[] image) throws IOException {
    servletResponseChannel.setContentType(contentType);
    OutputStream out = servletResponseChannel.getOutputStream();
    out.write(image);
    out.flush();
    out.close();
    setResponseCommitted(true);
  }

  /**
   * Gets the direct response string.
   *
   * @return the direct response string
   */
  private String getDirectResponseString() {
    return directResponseString;
  }

  /**
   * Redirects to another page.
   *
   * @param url the url
   * @param res the res
   * @throws IOException the redirect failed
   */
  private void redirect(String url, HttpServletResponse res) throws IOException {
    redirect(url, null, res);
  }

  /**
   * Redirects to another page.
   *
   * @param url the url
   * @param params the params
   * @param res the res
   * @throws IOException the redirect failed
   */
  private void redirect(String url, ParameterParser params, HttpServletResponse res)
      throws IOException {
    if (url != null) {
      if (params != null && params.getBooleanParameter("async")) {
        url += "&async=true&header=true";
      }
      res.sendRedirect(url.replace("&amp;", "&"));
    } else {
      res.sendRedirect(Page.SEARCH.getUrl());
    }
  }

  /**
   * Prints the partial page.
   *
   * @param pageContents the page contents
   * @param currentPage the current page
   * @param params the params
   * @param res the res
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws TemplateException the template exception
   */
  private void printPartialPage(
      String pageContents, WebPage currentPage, ParameterParser params, HttpServletResponse res)
      throws IOException, ServletException, TemplateException {
    boolean useWrapping = params.getBoolean("header");

    if (useWrapping || currentPage.useWrapping()) {
      Template mainTemplate = freemarkerConfig.getTemplate("modal.ftl");
      getTemplateMap().put("content", pageContents);
      pageContents = Freemarker.parseTemplate(getTemplateMap(), mainTemplate);
    }

    deliverHTML(pageContents, params, res);
  }

  /**
   * Process exception template.
   *
   * @param e the e
   * @param debug the debug
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private String processExceptionTemplate(Exception e, boolean debug) throws IOException {
    String pageContents = null;
    String error = e.toString();
    if (debug) {
      error += StackTraceFormatter.getStackTraceAsHTML(e);
    }
    Template t = freemarkerConfig.getTemplate("exception.ftl");
    Map<String, String> root = new HashMap<>();
    root.put("message", error);
    try {
      pageContents = Freemarker.parseTemplate(root, t);
    } catch (TemplateException e1) {
      pageContents = error;
    }
    return pageContents;
  }

  /**
   * Deliver html.
   *
   * @param outputString the output string
   * @param params the params
   * @param res the res
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  private void deliverHTML(String outputString, ParameterParser params, HttpServletResponse res)
      throws IOException, ServletException {
    res.setContentType(contentType);

    res.setCharacterEncoding("UTF-8");

    boolean supportsGzip = isGZIPSupported(params);

    if (isGZIPEnabled() && supportsGzip && outputString != null) {
      res.setHeader("Content-Encoding", "gzip");
      try {
        GZIPOutputStream gzos = new GZIPOutputStream(res.getOutputStream());
        gzos.write(outputString.getBytes());
        gzos.close();
      } catch (IOException ie) {
        logger.error(
            "Main.doImpl(): a problem occured while trying to write to GZIPOutputStream", ie);
        throw new IOException("<p>An error occured</p>" + ie);
      }
    } else if (!res.isCommitted()) {
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();
      if (outputString != null) {
        out.println(outputString);
        out.close();
      } else {
        out.close();
        throw new ServletException("No data to deliver");
      }
    }
  }

  /**
   * Checks if is gZIP enabled.
   *
   * @return true, if is gZIP enabled
   */
  private boolean isGZIPEnabled() {
    return WebProperties.getInstance().isGzipEnabled();
  }

  /**
   * Checks if is gZIP supported.
   *
   * @param params the params
   * @return true, if is gZIP supported
   */
  private boolean isGZIPSupported(ParameterParser params) {
    String encoding = params.getHttpServletRequest().getHeader("Accept-Encoding");
    return encoding != null && encoding.toLowerCase().contains("gzip");
  }

  /**
   * Gets the template map.
   *
   * @return the template map
   */
  public Map<String, Object> getTemplateMap() {
    return this.templateMap;
  }

  /**
   * Used to set template paths that are using the default template path, the index.ftl file. <br>
   * Example wise: by calling setTemplatePathWithIndex("/report"); <br>
   * The template path will become "/report/index.ftl"
   *
   * @param path the path to be prepended to the default templatePathString
   */
  public void setTemplatePathWithIndex(String path) {
    if (path == null) {
      throw new IllegalArgumentException("Path variable cannot be null.");
    }
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    this.templatePathString = path + defaultTemplatePath;
  }

  /**
   * Will set the template path directly.<br>
   * You are free to omit the file suffix.
   *
   * @param path the path to be set
   */
  public void setTemplatePath(String path) {
    if (path == null) {
      throw new IllegalArgumentException("Path variable cannot be null.");
    }
    if (!path.endsWith(".ftl")) {
      path = path + ".ftl";
    }
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    this.templatePathString = path;
  }

  /**
   * Returns the default index path.
   *
   * @return the default template path
   */
  private String getDefaultTemplatePath() {
    return defaultTemplatePath;
  }

  /**
   * Get the template path that this response handler has configured.
   *
   * @return the configured template path
   */
  private String getTemplatePath() {
    return this.templatePathString;
  }

  /**
   * Sets the content type.
   *
   * @param string the new content type
   */
  public void setContentType(String string) {
    this.contentType = string;
  }

  /**
   * Sets the response committed.
   *
   * @param responseCommitted the new response committed
   */
  public void setResponseCommitted(Boolean responseCommitted) {
    this.responseCommitted = responseCommitted;
  }

  /**
   * Checks if is response committed.
   *
   * @return the boolean
   */
  public Boolean isResponseCommitted() {
    return responseCommitted;
  }

  /**
   * Sets the download attachment.
   *
   * @param filename the new download attachment
   */
  public void setDownloadAttachment(String filename) {
    servletResponseChannel.setHeader("Content-Disposition", "attachment; filename=" + filename);
  }

  /**
   * Sets the direct to page.
   *
   * @param page the page
   * @param attrs the attrs
   */
  public void setDirectToPage(Page page, String... attrs) {
    this.redirectToUrl = page.getUrl(org.apache.commons.lang3.StringUtils.join(attrs, '&'));
  }

  /**
   * Adds the new trail point.
   *
   * @param contextDataKeeper the context data keeper
   */
  public void addNewTrailPoint(ContextItem contextDataKeeper) {
    this.trailPoint = contextDataKeeper;
  }

  public ContextItem getTrailPoint() {
    return trailPoint;
  }
}
