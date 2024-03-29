package com.github.freeacs.shell.tools;

public class Row {
  private boolean error;
  private boolean warn;
  private final Data[] dataArray;
  private final int lineNumber;

  public Row(int lineNumber, int size) {
    this.lineNumber = lineNumber;
    dataArray = new Data[size];
  }

  /** Index is 0-based (starts counting from 0). */
  public void addData(int index, Data data) {
    dataArray[index] = data;
  }

  public Data[] getDataArray() {
    return dataArray;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public boolean isWarn() {
    return warn;
  }

  public void setWarn(boolean warn) {
    this.warn = warn;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String toString() {
    StringBuilder retStr = new StringBuilder();
    for (Data data : dataArray) {
      retStr.append(data.getValue()).append(" ");
    }
    return retStr.toString();
  }
}
