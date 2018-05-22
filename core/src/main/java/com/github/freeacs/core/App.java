package com.github.freeacs.core;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Collections;

@SpringBootApplication(exclude = FlywayAutoConfiguration.class)
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    @Primary
    @Qualifier("main")
    @ConfigurationProperties("main.datasource")
    public DataSource main() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @Qualifier("syslog")
    @ConfigurationProperties("syslog.datasource")
    public DataSource syslog() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    ServletRegistrationBean<CoreServlet> core(@Qualifier("main") DataSource mainDataSource, @Qualifier("syslog") DataSource syslogDataSource) {
        ServletRegistrationBean<CoreServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new CoreServlet(mainDataSource, syslogDataSource));
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }
}