package com.github.freeacs.ws;

import com.github.freeacs.ws.impl.OKServlet;
import com.github.freeacs.ws.impl.XMLServer;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.transport.http.AdminServlet;
import org.apache.axis.transport.http.AxisServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    ServletRegistrationBean<OKServlet> monitor() {
        ServletRegistrationBean<OKServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new OKServlet());
        srb.setLoadOnStartup(2);
        srb.setUrlMappings(Arrays.asList("/monitor", "/ok"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<XMLServer> xmlServer() {
        ServletRegistrationBean<XMLServer> srb = new ServletRegistrationBean<>();
        srb.setServlet(new XMLServer());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/redirect"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<AxisServlet> axisServlet() {
        System.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME, com.github.freeacs.ws.axis.EngineConfigurationFactory.class.getName());
        ServletRegistrationBean<AxisServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new AxisServlet());
        srb.setUrlMappings(Arrays.asList("*.jws", "/services/*"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<AdminServlet> adminServlet() {
        ServletRegistrationBean<AdminServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new AdminServlet());
        srb.setLoadOnStartup(100);
        srb.setUrlMappings(Collections.singletonList("/servlet/AdminServlet"));
        return srb;
    }
}