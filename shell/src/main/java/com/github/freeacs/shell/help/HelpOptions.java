package com.github.freeacs.shell.help;

import java.util.ArrayList;
import java.util.List;

public class HelpOptions {
  private List<HelpOption> options = new ArrayList<>();

  public void addOption(HelpOption helpOption) {
    options.add(helpOption);
  }

  public List<HelpOption> getOptions() {
    return options;
  }
}
