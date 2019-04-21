package com.github.freeacs.tr069.methods;

import com.github.freeacs.dbi.tr069.TR069DMLoader;
import com.github.freeacs.dbi.tr069.TR069DMParameterMap;

/**
 * RequestProcessor will parse the xml from the CPE. Any vital information will be stored in the
 * SessionData or RequestResponse objects. Some logging.
 *
 * @author Morten
 */
public class HTTPRequestProcessor {

  private static TR069DMParameterMap tr069ParameterMap;

  public static TR069DMParameterMap getTR069ParameterMap() throws Exception {
    if (tr069ParameterMap == null) {
      tr069ParameterMap = TR069DMLoader.load();
    }
    return tr069ParameterMap;
  }

}
