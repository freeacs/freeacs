package com.github.freeacs.dbi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class to easily build SQL for insert/update, since it's the same columns
 * involved. This class is a wrapper class around DynamicStatement, which is more generic in it's
 * approach to SQL generation.
 *
 * @author Morten
 */
public class InsertOrUpdateStatement {
  public static class Field {
    /** The database name of the column. */
    private String column;
    /**
     * The value to insert/update in the column. Could be NullString, NullInteger, any "normal" type
     * (String, Boolean, etc.) and null (in which case it will be skipped)
     */
    private Object value;
    /**
     * Set to true if this column is the primary key. If the value is infact null, the
     * prepared-statement will set it to auto-generated (retrieve it in a new query after the
     * insert).
     */
    private boolean primaryKey;

    public Field(String column, Object o) {
      this(column, o, false);
    }

    public Field(String column, Object o, boolean primaryKey) {
      this.column = column;
      this.value = o;
      this.primaryKey = primaryKey;
    }

    public Field(String column, Integer i) {
      this(column, i, false);
    }

    public Field(String column, Integer i, boolean primaryKey) {
      if (i != null) {
        this.value = i;
      } else {
        this.value = new DynamicStatement.NullInteger();
      }
      this.column = column;
      this.primaryKey = primaryKey;
    }

    public Field(String column, String s) {
      this(column, s, false);
    }

    public Field(String column, String s, boolean primaryKey) {
      if (s != null) {
        this.value = s;
      } else {
        this.value = new DynamicStatement.NullString();
      }
      this.column = column;
      this.primaryKey = primaryKey;
    }

    public String getColumn() {
      return column;
    }

    public void setColumn(String column) {
      this.column = column;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    public boolean isPrimaryKey() {
      return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
      this.primaryKey = primaryKey;
    }

    public boolean equals(Object o) {
      if (o instanceof Field) {
        Field f = (Field) o;
        return f.getColumn().equals(getColumn());
      }
      return false;
    }

    public String toString() {
      return column;
    }
  }

  private List<Field> fields = new ArrayList<>();
  private String table;
  private boolean insert;

  public InsertOrUpdateStatement(String table, Field primaryKey) {
    this.table = table;
    primaryKey.setPrimaryKey(true);
    fields.add(primaryKey);
  }

  public void addField(Field field) {
    fields.add(field);
  }

  /**
   * This method will either make an insert or and update. If primary keys are null, it will make an
   * insert (the primary keys will be auto-generated), otherwise it will make an update.
   *
   * @param c
   * @return
   * @throws SQLException
   */
  public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
    List<String> autoGeneratePK = new ArrayList<>();
    List<Field> updateKeys = new ArrayList<>();
    for (Field field : fields) {
      if (field.isPrimaryKey()) {
        if (field.getValue() == null
            || field.getValue() instanceof DynamicStatement.NullString
            || field.getValue() instanceof DynamicStatement.NullInteger) {
          insert = true;
          autoGeneratePK.add(field.getColumn());
        } else {
          updateKeys.add(field);
        }
      }
    }
    DynamicStatement ds = new DynamicStatement();
    if (insert) {
      ds.setSql("INSERT INTO " + table + " (");
      for (Field field : fields) {
        if (!autoGeneratePK.contains(field.getColumn())) {
          ds.addSqlAndArguments(field.getColumn() + ", ", field.getValue());
        }
      }
      ds.cleanupSQLTail();
      ds.addSql(") VALUES (" + ds.getQuestionMarks() + ")");
      String[] primaryKeyStrArr = autoGeneratePK.toArray(new String[] {});
      return ds.makePreparedStatement(c, primaryKeyStrArr);
    } else { // update
      ds.setSql("UPDATE " + table + " SET ");
      for (Field field : fields) {
        if (!updateKeys.equals(field)) {
          ds.addSqlAndArguments(field.getColumn() + " = ?, ", field.getValue());
        }
      }
      ds.cleanupSQLTail();
      ds.addSql(" WHERE ");
      for (Field field : updateKeys) {
        ds.addSqlAndArguments(field.getColumn() + " = ?, ", field.getValue());
      }
      ds.cleanupSQLTail();
      return ds.makePreparedStatement(c);
    }
  }

  public boolean isInsert() {
    return insert;
  }
}
