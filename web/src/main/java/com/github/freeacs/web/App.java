package com.github.freeacs.web;

import com.github.freeacs.web.app.Main;
import com.github.freeacs.web.app.Monitor;
import com.github.freeacs.web.app.menu.MenuServlet;
import com.github.freeacs.web.app.security.LoginServlet;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.help.HelpServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    @Qualifier("xaps")
    public DataSource getXapsDataSource() {
        return null;
    }

    @Bean
    @Qualifier("syslog")
    public DataSource getSyslogDataSource() {
        return null;
    }

    @Bean
    ServletRegistrationBean<Monitor> monitor () {
        ServletRegistrationBean<Monitor> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Monitor());
        srb.setUrlMappings(Arrays.asList("/monitor", "/ok"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<Main> main (@Autowired @Qualifier("xaps") DataSource xapsDataSource, @Autowired @Qualifier("syslog") DataSource syslogDataSource) {
        ServletRegistrationBean<Main> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Main(xapsDataSource, syslogDataSource));
        srb.setName("main");
        srb.setUrlMappings(Collections.singletonList("/web"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<LoginServlet> loginServlet (@Autowired @Qualifier("xaps") DataSource xapsDataSource) {
        ServletRegistrationBean<LoginServlet> srb = new ServletRegistrationBean<LoginServlet>();
        srb.setServlet(new LoginServlet(xapsDataSource));
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
    FilterRegistrationBean<LoginServlet> loginFilter (@Autowired @Qualifier("xaps") DataSource xapsDataSource) {
        FilterRegistrationBean<LoginServlet> frb = new FilterRegistrationBean<LoginServlet>();
        frb.setFilter(new LoginServlet(xapsDataSource));
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