package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import java.util.Map;

/** The Class UnitStatusRealTimeMosData. */
public class UnitStatusRealTimeMosData extends InputData {
  /** The start. */
  private Input start = Input.getDateInput("start", DateUtils.Format.WITH_SECONDS);

  /**
   * Sets the start.
   *
   * @param start the new start
   */
  public void setStart(Input start) {
    this.start = start;
  }

  /**
   * Gets the start.
   *
   * @return the start
   */
  public Input getStart() {
    return start;
  }

  /** The end. */
  private Input end = Input.getDateInput("end", DateUtils.Format.WITH_SECONDS);

  /**
   * Sets the end.
   *
   * @param end the new end
   */
  public void setEnd(Input end) {
    this.end = end;
  }

  /**
   * Gets the end.
   *
   * @return the end
   */
  public Input getEnd() {
    return end;
  }

  /** The channel. */
  private Input channel = Input.getIntegerInput("channel");

  /**
   * Sets the channel.
   *
   * @param channel the new channel
   */
  public void setChannel(Input channel) {
    this.channel = channel;
  }

  /**
   * Gets the channel.
   *
   * @return the channel
   */
  public Input getChannel() {
    return channel;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
