package com.github.freeacs.web;

import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.web.app.Main;
import com.github.freeacs.web.app.Monitor;
import com.github.freeacs.web.app.menu.MenuServlet;
import com.github.freeacs.web.app.util.Freemarker;
import com.github.freeacs.web.help.HelpServlet;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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
        System.getProperties().setProperty("org.eclipse.jetty.server.Request.maxFormKeys", "100000");
        SpringApplication.run(App.class, args);
    }

    @Bean
    @Primary
    @Qualifier("main")
    @ConfigurationProperties("main.datasource")
    public DataSource mainDs() {
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
    ServletRegistrationBean<Main> main (@Qualifier("main") DataSource mainDataSource, @Value("${syslog.server.host}") String syslogServerHost) {
        SyslogClient.SYSLOG_SERVER_HOST = syslogServerHost;
        ServletRegistrationBean<Main> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Main(mainDataSource, mainDataSource));
        srb.setName("main");
        srb.setUrlMappings(Collections.singletonList(Main.servletMapping));
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