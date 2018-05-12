package com.github.freeacs.dbi.report;

public class Average {
	private Counter weightedCounter = new Counter();
	private Counter totalWeight = new Counter();
	private long dividend;

	public Average() {
		this(1);
	}

	private Counter getWeightedCounter() {
		return weightedCounter;
	}

	private Counter getTotalWeight() {
		return totalWeight;
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
			} catch (NumberFormatException nfe) {

			}
		}
	}

	public void add(long counter, long weight) {
		weightedCounter.add(counter * weight);
		totalWeight.add(weight);
	}

	public void add(Average average) {
		if (average.getDividend() != this.getDividend())
			throw new IllegalArgumentException("Cannot add an average if dividend differs");
		this.weightedCounter.add(average.getWeightedCounter());
		this.totalWeight.add(average.getTotalWeight());
	}

	public void setAverage(long counter, long weight) {
		weightedCounter = new Counter();
		totalWeight = new Counter();
		add(counter, weight);
	}

	public Long get() {
		if (totalWeight.get() == 0)
			return null;
		return (long) Math.round((double) weightedCounter.get() / (double) totalWeight.get());
	}

	public String toString() {
		return "" + get();
	}

	public long getDividend() {
		return dividend;
	}

	public Average clone() {
		Average clone = new Average(dividend);
		clone.setWeightedCounter(this.getWeightedCounter().clone());
		clone.setTotalWeight(this.getTotalWeight().clone());
		return clone;
	}

	private void setWeightedCounter(Counter weightedCounter) {
		this.weightedCounter = weightedCounter;
	}

	private void setTotalWeight(Counter totalWeight) {
		this.totalWeight = totalWeight;
	}
}
