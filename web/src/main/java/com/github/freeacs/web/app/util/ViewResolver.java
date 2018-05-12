package com.github.freeacs.web.app.util;

import com.github.freeacs.web.Page;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.util.Map;


/**
 * Used by app.xml for the Spring MVC view resolve
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public class ViewResolver extends FreeMarkerViewResolver {

	/**
	 * Instantiates a new view resolver.
	 */
	public ViewResolver(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.UrlBasedViewResolver#getAttributesMap()
	 */
	public Map<String, Object> getAttributesMap(){
		Map<String, Object> map = super.getAttributesMap();
		map.put("URL_MAP",Page.getPageURLMap());
		return map;
	}
}
