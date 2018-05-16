package com.github.freeacs.spp;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.OKServlet;
import com.github.freeacs.dbi.SyslogConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

import static com.github.freeacs.spp.HTTPProvisioning.VERSION;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public DBAccess getDBAccess() {
        return new DBAccess(com.github.freeacs.Properties.Module.SPP, SyslogConstants.FACILITY_SPP, VERSION);
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