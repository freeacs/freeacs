package com.github.freeacs.web.app.input;

import java.util.Arrays;
import java.util.List;

/**
 * A single select dropdown model.
 *
 * @param <T> Any type
 * @author Jarl Andre Hubenthal
 */
public class DropDownSingleSelect<T> extends InputSelectionModel<T> {
  /** Instantiates a new drop down single select. */
  DropDownSingleSelect() {
    this(null, null, null);
  }

  /**
   * Instantiates a new drop down single select.
   *
   * @param input the input
   * @param selected the selected
   * @param items the items
   */
  DropDownSingleSelect(Input input, T selected, List<T> items) {
    super(input, selected, items);
  }

  /**
   * Gets the selected.
   *
   * @return the selected
   */
  @SuppressWarnings("unchecked")
  public T getSelected() {
    return (T) selected;
  }

  /**
   * Sets the selected.
   *
   * @param selected the new selected
   */
  public void setSelected(T selected) {
    this.selected = selected;
  }

  /**
   * Gets the selected or all items as list.
   *
   * @return the selected or all items as list
   */
  public List<T> getSelectedOrAllItemsAsList() {
    if (getSelected() != null) {
      return Arrays.asList(getSelected());
    }
    return getItems();
  }

  /**
   * Gets the selected or first item.
   *
   * @return the selected or first item
   */
  public T getSelectedOrFirstItem() {
    if (getSelected() != null) {
      return getSelected();
    }
    return getItems() != null && !getItems().isEmpty() ? getItems().get(0) : null;
  }
}
