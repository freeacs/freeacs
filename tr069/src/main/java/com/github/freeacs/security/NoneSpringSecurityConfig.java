package com.github.freeacs.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
        value="auth.method",
        havingValue = "none",
        matchIfMissing = true
)
public class NoneSpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @PostConstruct
    public void init() {
        System.out.println("Started " + this.getClass().getName());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/**").permitAll();

    }
}
