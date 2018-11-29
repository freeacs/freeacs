package com.github.freeacs.tr069.methods;

import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.xml.Parser;

public class FRres {
  public static void process(HTTPRequestResponseData reqRes) throws TR069Exception {
    reqRes.getRequestData().setMethod(TR069Method.FACTORY_RESET);
    Parser parser = new Parser(reqRes.getRequestData().getXml());
    if (parser.getHeader().getNoMoreRequests() != null
        && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag()) {
      reqRes.getSessionData().setNoMoreRequests(true);
    }
  }
}
