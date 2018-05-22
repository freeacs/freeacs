package com.github.freeacs.ws;

import com.github.freeacs.ws.impl.OKServlet;
import com.github.freeacs.ws.impl.XMLServer;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.transport.http.AdminServlet;
import org.apache.axis.transport.http.AxisServlet;
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
    ServletRegistrationBean<OKServlet> monitor(@Qualifier("xaps") DataSource xaps, @Qualifier("syslog") DataSource syslog) {
        ServletRegistrationBean<OKServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new OKServlet(xaps, syslog));
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
    ServletRegistrationBean<AxisServlet> axisServlet(@Qualifier("xaps") DataSource xaps, @Qualifier("syslog") DataSource syslog) {
        ACSWS_BindingSkeleton.xapsDs = xaps;
        ACSWS_BindingSkeleton.syslogDs = syslog;
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