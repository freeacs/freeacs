package com.github.freeacs.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.freeacs.service.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/user/signin").permitAll()
                .anyRequest().authenticated()
                .and()
                .apply(new JwtConfigurer(jwtTokenProvider));
    }

    @Data
    @AllArgsConstructor
    public static class UserPrincipal implements UserDetails {
        @JsonIgnore
        private UserDto user;
        private final boolean isAccountNonExpired = true;
        private final boolean isAccountNonLocked = true;
        private final boolean isCredentialsNonExpired = true;
        private final boolean isEnabled = true;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return user.getAccessList()
                    .map(s -> (GrantedAuthority) () -> s)
                    .toJavaList();
        }

        @Override
        @JsonIgnore
        public String getPassword() {
            return user.getSecret();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }
    }
}
