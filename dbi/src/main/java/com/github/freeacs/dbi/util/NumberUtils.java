package com.github.freeacs.dbi.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public final class NumberUtils {
    /**
     * Parses a string to an integer.
     *
     * @param userIdStr the string to parse
     * @return an optional containing the integer if the string could be parsed, empty otherwise
     */
    public static Optional<Integer> parseStringId(String userIdStr) {
        if (userIdStr == null) {
            return Optional.empty();
        }
        Integer id = null;
        try {
            id = Integer.valueOf(userIdStr);
        } catch (NumberFormatException ignored) {
            log.warn("Failed to parse id {}", userIdStr);
        }
        return Optional.ofNullable(id);
    }
}
