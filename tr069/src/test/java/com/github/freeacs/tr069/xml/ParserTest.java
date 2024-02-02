package com.github.freeacs.tr069.xml;

import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ParserTest {

    @Test
    public void testParseInDebug() throws IOException, TR069Exception {
        String fileAsString = getFileAsString("/provision/cpe/Inform.xml");
        Parser parser = new Parser(new ByteArrayInputStream(fileAsString.getBytes()), fileAsString.length(),true);
        assertEquals(ProvisioningMethod.Inform, parser.getCwmpMethod());
        assertEquals("1-2", parser.getCwmpVersion());
        assertEquals(fileAsString, parser.getRawXMLForDebugging());
    }

    @Test
    public void testParseWithoutDebug() throws IOException, TR069Exception {
        String fileAsString = getFileAsString("/provision/cpe/Inform.xml");
        Parser parser = new Parser(new ByteArrayInputStream(fileAsString.getBytes()), fileAsString.length(),false);
        assertEquals(ProvisioningMethod.Inform, parser.getCwmpMethod());
        assertEquals("1-2", parser.getCwmpVersion());
        assertNull(parser.getRawXMLForDebugging());
    }

    @Test
    public void testParseWithNoBytes() throws IOException, TR069Exception {
        Parser parser = new Parser(new ByteArrayInputStream(new byte[]{}), 0,false);
        assertEquals(ProvisioningMethod.Empty, parser.getCwmpMethod());
        assertNull(parser.getCwmpVersion());
        assertNull(parser.getRawXMLForDebugging());
    }

    @Test
    public void testParseWithNegativeContentLength() throws IOException, TR069Exception {
        Parser parser = new Parser(new ByteArrayInputStream(new byte[]{}), -1,false);
        assertEquals(ProvisioningMethod.Empty, parser.getCwmpMethod());
        assertNull(parser.getCwmpVersion());
        assertNull(parser.getRawXMLForDebugging());
    }
}
