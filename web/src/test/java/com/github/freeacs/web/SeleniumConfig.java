package com.github.freeacs.web;

import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class SeleniumConfig {

  private WebDriver driver;

  public SeleniumConfig(long timeout) {
    FirefoxBinary firefoxBinary = new FirefoxBinary();
    firefoxBinary.addCommandLineOptions("--headless");
    FirefoxOptions firefoxOptions = new FirefoxOptions();
    firefoxOptions.setBinary(firefoxBinary);
    driver = new FirefoxDriver(firefoxOptions);
    driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
  }

  public WebDriver getDriver() {
    return driver;
  }
}
