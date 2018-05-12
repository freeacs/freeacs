package com.github.freeacs;

import com.github.freeacs.base.http.FileServlet;
import com.github.freeacs.base.http.OKServlet;
import com.github.freeacs.tr069.Provisioning;
import com.github.freeacs.tr069.test.system1.TestServlet;
import com.owera.xaps.base.http.FileServlet;
import com.owera.xaps.base.http.OKServlet;
import com.owera.xaps.tr069.Provisioning;
import com.owera.xaps.tr069.test.system1.TestServlet;
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
    ServletRegistrationBean<Provisioning> provisioning() {
        ServletRegistrationBean<Provisioning> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Provisioning());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<FileServlet> file() {
        ServletRegistrationBean<FileServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new FileServlet());
        srb.setUrlMappings(Collections.singletonList("/file/*"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<OKServlet> ok() {
        ServletRegistrationBean<OKServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new OKServlet());
        srb.setUrlMappings(Collections.singletonList("/ok"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<TestServlet> test() {
        ServletRegistrationBean<TestServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new TestServlet());
        srb.setUrlMappings(Collections.singletonList("/test"));
        return srb;
    }
}