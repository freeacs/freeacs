package com.github.freeacs.web.app.page.window;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/** The Class WindowData. */
public class WindowData extends InputData {
  /** The download. */
  private Input download = Input.getStringInput("download");

  /** The regular. */
  private Input regular = Input.getStringInput("regular");

  /** The frequency. */
  private Input frequency = Input.getStringInput("frequency");

  /** The page. */
  private Input page = Input.getStringInput("page");

  /**
   * Sets the download.
   *
   * @param download the new download
   */
  public void setDownload(Input download) {
    this.download = download;
  }

  /**
   * Gets the download.
   *
   * @return the download
   */
  public Input getDownload() {
    return download;
  }

  /**
   * Sets the regular.
   *
   * @param regular the new regular
   */
  public void setRegular(Input regular) {
    this.regular = regular;
  }

  /**
   * Gets the regular.
   *
   * @return the regular
   */
  public Input getRegular() {
    return regular;
  }

  /**
   * Sets the frequency.
   *
   * @param frequency the new frequency
   */
  public void setFrequency(Input frequency) {
    this.frequency = frequency;
  }

  /**
   * Gets the frequency.
   *
   * @return the frequency
   */
  public Input getFrequency() {
    return frequency;
  }

  /**
   * Sets the page.
   *
   * @param page the new page
   */
  public void setPage(Input page) {
    this.page = page;
  }

  /**
   * Gets the page.
   *
   * @return the page
   */
  public Input getPage() {
    return page;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
