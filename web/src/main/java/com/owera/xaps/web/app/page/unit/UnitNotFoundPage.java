package com.owera.xaps.web.app.page.unit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.owera.xaps.web.app.util.Freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;


/**
 * The Class UnitNotFoundPage.
 */
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
	public static String getErrorText(String unit,Configuration config) throws TemplateException, IOException{
		Map<String,Object> root = new HashMap<String,Object>();
		root.put("unitId", unit);
		return Freemarker.parseTemplate(root, config.getTemplate("/unit-status/notfound.ftl"));
	}
}
