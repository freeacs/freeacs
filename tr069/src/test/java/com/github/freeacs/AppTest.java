package com.github.freeacs;

import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.common.util.AbstractEmbeddedDataSourceClassTest;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.tr069.Properties;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class AppTest extends AbstractEmbeddedDataSourceClassTest {
  private static final Logger log = LoggerFactory.getLogger(AppTest.class);

  private final String cpe_inform = getFileAsString("/provision/cpe/Inform.xml");
  private final String cpe_getParameterValuesResponse = getFileAsString("/provision/cpe/GetParameterValuesResponse.xml");
  private final String cpe_setParameterValuesResponse =  getFileAsString("/provision/cpe/SetParameterValuesResponse.xml");
  private final String acs_informResponse = getFileAsString("/provision/acs/InformResponse.xml");

  public AppTest() throws IOException {
  }

  @BeforeClass
  public static void init() throws SQLException {
    ValueInsertHelper.insert(dataSource);
    Config baseConfig = ConfigFactory.load("application.conf");
    App.routes(dataSource, new Properties(baseConfig), ExecutorWrapperFactory.create(1));
    Spark.awaitInitialization();
  }

  @AfterClass
  public static void after() throws SQLException {
    Spark.stop();
    Sleep.terminateApplication();
    dataSource.unwrap(HikariDataSource.class).close();
  }

  @Test
  public void ok() throws UnirestException {
    HttpResponse<String> response = Unirest.get("http://localhost:4567/tr069/ok").asString();
    assertEquals("FREEACSOK", response.getBody());
  }

  @Test
  public void fails() throws UnirestException {
    HttpResponse<String> response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .header("Content-type", "text/xml")
            .asString();
    assertEquals("Unauthorized", response.getBody());
    assertEquals(401, response.getStatus());
  }

  @Test
  public void authenticated() throws UnirestException {
    HttpResponse<String> response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .header("Content-type", "text/xml")
            .asString();
    assertNull(response.getBody());
    assertEquals(204, response.getStatus());
  }

  @Test
  public void testUrls() throws UnirestException {
    assertInform("");
    assertInform("/");
    assertInform("/prov");
  }

  @Test
  public void provision() throws UnirestException {
    assertInform("/prov");
    HttpResponse<String> response;
    String body;
    response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .header("Content-type", "text/xml")
            .asString();
    body = response.getBody();
    assertTrue(body.contains("cwmp:GetParameterValues"));
    assertTrue(body.contains("<string>InternetGatewayDevice.DeviceInfo.VendorConfigFile.</string>"));
    assertTrue(body.contains("<string>InternetGatewayDevice.ManagementServer.PeriodicInformInterval</string>"));
    assertEquals(200, response.getStatus());
    response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .header("Content-type", "text/xml")
            .body(cpe_getParameterValuesResponse)
            .asString();
    body = response.getBody();
    assertTrue(body.contains("cwmp:SetParameterValues"));
    assertTrue(body.contains("<Name>InternetGatewayDevice.ManagementServer.PeriodicInformInterval</Name>"));
    assertTrue(body.contains("<ParameterKey>No data in DB</ParameterKey>"));
    assertEquals(200, response.getStatus());
    response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .header("Content-type", "text/xml")
            .body(cpe_setParameterValuesResponse)
            .asString();
    assertNull(response.getBody());
    assertEquals(204, response.getStatus());
    response =
            Unirest.post("http://localhost:4567/tr069/prov")
                    .basicAuth("test123", "password")
                    .header("Content-type", "text/xml")
                    .asString();
    assertNull(response.getBody());
    assertEquals(204, response.getStatus());
  }

  private void assertInform(final String provUrl) throws UnirestException {
    final String url = "http://localhost:4567/tr069" + provUrl;
    log.info("Using url: " + url);
    HttpResponse<String> response =
        Unirest.post(url)
            .basicAuth("test123", "password")
            .header("Content-type", "text/xml")
            .body(cpe_inform)
            .asString();
    String fromAcs = response.getBody().replaceAll("\\s","");
    String expectedFromAcs = acs_informResponse.replaceAll("\\s","");
    assertTrue(expectedFromAcs.equalsIgnoreCase(fromAcs));
    assertEquals(200, response.getStatus());
  }
}
