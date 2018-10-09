package com.owera.xaps.monitor;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateExceptionHandler;

public final class Freemarker {
  public Configuration initFreemarker() {
    Configuration config = new Configuration();
    config.setTemplateLoader(new ClassTemplateLoader(Freemarker.class, "/templates"));
    config.setTemplateUpdateDelay(0);
    config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    config.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
    config.setDefaultEncoding("UTF-8");
    config.setOutputEncoding("UTF-8");
    return config;
  }
}
