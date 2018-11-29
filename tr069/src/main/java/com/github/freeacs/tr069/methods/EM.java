package com.github.freeacs.tr069.methods;

import com.github.freeacs.http.HTTPRequestResponseData;

public class EM {
  public static void process(HTTPRequestResponseData reqRes) {
    reqRes.getRequestData().setMethod(TR069Method.EMPTY);
  }
}
