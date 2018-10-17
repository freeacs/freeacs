package com.github.freeacs.web.app.input;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This class enables ignoring specific request parameters.
 *
 * <p>Since this class extends an HttpServletRequestWrapper it has all the same methods.
 *
 * <p>The only overridden method is getParameter().
 *
 * @author Jarl Andre Hubenthal
 */
public class AbstractRequest extends HttpServletRequestWrapper {
  /**
   * Instantiates a new abstract request.
   *
   * @param request the request
   */
  public AbstractRequest(HttpServletRequest request) {
    super(request);
  }

  /** The ignored parameters. */
  private final List<String> ignoredParameters = new ArrayList<>();

  /**
   * Ignore parameter.
   *
   * @param param the param
   */
  public void ignoreParameter(String param) {
    ignoredParameters.add(param);
  }

  @Override
  public String getParameter(String key) {
    if (key != null && !ignoredParameters.contains(key)) {
      return super.getParameter(key);
    }
    return null;
  }
}
