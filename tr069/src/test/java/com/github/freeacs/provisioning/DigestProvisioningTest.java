package com.github.freeacs.provisioning;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.github.freeacs.utils.DigestUtil;
import com.github.freeacs.utils.MysqlDataSourceInitializer;
import com.github.freeacs.utils.RestTemplateConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-digest-security.properties",
        "classpath:application-discovery-off.properties",
        "classpath:application-fixedport.properties"
})
@ContextConfiguration(initializers = DigestProvisioningTest.DataSourceInitializer.class)
@Import(RestTemplateConfig.class)
public class DigestProvisioningTest extends AbstractProvisioningTest implements AbstractMySqlIntegrationTest {

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            MysqlDataSourceInitializer.initialize(mysql, applicationContext);
        }
    }

    public DigestProvisioningTest(@Autowired RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static final String DIGEST_REALM = "FreeACS";

    private RestTemplate restTemplate;

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        mvc.perform(post("/tr069")).andExpect(status().isUnauthorized());
    }

    @Test
    public void restTemplateTest() throws SQLException {
        addUnitsToProvision();
        try {
            restTemplate.exchange("http://localhost:7777/tr069", HttpMethod.POST,null, String.class);
        } catch(HttpStatusCodeException e) {
            var digest = new DigestUtil("test123", "password", "http://localhost:7777/tr069", "POST");
            digest.parseWwwAuthenticate(e.getResponseHeaders().toSingleValueMap());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", digest.getTokenDigest());
            System.out.println(headers);
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            restTemplate.exchange("http://localhost:7777/tr069", HttpMethod.POST, entity, String.class);
        }
    }

//    @Test
//    public void discoverUnit() throws Exception {
//        addUnitsToProvision();
//        provisionUnit(digest(UNIT_ID).password(UNIT_PASSWORD).realm(DIGEST_REALM));
//    }
}
