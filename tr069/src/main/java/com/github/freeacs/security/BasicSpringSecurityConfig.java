package com.github.freeacs.security;

import com.github.freeacs.controllers.FileController;
import com.github.freeacs.controllers.OKController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Slf4j
@Configuration
@ConditionalOnProperty(
        value = "auth.method",
        havingValue = "basic"
)
public class BasicSpringSecurityConfig extends AbstractSecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(conf -> conf.authenticationEntryPoint(basicEntryPoint()))
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests.requestMatchers(contextPath + OKController.CTX_PATH).permitAll();
                    if (!fileAuthUsed) {
                        authorizeRequests.requestMatchers(contextPath + FileController.CTX_PATH + "/**").permitAll();
                    }
                    authorizeRequests.anyRequest().authenticated();
                })
                .userDetailsService(acsUnitDetailsService)
                .build();
    }

    private BasicAuthenticationEntryPoint basicEntryPoint() {
        BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
        basicAuthenticationEntryPoint.setRealmName("FreeACS");
        return basicAuthenticationEntryPoint;
    }
}
