package com.github.freeacs;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.http.FileServlet;
import com.github.freeacs.base.http.OKServlet;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.tr069.Provisioning;
import com.github.freeacs.tr069.test.system1.TestServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

import static com.github.freeacs.tr069.Provisioning.VERSION;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public DBAccess getDBAccess() {
        return new DBAccess(com.github.freeacs.Properties.Module.TR069, SyslogConstants.FACILITY_TR069, VERSION);
    }

    @Bean
    ServletRegistrationBean<Provisioning> provisioning(@Autowired DBAccess dbAccess) {
        ServletRegistrationBean<Provisioning> srb = new ServletRegistrationBean<>();
        srb.setServlet(new Provisioning(dbAccess));
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
    ServletRegistrationBean<TestServlet> test() {
        ServletRegistrationBean<TestServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new TestServlet());
        srb.setUrlMappings(Collections.singletonList("/test"));
        return srb;
    }
}