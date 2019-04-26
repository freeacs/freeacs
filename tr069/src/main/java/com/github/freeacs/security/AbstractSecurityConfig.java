package com.github.freeacs.security;

import com.github.freeacs.controllers.FileController;
import com.github.freeacs.controllers.OKController;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

public abstract class AbstractSecurityConfig extends WebSecurityConfigurerAdapter {
    private final Boolean fileAuthUsed;
    private final String contextPath;

    protected AbstractSecurityConfig(String contextPath, Boolean fileAuthUsed) {
        this.contextPath = contextPath;
        this.fileAuthUsed = fileAuthUsed;
    }

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
}
