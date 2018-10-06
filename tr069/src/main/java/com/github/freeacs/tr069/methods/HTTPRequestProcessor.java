package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.NoDataAvailableException;
import com.github.freeacs.dbi.tr069.TR069DMLoader;
import com.github.freeacs.dbi.tr069.TR069DMParameterMap;
import com.github.freeacs.tr069.HTTPReqData;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.UnknownMethodException;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RequestProcessor will parse the xml from the CPE. Any vital information will be stored in the
 * SessionData or RequestResponse objects. Some logging.
 *
 * @author Morten
 */
public class HTTPRequestProcessor {

  private static TR069DMParameterMap tr069ParameterMap = null;

  public static TR069DMParameterMap getTR069ParameterMap() throws Exception {
    if (tr069ParameterMap == null) {
      tr069ParameterMap = TR069DMLoader.load();
    }
    return tr069ParameterMap;
  }

  /**
   * Process the request from the CPE. For some CPE-requests processing is simple (do-nothing), for
   * others it may involve more logic. Keep in mind that a HTTP-request from the CPE, might actually
   * be a TR-69 response!
   *
   * <p>If in Testmode, then the request is processed and validated in an entirely different way
   * than normal provisioning!
   *
   * @param reqRes
   * @param properties
   * @throws TR069Exception
   */
  public static void processRequest(
      HTTPReqResData reqRes, Map<String, HTTPRequestAction> requestMap, Properties properties)
      throws TR069Exception {
    try {
      String requestMethodName = extractMethodName(reqRes.getRequest().getXml());
      if (requestMethodName == null) requestMethodName = TR069Method.EMPTY;
      reqRes.getRequest().setMethod(requestMethodName);
      Log.debug(
          HTTPRequestProcessor.class,
          "Will process method " + requestMethodName + " (incoming request/response from CPE)");
      HTTPRequestAction reqAction = requestMap.get(requestMethodName);
      if (reqAction != null) {
        reqRes.getRequest().setXml(HTTPReqData.XMLFormatter.filter(reqRes.getRequest().getXml()));
        reqAction.getProcessRequestMethod().apply(reqRes);
      } else {
        throw new UnknownMethodException(requestMethodName);
      }
    } catch (Throwable t) {
      if (t instanceof TR069Exception) throw (TR069Exception) t;
      if (t instanceof NoDataAvailableException)
        throw new TR069Exception(
            "Device was not found in database - can only provision device if in server is in discovery mode and device supports basic authentication",
            TR069ExceptionShortMessage.NODATA);
      else
        throw new TR069Exception(
            "Could not process HTTP-request (from TR-069 client)",
            TR069ExceptionShortMessage.MISC,
            t);
    } finally {
      if (reqRes.getRequest().getMethod() == null) {
        reqRes.getRequest().setMethod(TR069Method.EMPTY);
        reqRes.getRequest().setXml("");
      }
      if (Log.isConversationLogEnabled()) {
        String unitId = reqRes.getSessionData().getUnitId();
        String xml = reqRes.getRequest().getXml();
        if (properties.isPrettyPrintQuirk(reqRes.getSessionData()))
          xml = HTTPReqData.XMLFormatter.prettyprint(reqRes.getRequest().getXml());
        Log.conversation(
            reqRes.getSessionData(),
            "============== FROM CPE ( "
                + Optional.ofNullable(unitId).orElseGet(() -> "Unknown")
                + " ) TO ACS ===============\n"
                + xml);
      }
    }
  }

  private static final Pattern methodNamePattern =
      Pattern.compile(":Body.*>\\s*<cwmp:(\\w+)(>|/>)", Pattern.DOTALL);

  /**
   * Fastest way to extract the method name without actually parsing the XML - the method name is
   * crucial to the next steps in TR-069 processing
   *
   * <p>The TR-069 Method is found after the first "<cwmp:" after ":Body"
   *
   * @param reqStr (TR-069 XML)
   * @return TR-069 methodName
   */
  static String extractMethodName(String reqStr) {
    String methodStr = getMethodStr(reqStr);
    if (methodStr != null && methodStr.endsWith("Response"))
      methodStr = methodStr.substring(0, methodStr.length() - 8);
    return methodStr;
  }

  private static String getMethodStr(String reqStr) {
    Matcher matcher = methodNamePattern.matcher(reqStr);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }
}
