package com.github.freeacs.tr069;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-discover.properties")
public class ProvisioningDiscoverIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBasicAuthorization(String u, String p) {
        String auth = u + ":" + p;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        return "Basic " + new String( encodedAuth );
    }

    private String getFileContent(String fileName) {
        InputStream is = ProvisioningDiscoverIntegrationTest.class.getClassLoader().getResourceAsStream(fileName);
        String text;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        return text;
    }

    @Test
    public void discoverDeviceWithBasicChallenge() {
        ResponseEntity<String> challengeRes = this.restTemplate.postForEntity("/", getFileContent("discover/1_inform.xml"), String.class);
        assertThat(challengeRes.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        HttpHeaders headers = challengeRes.getHeaders();
        String set_cookie = headers.getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headersWithCookueAndBasicChallenge = new HttpHeaders();
        headersWithCookueAndBasicChallenge.set("Cookie", set_cookie);
        headersWithCookueAndBasicChallenge.set("Authorization", getBasicAuthorization("test", "test"));
        ResponseEntity<String> informResponse = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<Object>(getFileContent("discover/1_inform.xml"), headersWithCookueAndBasicChallenge), String.class);
        assertThat(informResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(informResponse.getBody()).isEqualToIgnoringWhitespace(getFileContent("discover/2_informResponse.xml"));
        headersWithCookueAndBasicChallenge.remove("Authorization");
        ResponseEntity<String> getGPNResponse = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<>(headersWithCookueAndBasicChallenge), String.class);
        assertThat(getGPNResponse.getBody().replaceAll(">FREEACS-(\\d+)<", ">FREEACS-0<")).isEqualToIgnoringWhitespace(getFileContent("discover/4_GetParameterNames.xml"));
        ResponseEntity<String> getParameterValuesRequest = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<Object>(getFileContent("discover/5_GetParameterNamesResponse.xml"), headersWithCookueAndBasicChallenge), String.class);
        assertThat(getParameterValuesRequest.getBody().replaceAll(">FREEACS-(\\d+)<", ">FREEACS-0<")).isEqualToIgnoringWhitespace(getFileContent("discover/6_GetParameterValues.xml"));
        ResponseEntity<String> setParameterValuesRequest = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<Object>(getFileContent("discover/7_GetParameterValuesResponse.xml"), headersWithCookueAndBasicChallenge), String.class);
        assertThat(setParameterValuesRequest.getBody().replaceAll(">FREEACS-(\\d+)<", ">FREEACS-0<").replaceAll(">(\\d+)<", ">0<")).isEqualToIgnoringWhitespace(getFileContent("discover/8_SetParameterValues.xml"));
        ResponseEntity<String> noContentResponse = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<Object>(getFileContent("discover/9_SetParameterValuesResponse.xml"), headersWithCookueAndBasicChallenge), String.class);
        assertThat(noContentResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(noContentResponse.getBody()).isNullOrEmpty();
    }
}
