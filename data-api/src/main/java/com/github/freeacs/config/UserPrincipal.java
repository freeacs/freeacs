package com.github.freeacs.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.freeacs.dao.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    @JsonIgnore
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getAccesslist().startsWith("WEB")) {
            String[] modules = user.getAccesslist().substring(1, user.getAccesslist().length() - 1).split(",");
            return Arrays.stream(modules)
                    .map(s -> (GrantedAuthority) () -> s)
                    .collect(Collectors.toList());
        }
        if (user.getAccesslist() != null) {
            return Collections.singletonList((GrantedAuthority) () -> user.getAccesslist());
        }
        return Collections.emptyList();
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

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
