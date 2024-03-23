package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@With
@AllArgsConstructor
public class JobStopRule {
    private static final Pattern pattern = Pattern.compile("([ucant])(\\d+)/?(\\d*)");

    public static final int ANY_FAILURE_TYPE = 0;
    public static final int CONFIRMED_FAILURE_TYPE = 1;
    public static final int UNCONFIRMED_FAILURE_TYPE = 2;
    public static final int COUNT_TYPE = 3;
    public static final int TIMEOUT_TYPE = 4;

    private Integer numberMax;
    private final long numberLimit;
    private int ruleType;
    private final String ruleStr;

    public JobStopRule(String ruleStr) {
        this.ruleStr = ruleStr;
        Matcher m = pattern.matcher(ruleStr);
        if (m.matches()) {
            String type = m.group(1);
            if ("u".equals(type)) {
                ruleType = UNCONFIRMED_FAILURE_TYPE;
            } else if ("c".equals(type)) {
                ruleType = CONFIRMED_FAILURE_TYPE;
            } else if ("a".equals(type)) {
                ruleType = ANY_FAILURE_TYPE;
            } else if ("n".equals(type)) {
                ruleType = COUNT_TYPE;
            } else if ("t".equals(type)) {
                ruleType = TIMEOUT_TYPE;
            }
            numberLimit = Integer.parseInt(m.group(2));
            String numberMaxStr = m.group(3);
            if (numberMaxStr != null && !numberMaxStr.trim().isEmpty()) {
                numberMax = Integer.parseInt(numberMaxStr);
                if (numberLimit >= numberMax) {
                    throw new IllegalArgumentException(
                            "The first number must be less than the second (rule: " + ruleStr + ")");
                }
                if (numberMax < 3 || numberMax > 10000) {
                    throw new IllegalArgumentException(
                            "The last number must be between 3 and 10000 (rule: " + ruleStr + ")");
                }
                if (numberLimit < 2) {
                    throw new IllegalArgumentException(
                            "The first number must be between 2 and 10000 (rule: " + ruleStr + ")");
                }
            } else if (numberLimit < 1) {
                throw new IllegalArgumentException(
                        "The number must be greater than 0 (rule: " + ruleStr + ")");
            }
        } else {
            throw new IllegalArgumentException(
                    "The rule " + ruleStr + " does not match the regexp pattern " + pattern);
        }
    }
}