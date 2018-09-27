package com.github.freeacs.shell.output;

public class LineComparatorColumn {
  public static String SORT_ALFA = "a";
  public static String SORT_NUM = "n";

  public static String ASCENDING = "a";
  public static String DESCENDING = "d";

  private int columnIndex;
  private String sortType;
  private String order;

  public LineComparatorColumn(int columnIndex, String sortType, String order) {
    this.columnIndex = columnIndex;
    this.sortType = sortType;
    this.order = order;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public void setColumnIndex(int columnIndex) {
    this.columnIndex = columnIndex;
  }

  public String getSortType() {
    return sortType;
  }

  public void setSortType(String sortType) {
    this.sortType = sortType;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }
}
