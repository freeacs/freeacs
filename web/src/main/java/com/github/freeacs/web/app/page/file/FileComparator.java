package com.github.freeacs.web.app.page.file;

import com.github.freeacs.common.util.NaturalComparator;
import com.github.freeacs.dbi.File;
import java.util.Comparator;

/**
 * A file comparator that supports Id,Name,Version and Date.
 *
 * @author Jarl Andre Hubenthal
 */
public class FileComparator implements Comparator<File> {
  /** The Constant ID. */
  public static final int ID = 1;

  /** The Constant NAME. */
  public static final int NAME = 2;

  /** The Constant VERS. */
  public static final int VERS = 3;

  /** The Constant DATE. */
  public static final int DATE = 4;

  /** The field. */
  private int field;

  /**
   * Instantiates a new file comparator.
   *
   * @param field the field
   */
  public FileComparator(int field) {
    this.field = field;
  }

  /**
   * Checks if is.
   *
   * @param f the f
   * @return true, if successful
   */
  private boolean is(int f) {
    return field == f;
  }

  public int compare(File f1, File f2) {
    if (is(ID)) {
      return f1.getId().compareTo(f2.getId());
    } else if (is(NAME)) {
      return f1.getName().compareTo(f2.getName());
    } else if (is(VERS)) {
      return new NaturalComparator().compare(f1.getVersion(), f2.getVersion());
    } else if (is(DATE)) {
      int res = f1.getTimestamp().compareTo(f2.getTimestamp());
      if (res == -1) {
        return 1;
      } else if (res == 1) {
        return -1;
      }
      return res;
    } else {
      return 0;
    }
  }
}
