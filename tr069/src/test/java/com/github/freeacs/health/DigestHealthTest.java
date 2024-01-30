package com.github.freeacs.health;

import com.github.freeacs.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-h2-datasource.properties",
        "classpath:application-digest-security.properties"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DigestHealthTest {

    @Autowired
    protected MockMvc mvc;

    @Test
    public void canReachHealthEndpoint() throws Exception {
        mvc.perform(get("/tr069/ok"))
                .andExpect(status().isOk())
                .andExpect(content().string("FREEACSOK"));
    }
}
