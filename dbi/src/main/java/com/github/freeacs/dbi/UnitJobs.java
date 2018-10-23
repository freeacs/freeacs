package com.github.freeacs.dbi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of unit_job table is for each job to:
 *
 * <p>1. Make it possible to detect and store all information about UNCONFIRMED failures 2. Make it
 * possible to detect and store all information about CONFIRMED failures 3. Make it possible to
 * detect and store the number of COMPLETED unit-jobs 4. Read out all information about units with
 * failures 5. Delete old jobs
 *
 * <p>To do so we need the following operations:
 *
 * <p>Provisioning Server operations, 2 start operations and 2 end operations:
 *
 * <p>1.1 Insert a new row into the table. Should set: jobId = jobId unitId = unitId status =
 * STARTED startTimestamp = System.currentTimemillis() processed = 0 1.2 Update a row (given unitId
 * and jobId and processed = 1). Should set: status = STARTED startTimestamp =
 * System.currentTimemillis() processed = 0 1.3 Update a row (given unitId and jobId and unit job
 * OK). Should set: status = COMPLETED_OK endTimestamp = System.currentTimemillis() processed = 0
 * 1.4 Update a row (given unitId and jobId and unit_job FAILED). Should set: status =
 * CONFIRMED_FAILED endTimestamp = System.currentTimemillis() confirmed = confirmed + 1 processed =
 * 0;
 *
 * <p>Job Control Server SQLs:
 *
 * <p>2.1 Update a row (given unitId and jobId and processed = 0 and status = STARTED and
 * start_timestamp older than jobs constraint rule for timeout). Should set: endTimestamp =
 * System.currentTimemillis() status = UNCONFIRMED_FAILED unconfirmed = unconfirmed + 1 2.2 Read all
 * rows where processed = 0 and status <> STARTED 2.3 Update a row (given unitId and jobId and
 * processed = 0). Should set: processed = 1 2.4 Delete all row for a given jobId where processed =
 * 1 and status = COMPLETED_OK and confirmed = 0 and unconfirmed = 0 (remember the number of rows
 * affected) 2.5 Read all rows 2.6 Delete all rows for a certain job 2.7 Sum of confirmed and
 * unconfirmed columns for a given job 2.8 Sum of completed rows (which were not deleted in 2.4)
 *
 * <p>So, to draw the line from the operations to the requirements, we can make this simple list:
 *
 * <p>Operation 1: 1.1, 1.2, 2.1, 2.2, 2.7 Operation 2: 1.1, 1.2, 1.4, 2.2, 2.7 Operation 3: 1.1,
 * 1.2, 1.3, 2.2, 2.4 Operation 4: 2.5 Operation 5: 2.6
 *
 * <p>2010-04-08:
 *
 * <p>Staging server is a special type of an ACS Server. It stages devices to work with another ACS
 * Server or even another provisioning system! In this case the end result of a unit job will always
 * be UNCONFIRMED_FAILED, since the unit disappears. To counter this relatively non-intuitive
 * result, we will add a method which will set the status to COMPLETED_OK if it has been in an
 * UNCONFIRMED_FAILED state for more than one hour. This method is called markAsCompleted() and will
 * only be called from JCS-LPD running on a staging server.
 *
 * @author Morten
 */
public class UnitJobs {
  private static Logger logger = LoggerFactory.getLogger(UnitJobs.class);

  private DataSource connectionProperties;

  public UnitJobs(DataSource cp) {
    this.connectionProperties = cp;
  }

  /** 1.1 and 1.2 */
  public boolean start(UnitJob uj) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    try {
      c = connectionProperties.getConnection();
      pp =
          c.prepareStatement(
              "INSERT INTO unit_job (job_id, unit_id, status, start_timestamp, processed, confirmed, unconfirmed) VALUES (?, ?, ?, ?, 0, 0, 0)");
      pp.setInt(1, uj.getJobId());
      pp.setString(2, uj.getUnitId());
      pp.setString(3, UnitJobStatus.STARTED);
      pp.setTimestamp(4, new Timestamp(uj.getStartTimestamp().getTime()));
      pp.setQueryTimeout(60);
      pp.execute();
    } catch (SQLException sqlex) {
      if (pp != null) {
        pp.close();
      }
      pp =
          c.prepareStatement(
              "UPDATE unit_job SET status = ?, start_timestamp = ?, processed = 0 WHERE unit_id = ? and job_id = ?");
      pp.setString(1, UnitJobStatus.STARTED);
      pp.setTimestamp(2, new Timestamp(uj.getStartTimestamp().getTime()));
      pp.setString(3, uj.getUnitId());
      pp.setInt(4, uj.getJobId());
      pp.setQueryTimeout(60);
      int rowsUpdated = pp.executeUpdate();
      if (rowsUpdated == 0) {
        throw sqlex;
      }
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
    return true;
  }

