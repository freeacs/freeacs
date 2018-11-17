package com.github.freeacs.web.config;

public interface PasswordEncoder {
  String encode(CharSequence rawPassword);

  boolean matches(CharSequence rawPassword, String encodedPassword);
}
