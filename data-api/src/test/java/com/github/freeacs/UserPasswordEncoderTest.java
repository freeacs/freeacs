package com.github.freeacs;

import com.github.freeacs.config.UserPasswordEncoder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserPasswordEncoderTest {
    private static final String ENCODED_PASSWORD = "4E9BA006A68A8767D65B3761E038CF9040C54A00";

    private UserPasswordEncoder encoder;

    @Before
    public void init() {
        encoder = new UserPasswordEncoder();
    }

    @Test
    public void encodesPasswordCorrectly() {
        assertEquals(ENCODED_PASSWORD, encoder.encode("freeacs"));
    }

    @Test
    public void matchesPasswordCorrectly() {
        assertTrue(encoder.matches("freeacs", ENCODED_PASSWORD));
    }

    @Test
    public void failsToMatchPassword() {
        assertFalse(encoder.matches("doesnotmatch", ENCODED_PASSWORD));
    }
}
