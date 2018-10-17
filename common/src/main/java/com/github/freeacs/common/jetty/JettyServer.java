package com.github.freeacs.common.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import spark.embeddedserver.jetty.JettyServerFactory;

/** Creates Jetty Server instances. */
public class JettyServer implements JettyServerFactory {
  private final int maxPostSize;
  private final int maxFormKeys;

  JettyServer(int maxPostSize, int maxFormKeys) {
    this.maxPostSize = maxPostSize;
    this.maxFormKeys = maxFormKeys;
  }

  /**
   * Creates a Jetty server.
   *
   * @param maxThreads maxThreads
   * @param minThreads minThreads
   * @param threadTimeoutMillis threadTimeoutMillis
   * @return a new jetty server instance
   */
  public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
    Server server;
    if (maxThreads > 0) {
      int min = (minThreads > 0) ? minThreads : 8;
      int idleTimeout = (threadTimeoutMillis > 0) ? threadTimeoutMillis : 60000;

      server = new Server(new QueuedThreadPool(maxThreads, min, idleTimeout));
    } else {
      server = new Server();
    }
    server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", maxPostSize);
    server.setAttribute("org.eclipse.jetty.server.Request.maxFormKeys", maxFormKeys);
    return server;
  }

  /**
   * Creates a Jetty server with supplied thread pool.
   *
   * @param threadPool thread pool
   * @return a new jetty server instance
   */
  @Override
  public Server create(ThreadPool threadPool) {
    Server server = threadPool != null ? new Server(threadPool) : new Server();
    server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", maxPostSize);
    server.setAttribute("org.eclipse.jetty.server.Request.maxFormKeys", maxFormKeys);
    return server;
  }
}
