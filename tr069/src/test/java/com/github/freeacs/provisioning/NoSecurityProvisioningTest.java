package com.github.freeacs.provisioning;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
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

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-no-security.properties",
        "classpath:application-discovery-off.properties"
})
@ContextConfiguration(initializers = NoSecurityProvisioningTest.DataSourceInitializer.class)
@Slf4j
public class NoSecurityProvisioningTest extends AbstractProvisioningTest implements AbstractMySqlIntegrationTest {

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            MysqlDataSourceInitializer.initialize(mysql, applicationContext);
        }
    }

    private final Integer randomServerPort;

    public NoSecurityProvisioningTest(@LocalServerPort Integer randomServerPort) {
        this.randomServerPort = randomServerPort;
    }

    @Test
    public void noContentOnMissingAuthentication() throws Exception {
        mvc.perform(post("/tr069")).andExpect(status().isNoContent());
    }

    @Test
    public void discoverUnit() throws Exception {
        addUnitsToProvision(dbi, UNIT_TYPE_NAME, UNIT_ID_AUTO);

        String url = "http://localhost:%d/tr069".formatted(randomServerPort);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/xml");
        headers.set("SOAPAction", "");

        HttpEntity<String> entity = new HttpEntity<>(getFileAsString("/provision/cpe/Inform.xml"), headers);
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
        entity = new HttpEntity<>(getFileAsString("/provision/cpe/Inform.xml"), headers);
        HttpEntity<String> finalEntity = entity;
        assertDoesNotThrow(() -> sendRequestWithHeaders(url, HttpMethod.POST, finalEntity));
    }
}
