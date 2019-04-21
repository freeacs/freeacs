package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.Properties;
import java.util.HashMap;
import java.util.Map;

public class TR069Method {
  public static final String DOWNLOAD = "Download";

  public static final String FAULT = "Fault";

  public static final String REBOOT = "Reboot";

  public static final String FACTORY_RESET = "FactoryReset";

  /** Map of all (SOAP/HTTP-)request actions and what to do next. */
  private final Map<String, HTTPRequestAction> requestMap = new HashMap<>();
  /** Map of all (SOAP/HTTP-)response actions and what to return. */
  private final Map<String, HTTPResponseAction> responseMap = new HashMap<>();
  /** Map of all abbreviations - only used in event-logging. */
  private final Map<String, String> abbrevMap = new HashMap<>();

  public TR069Method(Properties properties) {

    getAbbrevMap().put(DOWNLOAD, "DO");
    getRequestMap().put(DOWNLOAD, new HTTPRequestAction(DOres::process));
    getResponseMap().put(DOWNLOAD, new HTTPResponseAction((req) -> HTTPResponseCreator.buildDO(req, properties.isFileAuthUsed())));

    getAbbrevMap().put(FAULT, "FA");
    getRequestMap().put(FAULT, new HTTPRequestAction(FAres::process));

    getAbbrevMap().put(REBOOT, "RE");
    getRequestMap().put(REBOOT, new HTTPRequestAction(REres::process));
    getResponseMap().put(REBOOT, new HTTPResponseAction(HTTPResponseCreator::buildRE));

    getAbbrevMap().put(FACTORY_RESET, "FR");
    getRequestMap().put(FACTORY_RESET, new HTTPRequestAction(FRres::process));
    getResponseMap().put(FACTORY_RESET, new HTTPResponseAction(HTTPResponseCreator::buildFR));
  }

  public Map<String, HTTPRequestAction> getRequestMap() {
    return requestMap;
  }

  public Map<String, HTTPResponseAction> getResponseMap() {
    return responseMap;
  }

  public Map<String, String> getAbbrevMap() {
    return abbrevMap;
  }
}
