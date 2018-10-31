package com.github.freeacs.common.scheduler;

public interface ExecutorWrapperFactory {
  static ExecutorWrapper create(int numThreads) {
    return new ExecutorWrapperImpl(numThreads);
  }

  static ExecutorWrapper create(
      int numThreads, String name, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
    return new ExecutorWrapperImpl(numThreads, name, uncaughtExceptionHandler);
  }
}
