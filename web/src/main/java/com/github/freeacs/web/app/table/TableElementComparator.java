/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.freeacs.web.app.table;

import com.github.freeacs.common.util.NaturalComparator;
import java.util.Comparator;

/**
 * Sorts table data based on the table element name.
 *
 * @author Jarl Andre Hubenthal
 */
public class TableElementComparator implements Comparator<TableElement> {
  public int compare(TableElement element1, TableElement element2) {
    return new NaturalComparator().compare(element1.getName(), element2.getName());
  }
}
