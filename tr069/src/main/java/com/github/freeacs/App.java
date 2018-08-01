package com.github.freeacs;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.FileServlet;
import com.github.freeacs.base.http.OKServlet;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.Provisioning;
import com.github.freeacs.tr069.methods.TR069Method;
import com.github.freeacs.tr069.test.system1.TestServlet;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.Collections;

import static com.github.freeacs.dbi.SyslogConstants.FACILITY_TR069;
import static com.github.freeacs.tr069.Provisioning.VERSION;

@SpringBootApplication(exclude = FlywayAutoConfiguration.class)
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    @Qualifier("main")
    @ConfigurationProperties("main.datasource")
    public DataSource mainDs() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DBAccess getDBAccess(@Qualifier("main") DataSource mainDataSource) {
        return new DBAccess(FACILITY_TR069, VERSION, mainDataSource, mainDataSource);
    }

    @Bean
    ServletRegistrationBean<Provisioning> provisioning(@Autowired DBAccess dbAccess, @Autowired TR069Method tr069Method, @Autowired Properties properties, @Value("${syslog.server.host}") String syslogServerHost) {
        SyslogClient.SYSLOG_SERVER_HOST = syslogServerHost;
        ServletRegistrationBean<Provisioning> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Provisioning(dbAccess, tr069Method, properties));
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<FileServlet> file(@Autowired DBAccess dbAccess) {
        ServletRegistrationBean<FileServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new FileServlet(dbAccess));
        srb.setUrlMappings(Collections.singletonList("/file/*"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<OKServlet> ok(@Autowired DBAccess dbAccess) {
        ServletRegistrationBean<OKServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new OKServlet(dbAccess));
        srb.setUrlMappings(Collections.singletonList("/ok"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<TestServlet> testServlet() {
        ServletRegistrationBean<TestServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new TestServlet());
        srb.setUrlMappings(Collections.singletonList("/test"));
        return srb;
    }
}