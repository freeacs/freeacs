package com.github.freeacs.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
        value="auth.method",
        havingValue = "basic"
)
public class BasicSpringSecurityConfig extends AbstractSecurityConfig {

    @PostConstruct
    public void init() {
        log.info("Started " + this.getClass().getName());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        allowHealthEndpoint(
                conditionalUseFileAuth(
                        http
                                .csrf().disable()
                                .authorizeRequests()
                )
        )
                .anyRequest().authenticated()
                .and().httpBasic()
                .authenticationEntryPoint(basicEntryPoint());
    }

    private BasicAuthenticationEntryPoint basicEntryPoint() {
        BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
        basicAuthenticationEntryPoint.setRealmName("FreeACS");
        return basicAuthenticationEntryPoint;
    }
}
