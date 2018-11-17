package com.github.freeacs.web.config;

import com.github.freeacs.dbi.crypto.Crypto;

public class SecurityConfig {

  public static PasswordEncoder encoder() {
    return new PasswordEncoder() {
      @Override
      public String encode(CharSequence rawPassword) {
        return Crypto.computeSHA1DigestAsHexUpperCase(rawPassword.toString());
      }

      @Override
      public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(encode(rawPassword));
      }
    };
  }
}
