package com.github.freeacs.download;

import com.github.freeacs.Main;
import com.github.freeacs.utils.DigestUtil;
import com.github.freeacs.utils.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;

import static com.github.freeacs.provisioning.AbstractProvisioningTest.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-digest-security.properties",
        "classpath:application-file-auth-enabled.properties"
})
@Import(RestTemplateConfig.class)
@Slf4j
public class DigestFileDownloadTest extends AbstractDownloadTest {

    private final Integer randomServerPort;

    public DigestFileDownloadTest(@LocalServerPort Integer randomServerPort) {
        this.randomServerPort = randomServerPort;
    }

    @Test
    public void unauthorizedOnMissingAuthentication() throws Exception {
        mvc.perform(get("/tr069/file/SOFTWARE/1.23.1/Test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void canDownloadFile() throws Exception {
        addTestfile(UNIT_TYPE_NAME, UNIT_ID);
        String url = "http://localhost:%d/tr069/file/SOFTWARE/1.23.1/%s".formatted(randomServerPort, UNIT_TYPE_NAME);

        // Initial request to handle authentication
        // Must send initial request without authentication to get digest challenge
        String digestToken = null;
        try {
            restTemplate.getForEntity(url, byte[].class);
            fail("Should not be able to download a file without authentication");
        } catch (HttpStatusCodeException e) {
            log.info("Handling authentication");
            var digest = new DigestUtil(UNIT_ID, UNIT_PASSWORD, url, HttpMethod.GET.name());
            digest.parseWwwAuthenticate(e.getResponseHeaders().toSingleValueMap());
            digestToken = digest.getTokenDigest();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", digestToken);
        HttpEntity<?> entity = new HttpEntity<>(null, headers);
        var result = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertArrayEquals(FILE_BYTES, result.getBody());
        assertEquals("application/octet-stream", result.getHeaders().getContentType().toString());
    }
}
