package com.github.freeacs.dbi.report;

public class Counter {
  private long counter;
  private long dividend;

  public Counter() {
    this.dividend = 1;
  }

  public Counter(long dividend) {
    this.dividend = dividend;
  }

  public long getDividend() {
    return dividend;
  }

  public void inc() {
    counter++;
  }

  public void add(long op) {
    counter += op;
  }

  public void add(Counter counter) {
    this.counter += counter.get();
  }

  public void add(String str) {
    if (str != null) {
      try {
        counter += Long.parseLong(str);
      } catch (NumberFormatException ignored) {
      }
    }
  }

  public Long get() {
    return counter;
  }

  public void set(long op) {
    counter = op;
  }

  public String toString() {
    return String.valueOf(counter);
  }

  public Counter clone() {
    Counter clone = new Counter(dividend);
    clone.set(counter);
    return clone;
  }
}