  /** 1.3 and 1.4 */
  public boolean stop(UnitJob uj) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    try {
      c = connectionProperties.getConnection();
      if (uj.getStatus().equals(UnitJobStatus.COMPLETED_OK)
          || uj.getStatus().equals(UnitJobStatus.STOPPED)) {
        pp =
            c.prepareStatement(
                "UPDATE unit_job SET status = '"
                    + uj.getStatus()
                    + "', end_timestamp = ?, processed = 0 WHERE unit_id = ? AND job_id = ?");
      } else {
        pp =
            c.prepareStatement(
                "UPDATE unit_job SET end_timestamp = ?, status = '"
                    + uj.getStatus()
                    + "', confirmed = confirmed + 1, processed = 0 WHERE unit_id = ? AND job_id = ?");
      }
      pp.setTimestamp(1, new Timestamp(uj.getEndTimestamp().getTime()));
      pp.setString(2, uj.getUnitId());
      pp.setInt(3, uj.getJobId());
      pp.setQueryTimeout(60);
      return pp.executeUpdate() > 0;
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /** Added 2010-04-08 (see comment above). */
  public int markAsCompleted(Job job) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    boolean finished = false;
    int rowsUpdated = 0;
    while (!finished) { // will loop if MySQLTransactionRollbackException occurs
      try {
        c = connectionProperties.getConnection();
        String sql =
            "UPDATE unit_job SET end_timestamp = ?, status = '" + UnitJobStatus.COMPLETED_OK;
        sql += "', processed = 0 WHERE ";
        sql +=
            "status = '"
                + UnitJobStatus.UNCONFIRMED_FAILED
                + "' AND end_timestamp < ? AND job_id = ?";
        pp = c.prepareStatement(sql);
        pp.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        pp.setTimestamp(2, new Timestamp(System.currentTimeMillis() - 3600 * 1000));
        pp.setInt(3, job.getId());
        pp.setQueryTimeout(60);
        rowsUpdated = pp.executeUpdate();
        finished = true;
      } finally {
        if (pp != null) {
          pp.close();
        }
        if (c != null) {
          c.close();
        }
      }
    }
    return rowsUpdated;
  }

