package com.github.freeacs.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
        value="auth.method",
        havingValue = "digest"
)
public class DigestSpringSecurityConfig extends AbstractSecurityConfig {

    private final String digestSecret;

    @Autowired
    public DigestSpringSecurityConfig(@Value("${digest.secret}") String digestSecret) {
        if ("changeme".equals(digestSecret)) {
            throw new IllegalArgumentException("Please change the digest.secret property to start using digest authentication");
        }
        this.digestSecret = digestSecret;
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
                                .addFilter(digestAuthenticationFilter())
                                .exceptionHandling().authenticationEntryPoint(digestEntryPoint())
                                .and()
                                .authorizeRequests()
                )
        )
                .anyRequest().authenticated();
    }

    private DigestAuthenticationFilter digestAuthenticationFilter() {
        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
        digestAuthenticationFilter.setUserDetailsService(acsUnitDetailsService);
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint());
        return digestAuthenticationFilter;
    }

    private DigestAuthenticationEntryPoint digestEntryPoint() {
        DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
        digestAuthenticationEntryPoint.setKey(digestSecret);
        digestAuthenticationEntryPoint.setRealmName("FreeACS");
        return digestAuthenticationEntryPoint;
    }

}
