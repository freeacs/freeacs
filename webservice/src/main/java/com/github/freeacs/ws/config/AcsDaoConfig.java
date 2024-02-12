package com.github.freeacs.ws.config;

import com.github.freeacs.dbi.ACSDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AcsDaoConfig {

    @Bean
    public ACSDao getACSDao(@Qualifier("main") DataSource dataSource) {
        return new ACSDao(dataSource);
    }
}
