package com.github.freeacs.security;

import com.github.freeacs.controllers.FileController;
import com.github.freeacs.controllers.OKController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class AbstractSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    protected AcsUnitDetailsService acsUnitDetailsService;
    @Value("${context-path}")
    private String contextPath;
    @Value("${file.auth.used}")
    private Boolean fileAuthUsed;

    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry conditionalUseFileAuth(
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequests) {
        if (!fileAuthUsed) {
            return authorizeRequests.antMatchers(contextPath + FileController.CTX_PATH + "/**").permitAll();
        }
        return authorizeRequests;
    }

    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry allowHealthEndpoint(
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequests) {
        return authorizeRequests.antMatchers(contextPath + OKController.CTX_PATH).permitAll();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(acsUnitDetailsService).passwordEncoder(passwordEncoder());
    }
}
