package com.github.freeacs.dbi.model;

import com.github.freeacs.dbi.domain.JobStopRule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JobStopRuleTest {

    @Test
    void testJobStopRule() {
        // Test the constructor with different ruleStr values
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new JobStopRule("invalidRuleStr"));
        assertTrue(exception.getMessage().contains("The rule invalidRuleStr does not match the regexp pattern"));
        assertDoesNotThrow(() -> new JobStopRule("u10"));
        assertDoesNotThrow(() -> new JobStopRule("c10/20"));
    }

    @Test
    void testJobStopRuleWithInvalidNumberMax() {
        // Test the constructor with invalid numberMax values
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> new JobStopRule("c10/2"));
        assertTrue(exception1.getMessage().contains("The first number must be less than the second (rule: c10/2)"));
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> new JobStopRule("c10/10001"));
        assertTrue(exception2.getMessage().contains("The last number must be between 3 and 10000 (rule: c10/10001)"));
    }

    @Test
    void testJobStopRuleWithInvalidNumberLimit() {
        // Test the constructor with invalid numberLimit values
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> new JobStopRule("c0"));
        assertTrue(exception1.getMessage().contains("The number must be greater than 0 (rule: c0)"));
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> new JobStopRule("c0/10"));
        assertTrue(exception2.getMessage().contains("The first number must be between 2 and 10000 (rule: c0/10)"));
    }

    @Test
    void testJobStopRuleWithValidUnconfirmedFailureType() {
        // Test the constructor with a valid unconfirmed failure type ruleStr
        JobStopRule rule = new JobStopRule("u10");
        assertEquals(JobStopRule.UNCONFIRMED_FAILURE_TYPE, rule.getRuleType());
        assertEquals(10, rule.getNumberLimit());
    }

    @Test
    void testJobStopRuleWithValidConfirmedFailureType() {
        // Test the constructor with a valid confirmed failure type ruleStr
        JobStopRule rule = new JobStopRule("c10/20");
        assertEquals(JobStopRule.CONFIRMED_FAILURE_TYPE, rule.getRuleType());
        assertEquals(10, rule.getNumberLimit());
        assertEquals(20, rule.getNumberMax());
    }

    @Test
    void testJobStopRuleWithValidAnyFailureType() {
        // Test the constructor with a valid any failure type ruleStr
        JobStopRule rule = new JobStopRule("a10");
        assertEquals(JobStopRule.ANY_FAILURE_TYPE, rule.getRuleType());
        assertEquals(10, rule.getNumberLimit());
    }

    @Test
    void testJobStopRuleWithValidCountType() {
        // Test the constructor with a valid count type ruleStr
        JobStopRule rule = new JobStopRule("n10/20");
        assertEquals(JobStopRule.COUNT_TYPE, rule.getRuleType());
        assertEquals(10, rule.getNumberLimit());
        assertEquals(20, rule.getNumberMax());
    }

    @Test
    void testJobStopRuleWithValidTimeoutType() {
        // Test the constructor with a valid timeout type ruleStr
        JobStopRule rule = new JobStopRule("t10");
        assertEquals(JobStopRule.TIMEOUT_TYPE, rule.getRuleType());
        assertEquals(10, rule.getNumberLimit());
    }
}