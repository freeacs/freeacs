package com.github.freeacs.web.app.input;

import lombok.Getter;

import java.util.List;

/**
 * An abstract contract for input selection models.
 *
 * @param <T> the generic type
 * @author Jarl Andre Hubenthal
 */
@Getter
abstract class InputSelectionModel<T> {

  /** The items. */
  protected final List<T> items;

  /** The selected. */
  protected Object selected;

  /** The input. */
  protected final Input input;

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
}
