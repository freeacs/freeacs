package com.github.freeacs.download;

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

import static com.github.freeacs.provisioning.AbstractProvisioningTest.*;
import static com.github.freeacs.provisioning.DigestProvisioningTest.DIGEST_REALM;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.digest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-digest-security.properties",
        "classpath:application-file-auth-enabled.properties"
})
public class DigestFileDownloadTest extends AbstractDownloadTest {

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void canDownloadFile() throws Exception {
        addTestfile(UNIT_TYPE_NAME, UNIT_ID);
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/" + UNIT_TYPE_NAME)
                .with(digest(UNIT_ID).password(UNIT_PASSWORD).realm(DIGEST_REALM)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(FILE_BYTES));
    }
}
