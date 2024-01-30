package com.github.freeacs.provisioning;

import com.github.freeacs.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.SQLException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.digest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-h2-datasource.properties",
        "classpath:application-digest-security.properties",
        "classpath:application-discovery-off.properties"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DigestProvisioningTest extends AbstractProvisioningTest {
    public static final String DIGEST_REALM = "FreeACS";

    @BeforeEach
    public void init() throws SQLException {
        addUnitsToProvision();
    }

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        mvc.perform(post("/tr069")).andExpect(status().isUnauthorized());
    }

    @Test
    public void discoverUnit() throws Exception {
       provisionUnit(digest(UNIT_ID).password(UNIT_PASSWORD).realm(DIGEST_REALM));
    }
}
