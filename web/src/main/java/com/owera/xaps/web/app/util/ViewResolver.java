package com.owera.xaps.web.app.util;

import java.util.Map;

import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import com.owera.xaps.web.Page;


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
