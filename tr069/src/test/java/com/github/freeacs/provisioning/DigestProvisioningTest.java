package com.github.freeacs.provisioning;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.github.freeacs.utils.DigestUtil;
import com.github.freeacs.utils.MysqlDataSourceInitializer;
import com.github.freeacs.utils.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpCookie;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static org.junit.jupiter.api.Assertions.*;
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
@Import(RestTemplateConfig.class)
@Slf4j
public class DigestProvisioningTest extends AbstractProvisioningTest implements AbstractMySqlIntegrationTest {

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            MysqlDataSourceInitializer.initialize(mysql, applicationContext);
        }
    }

    public DigestProvisioningTest(@Autowired RestTemplate restTemplate, @LocalServerPort Integer randomServerPort) {
        this.restTemplate = restTemplate;
        this.randomServerPort = randomServerPort;
    }

    public static final String DIGEST_REALM = "FreeACS";

    private RestTemplate restTemplate;

    private Integer randomServerPort;

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        mvc.perform(post("/tr069")).andExpect(status().isUnauthorized());
    }

    @Test
    public void restTemplateTest() throws SQLException, IOException {
        addUnitsToProvision();
        String url = "http://localhost:%d/tr069".formatted(randomServerPort);
        try {
            restTemplate.exchange(url, HttpMethod.POST,null, String.class);
        } catch(HttpStatusCodeException e) {
            log.info("Sending the Inform again");
            var digest = new DigestUtil(UNIT_ID, UNIT_PASSWORD, url, HttpMethod.POST.name());
            digest.parseWwwAuthenticate(e.getResponseHeaders().toSingleValueMap());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", digest.getTokenDigest());
            headers.set("Content-Type", "text/xml");
            headers.set("SOAPAction", "");
            HttpEntity<String> entity = new HttpEntity<>(getFileAsString("/provision/cpe/Inform.xml"), headers);
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());

            log.info("Sending get parameter values");
            List<HttpCookie> cookies = HttpCookie.parse(response.getHeaders().get("Set-Cookie").get(0));
            headers = new HttpHeaders();
            headers.set("Cookie", cookies.get(0).toString());
            headers.set("Authorization", digest.getTokenDigest());
            headers.set("Content-Type", "text/xml");
            headers.set("SOAPAction", "");
            entity = new HttpEntity<>(getFileAsString("/provision/cpe/GetParameterValuesResponse.xml"), headers);
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());

            log.info("sending set parameter values should end the session (this is normal)");
            entity = new HttpEntity<>(getFileAsString("/provision/cpe/SetParameterValuesResponse.xml"), headers);
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNull(response.getBody());

            log.info("Sending get parameter values again");
            // This should fail with empty body, since the session has been invalidated and we have not yet sent an Inform
            entity = new HttpEntity<>(getFileAsString("/provision/cpe/GetParameterValuesResponse.xml"), headers);
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNull(response.getBody());
        }
    }

//    @Test
//    public void discoverUnit() throws Exception {
//        addUnitsToProvision();
//        provisionUnit(digest(UNIT_ID).password(UNIT_PASSWORD).realm(DIGEST_REALM));
//    }
}
