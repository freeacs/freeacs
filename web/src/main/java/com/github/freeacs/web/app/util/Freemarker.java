package com.github.freeacs.web.app.util;

import freemarker.cache.ClassTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.ResourceBundleModel;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

/**
 * Providing customized configuration instance tailored for xaps web.
 *
 * @author Jarl Andre Hubenthal
 */
public final class Freemarker {
  /**
   * Inits the freemarker.
   *
   * @return the configuration
   */
  public static Configuration initFreemarker() {
    try {
      Configuration config = new Configuration();
      config.setTemplateLoader(new ClassTemplateLoader(Freemarker.class, "/templates"));
      config.setTemplateUpdateDelay(0);
      config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
      config.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
      config.setDefaultEncoding("ISO-8859-1");
      config.setOutputEncoding("ISO-8859-1");
      config.setNumberFormat("0");
      config.setSetting("url_escaping_charset", "ISO-8859-1");
      config.setLocale(Locale.ENGLISH);
      setAutoImport(config);
      setSharedVariables(config);
      return config;
    } catch (Throwable e) {
      throw new RuntimeException("Could not initialise Freemarker configuration", e);
    }
  }

  /**
   * Sets the auto import.
   *
   * @param config the new auto import
   */
  private static void setAutoImport(Configuration config) {
    config.addAutoInclude("/functions/functions.ftl");
    config.addAutoImport("macros", "/macros/macros.ftl");
  }

  /**
   * Sets the shared variables.
   *
   * @param config the new shared variables
   * @throws TemplateModelException the template model exception
   */
  private static void setSharedVariables(Configuration config) throws TemplateModelException {
    config.setAllSharedVariables(
        new ResourceBundleModel(ResourceHandler.getProperties(), new BeansWrapper()));
    config.setSharedVariable("format", new ResourceMethod());
  }

  /**
   * This method parses a template and generates a String representation.
   *
   * @param root A root map
   * @param template Template
   * @return the String representation
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String parseTemplate(Object root, Template template)
      throws TemplateException, IOException {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    template.process(root, out);
    return writer.toString();
  }
}
