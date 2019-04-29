package com.github.freeacs;

import com.github.freeacs.common.util.Sleep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Main {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("Shutdown Hook is running !");
            Sleep.terminateApplication();
        }));
        SpringApplication.run(Main.class, args);
    }
}