  /** 2.1 */
  public int markAsUnconfirmed(Job job) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    boolean finished = false;
    int rowsUpdated = 0;
    while (!finished) { // will loop if MySQLTransactionRollbackException occurs
      try {
        c = connectionProperties.getConnection();
        String sql =
            "UPDATE unit_job SET end_timestamp = ?, status = '" + UnitJobStatus.UNCONFIRMED_FAILED;
        sql += "', unconfirmed = unconfirmed + 1 WHERE ";
        sql += "status = '" + UnitJobStatus.STARTED + "' AND start_timestamp < ? AND job_id = ?";
        pp = c.prepareStatement(sql);
        pp.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        pp.setTimestamp(
            2, new Timestamp(System.currentTimeMillis() - job.getUnconfirmedTimeout() * 1000));
        pp.setInt(3, job.getId());
        pp.setQueryTimeout(60);
        rowsUpdated = pp.executeUpdate();
        finished = true;
      } finally {
        if (pp != null) {
          pp.close();
        }
        if (c != null) {
          c.close();
        }
      }
    }
    return rowsUpdated;
  }

  /** 2.2 */
  public List<UnitJob> readAllUnprocessed(Job job) throws SQLException {
    return read(false, job);
  }

  /** 2.3 */
  public void markAsProcessed(UnitJob uj) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    try {
      c = connectionProperties.getConnection();
      String sql = "UPDATE unit_job SET processed = 1 WHERE unit_id = ? AND job_id = ?";
      pp = c.prepareStatement(sql);
      pp.setString(1, uj.getUnitId());
      pp.setInt(2, uj.getJobId());
      pp.setQueryTimeout(60);
      pp.executeUpdate();
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /** 2.4 */
  public int countAndDeleteCompletedNoFailure(Job job) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    try {
      c = connectionProperties.getConnection();
      pp =
          c.prepareStatement(
              "DELETE FROM unit_job WHERE processed = 1 AND job_id = ? AND status = '"
                  + UnitJobStatus.COMPLETED_OK
                  + "' AND confirmed = 0 AND unconfirmed = 0");
      pp.setInt(1, job.getId());
      pp.setQueryTimeout(60);
      return pp.executeUpdate();
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /** 2.4 modified - due to introduction of STOPPED state for unit-jobs */
  public int countAndDeleteStoppedNoFailure(Job job) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    try {
      c = connectionProperties.getConnection();
      pp =
          c.prepareStatement(
              "DELETE FROM unit_job WHERE processed = 1 AND job_id = ? AND status = '"
                  + UnitJobStatus.STOPPED
                  + "' AND confirmed = 0 AND unconfirmed = 0");
      pp.setInt(1, job.getId());
      pp.setQueryTimeout(60);
      return pp.executeUpdate();
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /** 2.5 */
  public List<UnitJob> readAllProcessed(Job job) throws SQLException {
    return read(true, job);
  }

  /** 2.6 */
  public void delete(Job job) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    try {
      c = connectionProperties.getConnection();
      if (job == null) {
        pp = c.prepareStatement("DELETE FROM unit_job");
      } else {
        pp = c.prepareStatement("DELETE FROM unit_job WHERE job_id = ?");
        pp.setInt(1, job.getId());
      }
      pp.setQueryTimeout(60);
      pp.execute();
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /** 2.7 & 2.8 */
  public int count(Job job, String column, boolean isCompleted) throws SQLException {
    Connection c = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      c = connectionProperties.getConnection();
      String sql = "SELECT COUNT(status) FROM unit_job ";
      sql += "WHERE job_id = " + job.getId() + " AND ";
      if (isCompleted) {
        sql += "status = '" + UnitJobStatus.COMPLETED_OK + "' AND ";
      } else {
        sql += "status <> '" + UnitJobStatus.COMPLETED_OK + "' AND ";
      }
      sql += "processed = 1 AND ";
      sql += column + " > 0";
      ps = c.prepareStatement(sql);
      ps.setQueryTimeout(60);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
      return 0;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  private List<UnitJob> read(boolean processed, Job job) throws SQLException {
    Connection c = connectionProperties.getConnection();
    Statement s = null;
    ResultSet rs = null;
    List<UnitJob> unitJobs = new ArrayList<>();
    try {
      s = c.createStatement();
      String sql = "SELECT * FROM unit_job ";
      if (processed) {
        sql += "WHERE processed = 1 ";
        if (job != null) {
          sql += "AND job_id = " + job.getId() + " ";
        }
        sql += "ORDER BY unconfirmed DESC, confirmed DESC";
      } else {
        sql += "WHERE processed = 0 AND status <> '" + UnitJobStatus.STARTED + "' ";
        if (job != null) {
          sql += "AND job_id = " + job.getId() + " ";
        }
        sql += "ORDER BY start_timestamp ASC";
      }
      s.setQueryTimeout(60);
      rs = s.executeQuery(sql);
      while (rs.next()) {
        UnitJob uj = new UnitJob();
        uj.setJobId(rs.getInt("job_id"));
        uj.setUnitId(rs.getString("unit_id"));
        uj.setStartTimestamp(rs.getTimestamp("start_timestamp"));
        uj.setEndTimestamp(rs.getTimestamp("end_timestamp"));
        uj.setStatus(rs.getString("status"));
        uj.setConfirmedFailed(rs.getInt("confirmed"));
        uj.setUnconfirmedFailed(rs.getInt("unconfirmed"));
        int processedInt = rs.getInt("processed");
        if (processedInt == 1) {
          uj.setProcessed(true);
        }
        unitJobs.add(uj);
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
    return unitJobs;
  }

  /**
   * This method is purely for migration purposes, only used by ACS Shell to migrate data from one
   * database to another (perhaps after an upgrade of the database itself).
   */
  public void addOrChange(UnitJob uj) throws SQLException {
    Connection c = null;
    PreparedStatement pp = null;
    try {
      c = connectionProperties.getConnection();
      pp =
          c.prepareStatement(
              "INSERT INTO unit_job (job_id, unit_id, status, start_timestamp, end_timestamp, processed, unconfirmed, confirmed) VALUES (?, ?, ?, ?, ?, 0, ?, ?)");
      pp.setInt(1, uj.getJobId());
      pp.setString(2, uj.getUnitId());
      pp.setString(3, uj.getStatus());
      if (uj.getStartTimestamp() != null) {
        pp.setTimestamp(4, new Timestamp(uj.getStartTimestamp().getTime()));
      } else {
        pp.setTimestamp(4, null);
      }
      if (uj.getEndTimestamp() != null) {
        pp.setTimestamp(5, new Timestamp(uj.getEndTimestamp().getTime()));
      } else {
        pp.setTimestamp(5, null);
      }
      pp.setInt(6, uj.getUnconfirmedFailed());
      pp.setInt(7, uj.getConfirmedFailed());
      pp.setQueryTimeout(60);
      pp.execute();
      logger.info("Inserted new UnitJob for unit " + uj.getUnitId() + " on job " + uj.getJobId());
    } catch (SQLException sqlex) {
      if (pp != null) {
        pp.close();
      }
      pp =
          c.prepareStatement(
              "UPDATE unit_job SET status = ?, start_timestamp = ?, end_timestamp = ?, processed = 0, unconfirmed = ?, confirmed = ? WHERE unit_id = ? and job_id = ?");
      pp.setString(1, uj.getStatus());
      if (uj.getStartTimestamp() != null) {
        pp.setTimestamp(2, new Timestamp(uj.getStartTimestamp().getTime()));
      } else {
        pp.setTimestamp(2, null);
      }
      if (uj.getEndTimestamp() != null) {
        pp.setTimestamp(3, new Timestamp(uj.getEndTimestamp().getTime()));
      } else {
        pp.setTimestamp(3, null);
      }
      pp.setInt(4, uj.getUnconfirmedFailed());
      pp.setInt(5, uj.getConfirmedFailed());
      pp.setString(6, uj.getUnitId());
      pp.setInt(7, uj.getJobId());
      pp.setQueryTimeout(60);
      int rowsUpdated = pp.executeUpdate();
      if (rowsUpdated == 0) {
        throw sqlex;
      }
      logger.info("Updated UnitJob for unit " + uj.getUnitId() + " on job " + uj.getJobId());
    } finally {
      if (pp != null) {
        pp.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }
}
