package com.github.freeacs.common.jetty;

import spark.ExceptionMapper;
import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.EmbeddedServerFactory;
import spark.embeddedserver.jetty.EmbeddedJettyServer;
import spark.embeddedserver.jetty.JettyHandler;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

import javax.servlet.http.HttpSessionListener;

public class JettyFactory implements EmbeddedServerFactory {
  private final boolean httpOnly;
  private final int maxHttpPostSize;
  private final int maxFormKeys;
  private final HttpSessionListener httpSessionEventListener;

  public JettyFactory(boolean httpOnly, int maxHttpPostSize, int maxFormKeys,
                      HttpSessionListener httpSessionEventListener) {
    this.httpOnly = httpOnly;
    this.maxHttpPostSize = maxHttpPostSize;
    this.maxFormKeys = maxFormKeys;
    this.httpSessionEventListener = httpSessionEventListener;
  }

  @Override
  public EmbeddedServer create(
      Routes routeMatcher,
      StaticFilesConfiguration staticFilesConfiguration,
      ExceptionMapper exceptionMapper,
      boolean hasMultipleHandler) {
    MatcherFilter matcherFilter =
        new MatcherFilter(
            routeMatcher, staticFilesConfiguration, exceptionMapper, false, hasMultipleHandler);
    matcherFilter.init(null);
    JettyHandler handler = new JettyHandler(matcherFilter);
    handler.addEventListener(httpSessionEventListener);
    handler.getSessionCookieConfig().setHttpOnly(httpOnly);
    return new EmbeddedJettyServer(new JettyServer(maxHttpPostSize, maxFormKeys), handler);
  }
}
