package com.github.freeacs.dbi.model;

import com.github.freeacs.dbi.domain.UnitTypeParameterFlag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitTypeParameterFlagTest {

    @Test
    void testFlagSetterAndGetter() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("R");
        assertEquals("R", flag.getFlag());
    }

    @Test
    void testIsConfidential() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("RC");
        assertTrue(flag.isConfidential());
    }

    @Test
    void testIsAlwaysRead() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("RA");
        assertTrue(flag.isAlwaysRead());
    }

    @Test
    void testIsReadOnly() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("R");
        assertTrue(flag.isReadOnly());
    }

    @Test
    void testIsReadWrite() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("RW");
        assertTrue(flag.isReadWrite());
    }

    @Test
    void testIsSystem() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("X");
        assertTrue(flag.isSystem());
    }

    @Test
    void testIsBootRequired() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("RWB");
        assertTrue(flag.isBootRequired());
    }

    @Test
    void testIsSearchable() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("RS");
        assertTrue(flag.isSearchable());
    }

    @Test
    void testIsDisplayable() {
        UnitTypeParameterFlag flag = new UnitTypeParameterFlag("RD");
        assertTrue(flag.isDisplayable());
    }

    @Test
    void testInvalidFlag() {
        assertThrows(IllegalArgumentException.class, () -> new UnitTypeParameterFlag("Z"));
    }
}
