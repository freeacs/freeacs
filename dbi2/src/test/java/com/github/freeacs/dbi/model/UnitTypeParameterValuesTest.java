package com.github.freeacs.dbi.model;

import com.github.freeacs.dbi.domain.UnitTypeParameterValues;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class UnitTypeParameterValuesTest {

    @Test
    void testMatchWithRegexp() {
        UnitTypeParameterValues utpv = new UnitTypeParameterValues();
        utpv.setPattern("\\d+");
        assertTrue(utpv.match("123"));
        assertFalse(utpv.match("abc"));
    }

    @Test
    void testMatchWithNull() {
        UnitTypeParameterValues utpv = new UnitTypeParameterValues();
        utpv.setPattern("\\d+");
        assertFalse(utpv.match(null));
    }

    @Test
    void testSetPatternWithNull() {
        UnitTypeParameterValues utpv = new UnitTypeParameterValues();
        utpv.setPattern(null);
        assertNull(utpv.getPattern());
    }

    @Test
    void testMatchWithEnum() {
        UnitTypeParameterValues utpv = new UnitTypeParameterValues();
        utpv.setValues(Arrays.asList("value1", "value2", "value3"));
        assertTrue(utpv.match("value1"));
        assertFalse(utpv.match("value4"));
    }

    @Test
    void testSetPatternWithInvalidRegexp() {
        UnitTypeParameterValues utpv = new UnitTypeParameterValues();
        assertThrows(PatternSyntaxException.class, () -> utpv.setPattern("["));
    }

    @Test
    void testMatchBeforeSetPatternOrValues() {
        UnitTypeParameterValues utpv = new UnitTypeParameterValues();
        assertThrows(NullPointerException.class, () -> utpv.match("123"));
    }

}