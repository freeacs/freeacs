package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;

public class ADOres {
    public static void process(HTTPReqResData reqRes) {
        reqRes.getRequest().setMethod(TR069Method.ADD_OBJECT);
    }
}
