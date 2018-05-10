package com.owera.xaps.web;

import com.owera.xaps.web.app.Main;
import com.owera.xaps.web.app.Monitor;
import com.owera.xaps.web.app.menu.MenuServlet;
import com.owera.xaps.web.app.security.LoginServlet;
import com.owera.xaps.web.help.HelpServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    ServletRegistrationBean monitor () {
        ServletRegistrationBean srb = new ServletRegistrationBean();
        srb.setServlet(new Monitor());
        srb.setUrlMappings(Arrays.asList("/monitor", "/ok"));
        return srb;
    }

    @Bean
    ServletRegistrationBean main () {
        ServletRegistrationBean srb = new ServletRegistrationBean();
        srb.setServlet(new Main());
        srb.setName("main");
        srb.setUrlMappings(Arrays.asList("/web"));
        return srb;
    }

    @Bean
    ServletRegistrationBean loginServlet () {
        ServletRegistrationBean srb = new ServletRegistrationBean();
        srb.setServlet(new LoginServlet());
        srb.setUrlMappings(Arrays.asList("/login"));
        return srb;
    }

    @Bean
    ServletRegistrationBean helpServlet () {
        ServletRegistrationBean srb = new ServletRegistrationBean();
        srb.setServlet(new HelpServlet());
        srb.setUrlMappings(Arrays.asList("/help"));
        return srb;
    }
    @Bean
    ServletRegistrationBean menuServlet () {
        ServletRegistrationBean srb = new ServletRegistrationBean();
        srb.setServlet(new MenuServlet());
        srb.setUrlMappings(Arrays.asList("/menu"));
        return srb;
    }

    @Bean
    FilterRegistrationBean loginFilter () {
        FilterRegistrationBean frb = new FilterRegistrationBean();
        frb.setFilter(new LoginServlet());
        frb.setServletNames(Arrays.asList("main"));
        return frb;
    }
}