package com.github.freeacs.common.spark;

import com.github.freeacs.common.http.SimpleResponseWrapper;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import spark.Request;
import spark.Response;

public class ResponseHelper {
  @FunctionalInterface
  public interface CheckedConsumer<L, R> {
    void apply(L l, R r) throws ServletException, IOException;
  }

  public static byte[] process(
      CheckedConsumer<HttpServletRequest, HttpServletResponse> service,
      Request request,
      Response response,
      SimpleResponseWrapper responseWrapper)
      throws ServletException, IOException {
    service.apply(request.raw(), responseWrapper);
    response.status(responseWrapper.getStatus());
    response.type(responseWrapper.getContentType());
    response.header("Content-Length", String.valueOf(responseWrapper.getResponseAsBytes().length));
    responseWrapper.getHeaders().forEach((k, v) -> response.header(k, v.toString()));
    return responseWrapper.getResponseAsBytes();
  }
}
