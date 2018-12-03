package com.github.freeacs.base.db;

import org.junit.Test;

import static org.junit.Assert.*;

public class DBAccessSessionTR069Test {

    @Test
    public void shouldWorkForLessThan6Chars() {
        // Given:
        String shortUnitUd = "ABC";

        // When:
        String unittypeName = DBAccessSessionTR069.getUnittypeName(shortUnitUd);

        // Then:
        assertEquals("OUI-ABC", unittypeName);
    }

    @Test
    public void shouldWorkForMoreThan6Chars() {
        // Given:
        String shortUnitUd = "ABCDEFG123";

        // When:
        String unittypeName = DBAccessSessionTR069.getUnittypeName(shortUnitUd);

        // Then:
        assertEquals("OUI-ABCDEF", unittypeName);
    }

    @Test(expected = NullPointerException.class)
    public void cantHandleNullUnitId() {
        // Given:
        String shortUnitUd = null;

        // When:
        DBAccessSessionTR069.getUnittypeName(shortUnitUd);
    }
}
