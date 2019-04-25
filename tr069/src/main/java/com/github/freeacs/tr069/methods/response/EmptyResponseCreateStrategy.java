package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.xml.EmptyResponse;
import com.github.freeacs.tr069.xml.Response;

public class EmptyResponseCreateStrategy implements ResponseCreateStrategy {
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) {
        return new EmptyResponse(reqRes.getSessionData().getCwmpVersionNumber());
    }
}
