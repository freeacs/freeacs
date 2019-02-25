package com.github.freeacs.common.util;

import ch.vorburger.exec.ManagedProcessException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractEmbeddedDataSourceClassTest extends AbstractEmbeddedDataSourceHelper {

    @BeforeClass
    public static void setUpBeforeClass() throws ManagedProcessException {
        AbstractEmbeddedDataSourceHelper.setUpBeforeClass();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractEmbeddedDataSourceHelper.tearDownAfterClass();
    }

    public String getFileAsString(final String name) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(name).getFile());
        byte[] encoded = Files.readAllBytes(Path.of(file.toURI()));
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
