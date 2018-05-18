package com.github.freeacs.tr069;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProvisioningIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testStatusPage() {
        String body = this.restTemplate.getForObject("/", String.class);
        assertThat(body).contains("xAPS TR-069 Server Monitoring Page");
    }

    public String getAuthHeader(String u, String p) {
        String auth = u + ":" + p;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        return "Basic " + new String( encodedAuth );
    }

    @Test
    public void discoverDevice() {
        String inform = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\"><SOAP-ENV:Header><cwmp:ID SOAP-ENV:mustUnderstand=\"1\">1</cwmp:ID></SOAP-ENV:Header><SOAP-ENV:Body><cwmp:Inform><DeviceId><Manufacturer>ZTE</Manufacturer><OUI>44F436</OUI><ProductClass>F600W</ProductClass><SerialNumber>ZTEEQAWF5J00191</SerialNumber></DeviceId><Event SOAP-ENC:arrayType=\"cwmp:EventStruct[3]\"><EventStruct><EventCode>0 BOOTSTRAP</EventCode><CommandKey>TR069_ZTE_HOMEGATEWAY</CommandKey></EventStruct><EventStruct><EventCode>1 BOOT</EventCode><CommandKey></CommandKey></EventStruct><EventStruct><EventCode>4 VALUE CHANGE</EventCode><CommandKey></CommandKey></EventStruct></Event><MaxEnvelopes>1</MaxEnvelopes><CurrentTime>1970-01-02T00:08:34</CurrentTime><RetryCount>0</RetryCount><ParameterList SOAP-ENC:arrayType=\"cwmp:ParameterValueStruct[10]\"><ParameterValueStruct><Name>InternetGatewayDevice.DeviceSummary</Name><Value xsi:type=\"xsd:string\">InternetGatewayDevice:1.0[](Baseline:1, EthernetLAN:4,GE:4,WiFi:1, PONWAN:1, Voip:0, Time:1, IPPing:1)</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.DeviceInfo.SpecVersion</Name><Value xsi:type=\"xsd:string\">1.0</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.DeviceInfo.HardwareVersion</Name><Value xsi:type=\"xsd:string\">V5.2</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.DeviceInfo.SoftwareVersion</Name><Value xsi:type=\"xsd:string\">V5.2.10P4T26</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.DeviceInfo.ProvisioningCode</Name><Value xsi:type=\"xsd:string\">TLCO.GRP2</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.ManagementServer.ConnectionRequestURL</Name><Value xsi:type=\"xsd:string\">http://10.90.13.32:58000</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.ManagementServer.ParameterKey</Name><Value xsi:type=\"xsd:string\">239b947d94b2cda452888bfd69d7daa9</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.2.ExternalIPAddress</Name><Value xsi:type=\"xsd:string\">10.90.13.32</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.ManagementServer.ConnectionRequestURL</Name><Value xsi:type=\"xsd:string\">http://10.90.13.32:58000</Value></ParameterValueStruct><ParameterValueStruct><Name>InternetGatewayDevice.ManagementServer.URL</Name><Value xsi:type=\"xsd:string\">http://172.31.8.82/</Value></ParameterValueStruct></ParameterList></cwmp:Inform></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        ResponseEntity<String> challengeRes = this.restTemplate.postForEntity("/", inform, String.class);
        assertThat(challengeRes.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        HttpHeaders headers = challengeRes.getHeaders();
        String set_cookie = headers.getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headersWithCookueAndBasicChallenge = new HttpHeaders();
        headersWithCookueAndBasicChallenge.set("Cookie", set_cookie);
        headersWithCookueAndBasicChallenge.set("Authorization", getAuthHeader("test", "test"));
        HttpEntity<?> httpEntity = new HttpEntity<Object>(inform, headersWithCookueAndBasicChallenge);
        ResponseEntity<String> informResponse = this.restTemplate.exchange("/", HttpMethod.POST, httpEntity, String.class);
        assertThat(informResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
