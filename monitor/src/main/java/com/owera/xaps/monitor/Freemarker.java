package com.owera.xaps.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletContext;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public final class Freemarker  {
	public Configuration initFreemarker(ServletContext context){
		Configuration config = new Configuration();
		config.setTemplateLoader(new ClassTemplateLoader(Freemarker.class, "/templates"));
        config.setTemplateUpdateDelay(0);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        config.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        config.setDefaultEncoding("UTF-8");
        config.setOutputEncoding("UTF-8");
		return config;
	}

	public static String parseTemplate(Object root, Template template) throws TemplateException, IOException {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		template.process(root, out);
		return writer.toString();
	}
}
