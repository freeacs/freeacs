package com.github.freeacs.dbi;

import com.github.freeacs.dbi.DynamicStatement.NullInteger;
import com.github.freeacs.dbi.util.SystemParameters;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interface is only for reading the following tables/concepts:
 *
 * <p>1. Job 2. JobParameter
 *
 * @author Morten
 */
public class Jobs {
  private static Logger logger = LoggerFactory.getLogger(Jobs.class);
  private Map<Integer, Job> idMap = new HashMap<>();
  private Map<String, Job> nameMap = new HashMap<>();
  private Unittype unittype;
  private static Map<String, String> jobParameterRules = new HashMap<>();

  static {
    jobParameterRules.put(JobFlag.JobType.RESTART + SystemParameters.RESTART, "Allowed");
    jobParameterRules.put(JobFlag.JobType.RESTART + "REST_OF_PARAMETERS", "Forbidden");
    jobParameterRules.put(JobFlag.JobType.RESET + SystemParameters.RESET, "Allowed");
    jobParameterRules.put(JobFlag.JobType.RESET + "REST_OF_PARAMETERS", "Forbidden");
    jobParameterRules.put(
        JobFlag.JobType.SOFTWARE + SystemParameters.DESIRED_SOFTWARE_VERSION, "Allowed");
    jobParameterRules.put(JobFlag.JobType.SOFTWARE + SystemParameters.SOFTWARE_URL, "Allowed");
    jobParameterRules.put(JobFlag.JobType.SOFTWARE + "REST_OF_PARAMETERS", "Forbidden");
    jobParameterRules.put(
        JobFlag.JobType.TR069_SCRIPT + SystemParameters.DESIRED_TR069_SCRIPT, "Allowed");
    jobParameterRules.put(JobFlag.JobType.TR069_SCRIPT + "REST_OF_PARAMETERS", "Forbidden");
    jobParameterRules.put(JobFlag.JobType.CONFIG + SystemParameters.RESET, "Forbidden");
    jobParameterRules.put(JobFlag.JobType.CONFIG + SystemParameters.RESTART, "Forbidden");
    jobParameterRules.put(
        JobFlag.JobType.CONFIG + SystemParameters.DESIRED_SOFTWARE_VERSION, "Forbidden");
    jobParameterRules.put(JobFlag.JobType.CONFIG + SystemParameters.SOFTWARE_URL, "Forbidden");
    jobParameterRules.put(
        JobFlag.JobType.CONFIG + SystemParameters.DESIRED_TR069_SCRIPT, "Forbidden");
    jobParameterRules.put(JobFlag.JobType.CONFIG + SystemParameters.JOB_CURRENT, "Forbidden");
    jobParameterRules.put(JobFlag.JobType.CONFIG + SystemParameters.JOB_CURRENT_KEY, "Forbidden");
    jobParameterRules.put(JobFlag.JobType.CONFIG + SystemParameters.JOB_HISTORY, "Forbidden");
    jobParameterRules.put(JobFlag.JobType.CONFIG + SystemParameters.JOB_DISRUPTIVE, "Forbidden");
    jobParameterRules.put(JobFlag.JobType.CONFIG + "REST_OF_PARAMETERS", "Allowed");
    jobParameterRules.put(JobFlag.JobType.KICK + "REST_OF_PARAMETERS", "Forbidden");
    jobParameterRules.put(JobFlag.JobType.TELNET + "REST_OF_PARAMETERS", "Forbidden");
    jobParameterRules.put(JobFlag.JobType.SHELL + "REST_OF_PARAMETERS", "Forbidden");
  }

  public Jobs(Map<Integer, Job> idMap, Map<String, Job> nameMap, Unittype unittype) {
    this.idMap = idMap;
    this.nameMap = nameMap;
    this.unittype = unittype;
  }

  private void checkParameters(List<JobParameter> jobParameters) {
    for (JobParameter jp : jobParameters) {
      JobFlag.JobType jobType = jp.getJob().getFlags().getType();
      String utpName = jp.getParameter().getUnittypeParameter().getName();
      //			if (jp.getParameter().getUnittypeParameter().getFlag().isInspection())
      //				throw new IllegalArgumentException("The unit type parameter " + utpName + " is an
      // inspection parameter, cannot be set in job");
      // Special modification for TR069_SCRIPT-parameter:
      if (utpName.contains(SystemParameters.DESIRED_TR069_SCRIPT)) {
        utpName = SystemParameters.DESIRED_TR069_SCRIPT;
      }
      String rule = jobParameterRules.get(jobType + utpName);
      if (rule == null) {
        rule = jobParameterRules.get(jobType + "REST_OF_PARAMETERS");
      }
      if ("Forbidden".equals(rule)) {
        throw new IllegalArgumentException(
            "The job parameter " + utpName + " cannot be set for job type " + jobType);
      }
    }
  }

