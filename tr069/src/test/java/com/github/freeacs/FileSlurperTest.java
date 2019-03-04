package com.github.freeacs;

import com.github.freeacs.common.util.FileSlurper;
import org.junit.Test;

import java.io.IOException;
import static org.junit.Assert.assertNotNull;

public class FileSlurperTest {

    @Test
    public void test() throws IOException {
        assertNotNull(FileSlurper.getFileAsString("/provision/cpe/Inform.xml"));
    }
}
