package com.github.freeacs.web.app.input;

import java.util.List;

/**
 * A multi select dropdown model.
 *
 * @param <T> Any type
 * @author Jarl Andre Hubenthal
 */
public class DropDownMultiSelect<T> extends InputSelectionModel<T> {
  /** Instantiates a new drop down multi select. */
  DropDownMultiSelect() {
    this(null, null, null);
  }

  /**
   * Instantiates a new drop down multi select.
   *
   * @param input the input
   * @param selected the selected
   * @param items the items
   */
  DropDownMultiSelect(Input input, T[] selected, List<T> items) {
    super(input, selected, items);
  }

  /**
   * Gets the selected.
   *
   * @return the selected
   */
  @SuppressWarnings("unchecked")
  public T[] getSelected() {
    return (T[]) selected;
  }

  /**
   * Sets the selected.
   *
   * @param selected the new selected
   */
  public void setSelected(T[] selected) {
    this.selected = selected;
  }
}