  public void addOrChangeJobParameters(List<JobParameter> jobParameters, ACS acs)
      throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    Connection connection = null;
    PreparedStatement pp = null;
    String sql;
    boolean wasAutoCommit = false;
    try {
      checkParameters(jobParameters);
      connection = acs.getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      for (int i = 0; jobParameters != null && i < jobParameters.size(); i++) {
        JobParameter jobParameter = jobParameters.get(i);
        Parameter parameter = jobParameter.getParameter();
        String unitId = jobParameter.getUnitId();
        if (parameter.getValue() != null && parameter.getValue().length() > 250) {
          parameter.setValue(parameter.getValue().substring(0, 250) + "...");
        }
        String action = "Inserted";
        try {
          sql =
              "INSERT INTO job_param (job_id, unit_id, unit_type_param_id, value) VALUES (?, ?, ?, ?)";
          pp = connection.prepareStatement(sql);
          pp.setInt(1, jobParameter.getJob().getId());
          pp.setString(2, unitId);
          pp.setInt(3, parameter.getUnittypeParameter().getId());
          pp.setString(4, parameter.getValue());
          pp.setQueryTimeout(60);
          pp.executeUpdate();
          pp.close();
        } catch (SQLException sqlex) {
          pp.close();
          action = "Updated";
          sql =
              "UPDATE job_param SET value = ? WHERE job_id = ? AND unit_id = ? AND unit_type_param_id = ?";
          pp = connection.prepareStatement(sql);
          pp.setString(1, parameter.getValue());
          pp.setInt(2, jobParameter.getJob().getId());
          pp.setString(3, unitId);
          pp.setInt(4, parameter.getUnittypeParameter().getId());
          pp.setQueryTimeout(60);
          pp.executeUpdate();
          pp.close();
        }

        logger.info(action + " job parameter " + parameter.getUnittypeParameter().getName());
        if (unitId.equals(Job.ANY_UNIT_IN_GROUP)) {
          jobParameter
              .getJob()
              .getDefaultParameters()
              .put(parameter.getUnittypeParameter().getName(), jobParameter);
        }

        if (i > 0 && i % 100 == 0) {
          connection.commit();
        }
      }
      connection.commit();
      if (acs.getDbi() != null && !jobParameters.isEmpty()) {
        acs.getDbi()
            .publishChange(
                jobParameters.get(0).getJob(),
                jobParameters.get(0).getJob().getGroup().getUnittype());
      }
    } catch (SQLException sqlex) {
      if (connection != null) {
        connection.rollback();
      }
      throw sqlex;
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  public void deleteJobParameters(Job job, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    Connection connection = null;
    Statement s = null;
    String sql;
    SQLException sqle;
    try {
      connection = acs.getDataSource().getConnection();
      s = connection.createStatement();
      sql = "DELETE FROM job_param WHERE job_id = " + job.getId();
      s.setQueryTimeout(60);
      s.executeUpdate(sql);
      Job j = getById(job.getId());
      j.setDefaultParameters();

      logger.info("Deleted all job parameters for job " + job.getId());
      if (acs.getDbi() != null) {
        acs.getDbi().publishChange(job, job.getGroup().getUnittype());
      }
    } catch (SQLException sqlex) {
      sqle = sqlex;
      throw sqle;
    } finally {
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
  }

  public int deleteJobParameters(List<JobParameter> jobParameters, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    Connection connection = null;
    Statement s = null;
    String sql;
    SQLException sqle;
    boolean wasAutoCommit = false;
    try {
      connection = acs.getDataSource().getConnection();
      wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      s = connection.createStatement();
      int rowsDeleted = 0;
      for (int i = 0; i < jobParameters.size(); i++) {
        JobParameter jobParameter = jobParameters.get(i);
        Parameter parameter = jobParameter.getParameter();
        Integer utpId = parameter.getUnittypeParameter().getId();
        String unitId = jobParameter.getUnitId();
        sql =
            "DELETE FROM job_param WHERE job_id = '"
                + jobParameter.getJob().getId()
                + "' AND unit_id = '"
                + unitId
                + "' AND unit_type_param_id = "
                + utpId;
        s.setQueryTimeout(60);
        rowsDeleted += s.executeUpdate(sql);

        logger.info(
            "Deleted job parameter "
                + parameter.getUnittypeParameter().getName()
                + " on job "
                + jobParameter.getJob().getId());
        if (unitId.equals(Job.ANY_UNIT_IN_GROUP)) {
          Job j = getById(jobParameter.getJob().getId());
          if (j != null
              && j.getDefaultParameters() != null
              && parameter.getUnittypeParameter() != null) {
            j.getDefaultParameters().remove(parameter.getUnittypeParameter().getName());
          }
        }

        if (i > 0 && i % 100 == 0) {
          connection.commit();
        }
      }
      connection.commit();
      if (acs.getDbi() != null && !jobParameters.isEmpty()) {
        acs.getDbi()
            .publishChange(
                jobParameters.get(0).getJob(),
                jobParameters.get(0).getJob().getGroup().getUnittype());
      }
      return rowsDeleted;
    } catch (SQLException sqlex) {
      sqle = sqlex;
      if (connection != null) {
        connection.rollback();
      }
      throw sqle;
    } finally {
      if (s != null) {
        s.close();
      }
      if (connection != null) {
        connection.setAutoCommit(wasAutoCommit);
        connection.close();
      }
    }
  }

  protected void removeJobFromDataModel(Job job) {
    if (job.getDependency() != null) {
      job.getDependency().removeChild(job);
    }
    idMap.remove(job.getId());
    nameMap.remove(job.getName());
  }

  public void delete(Job job, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    deleteJobParameters(job, acs);
    Connection c = null;
    PreparedStatement pp = null;
    SQLException sqle;
    try {
      c = acs.getDataSource().getConnection();
      String sql = "DELETE FROM job WHERE job_id = ?";
      pp = c.prepareStatement(sql);
      pp.setInt(1, job.getId());
      pp.setQueryTimeout(60);
      pp.execute();
      removeJobFromDataModel(job);

      logger.info("Deleted job " + job.getId());
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(job, job.getGroup().getUnittype());
      }
    } catch (SQLException sqlex) {
      sqle = sqlex;
      throw sqle;
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /**
   * Returns a list over allowed job dependencies.
   *
   * @param job The Job
   * @return List<Job>
   */
  public List<Job> getAllowedDependencies(Job job) {
    List<Job> allowed = new ArrayList<>();
    for (Job j : idMap.values()) {
      if (j.getGroup().getUnittype().getName().equals(unittype.getName())
          && !isDependencyLoop(job, j)) {
        allowed.add(j);
      }
    }
    return allowed;
  }

  private boolean isDependencyLoop(Job job, Job dep) {
    Job dependency = dep;
    if (dependency == null || job == null) {
      return false;
    }
    if (job.getId().equals(dependency.getId())) {
      return true;
    }

    while (dependency != null) {
      if (dependency.getDependency() != null && dependency.getDependency().getId() == job.getId()) {
        return true;
      }
      dependency = dependency.getDependency();
    }
    return false;
  }

  public void add(Job job, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    job.setStatus(JobStatus.READY);
    job.validate();
    Connection c = null;
    PreparedStatement ps = null;
    try {
      if (nameMap.get(job.getName()) != null) {
        throw new IllegalArgumentException("The job name already exists, choose another name");
      }
      c = acs.getDataSource().getConnection();
      DynamicStatement ds = new DynamicStatement();
      ds.setSql("INSERT INTO job (");
      ds.addSqlAndArguments("job_name, ", job.getName());
      ds.addSqlAndArguments("job_type, ", job.getFlags().toString());
      ds.addSqlAndArguments("description, ", job.getDescription());
      ds.addSqlAndArguments("group_id, ", job.getGroup().getId());
      ds.addSqlAndArguments("unconfirmed_timeout, ", job.getUnconfirmedTimeout());
      ds.addSqlAndArguments("stop_rules, ", job.getStopRulesSerialized());
      ds.addSqlAndArguments("status, ", job.getStatus().toString());
      if (job.getFile() != null) {
        ds.addSqlAndArguments("firmware_id, ", job.getFile().getId());
      }
      if (job.getDependency() != null) {
        ds.addSqlAndArguments("job_id_dependency, ", job.getDependency().getId());
      }
      if (job.getRepeatCount() != null) {
        ds.addSqlAndArguments("repeat_count, ", job.getRepeatCount());
      }
      if (job.getRepeatInterval() != null) {
        ds.addSqlAndArguments("repeat_interval, ", job.getRepeatInterval());
      }
      ds.addSqlAndArguments("completed_no_failure, ", 0);
      ds.addSqlAndArguments("completed_had_failure, ", 0);
      ds.addSqlAndArguments("confirmed_failed, ", 0);
      ds.addSqlAndArguments("unconfirmed_failed) ", 0);
      ds.addSql(" VALUES (" + ds.getQuestionMarks() + ")");
      ps = ds.makePreparedStatement(c, "job_id");
      ps.setQueryTimeout(60);
      ps.executeUpdate();
      ResultSet gk = ps.getGeneratedKeys();
      if (gk.next()) {
        job.setId(gk.getInt(1));
      }

      Job dep = job.getDependency();
      if (dep != null && !dep.getChildren().contains(job)) {
        dep.getChildren().add(job);
      }

      idMap.put(job.getId(), job);
      nameMap.put(job.getName(), job);

      updateMandatoryJobParameters(job, acs);
      logger.info("Inserted job " + job.getId());
      if (acs.getDbi() != null) {
        acs.getDbi().publishAdd(job, job.getGroup().getUnittype());
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  private void updateMandatoryJobParameters(Job job, ACS acs) throws SQLException {
    if (job.getFlags().getType() == JobFlag.JobType.SOFTWARE) {
      Parameter param =
          new Parameter(
              unittype.getUnittypeParameters().getByName(SystemParameters.DESIRED_SOFTWARE_VERSION),
              job.getFile().getVersion());
      JobParameter jp = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, param);
      List<JobParameter> jobParameters = new ArrayList<>();
      jobParameters.add(jp);
      addOrChangeJobParameters(jobParameters, acs);
    } else if (job.getFlags().getType() == JobFlag.JobType.TR069_SCRIPT) {
      UnittypeParameter jobUtp =
          SystemParameters.getTR069ScriptParameter(
              job.getFile().getTargetName(),
              SystemParameters.TR069ScriptType.Version,
              acs,
              job.getUnittype());
      Parameter param = new Parameter(jobUtp, job.getFile().getVersion());
      JobParameter jp = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, param);
      List<JobParameter> jobParameters = new ArrayList<>();
      jobParameters.add(jp);
      addOrChangeJobParameters(jobParameters, acs);
    } else if (job.getFlags().getType() == JobFlag.JobType.RESTART) {
      Parameter param =
          new Parameter(unittype.getUnittypeParameters().getByName(SystemParameters.RESTART), "1");
      JobParameter jp = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, param);
      List<JobParameter> jobParameters = new ArrayList<>();
      jobParameters.add(jp);
      addOrChangeJobParameters(jobParameters, acs);
    } else if (job.getFlags().getType() == JobFlag.JobType.RESET) {
      Parameter param =
          new Parameter(unittype.getUnittypeParameters().getByName(SystemParameters.RESET), "1");
      JobParameter jp = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, param);
      List<JobParameter> jobParameters = new ArrayList<>();
      jobParameters.add(jp);
      addOrChangeJobParameters(jobParameters, acs);
    }
  }

  /**
   * Decided to skip unit-specific job parameters. Cause extra work/SQL in TR-069 server, has never
   * been used in 5 years.
   */
  public Map<String, JobParameter> readJobParameters(Job job, Unit unit, ACS acs) {
    return job.getDefaultParameters();
  }

  /**
   * This method can't publish the same way all other add/change/delete methods do in ACS. The
   * reason is simply that this method may be run very often, maybe every second. Thus we could end
   * up demanding all modules in ACS to reload the whole ACS object every second. Even if we
   * modified publish of Job-object to only read the job-table, that could still be a significant
   * load.
   *
   * <p>The conclusion: Prepare a message in this publish which gives the information about the
   * changes directly, in other words the modules may skip reading the job table. We make an effort
   * to only send the data if there really is a change. That will keep the number of message and
   * data and load and the very minimum.
   */
  public void changeFromCore(Job job, String publishMsg, ACS acs) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    SQLException sqle = null;
    try {
      c = acs.getDataSource().getConnection();
      DynamicStatement ds = new DynamicStatement();
      ds.addSql("UPDATE job SET ");
      ds.addSqlAndArguments("completed_had_failure = ?, ", job.getCompletedHadFailures());
      ds.addSqlAndArguments("completed_no_failure = ?, ", job.getCompletedNoFailures());
      ds.addSqlAndArguments("confirmed_failed = ?, ", job.getConfirmedFailed());
      ds.addSqlAndArguments("unconfirmed_failed = ?, ", job.getUnconfirmedFailed());
      if (JobStatus.COMPLETED.equals(job.getStatus())) {
        ds.addSqlAndArguments("end_timestamp = ?, ", new Date());
      }
      ds.addSqlAndArguments("status = ? ", job.getStatus().toString());
      ds.addSqlAndArguments("WHERE job_id = ? AND ", job.getId());
      ds.addSqlAndArguments(
          "(status = ? OR status = ?)", JobStatus.STARTED.toString(), JobStatus.PAUSED.toString());
      pp = ds.makePreparedStatement(c);
      pp.setQueryTimeout(60);
      pp.execute();
      String message =
          "\tUpdated job "
              + job.getId()
              + " with ("
              + job.getStatus()
              + ","
              + job.getCompletedNoFailures()
              + ",";
      message +=
          job.getCompletedHadFailures()
              + ","
              + job.getUnconfirmedFailed()
              + ","
              + job.getConfirmedFailed()
              + ")";

      logger.info(message);
      if (!publishMsg.isEmpty() && acs.getDbi() != null) {
        acs.getDbi().publishJobCounters(job.getId(), publishMsg);
      }
    } catch (SQLException sqlex) {
      sqle = sqlex;
      throw sqle;
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  public void changeStatus(Job job, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    Connection c = null;
    try {
      c = acs.getDataSource().getConnection();
      DynamicStatement ds = new DynamicStatement();
      ds.addSql("UPDATE job SET ");
      if (job.getStatus() == JobStatus.STARTED && job.getStartTimestamp() == null) {
        long startTms = System.currentTimeMillis();
        job.setStartTimestamp(new Date(startTms));
        ds.addSqlAndArguments("start_timestamp = ?, ", new java.sql.Timestamp(startTms));
      } else if (job.getStatus() == JobStatus.COMPLETED) {
        long endTms = System.currentTimeMillis();
        job.setEndTimestamp(new Date(endTms));
        ds.addSqlAndArguments("end_timestamp = ?, ", new java.sql.Timestamp(endTms));
      }
      ds.addSqlAndArguments("status = ? ", job.getStatus().toString());
      ds.addSqlAndArguments("WHERE job_id = ? AND ", job.getId());
      if (job.getStatus() == JobStatus.READY) {
        ds.addSql(" status = '" + JobStatus.READY + "'");
      } else if (job.getStatus() == JobStatus.STARTED) {
        ds.addSql("(status = '" + JobStatus.STARTED + "' OR");
        ds.addSql(" status = '" + JobStatus.READY + "' OR");
        ds.addSql(" status = '" + JobStatus.PAUSED + "')");
      } else if (job.getStatus() == JobStatus.PAUSED) {
        ds.addSql("(status = '" + JobStatus.STARTED + "' OR");
        ds.addSql(" status = '" + JobStatus.PAUSED + "')");
      } else if (job.getStatus() == JobStatus.COMPLETED) {
        ds.addSql("(status = '" + JobStatus.PAUSED + "' OR");
        ds.addSql(" status = '" + JobStatus.COMPLETED + "')");
      }
      PreparedStatement ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(10);
      int rowsUpdated = ps.executeUpdate();
      ps.close();
      if (rowsUpdated > 0) {
        logger.info("Updated job " + job.getId() + " with status = " + job.getStatus());
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(job, job.getGroup().getUnittype());
        }
      } else {
        String msg =
            "The job was not updated, most likely because the status change was not allowed ";
        msg += "(but could also happen because the job was deleted).";
        throw new SQLException(msg);
      }
    } finally {
      if (c != null) {
        c.close();
      }
    }
  }

  /**
   * This method is only to be used by the UI. The update checks to see whether or not the status of
   * the job allows the changes. It will not change the following of the job: - job size - counters
   * since they are updated by an other method (and another agent). It's important to separate the
   * various updates methods since the agents are independent of each other.
   */
  public int changeFromUI(Job job, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    job.validate();
    Connection c = null;
    try {
      c = acs.getDataSource().getConnection();
      if (isDependencyLoop(job, job.getDependency())) {
        throw new IllegalArgumentException(
            "Job "
                + job.getId()
                + " cannot depend upon job "
                + job.getDependency().getId()
                + " since that creates a loop");
      }
      DynamicStatement ds = new DynamicStatement();
      ds.addSql("UPDATE job SET ");
      ds.addSqlAndArguments("description = ?, ", job.getDescription());
      ds.addSqlAndArguments("stop_rules = ?, ", job.getStopRulesSerialized());
      if (job.getDependency() != null) {
        ds.addSqlAndArguments("job_id_dependency = ?, ", job.getDependency().getId());
      } else {
        ds.addSqlAndArguments("job_id_dependency = ?, ", new NullInteger());
      }
      if (job.getFile() != null) {
        ds.addSqlAndArguments("firmware_id = ?, ", job.getFile().getId());
      }

      if (job.getStatus() == JobStatus.STARTED && job.getStartTimestamp() == null) {
        long startTms = System.currentTimeMillis();
        job.setStartTimestamp(new Date(startTms));
        ds.addSqlAndArguments("start_timestamp = ?, ", new java.sql.Timestamp(startTms));
      } else if (job.getStatus() == JobStatus.COMPLETED) {
        long endTms = System.currentTimeMillis();
        job.setEndTimestamp(new Date(endTms));
        ds.addSqlAndArguments("end_timestamp = ?, ", new java.sql.Timestamp(endTms));
      }
      if (job.getRepeatCount() != null) {
        ds.addSqlAndArguments("repeat_count = ?, ", job.getRepeatCount());
      } else {
        ds.addSqlAndArguments("repeat_count = ?, ", new NullInteger());
      }
      if (job.getRepeatInterval() != null) {
        ds.addSqlAndArguments("repeat_interval = ?, ", job.getRepeatInterval());
      } else {
        ds.addSqlAndArguments("repeat_interval = ?, ", new NullInteger());
      }
      ds.addSqlAndArguments("unconfirmed_timeout = ? ", job.getUnconfirmedTimeout());
      ds.addSqlAndArguments("WHERE job_id = ? AND ", job.getId());
      ds.addSql(" status <> '" + JobStatus.COMPLETED + "'");

      PreparedStatement ps = ds.makePreparedStatement(c);
      ps.setQueryTimeout(10);
      int rowsUpdated = ps.executeUpdate();
      ps.close();
      if (rowsUpdated > 0) {
        updateMandatoryJobParameters(job, acs);
        logger.info("Updated job " + job.getId() + " with type/description/status/rules");
        if (acs.getDbi() != null) {
          acs.getDbi().publishChange(job, job.getGroup().getUnittype());
        }
      } else {
        String msg =
            "The job was not updated, most likely because status was " + JobStatus.COMPLETED;
        msg += " or because the job was recently removed from the system";
        throw new SQLException(msg);
      }
      return rowsUpdated;
    } finally {
      if (c != null) {
        c.close();
      }
    }
  }

  protected static void refreshJob(Integer jobId, ACS acs) throws SQLException {
    Connection c = null;
    Statement s = null;
    ResultSet rs = null;
    try {
      c = acs.getDataSource().getConnection();
      s = c.createStatement();
      s.setQueryTimeout(60);
      rs =
          s.executeQuery(
              "SELECT * FROM job j, group_ g, unit_type u WHERE j.group_id = g.group_id AND g.unit_type_id = u.unit_type_id AND j.job_id = "
                  + jobId);
      Unittype unittype = null;
      Job newJob = null;
      if (rs.next()) {
        unittype = acs.getUnittype(rs.getInt("u.unit_type_id"));
        if (unittype == null) {
          return;
        } // The unittype is not accessible for this user
        Job oldJob = unittype.getJobs().getById(rs.getInt("job_id"));
        if (oldJob == null) {
          return;
        } // The job is not accessible for this user
        newJob = new Job();
        newJob.setUnittype(unittype); // Use "old" unittype
        newJob.setNextPII(
            oldJob.getNextPII()); // Copy from old job object - not possible to change anyway
        newJob.setId(rs.getInt("job_id"));
        newJob.setName(rs.getString("job_name"));
        newJob.setFlags(new JobFlag(rs.getString("job_type")));
        newJob.setDescription(rs.getString("description"));
        newJob.setGroup(
            oldJob.getGroup()); // Copy from old job object - not possible to change anyway
        newJob.setUnconfirmedTimeout(rs.getInt("unconfirmed_timeout"));
        newJob.setStopRules(rs.getString("stop_rules"));
        String statusStr = rs.getString("status");
        try {
          newJob.setStatus(JobStatus.valueOf(statusStr));
        } catch (Throwable t) {
          if ("STOPPED".equals(statusStr)) {
            newJob.setStatus(JobStatus.PAUSED);
          }
        }
        newJob.setCompletedNoFailures(rs.getInt("completed_no_failure"));
        newJob.setCompletedHadFailures(rs.getInt("completed_had_failure"));
        newJob.setConfirmedFailed(rs.getInt("confirmed_failed"));
        newJob.setUnconfirmedFailed(rs.getInt("unconfirmed_failed"));
        newJob.setStartTimestamp(rs.getTimestamp("start_timestamp"));
        newJob.setEndTimestamp(rs.getTimestamp("end_timestamp"));
        String firmwareIdStr = rs.getString("firmware_id");
        if (firmwareIdStr != null) {
          newJob.setFile(unittype.getFiles().getById(Integer.parseInt(firmwareIdStr)));
        }
        String depIdStr = rs.getString("job_id_dependency");
        if (depIdStr != null) {
          newJob.setDependency(unittype.getJobs().getById(Integer.valueOf(depIdStr)));
        }
        String repeatCountStr = rs.getString("repeat_count");
        if (repeatCountStr != null) {
          newJob.setRepeatCount(Integer.parseInt(repeatCountStr));
        }
        String repeatIntervalStr = rs.getString("repeat_interval");
        if (repeatIntervalStr != null) {
          newJob.setRepeatInterval(Integer.parseInt(repeatIntervalStr));
        }
      }
      s.close();
      s = c.createStatement();
      s.setQueryTimeout(60);
      rs =
          s.executeQuery(
              "SELECT * FROM job_param WHERE job_id = "
                  + jobId
                  + " AND unit_id = '"
                  + Job.ANY_UNIT_IN_GROUP
                  + "'");
      if (newJob != null && rs.next()) {
        Integer unitTypeParamId = rs.getInt("unit_type_param_id");
        String value = rs.getString("value");
        if (value == null) {
          value = "";
        }
        Unittype ut = newJob.getUnittype();
        UnittypeParameter utp = ut.getUnittypeParameters().getById(unitTypeParamId);
        JobParameter jp =
            new JobParameter(newJob, Job.ANY_UNIT_IN_GROUP, new Parameter(utp, value));
        newJob.getDefaultParameters().put(utp.getName(), jp);
      }
      if (newJob != null) {
        unittype.getJobs().getIdMap().put(newJob.getId(), newJob);
        unittype.getJobs().getNameMap().put(newJob.getName(), newJob);
      } else {
        for (Unittype ut : acs.getUnittypes().getUnittypes()) {
          Job j = ut.getJobs().getIdMap().remove(jobId);
          if (j != null) {
            ut.getJobs().getNameMap().remove(j.getName());
          }
        }
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (s != null) {
        s.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  public Job[] getGroupJobs(Integer groupId) {
    List<Job> groupJobs = new ArrayList<>();
    for (Job job : idMap.values()) {
      if (job.getGroup().getId().intValue() == groupId.intValue()) {
        groupJobs.add(job);
      }
    }
    return groupJobs.toArray(new Job[groupJobs.size()]);
  }

  public Job[] getJobs() {
    if (idMap == null) {
      idMap = new HashMap<>();
    }
    return idMap.values().toArray(new Job[idMap.size()]);
  }

  public Job getByName(String jobName) {
    return nameMap.get(jobName);
  }

  public Job getById(Integer jobId) {
    if (idMap != null) {
      return idMap.get(jobId);
    }
    return null;
  }

  public Unittype getUnittype() {
    return unittype;
  }

  protected Map<Integer, Job> getIdMap() {
    return idMap;
  }

  protected Map<String, Job> getNameMap() {
    return nameMap;
  }
}
