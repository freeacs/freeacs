package com.github.freeacs.config;

import com.github.freeacs.config.SecurityConfig.UserPrincipal;
import com.github.freeacs.service.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ACSUserDetailsService implements UserDetailsService {
    @Getter
    private final UserService userService;

    @Autowired
    public ACSUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.getByUserName(username)
                .map(UserPrincipal::new)
                .getOrElseThrow(() -> new UsernameNotFoundException(username));
    }
}
