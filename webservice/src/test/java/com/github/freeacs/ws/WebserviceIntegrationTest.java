package com.github.freeacs.ws;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-h2.properties")
public class WebserviceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testWsdlPage() {
        String body = this.restTemplate.getForObject("/ws/acs.wsdl", String.class);
        assertThat(body).contains("GetUnittypesRequest");
    }
}
