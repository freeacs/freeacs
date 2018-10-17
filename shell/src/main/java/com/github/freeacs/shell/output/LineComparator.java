package com.github.freeacs.shell.output;

import java.util.Comparator;
import java.util.List;

public class LineComparator implements Comparator<Line> {
  private List<LineComparatorColumn> columnsToSort;

  public LineComparator(List<LineComparatorColumn> columnsToSort) {
    this.columnsToSort = columnsToSort;
  }

  private int compareImpl(String s1, String s2, LineComparatorColumn lcc) {
    if (s1 == null && s2 == null) {
      return 0;
    }
    if (s1 == null && s2 != null) {
      return -1;
    }
    if (s1 != null && s2 == null) {
      return 1;
    }
    if ("NULL".equals(s1) && "NULL".equals(s2)) {
      return 0;
    }
    if ("NULL".equals(s1) && !"NULL".equals(s2)) {
      return 1;
    }
    if (!"NULL".equals(s1) && "NULL".equals(s2)) {
      return -1;
    }

    if (lcc.getSortType().equals(LineComparatorColumn.SORT_NUM)) {
      try {
        int i1 = Integer.parseInt(s1);
        int i2 = Integer.parseInt(s2);
        return i1 - i2;
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException(
            "Column " + (lcc.getColumnIndex() + 1) + " could not be sorted as numbers.");
      }
    } else {
      return s1.compareTo(s2);
    }
  }

  @Override
  public int compare(Line o1, Line o2) {
    //		if (o1 == null && o2 == null)
    //			return 0;
    //		if (o1 == null && o2 != null)
    //			return -1;
    //		if (o1 != null && o2 == null)
    //			return 1;
    LineComparatorColumn decidingColumn = null;
    int compareVal = 0;
    for (LineComparatorColumn lcc : columnsToSort) {
      decidingColumn = lcc;
      if (o1.getValues().size() > lcc.getColumnIndex()
          && o2.getValues().size() > lcc.getColumnIndex()) {
        String val1 = o1.getValues().get(lcc.getColumnIndex());
        String val2 = o2.getValues().get(lcc.getColumnIndex());
        compareVal = compareImpl(val1, val2, lcc);
        if (compareVal != 0) {
          break;
        }
      }
    }
    if (!decidingColumn.getOrder().equals(LineComparatorColumn.ASCENDING)) {
      if (compareVal > 0) {
        return -1;
      }
      if (compareVal < 0) {
        return 1;
      }
    }
    return compareVal;
  }
}
