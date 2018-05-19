package com.github.freeacs.ws;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class FlywayConfiguration {

    private final DataSource syslogDataSource;
    private final DataSource xapsDataSource;

    @Autowired
    public FlywayConfiguration(@Qualifier("xaps") DataSource xapsDataSource, @Qualifier("syslog") DataSource syslogDataSource) {
        this.xapsDataSource = xapsDataSource;
        this.syslogDataSource = syslogDataSource;
    }

    @PostConstruct
    public void runFlyway() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(xapsDataSource);
        flyway.migrate();
        flyway.setDataSource(syslogDataSource);
        flyway.migrate();
    }
}
