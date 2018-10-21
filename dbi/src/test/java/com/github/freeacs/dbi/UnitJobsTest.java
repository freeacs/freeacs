package com.github.freeacs.dbi;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import org.junit.Test;

public class UnitJobsTest extends BaseDBITest {

  @Test
  public void addOrChange() throws SQLException {
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
    UnitJobs unitJobs = new UnitJobs(dataSource);
    TestUtils.createUnitJobAndVerify(unitJobs, unitId, job.getId());

    // Then:
    List<UnitJob> unitJobList = unitJobs.readAllUnprocessed(job);
    assertEquals(1, unitJobList.size());
    assertEquals(unitId, unitJobList.get(0).getUnitId());
  }
}
