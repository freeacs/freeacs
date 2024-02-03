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
  /** The download. */
  private Input download = Input.getStringInput("download");

  /** The regular. */
  private Input regular = Input.getStringInput("regular");

  /** The frequency. */
  private Input frequency = Input.getStringInput("frequency");

  /** The page. */
  private Input page = Input.getStringInput("page");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
