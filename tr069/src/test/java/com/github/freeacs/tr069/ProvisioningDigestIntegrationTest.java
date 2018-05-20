package com.github.freeacs.tr069;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-digest.properties")
public class ProvisioningDigestIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String getDigestAuthorization(String header, String u, String p) {
        Map<String, String> props = DigestHelper.getDigestMap(header);
        String qop = props.get("qop");
        String cnonce = "cnonce";
        String nc = "nc";
        return "Digest " +
                "    username=\""+ u + "\"," +
                "    realm=\"" + props.get("realm") + "\"," +
                "    nonce=\"" + props.get("nonce") + "\"," +
                "    nc=\"" + nc + "\"," +
                "    qop=\"" + qop + "\"," +
                "    cnonce=\"" + cnonce + "\"," +
                "    response=\"" + DigestHelper.getDigestAuthentication(header, "/", nc, cnonce, qop, u, p)  + "\"," +
                "    method=\"POST\"," +
                "    uri=\"/\"";
    }

    private String getFileContent(String fileName) {
        InputStream is = ProvisioningDigestIntegrationTest.class.getClassLoader().getResourceAsStream(fileName);
        String text;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        return text;
    }

    @Test
    public void discoverDeviceWithDigestChallenge() {
        ResponseEntity<String> challengeRes = this.restTemplate.postForEntity("/", getFileContent("digest/1_inform.xml"), String.class);
        assertThat(challengeRes.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        HttpHeaders headers = challengeRes.getHeaders();
        String set_cookie = headers.getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headersWithCookueAndBasicChallenge = new HttpHeaders();
        headersWithCookueAndBasicChallenge.set("Cookie", set_cookie);
        headersWithCookueAndBasicChallenge.set("Authorization", getDigestAuthorization(challengeRes.getHeaders().getFirst("WWW-Authenticate"), "test123", "password"));
        ResponseEntity<String> informResponse = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<Object>(getFileContent("digest/1_inform.xml"), headersWithCookueAndBasicChallenge), String.class);
        assertThat(informResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(informResponse.getBody()).isEqualToIgnoringWhitespace(getFileContent("digest/2_informResponse.xml"));
        headersWithCookueAndBasicChallenge.remove("Authorization");
        ResponseEntity<String> getGPNResponse = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<>(headersWithCookueAndBasicChallenge), String.class);
        assertThat(getGPNResponse.getBody().replaceAll(">FREEACS-(\\d+)<", ">FREEACS-0<")).isEqualToIgnoringWhitespace(getFileContent("digest/4_GetParameterValues.xml"));
        ResponseEntity<String> setParameterValuesRequest = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<Object>(getFileContent("digest/5_GetParameterValuesResponse.xml"), headersWithCookueAndBasicChallenge), String.class);
        assertThat(setParameterValuesRequest.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(setParameterValuesRequest.getBody().replaceAll(">FREEACS-(\\d+)<", ">FREEACS-0<").replaceAll(">(\\d+)<", ">0<")).isEqualToIgnoringWhitespace(getFileContent("digest/6_SetParameterValues.xml"));
        ResponseEntity<String> noContentResponse = this.restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<Object>(getFileContent("digest/7_SetParameterValuesResponse.xml"), headersWithCookueAndBasicChallenge), String.class);
        assertThat(noContentResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(noContentResponse.getBody()).isNullOrEmpty();
    }
}
