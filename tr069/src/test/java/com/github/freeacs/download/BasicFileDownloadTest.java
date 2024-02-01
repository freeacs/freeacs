package com.github.freeacs.download;

import com.github.freeacs.Main;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-basic-security.properties",
        "classpath:application-file-auth-enabled.properties"
})
public class BasicFileDownloadTest extends AbstractDownloadTest {

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        addTestfile("Test 1", "test123");
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test 1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void canDownloadFile() throws Exception {
        addTestfile("Test 2", "test1234");
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test 2")
                .with(httpBasic("test1234", AbstractProvisioningTest.UNIT_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(FILE_BYTES));
    }

    @Test
    public void cannotDownloadFileWithWrongPassword() throws Exception {
        addTestfile("Test 3", "test12345");
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test 3")
                        .with(httpBasic("WRONG_PASSWORD", AbstractProvisioningTest.UNIT_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }
}
