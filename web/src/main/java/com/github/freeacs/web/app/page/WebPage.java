package com.github.freeacs.web.app.page;

import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.util.SessionData;
import java.util.List;
import javax.sql.DataSource;

/**
 * The mixin for new pages.<br>
 * <br>
 * For a simple implementation of this type, use {@link AbstractWebPage}.
 *
 * @author Jarl Andre Hubenthal
 */
public interface WebPage {
  /**
   * Related to partial pages in that it defines whether or not the partial content should be
   * wrapped in a minimal template, modal.ftl
   *
   * <p>Will only have effective meaning if the outputHandler is partial, eg when
   * outputHandler.getDirectResponse() is not null.
   *
   * <p>Some pages must return simple values (eg for use in javascript) so this method enables some
   * other pages to be displayed in dialogs with a basic xaps web layout.
   *
   * @return A boolean true or false
   */
  boolean useWrapping();

  /**
   * Tells wheter or not this page requires "no cache" in the servlet outputHandler headers.
   *
   * @return A boolean true or false
   */
  boolean requiresNoCache();

  /**
   * Tells whether or not the page has been delivered (processed).
   *
   * @return A boolean true or false
   */
  boolean isPageProcessed();

  /**
   * Generates a list of shortcut MenuItems.
   *
   * @param sessionData The current SessionData
   * @return A list of MenuItems
   */
  List<MenuItem> getShortcutItems(SessionData sessionData);

  /**
   * Generates the page title used in the header.
   *
   * @param page the current page id
   * @return The page title
   */
  String getTitle(String page);

  /**
   * Returns the contents of the page. <br>
   * <br>
   * Should be named something else, like content or execute. But in general this API is only to be
   * used for creating new pages and the method is enforced, so it is pretty obvious what it does
   * anyway.
   *
   * @param params the parameter parser
   * @param outputHandler the outputHandler handler
   * @param xapsDataSource
   * @param syslogDataSource
   * @throws Exception the exception
   */
  void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception;
}
