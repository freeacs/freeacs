package com.github.freeacs;

import com.github.freeacs.config.ACSPasswordEncoder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ACSPasswordEncoderTest {
    private static final String ENCODED_PASSWORD = "A33E0694639DA19CF58FA1130B2D767F6F4531019FDD45D73D178CED";

    private ACSPasswordEncoder encoder;

    @Before
    public void init() {
        encoder = new ACSPasswordEncoder();
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
