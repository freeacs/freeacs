package com.github.freeacs.dbi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TestUtils {
  private TestUtils() {}

  public static class Param {
    final String name;
    final String flag;

    public Param(String name, String flag) {
      this.name = name;
      this.flag = flag;
    }
  }

  public static Unittype createUnittypeParameters(Unittype unittype, List<Param> params, ACS acs)
      throws SQLException {
    UnittypeParameters unittypeParameters = unittype.getUnittypeParameters();
    List<UnittypeParameter> parametersToCreate =
        params
            .stream()
            .map(
                param ->
                    new UnittypeParameter(
                        unittype, param.name, new UnittypeParameterFlag(param.flag)))
            .collect(Collectors.toList());
    unittypeParameters.addOrChangeUnittypeParameters(parametersToCreate, acs);
    unittype.setUnittypeParameters(unittypeParameters);
    return unittype;
  }

  public static Unittype createUnittype(String name, ACS acs) throws SQLException {
    Unittype unittype = new Unittype(name, name, name, Unittype.ProvisioningProtocol.TR069);
    acs.getUnittypes().addOrChangeUnittype(unittype, acs);
    return unittype;
  }

  public static Profile createProfile(String name, Unittype unittype, ACS acs) throws SQLException {
    Profile profile = new Profile(name, unittype);
    Profiles profiles = unittype.getProfiles();
    profiles.addOrChangeProfile(profile, acs);
    return profile;
  }

  public static Unit createUnitAndVerify(ACSUnit acsUnit, String unitId, Profile profile)
      throws SQLException {
    acsUnit.addUnits(Collections.singletonList(unitId), profile);
    Unit unit = acsUnit.getUnitById(unitId);
    assertEquals(unitId, unit.getId());
    assertEquals(profile.getName(), unit.getProfile().getName());
    assertEquals(profile.getUnittype().getName(), unit.getUnittype().getName());
    return unit;
  }

  public static Unit createUnitAndVerify(
      ACSUnit acsUnit, String unitId, ACS acs, String unitTypeName, String profileName)
      throws SQLException {
    Unittype unittype = createUnittype(unitTypeName, acs);
    Profile profile = unittype.getProfiles().getByName(profileName);
    return createUnitAndVerify(acsUnit, unitId, profile);
  }

  public static Job createJobAndVerify(
      String jobName, JobFlag jobFlag, String jobDesc, Unittype unittype, Group group, ACS acs)
      throws SQLException {
    Jobs jobs = unittype.getJobs();
    Job job = new Job(unittype, jobName, jobFlag, jobDesc, group, 60, "n1", null, null, 0, 0);
    jobs.add(job, acs);
    job = jobs.getByName(jobName);
    assertNotNull(job);
    return job;
  }

  public static Group createGroupAndVerify(
      String groupName, String profileName, Unittype unittype, ACS acs) throws SQLException {
    Groups groups = unittype.getGroups();
    Profile profile = unittype.getProfiles().getByName(profileName);
    Group group = new Group(groupName, groupName, null, unittype, profile);
    groups.addOrChangeGroup(group, acs);
    group = groups.getByName(groupName);
    assertNotNull(group);
    return group;
  }

  public static UnitJob createUnitJobAndVerify(UnitJobs unitJobs, String unitId, int jobId)
      throws SQLException {
    UnitJob unitJob = new UnitJob();
    unitJob.setUnitId(unitId);
    unitJob.setJobId(jobId);
    unitJob.setUnconfirmedFailed(1);
    unitJob.setConfirmedFailed(1);
    unitJob.setStatus("status");
    unitJob.setStartTimestamp(new Date());
    unitJob.setEndTimestamp(new Date());
    unitJobs.addOrChange(unitJob);
    Job job = new Job();
    job.setId(jobId);
    List<UnitJob> jobs = unitJobs.readAllUnprocessed(job);
    assertEquals(1, jobs.size());
    return unitJob;
  }

  public static File createFileAndVerify(
      Unittype unittype, String fileName, byte[] bytes, FileType fileType, String version, ACS acs)
      throws SQLException {
    Files files = unittype.getFiles();
    File file = new File();
    file.setName(fileName);
    file.setDescription(fileName);
    file.setUnittype(unittype);
    file.setBytes(bytes);
    file.setType(fileType);
    file.setVersion(version);
    files.addOrChangeFile(file, acs);
    file = files.getByName(fileName);
    assertNotNull(file);
    return file;
  }

  public static GroupParameter createGroupParameterAndVerify(
      String utpParamName, String value, Unittype unittype, Group group, ACS acs)
      throws SQLException {
    UnittypeParameters unittypeParameters = unittype.getUnittypeParameters();
    UnittypeParameter unittypeParameter = unittypeParameters.getByName(utpParamName);
    Parameter parameter = new Parameter(unittypeParameter, value);
    GroupParameter groupParameter = new GroupParameter(parameter, group);
    group.getGroupParameters().addOrChangeGroupParameter(groupParameter, acs);
    return groupParameter;
  }

  public static User createUser(
          String username, String fullname, String password, Boolean admin, ACS acs)
          throws SQLException {
    Users users = new Users(acs.getDataSource());
    User adminUser = users.getUnprotected("admin");
    User newUser = new User(username,fullname,"",admin, users);
    newUser.setSecretClearText(password);
    users.addOrChange(newUser,adminUser);

    return newUser;
  }
}
