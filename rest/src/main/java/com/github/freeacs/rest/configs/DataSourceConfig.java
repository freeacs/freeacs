package com.github.freeacs.rest.configs;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
  @Bean
  @ConfigurationProperties("main.datasource")
  public DataSource mainDs() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }
}
