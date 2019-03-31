package com.github.freeacs.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.freeacs.service.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    @JsonIgnore
    private UserDto user;

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
