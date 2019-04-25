package com.github.freeacs.tr069.base;

import org.junit.Test;

import static org.junit.Assert.*;

public class DBIActionsSessionTR069Test {

    @Test
    public void shouldWorkForLessThan6Chars() {
        // Given:
        String shortUnitUd = "ABC";

        // When:
        String unittypeName = DBIActions.getUnittypeName(shortUnitUd);

        // Then:
        assertEquals("OUI-ABC", unittypeName);
    }

    @Test
    public void shouldWorkForMoreThan6Chars() {
        // Given:
        String shortUnitUd = "ABCDEFG123";

        // When:
        String unittypeName = DBIActions.getUnittypeName(shortUnitUd);

        // Then:
        assertEquals("OUI-ABCDEF", unittypeName);
    }

    @Test(expected = NullPointerException.class)
    public void cantHandleNullUnitId() {
        // Given:
        String shortUnitUd = null;

        // When:
        DBIActions.getUnittypeName(shortUnitUd);
    }
}
