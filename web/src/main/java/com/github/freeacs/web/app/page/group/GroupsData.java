package com.github.freeacs.web.app.page.group;

import com.github.freeacs.web.app.input.InputData;
import java.util.Map;

/**
 * Group Overview input definition.
 *
 * @author Jarl Andre Hubenthal
 */
public class GroupsData extends InputData {
  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }
}
