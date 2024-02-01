package com.github.freeacs.provisioning;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.github.freeacs.utils.DigestUtil;
import com.github.freeacs.utils.MysqlDataSourceInitializer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-digest-security.properties",
        "classpath:application-discovery-off.properties"
})
@ContextConfiguration(initializers = DigestProvisioningTest.DataSourceInitializer.class)
@Slf4j
public class DigestProvisioningTest extends AbstractProvisioningTest implements AbstractMySqlIntegrationTest {

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            MysqlDataSourceInitializer.initialize(mysql, applicationContext);
        }
    }

    private final Integer randomServerPort;

    public DigestProvisioningTest(@LocalServerPort Integer randomServerPort) {
        this.randomServerPort = randomServerPort;
    }

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        mvc.perform(post("/tr069")).andExpect(status().isUnauthorized());
    }

    @Test
    public void restTemplateTest() throws Exception {
        addUnitsToProvision();
        String url = "http://localhost:%d/tr069".formatted(randomServerPort);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/xml");
        headers.set("SOAPAction", "");

        // Initial request to handle authentication
        // Must send initial request without authentication to get digest challenge
        String digestToken = null;
        try {
            HttpEntity<String> entity1 = new HttpEntity<>(getFileAsString("/provision/cpe/Inform.xml"), headers);
            restTemplate.exchange(url, HttpMethod.POST, entity1, String.class);
            fail("Should not be able to send an Inform without authentication");
        } catch (HttpStatusCodeException e) {
            log.info("Handling authentication");
            var digest = new DigestUtil(UNIT_ID, UNIT_PASSWORD, url, HttpMethod.POST.name());
            digest.parseWwwAuthenticate(e.getResponseHeaders().toSingleValueMap());
            digestToken = digest.getTokenDigest();
        }

        // Send Inform request
        HttpHeaders httpHeadersForAuthentication = new HttpHeaders();
        httpHeadersForAuthentication.addAll(headers);
        httpHeadersForAuthentication.set("Authorization", digestToken);
        HttpEntity<String> entity = new HttpEntity<>(getFileAsString("/provision/cpe/Inform.xml"), httpHeadersForAuthentication);
        ResponseEntity<String> response = sendRequestWithHeaders(url, HttpMethod.POST, entity);
        assertInformResponse(response);

        // Update headers with cookies from response
        updateHeadersWithCookies(headers, response);

        entity = new HttpEntity<>(null, headers);
        response = sendRequestWithHeaders(url, HttpMethod.POST, entity);
        assertGetParameterValuesRequest(response);

        // Send GetParameterValues request
        entity = new HttpEntity<>(getFileAsString("/provision/cpe/GetParameterValuesResponse.xml"), headers);
        response = sendRequestWithHeaders(url, HttpMethod.POST, entity);
        assertSetParameterValuesRequest(response);

        // Send SetParameterValues request and expect session end
        entity = new HttpEntity<>(getFileAsString("/provision/cpe/SetParameterValuesResponse.xml"), headers);
        response = sendRequestWithHeaders(url, HttpMethod.POST, entity);
        assertSessionEndResponse(response);

        // Attempt to send GetParameterValues again and expect failure due to session end
        entity = new HttpEntity<>(getFileAsString("/provision/cpe/GetParameterValuesResponse.xml"), headers);
        HttpEntity<String> finalEntity = entity;
        assertThrows(HttpStatusCodeException.class, () -> sendRequestWithHeaders(url, HttpMethod.POST, finalEntity));
    }
}
