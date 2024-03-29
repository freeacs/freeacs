package com.github.freeacs.dbi.report;

import lombok.Data;

@Data
public class Average {
  private Counter weightedCounter = new Counter();
  private Counter totalWeight = new Counter();
  private final long dividend;

  public Average() {
    this(1);
  }

  public Average(long dividend) {
    this.dividend = dividend;
  }

  public void add(long counter) {
    add(counter, 1);
  }

  public void add(String str) {
    if (str != null) {
      try {
        add(Long.parseLong(str), 1);
      } catch (NumberFormatException ignored) {
      }
    }
  }

  public void add(long counter, long weight) {
    weightedCounter.add(counter * weight);
    totalWeight.add(weight);
  }

  public void add(Average average) {
    if (average.getDividend() != getDividend()) {
      throw new IllegalArgumentException("Cannot add an average if dividend differs");
    }
    this.weightedCounter.add(average.getWeightedCounter());
    this.totalWeight.add(average.getTotalWeight());
  }

  public Long get() {
    if (totalWeight.get() == 0) {
      return null;
    }
    return Math.round((double) weightedCounter.get() / totalWeight.get());
  }

  public String toString() {
    return String.valueOf(get());
  }

  public Average clone() {
    Average clone = new Average(dividend);
    clone.setWeightedCounter(weightedCounter.clone());
    clone.setTotalWeight(totalWeight.clone());
    return clone;
  }
}
