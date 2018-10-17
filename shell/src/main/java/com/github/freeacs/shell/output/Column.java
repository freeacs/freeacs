package com.github.freeacs.shell.output;

public class Column {
  private int maxWidth;

  public void incWidthIfNecessary(int width) {
    if (width > maxWidth) {
      maxWidth = width;
    }
  }

  public int getMaxWidth() {
    return maxWidth;
  }
}
