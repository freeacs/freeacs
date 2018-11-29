package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPRequestResponseData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.xml.Parser;

public class DOres {
  public static void process(HTTPRequestResponseData reqRes) throws TR069Exception {
    reqRes.getRequestData().setMethod(TR069Method.DOWNLOAD);
    Parser parser = new Parser(reqRes.getRequestData().getXml());
    if (parser.getHeader().getNoMoreRequests() != null
        && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag()) {
      reqRes.getSessionData().setNoMoreRequests(true);
    }
  }
}
