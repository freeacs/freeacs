package com.github.freeacs.dbi.report;

import java.util.Calendar;

public enum PeriodType {
  ETERNITY(-1),
  MONTH(Calendar.MONTH),
  DAY(Calendar.DAY_OF_MONTH),
  HOUR(Calendar.HOUR_OF_DAY),
  MINUTE(Calendar.MINUTE),
  SECOND(Calendar.SECOND);

  private int type;

  PeriodType(int type) {
    this.type = type;
  }

  public int getTypeInt() {
    return type;
  }

  public String getTypeStr() {
    if (type == -1) {
      return "ETERNITY";
    }
    if (type == Calendar.MONTH) {
      return "MONTH";
    } else if (type == Calendar.DAY_OF_MONTH) {
      return "DAY";
    } else if (type == Calendar.HOUR_OF_DAY) {
      return "HOUR";
    } else if (type == Calendar.MINUTE) {
      return "MINUTE";
    } else if (type == Calendar.SECOND) {
      return "SECOND";
    }
    return "UNKNOWN";
  }

  public static PeriodType[] getTypes() {
    return new PeriodType[] {MONTH, DAY, HOUR};
  }

  public boolean isLongerThan(PeriodType periodType) {
    return type < periodType.getTypeInt();
  }

  public static PeriodType getType(String name) {
    for (PeriodType type : getTypes()) {
      if (type.getTypeStr().equals(name)) {
        return type;
      }
    }
    return null;
  }
}
