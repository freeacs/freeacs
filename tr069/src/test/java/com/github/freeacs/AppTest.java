package com.github.freeacs;

import static org.junit.Assert.*;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

public class AppTest {
  private final String inform =
      "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
          + "                   xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n"
          + "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
          + "                   xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n"
          + "    <SOAP-ENV:Header>\n"
          + "        <cwmp:ID SOAP-ENV:mustUnderstand=\"1\">1</cwmp:ID>\n"
          + "    </SOAP-ENV:Header>\n"
          + "    <SOAP-ENV:Body>\n"
          + "        <cwmp:Inform>\n"
          + "            <DeviceId>\n"
          + "                <Manufacturer>FakeManufacturer</Manufacturer>\n"
          + "                <OUI>000000</OUI>\n"
          + "                <ProductClass>FakeProductClass</ProductClass>\n"
          + "                <SerialNumber>FakeSerialNumber</SerialNumber>\n"
          + "            </DeviceId>\n"
          + "            <Event SOAP-ENC:arrayType=\"cwmp:EventStruct[3]\">\n"
          + "                <EventStruct>\n"
          + "                    <EventCode>0 BOOTSTRAP</EventCode>\n"
          + "                    <CommandKey>TR069_FakeManufacturer_HOMEGATEWAY</CommandKey>\n"
          + "                </EventStruct>\n"
          + "                <EventStruct>\n"
          + "                    <EventCode>1 BOOT</EventCode>\n"
          + "                    <CommandKey></CommandKey>\n"
          + "                </EventStruct>\n"
          + "                <EventStruct>\n"
          + "                    <EventCode>4 VALUE CHANGE</EventCode>\n"
          + "                    <CommandKey></CommandKey>\n"
          + "                </EventStruct>\n"
          + "            </Event>\n"
          + "            <MaxEnvelopes>1</MaxEnvelopes>\n"
          + "            <CurrentTime>1970-01-02T00:08:34</CurrentTime>\n"
          + "            <RetryCount>0</RetryCount>\n"
          + "            <ParameterList SOAP-ENC:arrayType=\"cwmp:ParameterValueStruct[10]\">\n"
          + "                <ParameterValueStruct>\n"
          + "                    <Name>InternetGatewayDevice.DeviceSummary</Name>\n"
          + "                    <Value xsi:type=\"xsd:string\">InternetGatewayDevice:1.0[](Baseline:1, EthernetLAN:4,GE:4,WiFi:1,\n"
          + "                        PONWAN:1, Voip:0, Time:1, IPPing:1)\n"
          + "                    </Value>\n"
          + "                </ParameterValueStruct>\n"
          + "                <ParameterValueStruct>\n"
          + "                    <Name>InternetGatewayDevice.DeviceInfo.SpecVersion</Name>\n"
          + "                    <Value xsi:type=\"xsd:string\">1.0</Value>\n"
          + "                </ParameterValueStruct>\n"
          + "                <ParameterValueStruct>\n"
          + "                    <Name>InternetGatewayDevice.DeviceInfo.HardwareVersion</Name>\n"
          + "                    <Value xsi:type=\"xsd:string\">V5.2</Value>\n"
          + "                </ParameterValueStruct>\n"
          + "                <ParameterValueStruct>\n"
          + "                    <Name>InternetGatewayDevice.DeviceInfo.SoftwareVersion</Name>\n"
          + "                    <Value xsi:type=\"xsd:string\">V5.2.10P4T26</Value>\n"
          + "                </ParameterValueStruct>\n"
          + "            </ParameterList>\n"
          + "        </cwmp:Inform>\n"
          + "    </SOAP-ENV:Body>\n"
          + "</SOAP-ENV:Envelope>";
  private final String getParameterNamesResponse =
      "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
          + "                   xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n"
          + "                   xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n"
          + "    <SOAP-ENV:Header>\n"
          + "        <cwmp:ID SOAP-ENV:mustUnderstand=\"1\">FREEACS-0</cwmp:ID>\n"
          + "    </SOAP-ENV:Header>\n"
          + "    <SOAP-ENV:Body>\n"
          + "        <cwmp:GetParameterNamesResponse>\n"
          + "            <ParameterList SOAP-ENC:arrayType=\"cwmp:ParameterInfoStruct[1384]\">\n"
          + "                <ParameterInfoStruct>\n"
          + "                    <Name>InternetGatewayDevice.</Name>\n"
          + "                    <Writable>0</Writable>\n"
          + "                </ParameterInfoStruct>\n"
          + "            </ParameterList>\n"
          + "        </cwmp:GetParameterNamesResponse>\n"
          + "    </SOAP-ENV:Body>\n"
          + "</SOAP-ENV:Envelope>\n";
  private final String getParameterValuesResponse =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
          + "                   xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n"
          + "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
          + "                   xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n"
          + "    <SOAP-ENV:Header>\n"
          + "        <cwmp:ID SOAP-ENV:mustUnderstand=\"1\">FREEACS-0</cwmp:ID>\n"
          + "    </SOAP-ENV:Header>\n"
          + "    <SOAP-ENV:Body SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"
          + "        <cwmp:GetParameterValuesResponse>\n"
          + "            <ParameterList xsi:type=\"SOAP-ENC:Array\" SOAP-ENC:arrayType=\"cwmp:ParameterValueStruct[9]\">\n"
          + "                <ParameterValueStruct>\n"
          + "                    <Name>InternetGatewayDevice.DeviceInfo.SoftwareVersion</Name>\n"
          + "                    <Value xsi:type=\"xsd:string\">v1.1.91</Value>\n"
          + "                </ParameterValueStruct>\n"
          + "                <ParameterValueStruct>\n"
          + "                    <Name>InternetGatewayDevice.ManagementServer.PeriodicInformInterval</Name>\n"
          + "                    <Value xsi:type=\"xsd:unsignedInt\">360</Value>\n"
          + "                </ParameterValueStruct>\n"
          + "            </ParameterList>\n"
          + "        </cwmp:GetParameterValuesResponse>\n"
          + "    </SOAP-ENV:Body>\n"
          + "</SOAP-ENV:Envelope>";
  private final String setParameterValuesResponse =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
          + "                   xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n"
          + "    <SOAP-ENV:Header>\n"
          + "        <cwmp:ID SOAP-ENV:mustUnderstand=\"1\">FREEACS-0</cwmp:ID>\n"
          + "    </SOAP-ENV:Header>\n"
          + "    <SOAP-ENV:Body SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"
          + "        <cwmp:SetParameterValuesResponse>\n"
          + "            <Status>0</Status>\n"
          + "        </cwmp:SetParameterValuesResponse>\n"
          + "    </SOAP-ENV:Body>\n"
          + "</SOAP-ENV:Envelope>";

