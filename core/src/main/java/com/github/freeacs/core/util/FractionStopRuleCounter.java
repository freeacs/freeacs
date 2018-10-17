package com.github.freeacs.core.util;

import com.github.freeacs.dbi.Job.StopRule;
import com.github.freeacs.dbi.UnitJob;
import com.github.freeacs.dbi.UnitJobStatus;

public class FractionStopRuleCounter {
  private StopRule rule;

  private int CONFIRMED = 0;
  private int UNCONFIRMED = 1;
  private int[] counters = new int[2];

  private UnitResultMap<String, UnitJobResult> unitJobResults;

  public FractionStopRuleCounter(StopRule rule) {
    if (rule.getNumberMax() == null) {
      throw new IllegalArgumentException("This rule is not a fraction stop rule: " + rule);
    }
    this.rule = rule;
    unitJobResults = new UnitResultMap<>(rule.getNumberMax());
  }

  public void addResult(UnitJob uj) {
    UnitJobResult ujr = unitJobResults.get(uj.getUnitId());
    if (ujr == null) {
      ujr = new UnitJobResult();
      unitJobResults.put(uj.getUnitId(), ujr);
    }
    if (uj.getStatus().equals(UnitJobStatus.COMPLETED_OK)) {
      counters[CONFIRMED] += ujr.setConfirmed(0);
      counters[UNCONFIRMED] += ujr.setUnconfirmed(0);
    } else {
      counters[CONFIRMED] += ujr.setConfirmed(uj.getConfirmedFailed());
      counters[UNCONFIRMED] += ujr.setUnconfirmed(uj.getUnconfirmedFailed());
    }

    if (unitJobResults.getEldestEntry() != null) {
      UnitJobResult eldestEntry = unitJobResults.getEldestEntry().getValue();
      if (eldestEntry.isConfirmedFailure()) {
        counters[CONFIRMED]--;
      }
      if (eldestEntry.isUnconfirmedFailure()) {
        counters[UNCONFIRMED]--;
      }
      unitJobResults.setEldestEntry(null);
    }
  }

  /** To be used from updateRules() - special case. */
  public void addResult(UnitJobResult ujr) {
    if (ujr.isConfirmedFailure()) {
      counters[CONFIRMED]++;
    }
    if (ujr.isUnconfirmedFailure()) {
      counters[UNCONFIRMED]++;
    }
    if (unitJobResults.getEldestEntry() != null) {
      UnitJobResult eldestEntry = unitJobResults.getEldestEntry().getValue();
      if (eldestEntry.isConfirmedFailure()) {
        counters[CONFIRMED]--;
      }
      if (eldestEntry.isUnconfirmedFailure()) {
        counters[UNCONFIRMED]--;
      }
      unitJobResults.setEldestEntry(null);
    }
  }

  public boolean ruleMatch() {
    int failures = 0;
    if (rule.getRuleType() == StopRule.ANY_FAILURE_TYPE) {
      failures = counters[CONFIRMED] + counters[UNCONFIRMED];
    } else if (rule.getRuleType() == StopRule.CONFIRMED_FAILURE_TYPE) {
      failures = counters[CONFIRMED];
    } else {
      failures = counters[UNCONFIRMED];
    }
    return failures >= rule.getNumberLimit();
  }

  public String toString() {
    return rule
        + " (confirmed failures:"
        + counters[CONFIRMED]
        + ", unconfirmed failures:"
        + counters[UNCONFIRMED]
        + ")";
  }

  public StopRule getRule() {
    return rule;
  }

  public UnitResultMap<String, UnitJobResult> getUnitJobResults() {
    return unitJobResults;
  }
}
