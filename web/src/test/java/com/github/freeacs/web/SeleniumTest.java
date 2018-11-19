package com.github.freeacs.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumTest {
  private static final long TIMEOUT = 10L;

  private SeleniumConfig config;

  public SeleniumTest(String url) {
    config = new SeleniumConfig(TIMEOUT);
    config.getDriver().get(url);
  }

  public void closeWindow() {
    this.config.getDriver().close();
  }

  public String getTitle() {
    return this.config.getDriver().getTitle();
  }

  public void goBack() {
    this.config.getDriver().navigate().back();
  }

  public void doLogin() {
    this.config.getDriver().findElement(By.name("username")).sendKeys("admin");
    this.config.getDriver().findElement(By.name("password")).sendKeys("freeacs");
    this.config.getDriver().findElement(By.name("login")).click();
  }

  public WebElement getElementById(String id) {
    WebDriverWait wait = new WebDriverWait(this.config.getDriver(), TIMEOUT);
    return wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
  }

  public WebElement getElementByName(String id) {
    WebDriverWait wait = new WebDriverWait(this.config.getDriver(), TIMEOUT);
    return wait.until(ExpectedConditions.presenceOfElementLocated(By.name(id)));
  }

  public WebElement getLinkByText(String txt) {
    WebDriverWait wait = new WebDriverWait(this.config.getDriver(), TIMEOUT);
    return wait.until(
        ExpectedConditions.presenceOfElementLocated(
            By.xpath("//a[contains(text(),'" + txt + "')]")));
  }
}
