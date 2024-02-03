package com.github.freeacs.common.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public interface FileSlurper {

    static String getFileAsString(final String name) throws IOException {
        try (InputStream inputStream = FileSlurper.class.getResourceAsStream(name)) {
            return IOUtils.toString(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
        }
    }
}
