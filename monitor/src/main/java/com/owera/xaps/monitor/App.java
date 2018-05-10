package com.owera.xaps.monitor;

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
    ServletRegistrationBean<OKServlet> ok() {
        ServletRegistrationBean<OKServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new OKServlet());
        srb.setUrlMappings(Collections.singletonList("/ok"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<MonitorServlet> monitor() {
        ServletRegistrationBean<MonitorServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new MonitorServlet());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }
}