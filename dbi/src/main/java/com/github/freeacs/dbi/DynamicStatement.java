package com.github.freeacs.dbi;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class DynamicStatement {
  private static SimpleDateFormat tmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static String[] leftovers = new String[] {" AND", " WHERE", " OR", "(", ","};

  private Long startTms;

  public static class NullInteger {}

  public static class NullString {}

  private StringBuilder sql = new StringBuilder();

  private List<Object> arguments = new ArrayList<>();

  public void addSql(String sql) {
    this.sql.append(sql);
  }

  public String getSql() {
    return sql.toString();
  }

  public void trim(int numberOfChar) {
    sql = new StringBuilder(sql.substring(0, sql.length() - numberOfChar));
  }

  public void setSql(String sql) {
    this.sql = new StringBuilder(sql);
  }

  public void addArguments(Object... oos) {
    Collections.addAll(arguments, oos);
  }

  /**
   * This methods accepts all kinds of objects. However, if you insert null objects, the null object
   * interpreted as a String: preparedStatement.setString(null); This may not be appropriate,
   * depending upon the database and database driver. To gain more control over the handling of null
   * objects, use the addSqlAnd*-method which corresponds to your type.
   */
  public void addSqlAndArguments(String sql, Object... oos) {
    this.sql.append(sql);
    Collections.addAll(arguments, oos);
  }

  public void addSqlAndStringArgs(String sql, String... strs) {
    this.sql.append(sql);
    for (String s : strs) {
      if (s != null) {
        arguments.add(s);
      } else {
        arguments.add(new NullString());
      }
    }
  }

  public void addSqlAndIntegerArgs(String sql, Integer... ints) {
    this.sql.append(sql);
    for (Integer i : ints) {
      if (i != null) {
        arguments.add(i);
      } else {
        arguments.add(new NullInteger());
      }
    }
  }

  public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
    return makePreparedStatement(c, (String[]) null);
  }

  public PreparedStatement makePreparedStatement(Connection c, String column) throws SQLException {
    if (column != null) {
      return makePreparedStatement(c, new String[] {column});
    } else {
      return makePreparedStatement(c, (String[]) null);
    }
  }

  public PreparedStatement makePreparedStatement(Connection c, String[] columns)
      throws SQLException {
    PreparedStatement pp;
    if (columns == null || columns.length == 0) {
      pp = c.prepareStatement(sql.toString());
    } else {
      pp = c.prepareStatement(sql.toString(), columns);
    }
    for (int i = 0; i < arguments.size(); i++) {
      Object o = arguments.get(i);
      if (o instanceof String) {
        pp.setString(i + 1, (String) o);
      } else if (o instanceof Integer) {
        pp.setInt(i + 1, (Integer) o);
      } else if (o instanceof Long) {
        pp.setInt(i + 1, ((Long) o).intValue());
      } else if (o instanceof Timestamp) {
        pp.setTimestamp(i + 1, (Timestamp) o);
      } else if (o instanceof java.util.Date) {
        pp.setTimestamp(i + 1, new Timestamp(((java.util.Date) o).getTime()));
      } else if (o instanceof Date) {
        pp.setDate(i + 1, (Date) o);
      } else if (o instanceof Boolean) {
        pp.setInt(i + 1, (Boolean) o ? 1 : 0);
      } else if (o instanceof Calendar) {
        java.util.Date d = ((Calendar) o).getTime();
        pp.setTimestamp(i + 1, new Timestamp(d.getTime()));
      } else if (o instanceof ByteArrayInputStream) {
        pp.setBinaryStream(i + 1, (ByteArrayInputStream) o, ((ByteArrayInputStream) o).available());
      } else if (o instanceof NullInteger) {
        pp.setNull(i + 1, Types.INTEGER);
      } else if (o instanceof NullString) {
        pp.setNull(i + 1, Types.VARCHAR);
      } else {
        pp.setString(i + 1, (String) o);
      }
    }
    startTms = System.nanoTime();
    return pp;
  }

  public String getQuestionMarks() {
    final StringBuilder qm = new StringBuilder();
    arguments.forEach(ignore -> qm.append("?,"));
    if (qm.toString().endsWith(",")) {
      return qm.substring(0, qm.length() - 1);
    }
    return qm.toString();
  }

  /**
   * Is useful to cleanup the SQL if there are leftovers of SQL which doesn't make sense, but is put
   * there because of a loop. Typical example is "AND" at the end.
   */
  public void cleanupSQLTail() {
    String tmp = getSql().trim();
    int counter = 0;
    while (counter < leftovers.length) {
      for (String leftoverStr : leftovers) {
        if (tmp.toUpperCase().endsWith(leftoverStr)) {
          tmp = tmp.substring(0, tmp.length() - leftoverStr.length());
          tmp = tmp.trim();
          counter = 0;
        } else {
          counter++;
        }
      }
    }
    if (!tmp.endsWith(" ")) {
      tmp += " ";
    }
    setSql(tmp);
  }

  public List<Object> getArguments() {
    return arguments;
  }

  /** For debug purpose only. */
  private String getArgument(int i) {
    Object o = arguments.get(i);
    if (o instanceof Timestamp) {
      return "'" + tmsFormat.format((Timestamp) o) + "'";
    }
    if (o instanceof Date) {
      return "'" + tmsFormat.format((Date) o) + "'";
    }
    if (o instanceof java.util.Date) {
      return "'" + tmsFormat.format((java.util.Date) o) + "'";
    }
    if (o instanceof Calendar) {
      return "'" + tmsFormat.format(((Calendar) o).getTime()) + "'";
    }
    if (o instanceof Integer) {
      return String.valueOf(o);
    }
    if (o instanceof Long) {
      return String.valueOf(o);
    } else if (o instanceof Pattern) {
      return "'" + ((Pattern) o).pattern() + "'";
    } else if (o instanceof String) {
      String arg = (String) o;
      if ("SYSDATE".equalsIgnoreCase(arg) || "SYSTIMESTAMP".equalsIgnoreCase(arg)) {
        return arg;
      }
      if ("NOW()".equalsIgnoreCase(arg) || "log_id_seq.nextval".equalsIgnoreCase(arg)) {
        return arg;
      } else {
        return "'" + arg + "'";
      }
    }
    return "'" + o + "'";
  }

  /** Will print the SQL with question marks substituted. */
  public String getSqlQuestionMarksSubstituted() {
    String sql = getSql();
    for (int i = 0; i < arguments.size(); i++) {
      sql = sql.replaceFirst("\\?", getArgument(i));
    }
    return sql;
  }

  public String getDebugMessage() {
    if (startTms == null) {
      return "The debug message from DynamicStatement should be printed after the execution of the sql";
    } else {
      double milli = (System.nanoTime() - startTms) / 1000000d;
      return "[" + String.format("%10.2f", milli) + " ms] " + getSqlQuestionMarksSubstituted();
    }
  }

  public String toString() {
    return getSql();
  }
}
