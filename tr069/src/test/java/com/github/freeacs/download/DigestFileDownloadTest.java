package com.github.freeacs.download;

import com.github.freeacs.Main;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;

import static com.github.freeacs.provisioning.AbstractProvisioningTest.UNIT_ID;
import static com.github.freeacs.provisioning.AbstractProvisioningTest.UNIT_PASSWORD;
import static com.github.freeacs.provisioning.DigestProvisioningTest.DIGEST_REALM;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.digest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-h2-datasource.properties",
        "classpath:application-digest-security.properties",
        "classpath:application-file-auth-enabled.properties"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DigestFileDownloadTest extends AbstractDownloadTest {

    @Before
    public void init() throws SQLException {
        addTestfile();
    }

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void canDownloadFile() throws Exception {
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test")
                .with(digest(UNIT_ID).password(UNIT_PASSWORD).realm(DIGEST_REALM)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(FILE_BYTES));
    }
}
