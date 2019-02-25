package com.github.freeacs.common.util;

import ch.vorburger.exec.ManagedProcessException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractEmbeddedDataSourceClassTest extends AbstractEmbeddedDataSourceHelper implements FileSlurper {

    @BeforeClass
    public static void setUpBeforeClass() throws ManagedProcessException {
        AbstractEmbeddedDataSourceHelper.setUpBeforeClass();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractEmbeddedDataSourceHelper.tearDownAfterClass();
    }
}
