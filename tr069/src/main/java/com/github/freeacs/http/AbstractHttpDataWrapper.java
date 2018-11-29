package com.github.freeacs.http;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.tr069.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

public abstract class AbstractHttpDataWrapper {
    protected final DBAccess dbAccess;
    protected final Properties properties;

    public AbstractHttpDataWrapper(DBAccess dbAccess, Properties properties) {
        this.dbAccess = dbAccess;
        this.properties = properties;
    }

    public HTTPRequestResponseData getHttpReqResDate(HttpServletRequest req, HttpServletResponse res) throws SQLException {
        HTTPRequestResponseData reqRes = new HTTPRequestResponseData(req, res, dbAccess);
        reqRes.getRequestData().setContextPath(properties.getContextPath());
        return reqRes;
    }
}
