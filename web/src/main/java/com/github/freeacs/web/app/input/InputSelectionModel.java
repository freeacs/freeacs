package com.github.freeacs.web.app.input;

import java.util.List;

/**
 * An abstract contract for input selection models.
 *
 * @param <T> the generic type
 * @author Jarl Andre Hubenthal
 */
abstract class InputSelectionModel<T> {
  /**
   * Instantiates a new input selection model.
   *
   * @param input the input
   * @param selected the selected
   * @param items the items
   */
  protected InputSelectionModel(Input input, List<T> selected, List<T> items) {
    this.input = input;
    this.selected = selected;
    this.items = items;
  }

  /**
   * Instantiates a new input selection model.
   *
   * @param input the input
   * @param selected the selected
   * @param items the items
   */
  protected InputSelectionModel(Input input, T[] selected, List<T> items) {
    this.input = input;
    this.selected = selected;
    this.items = items;
  }

  /**
   * Instantiates a new input selection model.
   *
   * @param input the input
   * @param selected the selected
   * @param items the items
   */
  protected InputSelectionModel(Input input, T selected, List<T> items) {
    this.input = input;
    this.selected = selected;
    this.items = items;
  }

  /**
   * Gets the items.
   *
   * @return the items
   */
  public List<T> getItems() {
    return items;
  }

  /**
   * Sets the items.
   *
   * @param items the new items
   */
  public void setItems(List<T> items) {
    this.items = items;
  }

  /**
   * Gets the input.
   *
   * @return the input
   */
  public Input getInput() {
    return input;
  }

  /** The items. */
  protected List<T> items;

  /** The selected. */
  protected Object selected;

  /** The input. */
  protected Input input;
}
