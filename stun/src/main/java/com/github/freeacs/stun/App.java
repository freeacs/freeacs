package com.owera.xaps.stun;

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
    ServletRegistrationBean<StunServlet> stun() {
        ServletRegistrationBean<StunServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new StunServlet());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<OKServlet> ok() {
        ServletRegistrationBean<OKServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new OKServlet());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/ok"));
        return srb;
    }
}