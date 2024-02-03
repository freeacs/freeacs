package com.github.freeacs.web.app.page.job;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Job Overview input definition.
 *
 * @author Jarl Andre Hubenthal
 */
@Setter
@Getter
public class JobsData extends InputData {
  /** The status. */
  private Input status = Input.getStringInput("status");

  /** The firmware. */
  private Input firmware = Input.getStringInput("firmware");

  /** The job name. */
  private Input jobName = Input.getStringInput("jobname");

  /** The filter. */
  private Input filter = Input.getStringInput("filterstring");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
