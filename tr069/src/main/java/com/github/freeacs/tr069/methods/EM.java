package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPRequestResponseData;

public class EM {
  public static void process(HTTPRequestResponseData reqRes) {
    reqRes.getRequestData().setMethod(TR069Method.EMPTY);
  }
}
