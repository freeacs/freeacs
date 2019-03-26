package com.github.freeacs.dao;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DaoConfig {

    @Bean
    public Jdbi getJdbi(DataSource dataSource) {
        return Jdbi.create(dataSource).installPlugins();
    }

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

    @Bean
    public UserDao getUserDao(Jdbi jdbi) {
        return jdbi.onDemand(UserDao.class);
    }

}
