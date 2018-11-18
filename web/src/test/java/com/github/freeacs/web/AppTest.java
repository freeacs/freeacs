package com.github.freeacs.web;

import static com.github.freeacs.common.util.DataSourceHelper.inMemoryDataSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.web.app.util.WebProperties;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

public class AppTest {

  private static DataSource ds;

  private static SeleniumTest seleniumTest;

  @BeforeClass
  public static void setUp() throws SQLException {
    ds = inMemoryDataSource();
    ValueInsertHelper.insert(ds);
    Config baseConfig = ConfigFactory.load("application.conf");
    App.routes(ds, new WebProperties(baseConfig), "/");
    Spark.awaitInitialization();
    seleniumTest = new SeleniumTest("http://localhost:4567/");
  }

  @AfterClass
  public static void tearDown() throws SQLException {
    seleniumTest.closeWindow();
    Spark.stop();
    Sleep.terminateApplication();
    ds.unwrap(HikariDataSource.class).close();
  }

  @Test
  public void test() {
    String actualTitle = seleniumTest.getTitle();
    assertNotNull(actualTitle);
    assertEquals("FreeACS Web | login", actualTitle);
  }
}
