package com.github.freeacs.rest.configs;

import com.github.freeacs.dbi.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class ACSConfig {

    @Bean
    public Users getUsers(DataSource dataSource) throws SQLException {
        return new Users(dataSource);
    }

    @Bean
    public Syslog getSyslog(DataSource dataSource, Users users) {
        User user = users.getUnprotected(Users.USER_ADMIN);
        Identity id = new Identity(SyslogConstants.FACILITY_WEB, "latest", user);
        return new Syslog(dataSource, id);
    }

    @Bean
    public DBI getACS(DataSource dataSource, Syslog syslog) throws SQLException {
        return new DBI(-1, dataSource, syslog);
    }

}
