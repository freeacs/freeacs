package com.github.freeacs.spp;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.OKServlet;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Collections;

import static com.github.freeacs.dbi.SyslogConstants.FACILITY_SPP;
import static com.github.freeacs.spp.HTTPProvisioning.VERSION;
import static com.github.freeacs.Module.SPP;

@SpringBootApplication
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
    public DBAccess getDBAccess(@Qualifier("xaps") DataSource xapsDataSource, @Qualifier("syslog") DataSource syslogDataSource) {
        return new DBAccess(SPP, FACILITY_SPP, VERSION, xapsDataSource, syslogDataSource);
    }

    @Bean
    ServletRegistrationBean<HTTPProvisioning> stun(@Autowired DBAccess dbAccess) {
        ServletRegistrationBean<HTTPProvisioning> srb = new ServletRegistrationBean<>();
        srb.setServlet(new HTTPProvisioning(dbAccess));
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
}