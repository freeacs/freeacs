package com.github.freeacs.base.db;

import org.junit.Test;

import static org.junit.Assert.*;

public class DBAccessSessionTR069Test {

    @Test
    public void parseUnittypeName() {
        // Given:
        String unittypeNameStr = "ODU/xxx/yyy";

        // When:
        String parsedUnittypeName = DBAccessSessionTR069.parseUnittypeName(unittypeNameStr);

        // Then:
        assertEquals("ODU-xxx-yyy", parsedUnittypeName);
    }
}
