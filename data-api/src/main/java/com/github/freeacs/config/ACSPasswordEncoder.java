package com.github.freeacs.config;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_224;

public class ACSPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence charSequence) {
        return encodeString(charSequence);
    }

    private String encodeString(CharSequence charSequence) {
        return Hex.encodeHexString(new DigestUtils(SHA_224).digest(charSequence.toString())).toUpperCase();
    }

    @Override
    public boolean matches(CharSequence rawPass, String encodedPass) {
        return encodeString(rawPass).equals(encodedPass);
    }
}
