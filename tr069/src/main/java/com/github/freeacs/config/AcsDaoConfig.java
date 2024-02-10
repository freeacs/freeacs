package com.github.freeacs.config;

import com.github.freeacs.dbi.ACSDao;
import com.github.freeacs.dbi.DBI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcsDaoConfig {

    @Bean
    public ACSDao getACSDao(DBI dbi) {
        return new ACSDao(dbi.getDataSource());
    }
}
