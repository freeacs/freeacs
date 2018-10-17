package com.github.freeacs.web.app.input;

import java.util.List;

/**
 * A check box group input model.
 *
 * @param <T> the generic type
 * @author Jarl Andre Hubenthal
 */
public class CheckBoxGroup<T> extends InputSelectionModel<T> {
  /** Instantiates a new check box group. */
  CheckBoxGroup() {
    this(null, null, null);
  }

  /**
   * Instantiates a new check box group.
   *
   * @param input the input
   * @param checked the checked
   * @param items the items
   */
  CheckBoxGroup(Input input, List<T> checked, List<T> items) {
    super(input, checked, items);
  }

  /**
   * Gets the selected.
   *
   * @return the selected
   */
  @SuppressWarnings("unchecked")
  public List<T> getSelected() {
    return (List<T>) selected;
  }

  /**
   * Sets the selected.
   *
   * @param selected the new selected
   */
  public void setSelected(List<T> selected) {
    this.selected = selected;
  }
}
