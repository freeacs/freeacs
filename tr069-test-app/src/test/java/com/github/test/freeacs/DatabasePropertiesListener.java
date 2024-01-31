package com.github.test.freeacs;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

// Add this factory to tr069/src/test/resources/META-INF/spring.factories
public class DatabasePropertiesListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    public static boolean TESTCONTAINERS_ENABLED = false;
    public static Integer SERVER_PORT = null;

    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (TESTCONTAINERS_ENABLED) {
            try {
                AbstractMySqlIntegrationTest.beforeAll();
                Runtime.getRuntime().addShutdownHook(new Thread(AbstractMySqlIntegrationTest::afterAll));
                ConfigurableEnvironment environment = event.getEnvironment();
                Properties props = new Properties();
                props.put("spring.test.database.replace", "none"); // Tells Spring Boot not to start in-memory db for tests.
                props.put("main.datasource.jdbcUrl", AbstractMySqlIntegrationTest.mysql.getJdbcUrl());
                props.put("main.datasource.username", AbstractMySqlIntegrationTest.mysql.getUsername());
                props.put("main.datasource.password", AbstractMySqlIntegrationTest.mysql.getPassword());
                if (SERVER_PORT != null) {
                    props.put("server.port", SERVER_PORT.toString());
                }
                environment.getPropertySources().addFirst(new PropertiesPropertySource("testDbProps", props));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to start Testcontainer for MySQL, will fall back to default properties");
            }
        } else {
            System.out.println("Did not start testcontainers");
        }
    }
}