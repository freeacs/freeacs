package com.github.freeacs.health;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.github.freeacs.utils.MysqlDataSourceInitializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
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
        "classpath:application-digest-security.properties"
})
@ContextConfiguration(initializers = DigestHealthTest.DataSourceInitializer.class)
public class DigestHealthTest implements AbstractMySqlIntegrationTest {

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            MysqlDataSourceInitializer.initialize(mysql, applicationContext);
        }
    }

    @Autowired
    protected MockMvc mvc;

    @Test
    public void canReachHealthEndpoint() throws Exception {
        mvc.perform(get("/tr069/ok"))
                .andExpect(status().isOk())
                .andExpect(content().string("FREEACSOK"));
    }
}
