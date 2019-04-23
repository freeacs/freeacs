package com.github.freeacs.http;

import com.github.freeacs.tr069.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractHttpDataWrapper {
    protected final Properties properties;

    public AbstractHttpDataWrapper(Properties properties) {
        this.properties = properties;
    }

    protected HTTPRequestResponseData getHttpRequestResponseData(HttpServletRequest req, HttpServletResponse res) {
        HTTPRequestResponseData reqRes = new HTTPRequestResponseData(req, res);
        reqRes.getRequestData().setContextPath(properties.getContextPath());
        return reqRes;
    }
}
