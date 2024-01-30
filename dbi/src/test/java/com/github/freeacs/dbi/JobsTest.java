package com.github.freeacs.dbi;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JobsTest extends BaseDBITest {

  @Test
  public void add() throws SQLException {
    // Given:
    String unitId = "YidduJb7sssC1";
    String unittypeName = "Test unittype 1";
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
    Assertions.assertEquals(jobName, job.getName());
    Assertions.assertEquals(jobDesc, job.getDescription());
    Assertions.assertEquals(jobFlag, job.getFlags());
    Assertions.assertEquals(unittype, job.getUnittype());
    Assertions.assertEquals(group, job.getGroup());
  }

  @Test
  public void delete() throws SQLException {
    // Given:
    String unitId = "YidduJb7sssC2";
    String unittypeName = "Test unittype 2";
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
    Assertions.assertNull(unittype.getJobs().getByName(jobName));
  }
}
