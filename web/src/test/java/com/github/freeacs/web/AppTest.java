package com.github.freeacs.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.common.util.AbstractEmbeddedDataSourceClassTest;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.WebProperties;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariDataSource;
import freemarker.template.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import spark.Spark;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppTest extends AbstractEmbeddedDataSourceClassTest {

  private static SeleniumTest seleniumTest;

  @BeforeClass
  public static void setUp() throws SQLException {
    ValueInsertHelper.insert(dataSource);
    Config baseConfig = ConfigFactory.load("application.conf");
    WebProperties properties = new WebProperties(baseConfig);
    Configuration configuration = Freemarker.initFreemarker();
    ObjectMapper objectMapper = new ObjectMapper();
    App.routes(dataSource, properties, configuration, objectMapper);
    Spark.awaitInitialization();
    seleniumTest =
        new SeleniumTest(
            "http://localhost:" + properties.getServerPort() + properties.getContextPath());
  }

  @AfterClass
  public static void tearDown() throws SQLException {
    seleniumTest.closeWindow();
    Spark.stop();
    Sleep.terminateApplication();
    dataSource.unwrap(HikariDataSource.class).close();
  }

  @Test
  public void test() throws InterruptedException {
    String actualTitle = seleniumTest.getTitle();
    assertNotNull(actualTitle);
    assertEquals("FreeACS Web | login", actualTitle);
    seleniumTest.doLogin();
    WebElement searchButton = seleniumTest.getElementById("submitSearchButton");
    assertEquals("FreeACS Web | Search", seleniumTest.getTitle());
    searchButton.click();
    WebElement unitLink = seleniumTest.getLinkByText("test123");
    unitLink.click();
    WebElement unitConfigurationLink = seleniumTest.getLinkByText("Go to Unit configuration");
    assertEquals(
        "FreeACS Web | Unit Dashboard | test123 | Default | Test", seleniumTest.getTitle());
    unitConfigurationLink.click();
    Thread.sleep(1000);
    WebElement logoutLink = seleniumTest.getLinkByText("Logout");
    logoutLink.click();
    WebElement loginButton = seleniumTest.getElementByName("login");
    assertEquals("FreeACS Web | login", seleniumTest.getTitle());
    assertNotNull(loginButton);
  }
}
