package com.owera.xaps.syslogserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    ServletRegistrationBean<SyslogServlet> provisioning() {
        ServletRegistrationBean<SyslogServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new SyslogServlet());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }
}