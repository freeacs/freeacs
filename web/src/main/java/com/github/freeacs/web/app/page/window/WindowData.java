package com.github.freeacs.web.app.page.window;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** The Class WindowData. */
@Setter
@Getter
public class WindowData extends InputData {
  /** The download.
   * -- GETTER --
   *  Gets the download.
   *
   *
   * -- SETTER --
   *  Sets the download.
   *
   @return the download
    * @param download the new download
   */
  private Input download = Input.getStringInput("download");

  /** The regular.
   * -- GETTER --
   *  Gets the regular.
   *
   *
   * -- SETTER --
   *  Sets the regular.
   *
   @return the regular
    * @param regular the new regular
   */
  private Input regular = Input.getStringInput("regular");

  /** The frequency.
   * -- GETTER --
   *  Gets the frequency.
   *
   *
   * -- SETTER --
   *  Sets the frequency.
   *
   @return the frequency
    * @param frequency the new frequency
   */
  private Input frequency = Input.getStringInput("frequency");

  /** The page.
   * -- GETTER --
   *  Gets the page.
   *
   *
   * -- SETTER --
   *  Sets the page.
   *
   @return the page
    * @param page the new page
   */
  private Input page = Input.getStringInput("page");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
