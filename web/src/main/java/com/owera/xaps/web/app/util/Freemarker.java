package com.owera.xaps.web.app.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletContext;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.ResourceBundleModel;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;


/**
 * Providing customized configuration instance tailored for xaps web.
 * 
 * @author Jarl Andre Hubenthal
 * 
 */
public final class Freemarker {
	
	/**
	 * Inits the freemarker.
	 *
	 * @param context the context
	 * @return the configuration
	 */
	public static Configuration initFreemarker(ServletContext context){
		return initFreemarker(context, "/WEB-INF/templates");
	}
	
	/**
	 * Inits the freemarker for class loading.
	 *
	 * @param clazz the clazz
	 * @return the configuration
	 */
	public static Configuration initFreemarkerForClassLoading(Class<?> clazz) {
		try{
			Configuration config = new Configuration();
			config.setTemplateLoader(new ClassTemplateLoader(clazz,""));
			config.setTemplateUpdateDelay(0);
			config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			config.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
			config.setDefaultEncoding("ISO-8859-1");
			config.setOutputEncoding("ISO-8859-1");
			config.setNumberFormat("0");
			config.setSetting("url_escaping_charset", "ISO-8859-1");
			config.setLocale(Locale.ENGLISH);
			setSharedVariables(config);
			return config;
		}catch(Throwable e){
			throw new RuntimeException("Could not initialise Freemarker configuration",e);
		}
	}
	
	/**
	 * Inits the freemarker for file system.
	 *
	 * @param path the path
	 * @return the configuration
	 */
	public static Configuration initFreemarkerForFileSystem(String path) {
		try{
			Configuration config = new Configuration();
			config.setTemplateLoader(new FileTemplateLoader(new File(path)));
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
		}catch(Throwable e){
			throw new RuntimeException("Could not initialise Freemarker configuration",e);
		}
	}

	/**
	 * Inits the freemarker.
	 *
	 * @param context the context
	 * @param folderToLookForTemplates the folder to look for templates
	 * @return the configuration
	 */
	public static Configuration initFreemarker(ServletContext context,String folderToLookForTemplates) {
		try{
			Configuration config = new Configuration();
			config.setServletContextForTemplateLoading(context, folderToLookForTemplates);
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
		}catch(Throwable e){
			throw new RuntimeException("Could not initialise Freemarker configuration",e);
		}
	}
	
	/**
	 * Sets the auto import.
	 *
	 * @param config the new auto import
	 */
	private static void setAutoImport(Configuration config) {
		config.addAutoInclude("/functions/functions.ftl");
		config.addAutoImport("macros","/macros/macros.ftl");
	}

	/**
	 * Sets the shared variables.
	 *
	 * @param config the new shared variables
	 * @throws TemplateModelException the template model exception
	 */
	private static void setSharedVariables(Configuration config) throws TemplateModelException {
	    config.setAllSharedVariables(new ResourceBundleModel(ResourceHandler.getProperties(), new BeansWrapper()));
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
	public static String parseTemplate(Object root, Template template) throws TemplateException, IOException {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		template.process(root, out);
		return writer.toString();
	}
}
