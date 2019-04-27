package com.github.freeacs.utils;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public abstract class Matchers {
    public static Matcher<String> hasNoSpace() {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(final Object item) {
                return !StringUtils.containsWhitespace(item.toString());
            }
            @Override
            public void describeTo(final Description description) {
                description.appendText("Should not contain space");
            }
        };
    }
}
