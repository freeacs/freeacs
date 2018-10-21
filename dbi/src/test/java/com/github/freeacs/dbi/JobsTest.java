package com.github.freeacs.dbi;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.Test;

public class JobsTest extends BaseDBITest {

  @Test
  public void add() throws SQLException {
    // Given:
    String unitId = "YidduJb7sssC";
    String unittypeName = "Test unittype";
    String groupName = "Group";
    String profileName = "Default";
    String jobName = "Job";
    JobFlag jobFlag = new JobFlag("CONFIG|REGULAR");
    String jobDesc = "Description";

    // When:
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    Profile profile = unittype.getProfiles().getByName(profileName);
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    TestUtils.createUnitAndVerify(acsUnit, unitId, profile);
    Group group = TestUtils.createGroupAndVerify(groupName, profileName, unittype, acs);
    Job job = TestUtils.createJobAndVerify(jobName, jobFlag, jobDesc, unittype, group, acs);

    // Then:
    assertEquals(jobName, job.getName());
    assertEquals(jobDesc, job.getDescription());
    assertEquals(jobFlag, job.getFlags());
    assertEquals(unittype, job.getUnittype());
    assertEquals(group, job.getGroup());
  }

  @Test
  public void delete() throws SQLException {
    // Given:
    String unitId = "YidduJb7sssC";
    String unittypeName = "Test unittype";
    String groupName = "Group";
    String profileName = "Default";
    String jobName = "Job";
    JobFlag jobFlag = new JobFlag("CONFIG|REGULAR");
    String jobDesc = "Description";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    Profile profile = unittype.getProfiles().getByName(profileName);
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    TestUtils.createUnitAndVerify(acsUnit, unitId, profile);
    Group group = TestUtils.createGroupAndVerify(groupName, profileName, unittype, acs);
    Job job = TestUtils.createJobAndVerify(jobName, jobFlag, jobDesc, unittype, group, acs);

    // When:
    unittype.getJobs().delete(job, acs);

    // Then:
    assertNull(unittype.getJobs().getByName(jobName));
  }
}
