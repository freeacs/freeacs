package com.github.freeacs.web.app.util;

import com.github.freeacs.web.Page;
import freemarker.template.Template;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;



/**
 * A FreeMarker View that supports decoration of FreeMarker templates with a
 * layout template.
 * 
 * @author Stefan Haberl
 */
public class DecoratingFreeMarkerView extends FreeMarkerView {

  /** The Constant KEY_CONTENT_TEMPLATE. */
  public static final String KEY_CONTENT_TEMPLATE = "content_page";

  /** The decorating template. */
  private String decoratingTemplate;

  /**
   * Sets the name of the layout template which will be used to decorate the
   * single Freemarker pages.
   * 
   * @param decoratingTemplate
   *          The name of the decorating template
   */
  public void setDecoratingTemplate(String decoratingTemplate) {
    this.decoratingTemplate = decoratingTemplate;
  }

  /**
   * Exposes the name of the content template of this request under.
   *
   * @param model The model that will be passed to the template at merge time
   * @param request current HTTP request
   * @throws Exception if there's a fatal error while we're adding information to the
   * context
   * <p>
   * In the decorating template include the content template at your desired
   * spot using <code>&lt;#include content_page /&gt;</code>
   * @see #KEY_CONTENT_TEMPLATE
   * @see #renderMergedTemplateModel
   */
  protected void exposeHelpers(Map<String, Object> model,
      HttpServletRequest request) throws Exception {

    model.put(KEY_CONTENT_TEMPLATE, "/" + getUrl());
    model.put("URL_MAP", Page.getPageURLMap());
  }

  /**
   * Retrieve the FreeMarker template for the given locale, to be rendering by
   * this view.
   * <p>
   * Will retrieve the decorating template
   * 
   * @param locale
   *          the current locale
   * @return the FreeMarker template to render
   * @throws IOException
   *           if the template file could not be retrieved
   * @see #setDecoratingTemplate(String)
   */
  protected Template getTemplate(Locale locale) throws IOException {
    return getTemplate(this.decoratingTemplate, locale);
  }

}
