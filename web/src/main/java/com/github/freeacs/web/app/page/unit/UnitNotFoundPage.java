package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.web.app.util.Freemarker;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** The Class UnitNotFoundPage. */
public abstract class UnitNotFoundPage {
  /**
   * Gets the error text.
   *
   * @param unit the unit
   * @param config the config
   * @return the error text
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String getErrorText(String unit, Configuration config)
      throws TemplateException, IOException {
    Map<String, Object> root = new HashMap<>();
    root.put("unitId", unit);
    return Freemarker.parseTemplate(
        root, config.getTemplate("templates/" + "/unit-status/notfound.ftl"));
  }
}
