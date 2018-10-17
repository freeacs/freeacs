package com.github.freeacs.shell.tools;

import java.util.ArrayList;
import java.util.List;

public class Data {
  private String value;
  private List<String> errorMessages = new ArrayList<>();
  private List<String> warningMessages = new ArrayList<>();
  private List<Row> duplicateRows = new ArrayList<>();
  private Row row;

  public Data(Row row, String value) {
    this.row = row;
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void addErrorMessage(String errorMessage) {
    this.errorMessages.add(errorMessage);
  }

  public void addWarningMessage(String warningMessage) {
    this.warningMessages.add(warningMessage);
  }

  public Row getRow() {
    return row;
  }

  public List<Row> getDuplicateRows() {
    return duplicateRows;
  }

  public void addDuplicateRow(Row row) {
    duplicateRows.add(row);
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }

  public List<String> getWarningMessages() {
    return warningMessages;
  }
}
