package com.github.freeacs.config;

import com.github.freeacs.dbi.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

import static com.github.freeacs.dbi.SyslogConstants.FACILITY_TR069;

@Configuration
public class DBIConfig {

    @Bean
    public DBI getDBI(DataSource dataSource) throws SQLException {
        ACS.setStrictOrder(false);
        Users users = new Users(dataSource);
        Identity id = new Identity(FACILITY_TR069, "latest", users.getUnprotected(Users.USER_ADMIN));
        Syslog syslog = new Syslog(dataSource, id);
        return new DBI(Integer.MAX_VALUE, dataSource, syslog);
    }
}
