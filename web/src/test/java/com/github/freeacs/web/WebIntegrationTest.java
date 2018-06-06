package com.github.freeacs.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-h2.properties")
public class WebIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testStatusPage() {
        ResponseEntity<String> body = this.restTemplate.exchange("/web", HttpMethod.GET, null, String.class);
        assertThat(body.getStatusCode().value()).isEqualTo(302);
        assertThat(body.getHeaders().getFirst("Location")).endsWith("/web/login");
    }
}
