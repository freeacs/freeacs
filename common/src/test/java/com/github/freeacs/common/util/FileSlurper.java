package com.github.freeacs.common.util;

import org.apache.commons.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public interface FileSlurper {

    static String getFileAsString(final String name) throws IOException {
        try (InputStream inputStream = FileSlurper.class.getResourceAsStream(name)) {
            return IOUtil.toString(Objects.requireNonNull(inputStream), "UTF-8");
        }
    }
}
