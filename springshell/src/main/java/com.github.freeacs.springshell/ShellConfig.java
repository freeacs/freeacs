package com.github.freeacs.springshell;

import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.github.freeacs.shell.ACSShell;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class ShellConfig {

    public ShellConfig() {
        ACSVersionCheck.setDatabaseChecked(false);
    }

    @Bean
    @ConfigurationProperties("main.datasource")
    public DataSource mainDs() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public Identity getIdentity() {
        return new Identity(SyslogConstants.FACILITY_SHELL, ACSShell.version, new User("admin", "Admin", "Admin", true, null));
    }

    @Bean
    public Syslog getSyslog(DataSource dataSource, Identity identity) {
        return new Syslog(dataSource, identity);
    }

    @Bean
    public DBI getDBI(DataSource dataSource, Syslog syslog) throws SQLException {
        return new DBI(Integer.MAX_VALUE, dataSource, syslog);
    }

    @Bean
    public UnitJobs getUnitJobs(DataSource dataSource) {
        return new UnitJobs(dataSource);
    }

    @Bean
    public ACSUnit getACSUnit(DataSource dataSource, DBI dbi, Syslog syslog) throws SQLException {
        return new ACSUnit(dataSource, dbi.getAcs(), syslog);
    }
}
