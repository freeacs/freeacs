package com.github.freeacs.common.jetty;

import spark.ExceptionMapper;
import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.EmbeddedServerFactory;
import spark.embeddedserver.jetty.EmbeddedJettyServer;
import spark.embeddedserver.jetty.JettyHandler;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

public class JettyFactory implements EmbeddedServerFactory {
  private final boolean httpOnly;
  private final int maxHttpPostSize;
  private final int maxFormKeys;

  public JettyFactory(boolean httpOnly, int maxHttpPostSize, int maxFormKeys) {
    this.httpOnly = httpOnly;
    this.maxHttpPostSize = maxHttpPostSize;
    this.maxFormKeys = maxFormKeys;
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
    handler.getSessionCookieConfig().setHttpOnly(httpOnly);
    return new EmbeddedJettyServer(new JettyServer(maxHttpPostSize, maxFormKeys), handler);
  }
}
