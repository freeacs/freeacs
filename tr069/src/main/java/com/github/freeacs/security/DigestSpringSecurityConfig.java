package com.github.freeacs.security;

import com.github.freeacs.controllers.FileController;
import com.github.freeacs.controllers.OKController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;

@Slf4j
@Configuration
@ConditionalOnProperty(
        value = "auth.method",
        havingValue = "digest"
)
public class DigestSpringSecurityConfig extends AbstractSecurityConfig {
    @Bean
    public SecurityFilterChain configure(HttpSecurity http, UserDetailsService userDetailsService, @Value("${digest.secret}") String digestSecret) throws Exception {
        DigestAuthenticationEntryPoint authenticationEntryPoint = digestEntryPoint(digestSecret);
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(configure -> configure.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(digestAuthenticationFilter(authenticationEntryPoint, digestUserCache(), userDetailsService), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests.requestMatchers(contextPath + OKController.CTX_PATH).permitAll();
                    authorizeRequests.requestMatchers(contextPath + "/test/*").permitAll();
                    if (!fileAuthUsed) {
                        authorizeRequests.requestMatchers(contextPath + FileController.CTX_PATH + "/**").permitAll();
                    }
                    authorizeRequests.anyRequest().authenticated();
                })
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(IF_REQUIRED))
                .requestCache(rc -> rc.requestCache(new NullRequestCache()))
                .build();
    }

    private DigestAuthenticationFilter digestAuthenticationFilter(DigestAuthenticationEntryPoint digestAuthenticationEntryPoint, UserCache digestUserCache, UserDetailsService userDetailsService) {
        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
        digestAuthenticationFilter.setUserDetailsService(userDetailsService);
        digestAuthenticationFilter.setCreateAuthenticatedToken(true);
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestAuthenticationEntryPoint);
        digestAuthenticationFilter.setUserCache(digestUserCache);
        return digestAuthenticationFilter;
    }

    private DigestAuthenticationEntryPoint digestEntryPoint(@Value("${digest.secret}") String digestSecret) {
        if ("changeme".equals(digestSecret)) {
            throw new IllegalArgumentException("Please change the digest.secret property to start using digest authentication");
        }
        DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
        digestAuthenticationEntryPoint.setKey(digestSecret);
        digestAuthenticationEntryPoint.setNonceValiditySeconds(300);
        digestAuthenticationEntryPoint.setRealmName("FreeACS");
        return digestAuthenticationEntryPoint;
    }

    private UserCache digestUserCache() throws Exception {
        return new SpringCacheBasedUserCache(new ConcurrentMapCache("digestUserCache"));
    }
}
