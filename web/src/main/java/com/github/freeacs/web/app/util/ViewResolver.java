package com.github.freeacs.web.app.util;

import com.github.freeacs.web.Page;
import java.util.Map;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

/**
 * Used by app.xml for the Spring MVC view resolve
 *
 * @author Jarl Andre Hubenthal
 */
public class ViewResolver extends FreeMarkerViewResolver {
  public Map<String, Object> getAttributesMap() {
    Map<String, Object> map = super.getAttributesMap();
    map.put("URL_MAP", Page.getPageURLMap());
    return map;
  }
}
