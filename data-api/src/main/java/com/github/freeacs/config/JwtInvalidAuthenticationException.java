package com.github.freeacs.config;

import org.springframework.security.core.AuthenticationException;

class JwtInvalidAuthenticationException extends AuthenticationException {
    JwtInvalidAuthenticationException(String e) {
        super(e);
    }
}
