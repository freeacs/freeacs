package com.github.freeacs.download;

import com.github.freeacs.Main;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.freeacs.provisioning.AbstractProvisioningTest.UNIT_TYPE_NAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-digest-security.properties",
        "classpath:application-file-auth-disabled.properties"
})
public class DigestNoFileAuthFileDownloadTest extends AbstractDownloadTest {

    @Test
    public void notFoundOnUnitTypeThatDoesNotExist() throws Exception {
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/DoesNotExist"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void canDownloadFile() throws Exception {
        addTestfile(UNIT_TYPE_NAME, AbstractProvisioningTest.UNIT_ID);
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(FILE_BYTES));
    }
}
