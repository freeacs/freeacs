package com.github.freeacs.rest.configs;

import com.github.freeacs.rest.repositories.UnitRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {
    @Bean
    UnitRepository getUnitRepository(Jdbi jdbi) {
        return jdbi.onDemand(UnitRepository.class);
    }
}
