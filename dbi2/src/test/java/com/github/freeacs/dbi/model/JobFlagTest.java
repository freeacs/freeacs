package com.github.freeacs.dbi.model;

import com.github.freeacs.dbi.domain.JobType;
import com.github.freeacs.dbi.domain.JobFlag;
import com.github.freeacs.dbi.domain.JobServiceWindow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JobFlagTest {

    @Test
    void testJobFlagConstructorWithFlagStr() {
        // Test the constructor with a flagStr
        JobFlag jobFlag = new JobFlag("TR069_SCRIPT|DISRUPTIVE");
        assertEquals(JobType.TR069_SCRIPT, jobFlag.getType());
        assertEquals(JobServiceWindow.DISRUPTIVE, jobFlag.getServiceWindow());
    }

    @Test
    void testJobFlagConstructorWithJobTypeAndJobServiceWindow() {
        // Test the constructor with a JobType and JobServiceWindow
        JobFlag jobFlag = new JobFlag(JobType.TR069_SCRIPT, JobServiceWindow.REGULAR);
        assertEquals(JobType.TR069_SCRIPT, jobFlag.getType());
        assertEquals(JobServiceWindow.REGULAR, jobFlag.getServiceWindow());
    }

    @Test
    void testGetFlag() {
        // Test the getFlag method
        JobFlag jobFlag = new JobFlag(JobType.TR069_SCRIPT, JobServiceWindow.DISRUPTIVE);
        assertEquals("TR069_SCRIPT|DISRUPTIVE", jobFlag.getFlag());
    }

    @Test
    void testJobFlagConstructorWithInvalidFlagStr() {
        // Test the constructor with an invalid flagStr
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new JobFlag("INVALID|DISRUPTIVE"));
        assertTrue(exception.getMessage().contains("No enum constant com.github.freeacs.dbi.domain.JobType.INVALID"));
    }
}