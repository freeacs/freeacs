package com.github.freeacs.config;

import com.github.freeacs.dbi.ACSDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AcsDaoConfig {

    @Bean
    public ACSDao getACSDao(DataSource dataSource) {
        return new ACSDao(dataSource);
    }
}
