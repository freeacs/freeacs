package com.github.freeacs.springshell;

import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.github.freeacs.shell.ACSShell;
import com.zaxxer.hikari.HikariDataSource;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.shell.jline.PromptProvider;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
public class ShellApp {
    @Bean
    @Primary
    @ConfigurationProperties("main.datasource")
    public DataSource mainDs() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DBI getDBI() throws SQLException {
        ACSVersionCheck.setDatabaseChecked(false);
        Identity id = new Identity(SyslogConstants.FACILITY_SHELL, ACSShell.version, new User("admin", "Admin", "Admin", true, null));
        Syslog syslog = new Syslog(mainDs(), id);
        return new DBI(Integer.MAX_VALUE, mainDs(), syslog);
    }

    public static void main(String[] args) {
        SpringApplication.run(ShellApp.class, args);
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("freeacs-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }
}
