package com.github.freeacs.dao;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DaoConfig {

    private final Jdbi jdbi;

    @Autowired
    public DaoConfig(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource).installPlugins();
    }

    @Bean
    public Jdbi getJdbi() {
        return jdbi;
    }

    @Bean
    public UnitTypeDao getUnitTypeDao() {
        return jdbi.onDemand(UnitTypeDao.class);
    }

    @Bean
    public ProfileDao getProfileDao() {
        return jdbi.onDemand(ProfileDao.class);
    }

    @Bean
    public UnitDao getUnitDao() {
        return jdbi.onDemand(UnitDao.class);
    }

    @Bean
    public UserDao getUserDao() {
        return jdbi.onDemand(UserDao.class);
    }

    @Bean
    public PermissionDao getPermissionDao() {
        return jdbi.onDemand(PermissionDao.class);
    }
}
