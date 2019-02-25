package com.github.freeacs.common.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public interface FileSlurper {

    default String getFileAsString(final String name) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(name);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
}
