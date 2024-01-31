package com.github.freeacs.security;

import com.github.freeacs.controllers.FileController;
import com.github.freeacs.controllers.OKController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

@Slf4j
@Configuration
@ConditionalOnProperty(
        value = "auth.method",
        havingValue = "digest"
)
public class DigestSpringSecurityConfig extends AbstractSecurityConfig {
    @Bean
    public SecurityFilterChain configure(HttpSecurity http, DigestAuthenticationFilter digestAuthenticationFilter, DigestAuthenticationEntryPoint digestAuthenticationEntryPoint) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(configure -> configure.authenticationEntryPoint(digestAuthenticationEntryPoint))
                .addFilterBefore(digestAuthenticationFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests.requestMatchers(contextPath + OKController.CTX_PATH).permitAll();
                    if (!fileAuthUsed) {
                        authorizeRequests.requestMatchers(contextPath + FileController.CTX_PATH + "/**").permitAll();
                    }
                    authorizeRequests.anyRequest().authenticated();
                })
                .build();
    }

    @Bean
    public DigestAuthenticationFilter digestAuthenticationFilter(DigestAuthenticationEntryPoint digestAuthenticationEntryPoint, UserCache digestUserCache, UserDetailsService userDetailsService) {
        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
        digestAuthenticationFilter.setUserDetailsService(userDetailsService);
        digestAuthenticationFilter.setCreateAuthenticatedToken(true);
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestAuthenticationEntryPoint);
        digestAuthenticationFilter.setUserCache(digestUserCache);
        return digestAuthenticationFilter;
    }

    @Bean
    public DigestAuthenticationEntryPoint digestEntryPoint(@Value("${digest.secret}") String digestSecret) {
        if ("changeme".equals(digestSecret)) {
            throw new IllegalArgumentException("Please change the digest.secret property to start using digest authentication");
        }
        DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
        digestAuthenticationEntryPoint.setKey(digestSecret);
        digestAuthenticationEntryPoint.setNonceValiditySeconds(10);
        digestAuthenticationEntryPoint.setRealmName("FreeACS");
        return digestAuthenticationEntryPoint;
    }

    @Bean
    UserCache digestUserCache() throws Exception {
        return new SpringCacheBasedUserCache(new ConcurrentMapCache("digestUserCache"));
    }
}
