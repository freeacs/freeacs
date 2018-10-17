package com.github.freeacs.core.util;

public class UnitJobResult {
  private boolean confirmedFailure;
  private boolean unconfirmedFailure;

  /**
   * The method sets the confirmed flag, but also return -1, 0 or 1, depending upon these
   * conditions: 1: change to confirmedFailure 0: no change -1: change to !confirmedFailure
   */
  public int setConfirmed(int confirmed) {
    int result = 0;
    if (confirmed > 0) {
      if (!confirmedFailure) {
        result = 1;
      }
      confirmedFailure = true;
    } else {
      if (confirmedFailure) {
        result = -1;
      }
      confirmedFailure = false;
    }
    return result;
  }

  public int setUnconfirmed(int unconfirmed) {
    int result = 0;
    if (unconfirmed > 0) {
      if (!unconfirmedFailure) {
        result = 1;
      }
      unconfirmedFailure = true;
    } else {
      if (unconfirmedFailure) {
        result = -1;
      }
      unconfirmedFailure = false;
    }
    return result;
  }

  public boolean isConfirmedFailure() {
    return confirmedFailure;
  }

  public boolean isUnconfirmedFailure() {
    return unconfirmedFailure;
  }
}
