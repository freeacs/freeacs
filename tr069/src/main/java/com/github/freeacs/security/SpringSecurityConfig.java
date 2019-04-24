package com.github.freeacs.security;

import com.github.freeacs.tr069.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    private final String authMethod;
    private final AcsUnitDetailsService acsUnitDetailsService;
    private final String digestSecret;

    @Autowired
    public SpringSecurityConfig(@Value("${auth.method}") String authMethod,
                                AcsUnitDetailsService acsUnitDetailsService,
                                Properties properties) {
        this.authMethod = authMethod;
        this.acsUnitDetailsService = acsUnitDetailsService;
        this.digestSecret = properties.getDigestSecret();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        boolean isBasic = "basic".equalsIgnoreCase(authMethod);
        boolean isDigest = "digest".equalsIgnoreCase(authMethod);

        if (isDigest) {
            http.addFilter(digestAuthenticationFilter())
                    .exceptionHandling().authenticationEntryPoint(digestEntryPoint());
        }

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config = null;

        if (isDigest || isBasic) {
             config = http
                    .csrf().disable()
                    .authorizeRequests()
                    .anyRequest().authenticated();
        }

        if (isBasic) {
            config.and().httpBasic().authenticationEntryPoint(basicEntryPoint());
        }
    }

    private DigestAuthenticationFilter digestAuthenticationFilter() {
        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
        digestAuthenticationFilter.setUserDetailsService(acsUnitDetailsService);
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint());
        return digestAuthenticationFilter;
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

    private DigestAuthenticationEntryPoint digestEntryPoint() {
        DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
        digestAuthenticationEntryPoint.setKey(digestSecret);
        digestAuthenticationEntryPoint.setRealmName("FreeACS");
        return digestAuthenticationEntryPoint;
    }
}
