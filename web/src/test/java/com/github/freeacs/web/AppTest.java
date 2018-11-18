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
  public void test() {
    String actualTitle = seleniumTest.getTitle();
    assertNotNull(actualTitle);
    assertEquals("FreeACS Web | login", actualTitle);
    seleniumTest.doLogin();
    WebElement searchButton = seleniumTest.getElement("submitSearchButton");
    assertEquals("FreeACS Web | Search", seleniumTest.getTitle());
    searchButton.click();
  }
}
