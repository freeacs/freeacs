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
    public Syslog getSyslog(DataSource dataSource) throws SQLException {
        Users users = new Users(dataSource);
        Identity id = new Identity(FACILITY_TR069, "latest", users.getUnprotected(Users.USER_ADMIN));
        return new Syslog(dataSource, id);
    }

    @Bean
    public DBI getDBI(DataSource dataSource, Syslog syslog) throws SQLException {
        ACS.setStrictOrder(false);
        return DBI.createAndInitialize(Integer.MAX_VALUE, dataSource, syslog.getIdentity().getFacility());
    }
}
