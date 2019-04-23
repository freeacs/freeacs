package com.github.freeacs;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.dbi.ScriptExecutions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import javax.sql.DataSource;

import static com.github.freeacs.dbi.SyslogConstants.FACILITY_TR069;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public ScriptExecutions getScriptExecutions(DataSource dataSource) {
        return new ScriptExecutions(dataSource);
    }

    @Bean
    public DataSource getDataSource(Config config) {
        return HikariDataSourceHelper.dataSource(config.getConfig("main"));
    }

    @Bean
    public DBAccess getDBAccess(DataSource dataSource) {
        return DBAccess.createInstance(FACILITY_TR069, "latest", dataSource);
    }

    @Bean
    public ExecutorWrapper getExecutor() {
        return ExecutorWrapperFactory.create(4);
    }

    @Bean
    public Config getTypeSafeConfig() {
        return ConfigFactory.load().resolve();
    }

    @Bean
    public TypeSafeConfigPropertySource typeSafeBackedSpringConfig(Config conf, ConfigurableEnvironment env) {
        TypeSafeConfigPropertySource source = new TypeSafeConfigPropertySource("typeSafe", conf);
        MutablePropertySources sources = env.getPropertySources();
        sources.addFirst(source);
        return source;
    }

    public class TypeSafeConfigPropertySource extends PropertySource<Config> {

        TypeSafeConfigPropertySource(String name, Config source) {
            super(name, source);
        }

        @Override
        public Object getProperty(String path) {
            if (source.hasPath(path)) {
                return source.getAnyRef(path);
            }
            return null;
        }
    }

}
