package com.github.freeacs;

import com.github.freeacs.common.util.FileSlurper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileSlurperTest {

    @Test
    public void test() throws IOException {
        assertNotNull(FileSlurper.getFileAsString("/provision/cpe/Inform.xml"));
    }
}
