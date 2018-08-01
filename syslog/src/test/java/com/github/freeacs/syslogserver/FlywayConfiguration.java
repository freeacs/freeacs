package com.github.freeacs.syslogserver;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class FlywayConfiguration {

    private final DataSource mainDataSource;

    @Autowired
    public FlywayConfiguration(@Qualifier("main") DataSource mainDataSource) {
        this.mainDataSource = mainDataSource;
    }

    @PostConstruct
    public void runFlyway() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(mainDataSource);
        flyway.migrate();
    }
}
