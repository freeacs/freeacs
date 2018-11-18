package com.github.freeacs.web;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

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

  public void doLogin() {
    this.config.getDriver().findElement(By.name("username")).sendKeys("admin");
    this.config.getDriver().findElement(By.name("password")).sendKeys("freeacs");
    this.config.getDriver().findElement(By.name("login")).click();
  }

  public WebElement getElement(String id) {
    WebDriverWait wait = new WebDriverWait(this.config.getDriver(), 5L);
    return wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
  }
}