  @BeforeClass
  public static void init() throws SQLException {
    DataSource ds = dataSource();
    ValueInsertHelper.insert(ds);
    Config baseConfig = ConfigFactory.load("application.properties");
    App.routes(baseConfig, ds);
    Spark.awaitInitialization();
  }

  @AfterClass
  public static void after() {
    Spark.stop();
  }

  @Test
  public void ok() throws UnirestException {
    HttpResponse<String> response = Unirest.get("http://localhost:4567/tr069/ok").asString();
    assertEquals("FREEACSOK", response.getBody());
  }

  @Test
  public void fails() throws UnirestException {
    HttpResponse<String> response = Unirest.post("http://localhost:4567/tr069/prov").asString();
    assertEquals("Unauthorized", response.getBody());
    assertEquals(401, response.getStatus());
  }

  @Test
  public void authenticated() throws UnirestException {
    HttpResponse<String> response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .asString();
    assertNull(response.getBody());
    assertEquals(204, response.getStatus());
  }

  @Test
  public void provision() throws UnirestException {
    HttpResponse<String> response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .body(inform)
            .asString();
    assertEquals(
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n"
            + "<soapenv:Header>\n"
            + "\t<cwmp:ID soapenv:mustUnderstand=\"1\">1</cwmp:ID>\n"
            + "</soapenv:Header>\n"
            + "<soapenv:Body>\n"
            + "\t\t<cwmp:InformResponse>\n"
            + "\t\t\t<MaxEnvelopes>1</MaxEnvelopes>\n"
            + "\t\t</cwmp:InformResponse>\n"
            + "</soapenv:Body>\n"
            + "</soapenv:Envelope>\n",
        response.getBody());
    assertEquals(200, response.getStatus());
    response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .asString();
    assertTrue(
        response
            .getBody()
            .endsWith(
                "<soapenv:Body>\n"
                    + "\t\t<cwmp:GetParameterValues>\n"
                    + "\t\t\t<ParameterNames soapenc:arrayType=\"xsd:string[2]\">\n"
                    + "\t\t\t\t<string>InternetGatewayDevice.DeviceInfo.VendorConfigFile.</string>\n"
                    + "\t\t\t\t<string>InternetGatewayDevice.ManagementServer.PeriodicInformInterval</string>\n"
                    + "\t\t\t</ParameterNames>\n"
                    + "\t\t</cwmp:GetParameterValues>\n"
                    + "</soapenv:Body>\n"
                    + "</soapenv:Envelope>\n"));
    assertEquals(200, response.getStatus());
    response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .body(getParameterNamesResponse)
            .asString();
    assertTrue(
        response
            .getBody()
            .endsWith(
                "<soapenv:Body>\n"
                    + "\t\t<cwmp:GetParameterValues>\n"
                    + "\t\t\t<ParameterNames soapenc:arrayType=\"xsd:string[1]\">\n"
                    + "\t\t\t\t<string>InternetGatewayDevice.</string>\n"
                    + "\t\t\t</ParameterNames>\n"
                    + "\t\t</cwmp:GetParameterValues>\n"
                    + "</soapenv:Body>\n"
                    + "</soapenv:Envelope>\n"));
    assertEquals(200, response.getStatus());
    response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .body(getParameterValuesResponse)
            .asString();
    assertTrue(
        response
            .getBody()
            .contains(
                "<soapenv:Body>\n"
                    + "\t\t<cwmp:SetParameterValues>\n"
                    + "\t\t\t<ParameterList soapenc:arrayType=\"cwmp:ParameterValueStruct[1]\">\n"
                    + "\t\t\t\t<ParameterValueStruct>\n"
                    + "\t\t\t\t\t<Name>InternetGatewayDevice.ManagementServer.PeriodicInformInterval</Name>"));
    assertTrue(
        response
            .getBody()
            .endsWith(
                "\t\t\t\t</ParameterValueStruct>\n"
                    + "\t\t\t</ParameterList>\n"
                    + "\t\t\t<ParameterKey>No data in DB</ParameterKey>\n"
                    + "\t\t</cwmp:SetParameterValues>\n"
                    + "</soapenv:Body>\n"
                    + "</soapenv:Envelope>\n"));
    assertEquals(200, response.getStatus());
    response =
        Unirest.post("http://localhost:4567/tr069/prov")
            .basicAuth("test123", "password")
            .body(setParameterValuesResponse)
            .asString();
    assertNull(response.getBody());
    assertEquals(204, response.getStatus());
  }

  private static DataSource dataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
    hikariConfig.setConnectionTestQuery("VALUES 1");
    hikariConfig.addDataSourceProperty(
        "URL", "jdbc:h2:mem:testdb;MODE=MYSQL;INIT=RUNSCRIPT FROM 'classpath:h2-schema.sql';");
    hikariConfig.addDataSourceProperty("user", "sa");
    hikariConfig.addDataSourceProperty("password", "sa");
    return new HikariDataSource(hikariConfig);
  }
}
