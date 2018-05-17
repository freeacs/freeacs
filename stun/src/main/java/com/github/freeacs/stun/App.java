package com.github.freeacs.stun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
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
    ServletRegistrationBean<StunServlet> stun(@Autowired @Qualifier("xaps") DataSource xapsDataSource, @Autowired @Qualifier("syslog") DataSource syslogDataSource) {
        ServletRegistrationBean<StunServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new StunServlet(xapsDataSource, syslogDataSource));
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }

    @Bean
    ServletRegistrationBean<OKServlet> ok() {
        ServletRegistrationBean<OKServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new OKServlet());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/ok"));
        return srb;
    }
}