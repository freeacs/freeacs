package com.github.freeacs.web;

import static com.github.freeacs.common.util.DataSourceHelper.inMemoryDataSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.app.util.WebProperties;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariDataSource;
import freemarker.template.Configuration;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import spark.Spark;

public class AppTest {

  private static DataSource dataSource;

  private static SeleniumTest seleniumTest;

  @BeforeClass
  public static void setUp() throws SQLException {
    dataSource = inMemoryDataSource();
    dataSource
        .getConnection()
        .createStatement()
        .execute(
            "CREATE ALIAS DATE_FORMAT FOR \"com.github.freeacs.web.H2CustomFunctions.convertDatetimeToString\";");
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
    Thread.sleep(1000);
    WebElement searchButton = seleniumTest.getElementById("submitSearchButton");
    assertEquals("FreeACS Web | Search", seleniumTest.getTitle());
    searchButton.click();
    Thread.sleep(1000);
    WebElement unitLink = seleniumTest.getLinkByText("test123");
    unitLink.click();
    Thread.sleep(1000);
    WebElement unitConfigurationLink = seleniumTest.getLinkByText("Go to Unit configuration");
    assertEquals(
        "FreeACS Web | Unit Dashboard | test123 | Default | Test", seleniumTest.getTitle());
    unitConfigurationLink.click();
    Thread.sleep(1000);
    WebElement logoutLink = seleniumTest.getLinkByText("Secret");
    assertEquals(
        "FreeACS Web | Unit Configuration | test123 | Default | Test", seleniumTest.getTitle());
    assertNotNull(logoutLink);
    seleniumTest.goBack();
    Thread.sleep(1000);
    WebElement unitHistoryLink = seleniumTest.getLinkByText("Go to Unit history");
    unitHistoryLink.click();
    Thread.sleep(1000);
    WebElement syslogImage = seleniumTest.getElementById("SyslogImage");
    assertNotNull(syslogImage);
    logoutLink = seleniumTest.getLinkByText("Logout");
    assertEquals(
        "FreeACS Web | Unit Dashboard | test123 | Default | Test", seleniumTest.getTitle());
    logoutLink.click();
    Thread.sleep(1000);
    WebElement loginButton = seleniumTest.getElementByName("login");
    assertEquals("FreeACS Web | login", seleniumTest.getTitle());
    assertNotNull(loginButton);
  }
}
