package com.github.freeacs.tr069.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void cantHandleNullUnitId() {
        // Given:
        String shortUnitUd = null;

        // When:
        assertThrows(NullPointerException.class, () -> {
            DBIActions.getUnittypeName(shortUnitUd);
        });
    }
}
