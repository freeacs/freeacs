package com.github.freeacs.dao;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig {

    @Bean
    public UnitTypeDao getUnitTypeDao(Jdbi jdbi) {
        return jdbi.onDemand(UnitTypeDao.class);
    }

    @Bean
    public ProfileDao getProfileDao(Jdbi jdbi) {
        return jdbi.onDemand(ProfileDao.class);
    }

    @Bean
    public UnitDao getUnitDao(Jdbi jdbi) {
        return jdbi.onDemand(UnitDao.class);
    }

}
