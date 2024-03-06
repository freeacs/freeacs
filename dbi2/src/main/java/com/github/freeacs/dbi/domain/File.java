package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.With;

import javax.sql.DataSource;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Objects;

@Data
@With
@AllArgsConstructor
public class File {
  private UnitType unitType;
  private Integer id;
  private String name;
  private String oldName;
  private FileType type;
  private String description;
  private String version;
  private Date timestamp;
  private int length;
  private String targetName;
  private User owner;
  private byte[] content;

  private boolean validateInput = true;

  private DataSource dataSource;

  /** Code-order: id, unittype, name, type, desc, version, timestamp, targetname, (content) */
  public File() {}

  public File(
      UnitType unittype,
      String name,
      FileType type,
      String description,
      String version,
      Date timestamp,
      String targetName,
      User owner) {
    setUnittype(unittype);
    setName(name);
    setType(type);
    setDescription(description);
    setVersion(version);
    setTimestamp(timestamp);
    setTargetName(targetName);
    setOwner(owner);
  }

  protected byte[] getContentProtected() {
    return content;
  }

  @SneakyThrows
  public byte[] getContent() {
    if (content == null) {
      Connection c = null;
      Statement s = null;
      ResultSet rs = null;
      try {
        c = dataSource.getConnection();
        s = c.createStatement();
        s.setQueryTimeout(60);
        rs = s.executeQuery("SELECT content FROM filestore WHERE id = '" + id + "'");
        if (rs.next()) {
          Blob blob = rs.getBlob("content");
          content = blob.getBytes(1, (int) blob.length());
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
      if (content == null) {
        content = new byte[0];
      }
    }
    return content;
  }

  public void setUnittype(UnitType unittype) {
    if (unittype == null) {
      throw new IllegalArgumentException("Unittype cannot be null");
    }
    this.unitType = unittype;
  }

  public void setName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("File name cannot be null");
    }
    if (!name.equals(this.name)) {
      this.oldName = this.name;
    }
    this.name = name;
  }

  public void setType(FileType type) {
    if (validateInput && type == null) {
      throw new IllegalArgumentException("File type cannot be null");
    }
    this.type = type;
  }

  public void setTimestamp(Date created) {
    this.timestamp = Objects.requireNonNullElseGet(created, Date::new);
  }

  public void setVersion(String version) {
    if (validateInput && version == null) {
      throw new IllegalArgumentException("File version cannot be null");
    }
    this.version = version;
  }

  public void setTargetName(String targetName) {
    if (validateInput && type == FileType.TR069_SCRIPT && targetName == null) {
      throw new IllegalArgumentException(
          "File target name cannot be null if File type is " + FileType.TR069_SCRIPT);
    }
    if (type == FileType.TR069_SCRIPT && targetName == null) {
      targetName = name;
    }
    this.targetName = targetName;
  }

  public void setBytes(byte[] bytes) {
    this.content = bytes;
    this.length = bytes.length;
  }

  public void setLength(int length) {
    if (length < 0) {
      length = 0;
    }
    this.length = length;
  }

  /* MISC methods */

  /** Necessary to retrieve content - we do not cache content as default action. */
  protected void setConnectionProperties(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /** Used by Web. */
  public String getNameAndVersion() {
    return name + " (ver: " + version + ")";
  }

  public void validate() {
    setUnittype(unitType);
    setName(name);
    setType(type);
    setDescription(description);
    setVersion(version);
    setTimestamp(timestamp);
    setTargetName(targetName);
  }

  /** To avoid storing file content in ACS-object - this must always be used with care! */
  public void resetContentToNull() {
    content = null;
  }
}
