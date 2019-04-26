package com.github.freeacs.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final AcsUnitDetailsService acsUnitDetailsService;

    @Autowired
    public BasicSpringSecurityConfig(AcsUnitDetailsService acsUnitDetailsService,
                                     @Value("${file.auth.used}") Boolean fileAuthUsed,
                                     @Value("${context-path}") String contextPath) {
        super(contextPath, fileAuthUsed);
        this.acsUnitDetailsService = acsUnitDetailsService;
    }

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(acsUnitDetailsService).passwordEncoder(passwordEncoder());
    }

    private BasicAuthenticationEntryPoint basicEntryPoint() {
        BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
        basicAuthenticationEntryPoint.setRealmName("FreeACS");
        return basicAuthenticationEntryPoint;
    }
}
