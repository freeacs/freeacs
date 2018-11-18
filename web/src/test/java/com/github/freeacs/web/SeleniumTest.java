package com.github.freeacs.web;

public class SeleniumTest {
  private SeleniumConfig config;

  public SeleniumTest(String url) {
    config = new SeleniumConfig();
    config.getDriver().get(url);
  }

  public void closeWindow() {
    this.config.getDriver().close();
  }

  public String getTitle() {
    return this.config.getDriver().getTitle();
  }
}
