package com.github.freeacs.dbi.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Key implements Comparable<Key> {
  public static SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyyMM");
  public static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyyMMdd");
  public static SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("yyyyMMddHH");
  public static SimpleDateFormat MINUTE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
  public static SimpleDateFormat SECOND_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

  private Date tms;
  private PeriodType periodType;
  private List<KeyElement> additionalKeys = new ArrayList<>();
  private String tmsStr;
  private String periodTypeStr;

  public Key(Date tms, PeriodType periodType) {
    this(tms, periodType, (KeyElement) null);
  }

  public Key(Date tms, PeriodType periodType, KeyElement... additionalKeys) {
    this.tms = tms;
    this.periodType = periodType;
    this.tmsStr = getTmsStr();
    this.periodTypeStr = getPeriodTypeStr();
    java.util.Collections.addAll(this.additionalKeys, additionalKeys);
  }

  protected Key clone(PeriodType pt) {
    KeyElement[] keArr = new KeyElement[additionalKeys.size()];
    additionalKeys.toArray(keArr);
    return new Key(tms, pt, keArr);
  }

  private String getTmsStr() {
    if (periodType == PeriodType.ETERNITY) {
      return "";
    }
    if (periodType == PeriodType.MONTH) {
      return MONTH_FORMAT.format(tms);
    } else if (periodType == PeriodType.DAY) {
      return DAY_FORMAT.format(tms);
    } else if (periodType == PeriodType.HOUR) {
      return HOUR_FORMAT.format(tms);
    } else if (periodType == PeriodType.MINUTE) {
      return MINUTE_FORMAT.format(tms);
    } else if (periodType == PeriodType.SECOND) {
      return SECOND_FORMAT.format(tms);
    }
    return String.valueOf(tms);
  }

  private String getPeriodTypeStr() {
    if (periodType == PeriodType.MONTH) {
      return "MONTH";
    } else if (periodType == PeriodType.DAY) {
      return "DAY";
    } else if (periodType == PeriodType.HOUR) {
      return "HOUR";
    } else if (periodType == PeriodType.MINUTE) {
      return "MINUTE";
    } else if (periodType == PeriodType.SECOND) {
      return "SECOND";
    }
    return "UNKNOWN";
  }

  public String toString() {
    return getKeyString();
  }

  public KeyElement getKeyElement(String keyName) {
    for (KeyElement ke : additionalKeys) {
      if (ke.getName().equals(keyName)) {
        return ke;
      }
    }
    return null;
  }

  public String getKeyString() {
    StringBuilder sb = new StringBuilder();
    sb.append(tmsStr);
    sb.append("-");
    sb.append(periodTypeStr);
    sb.append("-");
    for (int j = 0; j < additionalKeys.size(); j++) {
      sb.append(additionalKeys.get(j).getValue());
      if (j < additionalKeys.size() - 1) {
        sb.append("-");
      }
    }
    return sb.toString();
  }

  public String getKeyStringFallbackOnMethodName(boolean time, String method, String... keys) {
    StringBuilder sb = new StringBuilder();
    if (time) {
      sb.append(tmsStr);
      sb.append("|");
      sb.append(periodTypeStr);
      sb.append("|");
    }
    for (String key : keys) {
      boolean match = false;
      for (KeyElement additionalKey : additionalKeys) {
        if (additionalKey.getName().equals(key)) {
          match = true;
          sb.append(additionalKey.getValue());
          sb.append("|");
        }
      }
      if (!match) {
        throw new IllegalArgumentException(
            "The keyName " + key + " was not recognized in this key");
      }
    }
    String s = sb.toString();
    if ("".equals(s)) {
      return "Total (" + method + ")";
    } else {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }

  public String getKeyString(boolean time, String... keys) {
    StringBuilder sb = new StringBuilder();
    if (time) {
      sb.append(tmsStr);
      sb.append("|");
      sb.append(periodTypeStr);
      sb.append("|");
    }
    for (String key : keys) {
      boolean match = false;
      for (KeyElement additionalKey : additionalKeys) {
        if (additionalKey.getName().equals(key)) {
          match = true;
          sb.append(additionalKey.getValue());
          sb.append("|");
        }
      }
      if (!match) {
        throw new IllegalArgumentException(
            "The keyName " + key + " was not recognized in this key");
      }
    }
    String s = sb.toString();
    if ("".equals(s)) {
      return "Total";
    } else {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }

  public boolean equals(Object o) {
    return o instanceof Key && getKeyString().equals(((Key) o).getKeyString());
  }

  public int hashCode() {
    return getKeyString().hashCode();
  }

  public int compareTo(Key k) {
    return getKeyString().compareTo(k.getKeyString());
  }

  public Key transform(String... keyNames) {
    KeyElement[] transformedKeys = new KeyElement[keyNames.length];
    for (int i = 0; i < keyNames.length; i++) {
      String keyName = keyNames[i];
      boolean match = false;
      for (KeyElement keyElement : additionalKeys) {
        if (keyName.equals(keyElement.getName())) {
          match = true;
          transformedKeys[i] = keyElement;
        }
      }
      if (!match) {
        throw new IllegalArgumentException(
            "The keyName "
                + keyName
                + " used in the tranform() method was not recognized in this key");
      }
    }
    return new Key(tms, periodType, transformedKeys);
  }

  public Date getTms() {
    return tms;
  }

  public PeriodType getPeriodType() {
    return periodType;
  }

  public List<KeyElement> getAdditionalKeys() {
    return additionalKeys;
  }
}
