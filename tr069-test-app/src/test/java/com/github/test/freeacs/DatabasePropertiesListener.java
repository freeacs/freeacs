package com.github.test.freeacs;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

// Add this factory to tr069/src/test/resources/META-INF/spring.factories
@ConditionalOnProperty(name = "spring.test.database.testcontainers", havingValue = "true")
public class DatabasePropertiesListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        try {
            AbstractMySqlIntegrationTest.beforeAll();
            Runtime.getRuntime().addShutdownHook(new Thread(AbstractMySqlIntegrationTest::afterAll));
            ConfigurableEnvironment environment = event.getEnvironment();
            Properties props = new Properties();
            props.put("spring.test.database.replace", "none"); // Tells Spring Boot not to start in-memory db for tests.
            props.put("main.datasource.jdbcUrl", AbstractMySqlIntegrationTest.mysql.getJdbcUrl());
            props.put("main.datasource.username", AbstractMySqlIntegrationTest.mysql.getUsername());
            props.put("main.datasource.password", AbstractMySqlIntegrationTest.mysql.getPassword());
            environment.getPropertySources().addFirst(new PropertiesPropertySource("testDbProps", props));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}