package com.github.freeacs;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.dbi.ScriptExecutions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import javax.sql.DataSource;

import java.sql.SQLException;

import static com.github.freeacs.dbi.SyslogConstants.FACILITY_TR069;

@SpringBootApplication
@EnableWebSecurity
@EnableScheduling
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public DataSource getDataSource(Environment environment) {
        return HikariDataSourceHelper.dataSource(environment);
    }

    @Bean
    public DBAccess getDBAccess(DataSource dataSource) throws SQLException {
        return new DBAccess(FACILITY_TR069, "latest", dataSource);
    }

}
