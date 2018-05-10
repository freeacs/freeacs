package com.owera.xaps.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletContext;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public final class Freemarker  {
	public Configuration initFreemarker(){
		Configuration config = new Configuration();
		config.setClassForTemplateLoading(Freemarker.class, "/templates");
        config.setTemplateUpdateDelay(120);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        config.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        config.setDefaultEncoding("UTF-8");
        config.setOutputEncoding("UTF-8");
		return config;
	}

}
