/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.freeacs.web.app.util;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * A resource format method.
 *
 * @author Jarl Andre Hubenthal
 */
public class ResourceMethod implements TemplateMethodModel {
  /** The pattern. */
  private Pattern pattern = Pattern.compile("\\{.*?\\}");

  /**
   * Executes the method.
   *
   * @param list the list
   * @return the string
   * @throws TemplateModelException the template model exception
   */
  @SuppressWarnings("rawtypes")
  public String exec(List list) throws TemplateModelException {
    if (list.size() > 1) {
      String value = (String) list.get(0);
      List args = list.subList(1, list.size());
      Matcher matcher = pattern.matcher(value);
      while (matcher.find()) {
        String match = matcher.group();
        String s = match.substring(1, match.length() - 1);
        if (NumberUtils.isNumber(s)) {
          String matchedValue = (String) args.get(Integer.parseInt(s));
          if (matchedValue != null) {
            value = value.replace(match, matchedValue);
          }
        }
      }
      return value;
    } else if (!list.isEmpty()) {
      throw new TemplateModelException(
          "Unnecessary call to this method without any arguments other than property key. Reference properties directly instead.");
    } else {
      throw new TemplateModelException("Wrong number of arguments supplied");
    }
  }
}
