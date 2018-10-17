package com.github.freeacs.web.help;

import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.util.Freemarker;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Is basically used to provide in-page help for pages in xAPS Web,<br>
 * but can possibly be used to describe any topic within the xAPS system.
 *
 * @author Jarl Andre Hubenthal
 */
public abstract class HelpPage {
  /** The Constant config. */
  private static final Configuration config = Freemarker.initFreemarker();

  /** The Constant NO_DATA. */
  private static final String NO_DATA = "No data available";

  /**
   * Gets the hTML for page by class.
   *
   * @param page the page
   * @return the hTML for page by class
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public static String getHTMLForPageByClass(Page page) throws IOException, TemplateException {
    return getHTMLForPage(page);
  }

  /**
   * Gets the hTML for page.
   *
   * @param page the page
   * @return the hTML for page
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public static String getHTMLForPage(Page page) throws IOException, TemplateException {
    Template template = config.getTemplate("HelpPage.ftl");

    Map<String, Object> map = new HashMap<>();

    map.put("title", page.getTitle());

    InputStream is = page.getClazz().getResourceAsStream(page.getClazz().getSimpleName() + ".html");
    if (is != null) {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      char[] buf = new char[is.available()];
      br.read(buf);
      String html = new String(buf);
      String contents = getBodyContents(html);
      map.put("content", contents);
      br.close();
    } else {
      map.put("content", NO_DATA);
    }

    map.put("time", new Date());

    return Freemarker.parseTemplate(map, template);
  }

  /**
   * Getting the contents within a BODY in HTML. If not present, returns the original html.
   *
   * @param html the html
   * @return The body contents
   */
  private static String getBodyContents(String html) {
    int start = html.indexOf("<body");
    if (start > -1) {
      start = html.indexOf('>', start);
      if (start > -1) {
        start += 1;
        int end = html.indexOf("</body>", start);
        if (end > -1) {
          return html.substring(start, end);
        }
      }
    }
    return html;
  }

  /**
   * Finds and retrieves the contents of a resource path.
   *
   * @param path The resource path to look for
   * @param def The default value to return if no resource path is not found
   * @return The resource path contents
   */
  private static String getFileContents(String path, String def) {
    InputStream is = HelpPage.class.getResourceAsStream(path);
    if (is != null) {
      Scanner scanner = new Scanner(is).useDelimiter("\\Z");
      String contents = scanner.next();
      scanner.close();
      if (contents != null) {
        return contents.replace("%VERSION%", "latest");
      }
    }
    return def;
  }

  /**
   * Retrieves the content from an HTML file. Wraps it in a help page. Does not include any other
   * files.
   *
   * @param path The path to the HTML file to retrieve
   * @return A HTML string
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public static String getHTMLForPageByResourcePath(String path)
      throws IOException, TemplateException {
    Template template = config.getTemplate("HelpPage.ftl");

    Map<String, Object> map = new HashMap<>();

    String shortName = path.substring(path.lastIndexOf('/') + 1, path.length());

    String fileName = camelCase(!shortName.endsWith("Page") ? shortName + "Page" : shortName);

    path = path.substring(0, path.lastIndexOf('/'));

    map.put("title", shortName);

    String contents = getFileContents(path + "/" + fileName + ".html", NO_DATA);

    map.put("content", contents);

    map.put("time", new Date());

    return Freemarker.parseTemplate(map, template);
  }

  /**
   * Uppercases the first character of a string.
   *
   * @param string A string to camelcase
   * @return The string with the first character uppercased.
   */
  private static String camelCase(String string) {
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }
}
