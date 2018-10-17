package com.github.freeacs.dbi.util;

import com.github.freeacs.dbi.DynamicStatement;

public class SQLUtil {
  /**
   * As string-input coming from Web/XML-based input cannot contain the character '%' (due to
   * conflicts over URL-encoding, etc), we offer a method to convert from a semi-regexp format to a
   * SQL-format, to make a more powerful string compare in the SQL statements. The search is greedy
   * (or Google-like), since if omitting any special characters will lead to partial matches (~ LIKE
   * '%input%'), not an exact match (~ = 'input')
   *
   * <p>These are the characters allowed in the input (apart from normal chars):
   *
   * <p>* (asterix) : 0 or more character of any kind _ (underscore) : 1 character of any kind ^
   * (circumflex) : Start of string $ (dollar) : End of string ! (exclamation) : Negation, only to
   * be used at the beginning of filter. | (pipe) : Will split the search in several parts (a OR b
   * OR c) or !(a AND b AND c)
   *
   * <p>Conversion rules
   *
   * <p>0. If ! found at start, remove it (but remember to flip search) 1. If ^ not found at start,
   * prepend % 2. If $ not found at end, append % 3. If ^ found at start, remove 4. If $ found at
   * end, remove it 5. If * found, replace with %
   *
   * <p>Examples: foo -> %foo% (LIKE) ^foo -> foo% (LIKE) ^foo$ -> foo (EQUAL) foo$ -> %foo (LIKE)
   * f*o -> f%o (LIKE)
   *
   * <p>!foo -> %foo% (UNLIKE) !^foo -> foo% (UNLIKE) !^foo$-> foo (UNEQUAL) !foo$ -> %foo (UNLIKE)
   * !f*o -> f%o (UNLIKE)
   *
   * <p>f_o -> %f_o% (LIKE) ^f_o$ -> f_o (LIKE - due to _)
   *
   * @param regex
   * @return
   */
  public static DynamicStatement input2SQLCriteria(
      DynamicStatement ds, String criteriaName, String criteria) {
    if (criteria == null) {
      return ds;
    }
    boolean equality = true;
    if (criteria.startsWith("!")) {
      criteria = criteria.substring(1);
      equality = false;
    }
    String[] contentArr = criteria.split("\\|");
    if (contentArr.length > 1 && equality) {
      ds.addSql("(");
    }
    for (String c : contentArr) {
      c = c.replace('*', '%');
      String searchStr = "%" + c + "%";
      boolean exact =
          c.startsWith("^") && c.endsWith("$") && c.indexOf('%') == -1 && c.indexOf('_') == -1;
      if (c.startsWith("^")) {
        searchStr = searchStr.substring(2);
      } // remove %^
      if (c.endsWith("$")) {
        searchStr = searchStr.substring(0, searchStr.length() - 2);
      } // remove $%
      if (exact) {
        if (equality) {
          ds.addSqlAndArguments(criteriaName + " = ? OR ", searchStr);
        } else {
          ds.addSqlAndArguments(criteriaName + " <> ? AND ", searchStr);
        }
      } else if (equality) {
        ds.addSqlAndArguments(criteriaName + " LIKE ? OR ", searchStr);
      } else {
        ds.addSqlAndArguments(criteriaName + " NOT LIKE ? AND ", searchStr);
      }
    }
    ds.cleanupSQLTail();
    if (contentArr.length > 1 && equality) {
      ds.addSql(")");
    }
    return ds;
  }
}
