package com.github.freeacs.web;

import com.github.freeacs.web.app.Main;
import com.github.freeacs.web.app.Monitor;
import com.github.freeacs.web.app.menu.MenuServlet;
import com.github.freeacs.web.app.security.LoginServlet;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.help.HelpServlet;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication(exclude = FlywayAutoConfiguration.class)
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    @Primary
    @Qualifier("xaps")
    @ConfigurationProperties("xaps.datasource")
    public DataSource getXapsDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @Qualifier("syslog")
    @ConfigurationProperties("syslog.datasource")
    public DataSource getSyslogDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    ServletRegistrationBean<Monitor> monitor () {
        ServletRegistrationBean<Monitor> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Monitor());
        srb.setUrlMappings(Arrays.asList("/monitor", "/ok"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<Main> main (@Qualifier("xaps") DataSource xapsDataSource, @Qualifier("syslog") DataSource syslogDataSource) {
        ServletRegistrationBean<Main> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Main(xapsDataSource, syslogDataSource));
        srb.setName("main");
        srb.setUrlMappings(Collections.singletonList("/web"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<LoginServlet> loginServlet (@Qualifier("xaps") DataSource xapsDataSource) {
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
    FilterRegistrationBean<LoginServlet> loginFilter (@Qualifier("xaps") DataSource xapsDataSource) {
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