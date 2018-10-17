package com.github.freeacs.shell.help;

import java.util.ArrayList;
import java.util.List;

public class HelpExamples {
  private List<String> examples = new ArrayList<>();

  public void addExample(String example) {
    examples.add(example);
  }

  public List<String> getExamples() {
    return examples;
  }
}
