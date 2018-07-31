package com.owera.xaps.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@SpringBootApplication(exclude = FlywayAutoConfiguration.class)
public class App {

    public static void main(String[] args) {
        System.getProperties().setProperty("org.eclipse.jetty.server.Request.maxFormKeys", "100000");
        SpringApplication.run(App.class, args);
    }

    @Bean
    ServletRegistrationBean<MonitorServlet> monitor (@Autowired Properties properties) {
        ServletRegistrationBean<MonitorServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new MonitorServlet(properties));
        srb.setUrlMappings(Collections.singletonList("/web"));
        return srb;
    }
}