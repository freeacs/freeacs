package com.github.test.freeacs;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.Sleep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.freeacs" })
@Slf4j
public class TestMain {

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("Shutdown Hook is running !");
            Sleep.terminateApplication();
        }));
        Properties props = new Properties();
        props.put("spring.test.database.testcontainers", "true");
        new SpringApplicationBuilder(Main.class).properties(props).run(args);
    }
}
