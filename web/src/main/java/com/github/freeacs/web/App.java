package com.github.freeacs.web;

import com.github.freeacs.web.app.Main;
import com.github.freeacs.web.app.Monitor;
import com.github.freeacs.web.app.menu.MenuServlet;
import com.github.freeacs.web.app.security.LoginServlet;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.help.HelpServlet;
import com.owera.xaps.web.app.Main;
import com.owera.xaps.web.app.Monitor;
import com.owera.xaps.web.app.menu.MenuServlet;
import com.owera.xaps.web.app.security.LoginServlet;
import com.owera.xaps.web.app.util.Freemarker;
import com.owera.xaps.web.help.HelpServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    ServletRegistrationBean<Monitor> monitor () {
        ServletRegistrationBean<Monitor> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Monitor());
        srb.setUrlMappings(Arrays.asList("/monitor", "/ok"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<Main> main () {
        ServletRegistrationBean<Main> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Main());
        srb.setName("main");
        srb.setUrlMappings(Collections.singletonList("/web"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<LoginServlet> loginServlet () {
        ServletRegistrationBean<LoginServlet> srb = new ServletRegistrationBean<LoginServlet>();
        srb.setServlet(new LoginServlet());
        srb.setUrlMappings(Collections.singletonList("/login"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<HelpServlet> helpServlet () {
        ServletRegistrationBean<HelpServlet> srb = new ServletRegistrationBean<HelpServlet>();
        srb.setServlet(new HelpServlet());
        srb.setUrlMappings(Collections.singletonList("/help"));
        return srb;
    }
    @Bean
    ServletRegistrationBean<MenuServlet> menuServlet () {
        ServletRegistrationBean<MenuServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new MenuServlet());
        srb.setUrlMappings(Collections.singletonList("/menu"));
        return srb;
    }

    @Bean
    FilterRegistrationBean<LoginServlet> loginFilter () {
        FilterRegistrationBean<LoginServlet> frb = new FilterRegistrationBean<LoginServlet>();
        frb.setFilter(new LoginServlet());
        frb.setServletNames(Collections.singletonList("main"));
        return frb;
    }

    @Bean
    public FreeMarkerViewResolver freemarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setCache(true);
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        return resolver;
    }

    @Bean
    public FreeMarkerConfigurer freemarkerConfig() {
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setConfiguration(Freemarker.initFreemarker());
        return freeMarkerConfigurer;
    }
}