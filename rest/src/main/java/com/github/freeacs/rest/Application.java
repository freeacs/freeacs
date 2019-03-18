package com.github.freeacs.rest;

import com.oembedler.moon.graphql.boot.GraphQLWebAutoConfiguration;
import com.oembedler.moon.graphql.boot.GraphQLWebsocketAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
