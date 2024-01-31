package com.github.freeacs.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public abstract class AbstractSecurityConfig {

    @Autowired
    AcsUnitDetailsService acsUnitDetailsService;

    @Value("${context-path}")
    String contextPath;

    @Value("${file.auth.used}")
    Boolean fileAuthUsed;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

}
