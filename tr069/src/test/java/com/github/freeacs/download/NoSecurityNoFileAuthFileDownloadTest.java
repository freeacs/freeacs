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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-h2-datasource.properties",
        "classpath:application-no-security.properties",
        "classpath:application-file-auth-disabled.properties"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NoSecurityNoFileAuthFileDownloadTest extends AbstractDownloadTest {

    @Before
    public void init() throws SQLException {
        addTestfile();
    }

    @Test
    public void notFoundOnUnitTypeThatDoesNotExist() throws Exception {
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/DoesNotExist"))
                .andExpect(status().isNotFound()); // not unauthorized
    }

    @Test
    public void canDownloadFile() throws Exception {
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(FILE_BYTES));
    }
}
