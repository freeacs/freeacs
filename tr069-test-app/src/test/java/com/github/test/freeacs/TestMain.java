package com.github.test.freeacs;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.Sleep;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.freeacs" })
public class TestMain {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Sleep::terminateApplication));
        SpringApplication.run(Main.class, args);
    }
}
