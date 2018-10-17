package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.ACSVersionCheck;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Files {
  private static Logger logger = LoggerFactory.getLogger(Files.class);
  private Map<String, File> nameMap;
  private Map<String, File> versionTypeMap;
  private Map<Integer, File> idMap;
  private Unittype unittype;

  protected Files(
      Map<Integer, File> idMap,
      Map<String, File> nameMap,
      Map<String, File> versionTypeMap,
      Unittype unittype) {
    this.idMap = idMap;
    this.nameMap = nameMap;
    this.versionTypeMap = versionTypeMap;
    this.unittype = unittype;
  }

  public File getById(Integer id) {
    return idMap.get(id);
  }

  public File getByName(String name) {
    return nameMap.get(name);
  }

  public File getByVersionType(String version, FileType type) {
    return versionTypeMap.get(version + type);
  }

  @Override
  public String toString() {
    return "Contains " + nameMap.size() + " files";
  }

  public File[] getFiles() {
    File[] fileNames = new File[nameMap.size()];
    return nameMap.values().toArray(fileNames);
  }

  public File[] getFiles(FileType type) {
    List<File> filteredFiles = new ArrayList<>();
    for (File f : nameMap.values()) {
      if (type == null || f.getType() == type) {
        filteredFiles.add(f);
      }
    }
    return filteredFiles.toArray(new File[] {});
  }

  public void addOrChangeFile(File file, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    file.setConnectionProperties(acs.getDataSource()); // just in
    // case...
    file.validate();
    addOrChangeFileImpl(unittype, file, acs);
    nameMap.put(file.getName(), file);
    idMap.put(file.getId(), file);
    versionTypeMap.put(file.getVersion() + file.getType(), file);
    if (file.getOldName() != null) {
      nameMap.remove(file.getOldName());
      file.setOldName(null);
    }
  }

  private void deleteFileImpl(Unittype unittype, File file, ACS acs) throws SQLException {
    Statement s = null;
    String sql;
    Connection c = acs.getDataSource().getConnection();
    try {
      s = c.createStatement();
      sql = "DELETE FROM filestore WHERE ";
      sql += "id = '" + file.getId() + "'";
      s.setQueryTimeout(60);
      s.executeUpdate(sql);

      logger.info("Deleted file " + file.getName());
      if (acs.getDbi() != null) {
        acs.getDbi().publishDelete(file, unittype);
      }
    } finally {
      if (s != null) {
        s.close();
      }
      c.close();
    }
  }

  /**
   * The first time this method is run, the flag is set. The second time this method is run, the
   * parameter is removed from the nameMap. Setting the cascade argument = true will also delete all
   * unittype parameters and enumerations for all these parameters.
   *
   * @throws SQLException
   */
  public void deleteFile(File file, ACS acs) throws SQLException {
    if (!acs.getUser().isUnittypeAdmin(unittype.getId())) {
      throw new IllegalArgumentException("Not allowed action for this user");
    }
    deleteFileImpl(unittype, file, acs);
    nameMap.remove(file.getName());
    idMap.remove(file.getId());
    versionTypeMap.remove(file.getVersion() + file.getType());
  }

  private void addOrChangeFileImpl(Unittype unittype, File file, ACS acs) throws SQLException {
    Connection c = acs.getDataSource().getConnection();
    PreparedStatement s = null;
    String sql = null;
    // The file owner is set automatically to the logged-in user upon
    // add/change of file.
    // If logged-in user isAdmin, will skip this override, and will allow
    // any user specified in the file object.
    if (!unittype.getAcs().getUser().isAdmin() && unittype.getAcs().getUser().getId() != null) {
      file.setOwner(unittype.getAcs().getUser());
    }
    if (file.getId() == null) {
      try {
        DynamicStatement ds = new DynamicStatement();
        ds.addSql("INSERT INTO filestore (");
        ds.addSqlAndArguments("name,", file.getName());
        ds.addSqlAndArguments("type,", file.getType().toString());
        ds.addSqlAndArguments("unit_type_id,", unittype.getId());
        ds.addSqlAndArguments("description,", file.getDescription());
        ds.addSqlAndArguments("version,", file.getVersion());
        if (file.getContentProtected() != null) {
          ds.addSqlAndArguments("content,", new ByteArrayInputStream(file.getContentProtected()));
        }
        if (ACSVersionCheck.fileReworkSupported) {
          ds.addSqlAndStringArgs("target_name,", file.getTargetName());
          if (file.getOwner() != null && file.getOwner().getId() != null) {
            ds.addSqlAndArguments("owner,", file.getOwner().getId());
          }
        }
        ds.addSqlAndArguments("timestamp_", new Timestamp(file.getTimestamp().getTime()));
        ds.addSql(") values (" + ds.getQuestionMarks() + ")");
        s = ds.makePreparedStatement(c, "id");
        s.setQueryTimeout(60);
        s.executeUpdate();
        ResultSet gk = s.getGeneratedKeys();
        if (gk.next()) {
          file.setId(gk.getInt(1));
        }

        // }

        logger.info("Added file " + file.getName());
        if (acs.getDbi() != null) {
          acs.getDbi().publishAdd(file, unittype);
        }
      } finally {
        if (s != null) {
          s.close();
        }
        c.close();
      }
    } else {
      try {
        sql =
            "UPDATE filestore SET description=?,name=?,timestamp_=?,type=?,version=?,unit_type_id=?";
        if (ACSVersionCheck.fileReworkSupported) {
          sql += ",target_name=?,owner=?";
        }
        sql += " WHERE id=?";
        s = c.prepareStatement(sql);
        if (file.getDescription() != null) {
          s.setString(1, file.getDescription());
        } else {
          s.setNull(1, Types.VARCHAR);
        }
        s.setString(2, file.getName());
        s.setTimestamp(3, new Timestamp(file.getTimestamp().getTime()));
        s.setString(4, file.getType().toString());
        // s.setString(5, file.getSubtype());
        s.setString(5, file.getVersion());
        s.setInt(6, unittype.getId());
        if (ACSVersionCheck.fileReworkSupported) {
          if (file.getTargetName() != null) {
            s.setString(7, file.getTargetName());
          } else {
            s.setNull(7, Types.VARCHAR);
          }
          if (file.getOwner() != null && file.getOwner().getId() != null) {
            s.setInt(8, file.getOwner().getId());
          } else {
            s.setNull(8, Types.INTEGER);
          }
          s.setInt(9, file.getId());
        } else {
          s.setInt(7, file.getId());
        }
        s.setQueryTimeout(60);
        int rowsAffected = s.executeUpdate();
        if (rowsAffected == 0) {
          throw new SQLException("The file [" + file.getId() + "] does not exist!");
        }
        if (file.getContentProtected() != null) {
          DynamicStatement ds = new DynamicStatement();
          ds.addSqlAndArguments(
              "UPDATE filestore set content=? where id=?",
              new ByteArrayInputStream(file.getContentProtected()),
              file.getId());
          s = ds.makePreparedStatement(c);
          s.setQueryTimeout(60);
          s.executeUpdate();
        }
        logger.info("Updated file to " + file.getName());
        if (acs.getDbi() != null) {
          acs.getDbi().publishFile(file, unittype);
        }
      } finally {
        if (s != null) {
          s.close();
        }
        c.close();
      }
    }
  }

  /** Only used to refresh the cache, used from DBI. */
  protected static void refreshFile(Integer fileId, Integer unittypeId, ACS acs)
      throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    Connection c = acs.getDataSource().getConnection();
    try {
      Unittype unittype = acs.getUnittype(unittypeId);
      if (unittype == null) {
        return;
      } // unittype not accessible by this user
      Files files = unittype.getFiles();
      File file = files.getById(fileId);
      DynamicStatement ds = new DynamicStatement();
      ds.addSql(
          "SELECT id, name, type, unit_type_id, description, version, length(content) as length, timestamp_ ");
      ds.addSqlAndArguments("FROM filestore WHERE id = ?", fileId);
      ps = ds.makePreparedStatement(c);
      rs = ps.executeQuery();
      if (rs.next()) {
        file.setConnectionProperties(acs.getDataSource());
        file.setName(rs.getString("name"));
        file.setType(FileType.valueOf(rs.getString("type")));
        // file.setSubtype(rs.getString("subtype"));
        file.setDescription(rs.getString("description"));
        file.setVersion(rs.getString("version"));
        file.setLength(rs.getInt("length"));
        file.setTimestamp(rs.getTimestamp("timestamp_"));
        file.resetContentToNull();
        files.getNameMap().put(file.getName(), file);
        files.getIdMap().put(file.getId(), file);
        files.getVersionTypeMap().put(file.getVersion() + file.getType(), file);
      }
      logger.debug("Refreshed file " + file.getName());
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      c.close();
    }
  }

  protected Map<String, File> getNameMap() {
    return nameMap;
  }

  protected Map<String, File> getVersionTypeMap() {
    return versionTypeMap;
  }

  protected Map<Integer, File> getIdMap() {
    return idMap;
  }
}
