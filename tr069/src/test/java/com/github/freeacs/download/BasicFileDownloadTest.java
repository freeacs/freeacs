package com.github.freeacs.download;

import com.github.freeacs.Main;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-basic-security.properties",
        "classpath:application-file-auth-enabled.properties"
})
public class BasicFileDownloadTest extends AbstractDownloadTest {

    @Value("${local.server.port}")
    private int port;

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        addTestfile("Test 1", "test123");
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test 1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void canDownloadFile() throws Exception {
        addTestfile("Test 2", "test1234");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("test1234", AbstractProvisioningTest.UNIT_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange("http://localhost:" + port + "/tr069/file/SOFTWARE/1.23.1/Test 2", HttpMethod.GET, entity, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(FILE_BYTES, response.getBody());
        // Run request again to test caching (tested manually)
        response = restTemplate.exchange("http://localhost:" + port + "/tr069/file/SOFTWARE/1.23.1/Test 2", HttpMethod.GET, entity, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(FILE_BYTES, response.getBody());
    }

    @Test
    public void cannotDownloadFileWithWrongPassword() throws Exception {
        addTestfile("Test 3", "test12345");
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test 3")
                        .with(httpBasic("WRONG_PASSWORD", AbstractProvisioningTest.UNIT_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }
}
