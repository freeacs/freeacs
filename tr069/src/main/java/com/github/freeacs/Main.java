package com.github.freeacs;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.dbi.*;
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
    public DBI getDBI(DataSource dataSource) throws SQLException {
        ACS.setStrictOrder(false);
        Users users = new Users(dataSource);
        Identity id = new Identity(FACILITY_TR069, "latest", users.getUnprotected(Users.USER_ADMIN));
        Syslog syslog = new Syslog(dataSource, id);
        return new DBI(Integer.MAX_VALUE, dataSource, syslog);
    }

}
