package com.github.freeacs.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserPasswordEncoder implements PasswordEncoder {
    private String convertByte2HexUpperCase(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (byte b : bytes) {
            s.append(String.format("%02x", b));
        }
        return s.toString().toUpperCase();
    }

    private String computeSHA1DigestAsHexUpperCase(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] sha1hash;
            md.update(text.getBytes(), 0, text.length());
            sha1hash = md.digest();
            return convertByte2HexUpperCase(sha1hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "The JVM does not support SHA1 digest mechanism - maybe it's not correct JVM version", e);
        }
    }

    @Override
    public String encode(CharSequence charSequence) {
        return computeSHA1DigestAsHexUpperCase(charSequence.toString());
    }

    @Override
    public boolean matches(CharSequence rawPass, String encodedPass) {
        return computeSHA1DigestAsHexUpperCase(rawPass.toString()).equals(encodedPass);
    }
}
