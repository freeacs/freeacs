package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Parser;

public class TCreq {
  public static void process(HTTPRequestResponseData reqRes) throws TR069Exception {
    reqRes.getRequestData().setMethod(TR069Method.TRANSFER_COMPLETE);
    Parser parser = new Parser(reqRes.getRequestData().getXml());
    Header header = parser.getHeader();
    reqRes.setTR069TransactionID(header.getId());
    if (parser.getFault() != null && !"0".equals(parser.getFault().getFaultCode())) {
      reqRes.getRequestData().setFault(parser.getFault());
      Log.debug(TCreq.class, "TCReq reported a fault");
    } else {
      Log.debug(TCreq.class, "TCReq is ok (download is assumed ok)");
    }
  }
}
