package com.github.freeacs.dbi;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jun 2011
 *
 * <p>UnitQueryWithinUnittype is an attempt to resolve a query which we have not been able to "get
 * through" UnitQueryCrossUnittype. The following things are new abilities for ACS: 1. Possible to
 * return unit-object + certain unit parameters for all units, previously only the unit object was
 * returned, and it required another query to retrieve the set of unit parameters. Granted, this
 * query only returns the parameters searched for, but this can be very useful. The use-case which
 * triggered this development was the TelnetController which wants to run repeating jobs. In order
 * to do so it must be able to retrieve the Job.History-parameter for all devices in the job, and
 * then check the time stamp. Since this operation was to be repeated every hour, it would be a bad
 * idea to run one SQL for each unit to retrieve Job.History parameter. 2. Possible to search with
 * operands like <, <=, >= and >. This has never been deemed interesting before, since all unit
 * parameters were treated like VARCHAR. This class offers a way to specify the type of the column,
 * and convert to the type upon query execution. 3. Possible to search for NULL (non-existence). The
 * UnitQueryCrossUnittype class can only handle searches for existing values, primarily because
 * handling NULL also meant being able to handle 4. 4. Possible to take non-existent unit parameters
 * into account. Searching for a value should (in case of a profile-match) return all non-existing
 * unit parameters. If a profile does not match and the query search for non-matching values, again
 * the query should return all non-existing unit-parameters.
 *
 * <p>So is UnitQueryWithinUnittype (UQWU) always better than UnitQueryCrossUnittype (UQCU)? The
 * answer is: no. Or maybe: not yet: a. UQWU cannot answer queries like "give me all units with a
 * parameter value like X, since UQWU needs to know *which* parameter you're asking for. UQCU
 * handles this. This kind of query will also invariably crash (or at least be extremely slow and
 * complicated) with the idea of unit parameter inheriting the profile parameter. b. Because of a.
 * UQWU cannot search across unittypes.
 */
public class UnitQueryWithinUnittype {
  private static Logger logger = LoggerFactory.getLogger(UnitQueryWithinUnittype.class);
  public static Cache patternCache = new Cache();
  private Connection connection;
  private Unittype unittype;
  private List<Profile> profiles;

  private ACS acs;

  public UnitQueryWithinUnittype(Connection c, ACS acs, Unittype unittype, List<Profile> profiles) {
    this.connection = c;
    this.acs = acs;
    if (unittype != null) {
      this.unittype = unittype;
    } else if (profiles != null && !profiles.isEmpty() && profiles.get(0).getUnittype() != null) {
      this.unittype = profiles.get(0).getUnittype();
    } else {
      throw new IllegalArgumentException(
          "UnitQueryWithinUnittype requires a unittype - not found explicitly nor implicitly");
    }

    if (profiles == null || profiles.isEmpty()) {
      this.profiles = Arrays.asList(unittype.getProfiles().getProfiles());
    } else {
      for (Profile p : profiles) {
        if (this.unittype.getProfiles().getById(p.getId()) == null) {
          throw new IllegalArgumentException(
              "UnitQueryWithinUnittype requires that profiles must be within the unittype specified");
        }
      }
      this.profiles = profiles;
    }
  }

  public UnitQueryWithinUnittype(Connection c, ACS acs, Unittype unittype, Profile profile) {
    this.connection = c;
    this.acs = acs;
    if (unittype != null) {
      this.unittype = unittype;
    } else if (profiles != null && !profiles.isEmpty() && profiles.get(0).getUnittype() != null) {
      this.unittype = profiles.get(0).getUnittype();
    } else {
      throw new IllegalArgumentException(
          "UnitQueryWithinUnittype requires a unittype - not found explicitly nor implicitly");
    }

    if (profile == null) {
      this.profiles = Arrays.asList(unittype.getProfiles().getProfiles());
    } else {
      if (this.unittype.getProfiles().getById(profile.getId()) == null) {
        throw new IllegalArgumentException(
            "UnitQueryWithinUnittype requires that profiles must be within the unittype specified");
      }
      this.profiles = new ArrayList<>();
      this.profiles.add(profile);
    }
  }

  /**
   * Matches two strings. FixedOp may be a string without wildchar (% or _). Typically this string
   * is a profile-parameter (used in profile-matching) or a unit/profile-parameter (used in
   * group-matching). VarOp is the search-string, and may contain % or _. Typically this is the
   * searched for string in group-search or unit-query-search. Usage of % or _ is only possible for
   * TEXT matching, not for NUMBER matching
   */
  public static boolean match(
      String fixedOp, String varOp, Parameter.Operator op, Parameter.ParameterDataType type) {
    // If fixedOp == null, varOp can match only if null and operator is EQ, or the opposite.
    if (fixedOp == null) {
      return (Parameter.Operator.EQ.equals(op) && varOp == null)
          || ((!Parameter.Operator.EQ.equals(op) || varOp == null)
              && (!Parameter.Operator.NE.equals(op) || varOp != null)
              && Parameter.Operator.NE.equals(op)
              && varOp != null);
    }
    // If varOp == null, fixedOp can match only if operator is NE (since fixedOp cannot be null).
    if (varOp == null) {
      return Parameter.Operator.NE.equals(op);
    }
    switch (type) {
      case NUMBER:
        // Neither varOp nor fixedOp can be NULL here.
        try {
          Long operandL = Long.valueOf(varOp);
          Long ppL = Long.valueOf(fixedOp);
          return (Parameter.Operator.EQ.equals(op) && ppL.longValue() == operandL.longValue())
              || (Parameter.Operator.NE.equals(op) && ppL.longValue() != operandL.longValue())
              || (Parameter.Operator.LT.equals(op) && ppL < operandL)
              || (Parameter.Operator.LE.equals(op) && ppL <= operandL)
              || (Parameter.Operator.GE.equals(op) && ppL >= operandL)
              || (Parameter.Operator.GT.equals(op) && ppL > operandL);
        } catch (NumberFormatException ignored) {
        }
        return false;
      case TEXT:
        if (Parameter.Operator.EQ.equals(op)) {
          if (varOp.contains("_") || varOp.contains("%")) {
            return matchWildcardString(fixedOp, varOp);
          }
          return fixedOp.equals(varOp);
        }
        if (Parameter.Operator.NE.equals(op)) {
          if (varOp.contains("_") || varOp.contains("%")) {
            return !matchWildcardString(fixedOp, varOp);
          }
          return !fixedOp.equalsIgnoreCase(varOp);
        }
        int compareInt = fixedOp.compareToIgnoreCase(varOp);
        return (Parameter.Operator.LT.equals(op) && compareInt < 0)
            || (Parameter.Operator.LE.equals(op) && compareInt <= 0)
            || (Parameter.Operator.GE.equals(op) && compareInt >= 0)
            || (Parameter.Operator.GT.equals(op) && compareInt > 0);
      default:
        return false;
    }
  }

  private static boolean matchWildcardString(String fixedOp, String varOp) {
    String patternStr = varOp;
    CacheValue cv = patternCache.get(patternStr);
    if (cv == null) {
      patternStr = patternStr.replaceAll("_", ".?");
      patternStr = patternStr.replaceAll("%", ".*");
      Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
      cv = new CacheValue(pattern, Cache.SESSION, 60 * 60 * 1000);
      patternCache.put(patternStr, cv);
    }
    Pattern pattern = (Pattern) cv.getObject();
    Matcher matcher = pattern.matcher(fixedOp);
    boolean retVal = matcher.matches();
    matcher.reset();
    return retVal;
  }

  /**
   * The setting of the operator and the value argument is dependent upon the search phrase
   * (contains wildcards?), the matching against the corresponding profile value and the chosen
   * operator (eq, ne, gt, lt, ge, le). The responsibility of this method is simply to return a
   * string like "up.value = 'value'"
   */
  private void addUnitParameterClause(
      String table, Profile p, DynamicStatement ds, Parameter parameter) {
    UnittypeParameter utp = parameter.getUnittypeParameter();
    String operand = parameter.getValue();
    if (parameter.valueWasNull()) {
      operand = null;
    } else {
      operand = operand.replace('*', '%');
      if (operand.startsWith("^")) {
        operand = operand.substring(1);
      }
      if (operand.endsWith("$")) {
        operand = operand.substring(0, operand.length() - 1);
      }
    }
    Parameter.Operator op = parameter.getOp();
    Parameter.ParameterDataType type = parameter.getType();
    String profileParameterValue = null;
    ProfileParameter pp = p.getProfileParameters().getById(utp.getId());
    if (pp != null) {
      profileParameterValue = pp.getValue();
    }
    boolean profileMatch =
        match(profileParameterValue, operand, parameter.getOp(), parameter.getType());
    /*
     * Use case for profile-match = true
     * 1.  operand = 3.15, operator = EQ   -> up.value = '3.15' || up.value IS NULL
     * 2.  operand = 3.15, operator = NE   -> up.value <> '3.15' || up.value IS NULL
     * 3.  operand = %3.15%, operator = EQ -> up.value LIKE '%3.15%' || up.value IS NULL
     * 4.  operand = %3.15%, operator = NE -> up.value NOT LIKE '%3.15%' || up.value IS NULL
     * 5.  operand = 3.15, operator = LT   -> up.value < '3.15' || up.value IS NULL
     * 6.  operand = 3.15, operator = LE   -> up.value <= '3.15' || up.value IS NULL
     * 7.  operand = 3.15, operator = GT   -> up.value > '3.15' || up.value IS NULL
     * 8.  operand = 3.15, operator = GE   -> up.value >= '3.15' || up.value IS NULL
     * 9.  operand = NULL, operator = EQ   -> up.value IS NULL
     * 10. operand = NULL, operator = NE   -> all units match
     * Use case 10 stands out, it needs no search-criteria
     *
     * Use cases for profile-match = false
     * 1.  operand = 3.15, operator = EQ   -> up.value = '3.15'
     * 2.  operand = 3.15, operator = NE   -> up.value <> '3.15' || up.value IS NULL
     * 3.  operand = %3.15%, operator = EQ -> up.value LIKE '%3.15%'
     * 4.  operand = %3.15%, operator = NE -> up.value NOT LIKE '%3.15%' || up.value IS NULL
     * 5.  operand = 3.15, operator = LT   -> up.value < '3.15'
     * 6.  operand = 3.15, operator = LE   -> up.value <= '3.15'
     * 7.  operand = 3.15, operator = GT   -> up.value > '3.15'
     * 8.  operand = 3.15, operator = GE   -> up.value >= '3.15'
     * 9.  operand = NULL, operator = EQ   -> up.value IS NULL
     * 10. operand = NULL, operator = NE   -> up.value IS NOT NULL
     * Use case 2 and 4 stands out, they need an extra expression
     */
    if (profileMatch) {
      if (operand == null) {
        if (Parameter.Operator.EQ.equals(op)) {
          ds.addSqlAndArguments(table + ".value IS NULL");
        }
      } else {
        ds.addSqlAndArguments(
            "("
                + table
                + ".value "
                + op.getSQL(operand)
                + " "
                + type.getSQL()
                + " OR "
                + table
                + ".value IS NULL)",
            operand);
      }
    } else if (operand == null) {
      if (Parameter.Operator.NE.equals(op)) {
        ds.addSqlAndArguments(table + ".value IS NOT NULL");
      }
      if (Parameter.Operator.EQ.equals(op)) {
        ds.addSqlAndArguments(table + ".value IS NULL");
      }
    } else {
      ds.addSqlAndArguments(table + ".value " + op.getSQL(operand) + " " + type.getSQL(), operand);
    }
  }

  private DynamicStatement computeSQL(List<Parameter> parameters, Profile p, boolean countSQL) {
    // does not actually use this statement to make SQL, but uses
    // it to determine which computeSQL to be used
    DynamicStatement ds = new DynamicStatement();
    for (Parameter parameter : parameters) {
      addUnitParameterClause("dummy", p, ds, parameter);
    }
    if (ds.getSql().contains("IS NULL")) {
      return computeSQLForNullParamValues(parameters, p, countSQL);
    } else {
      return computeSQLForNonNullParamValues(parameters, p, countSQL);
    }
  }

  /**
   * Feb 2013: Another attempt to improve this terrible/challenging SQL...the response from MySQL
   * was not acceptable with the previous solution after all. This attempt may go better with MySQL
   * - at least initial test indicate a reduction in processing to 1/100. However, this will only
   * work when searching for non-null parameter values!!
   *
   * @param parameters
   * @param p
   * @param countSQL
   * @return
   */
  private DynamicStatement computeSQLForNonNullParamValues(
      List<Parameter> parameters, Profile p, boolean countSQL) {
    DynamicStatement ds = new DynamicStatement();

    /*
     * Example of a search using 0 unit-param values:
     * SELECT u.unit_id, u.profile_id, u.unit_type_id FROM u.unit
     * WHERE u.profile_id = 12
     *
     * Example of a search using 1 unit-param value:
     * SELECT u.unit_id, u.profile_id, u.unit_type_id, up1.value FROM unit u, unit_param up1 -- special for 1-param search
     * WHERE u.profile_id = 12 AND u.unit_id = up1.unit_id
     *   AND up1.unit_type_param_id = 432 AND up1.value = '2011' -- special for 1-param search
     *
     * Example of a search using 2 unit-param values:
     * SELECT u.unit_id, u.profile_id, u.unit_type_id, up1.value, up2.value FROM unit u,
     * (SELECT u.unit_id, up.value FROM unit u, unit_param up WHERE u.unit_id = up.unit_id AND u.profile_id = 12 AND up.unit_type_param_id = 432 AND up.value = '2011') up1,
     * (SELECT u.unit_id, up.value FROM unit u, unit_param up WHERE u.unit_id = up.unit_id AND u.profile_id = 12 AND up.unit_type_param_id = 430 AND up.value = '10') up2
     * WHERE u.profile_id = 12 AND u.unit_id = up1.unit_id AND u.unit_id = up2.unit_id
     *
     * Example of a search using 3 unit-param values:
     * SELECT u.unit_id, u.profile_id, u.unit_type_id, up1.value, up2.value, up3.value FROM unit u,
     * (SELECT u.unit_id, up.value FROM unit u, unit_param up WHERE u.unit_id = up.unit_id AND u.profile_id = 12 AND up.unit_type_param_id = 432 AND up.value = '2011') up1,
     * (SELECT u.unit_id, up.value FROM unit u, unit_param up WHERE u.unit_id = up.unit_id AND u.profile_id = 12 AND up.unit_type_param_id = 430 AND up.value = '10') up2,
     * (SELECT u.unit_id, up.value FROM unit u, unit_param up WHERE u.unit_id = up.unit_id AND u.profile_id = 12 AND up.unit_type_param_id = 428 AND up.value = '27') up3
     * WHERE u.profile_id = 12 AND u.unit_id = up1.unit_id AND u.unit_id = up2.unit_id AND u.unit_id = up3.unit_id
     */

    if (countSQL) {
      ds.addSql("SELECT count(u.unit_id) ");
    } else {
      ds.addSql("SELECT u.unit_id, u.profile_id, u.unit_type_id, ");
      for (int i = 0; i < parameters.size(); i++) {
        ds.addSql("up" + (i + 1) + ".value, ");
      }
      ds.trim(2); // remove last ", ".
    }
    ds.addSql(" FROM unit u, ");
    if (parameters.size() == 1) {
      ds.addSql("unit_param up1 ");
    } else if (parameters.size() > 1) {
      for (int i = 0; i < parameters.size(); i++) {
        Parameter parameter = parameters.get(i);
        ds.addSql(
            "(SELECT u.unit_id, up.value FROM unit u, unit_param up WHERE u.unit_id = up.unit_id AND ");
        ds.addSqlAndArguments("u.profile_id = ? AND ", p.getId());
        ds.addSqlAndArguments(
            "up.unit_type_param_id = ? AND ", parameter.getUnittypeParameter().getId());
        addUnitParameterClause("up", p, ds, parameter);
        ds.addSql(") up" + (i + 1) + ", ");
      }
    }
    ds.cleanupSQLTail();
    ds.addSqlAndArguments(
        "WHERE u.unit_type_id = ? AND u.profile_id = ? AND ", p.getUnittype().getId(), p.getId());

    if (parameters.size() > 1) {
      for (int i = 0; i < parameters.size(); i++) {
        ds.addSql("u.unit_id = up" + (i + 1) + ".unit_id AND ");
      }
    } else if (parameters.size() == 1) {
      ds.addSql("u.unit_id = up1.unit_id AND ");
      ds.addSqlAndArguments(
          "up1.unit_type_param_id = ? AND ", parameters.get(0).getUnittypeParameter().getId());
      addUnitParameterClause("up1", p, ds, parameters.get(0));
    }
    ds.cleanupSQLTail();
    return ds;
  }

  /**
   * This is an older implementation, which has replaced even older implementations. The query is
   * not very efficient, but handles NULL param values (f.ex. searches like "value IS NULL").
   *
   * @param parameters
   * @param p
   * @param countSQL
   * @return
   */
  private DynamicStatement computeSQLForNullParamValues(
      List<Parameter> parameters, Profile p, boolean countSQL) {
    DynamicStatement ds = new DynamicStatement();

    /*
     * Always perform a LEFT join between unit and unit-param table
     * to show which parameter are non-existent in the unit-param table.
     *
     * Example of 2 parameter-search:
     * 1. The first select will add one column for each parameter (up1.value, up2.value, etc)
     * 2. For each parameter one LEFT JOIN-clause will be added. The name of the temp. tables will be up1, up2, etc.
     * 3. For WHERE statement at the bottom will increase with one extra comparison for each parameter
     *
     * SELECT u.unit_id, u.profile_id, u.unit_type_id, up1.value, up2.value FROM unit u
     *
     * LEFT JOIN (
     *  SELECT value, unit_id
     *  FROM unit_param
     *  WHERE unit_type_param_id = 2758
     * ) up1 ON u.unit_id = up1.unit_id
     *
     * LEFT JOIN (
     *  SELECT value, unit_id
     *  FROM unit_param
     *  WHERE unit_type_param_id = 2759
     * ) up2 ON u.unit_id = up2.unit_id
     *
     * WHERE up1.value = 'Hello' AND up2.value = 'World'
     *
     * The last WHERE-statement may be subject to many changes, depending upon the
     * operand, operator, parameter-data-type and profile-match, see below
     */

    // 1.
    if (countSQL) {
      ds.addSql("SELECT count(u.unit_id) ");
    } else {
      ds.addSql("SELECT u.unit_id, u.profile_id, u.unit_type_id, ");
      for (int i = 0; i < parameters.size(); i++) {
        String tName = "up" + (i + 1);
        ds.addSql(tName + ".value, ");
      }
      ds.trim(2); // remove last ", ".
    }
    ds.addSql(" FROM unit u ");

    // 2.
    for (int i = 0; i < parameters.size(); i++) {
      String tName = "up" + (i + 1);
      Parameter parameter = parameters.get(i);
      ds.addSql("LEFT JOIN (");
      ds.addSql("SELECT unit_id, value ");
      ds.addSql("FROM unit_param ");
      ds.addSqlAndArguments(
          "WHERE unit_type_param_id = ?) " + tName + " ", parameter.getUnittypeParameter().getId());
      ds.addSql("ON u.unit_id = " + tName + ".unit_id ");
    }

    // 3.
    ds.addSql("WHERE ");
    for (int i = 0; i < parameters.size(); i++) {
      String tName = "up" + (i + 1);
      Parameter parameter = parameters.get(i);
      UnittypeParameter utp = parameter.getUnittypeParameter();
      String operand = parameter.getValue();
      if (parameter.valueWasNull()) {
        operand = null;
      } else {
        operand = operand.replace('*', '%');
        if (operand.startsWith("^")) {
          operand = operand.substring(1);
        }
        if (operand.endsWith("$")) {
          operand = operand.substring(0, operand.length() - 1);
        }
      }
      Parameter.Operator op = parameter.getOp();
      Parameter.ParameterDataType type = parameter.getType();
      String profileParameterValue = null;
      ProfileParameter pp = p.getProfileParameters().getById(utp.getId());
      if (pp != null) {
        profileParameterValue = pp.getValue();
      }
      boolean profileMatch =
          match(profileParameterValue, operand, parameter.getOp(), parameter.getType());
      /*
       * Use case for profile-match = true
       * 1.  operand = 3.15, operator = EQ   -> up.value = '3.15' || up.value IS NULL
       * 2.  operand = 3.15, operator = NE   -> up.value <> '3.15' || up.value IS NULL
       * 3.  operand = %3.15%, operator = EQ -> up.value LIKE '%3.15%' || up.value IS NULL
       * 4.  operand = %3.15%, operator = NE -> up.value NOT LIKE '%3.15%' || up.value IS NULL
       * 5.  operand = 3.15, operator = LT   -> up.value < '3.15' || up.value IS NULL
       * 6.  operand = 3.15, operator = LE   -> up.value <= '3.15' || up.value IS NULL
       * 7.  operand = 3.15, operator = GT   -> up.value > '3.15' || up.value IS NULL
       * 8.  operand = 3.15, operator = GE   -> up.value >= '3.15' || up.value IS NULL
       * 9.  operand = NULL, operator = EQ   -> up.value IS NULL
       * 10. operand = NULL, operator = NE   -> all units match
       * Use case 10 stands out, it needs no search-criteria
       *
       * Use cases for profile-match = false
       * 1.  operand = 3.15, operator = EQ   -> up.value = '3.15'
       * 2.  operand = 3.15, operator = NE   -> up.value <> '3.15' || up.value IS NULL
       * 3.  operand = %3.15%, operator = EQ -> up.value LIKE '%3.15%'
       * 4.  operand = %3.15%, operator = NE -> up.value NOT LIKE '%3.15%' || up.value IS NULL
       * 5.  operand = 3.15, operator = LT   -> up.value < '3.15'
       * 6.  operand = 3.15, operator = LE   -> up.value <= '3.15'
       * 7.  operand = 3.15, operator = GT   -> up.value > '3.15'
       * 8.  operand = 3.15, operator = GE   -> up.value >= '3.15'
       * 9.  operand = NULL, operator = EQ   -> Can never match - since (profile-param is not NULL)
       * 10. operand = NULL, operator = NE   -> up.value IS NOT NULL
       * Use case 2 and 4 stands out, they need an extra expression
       */
      if (profileMatch) {
        if (operand == null) {
          if (Parameter.Operator.EQ.equals(op)) {
            ds.addSqlAndArguments(tName + ".value IS NULL AND ");
          }
        } else {
          ds.addSqlAndArguments(
              "("
                  + tName
                  + ".value "
                  + op.getSQL(operand)
                  + " "
                  + type.getSQL()
                  + " OR "
                  + tName
                  + ".value IS NULL) AND ",
              operand);
        }
      } else if (operand == null) {
        if (Parameter.Operator.NE.equals(op)) {
          ds.addSqlAndArguments(tName + ".value IS NOT NULL AND ");
        }
        if (Parameter.Operator.EQ.equals(op)) {
          ds.addSqlAndArguments(tName + ".value = '#@#@94ks94' AND ");
        } // should never match anything!
      } else {
        ds.addSqlAndArguments(
            "(" + tName + ".value " + op.getSQL(operand) + " " + type.getSQL() + ") AND ", operand);
      }
    }
    if (p != null) {
      ds.addSql("profile_id = " + p.getId());
    }
    ds.cleanupSQLTail();
    return ds;
  }

  private int getUnitCount(List<Parameter> parameters, Profile p) throws SQLException {
    DynamicStatement ds = computeSQL(parameters, p, true);
    ResultSet rs = null;
    PreparedStatement pp = null;
    try {
      pp = ds.makePreparedStatement(connection);
      rs = pp.executeQuery();
      if (logger.isDebugEnabled()) {
        logger.debug(ds.getDebugMessage());
      }
      if (rs.next()) {
        return rs.getInt(1);
      }
      return 0;
    } catch (SQLException sqle) {
      logger.error("The sql that failed:" + ds.getSqlQuestionMarksSubstituted());
      throw sqle;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (pp != null) {
        pp.close();
      }
    }
  }

  private Map<String, Unit> getUnits(
      Map<String, Unit> units, List<Parameter> parameters, Profile p, Integer limit)
      throws SQLException {
    DynamicStatement ds = computeSQL(parameters, p, false);
    ResultSet rs = null;
    PreparedStatement pp = null;
    try {
      pp = ds.makePreparedStatement(connection);
      if (limit != null && limit > 0 && limit < 10000) {
        pp.setFetchSize(limit);
      } else {
        pp.setFetchSize(1000);
      }
      rs = pp.executeQuery();
      if (logger.isDebugEnabled()) {
        logger.debug(ds.getDebugMessage());
      }
      while (rs.next()) {
        String unitId = rs.getString("u.unit_id");
        Integer profileId = rs.getInt("u.profile_id");
        Integer unittypeId = rs.getInt("u.unit_type_id");
        Unittype unittype = acs.getUnittype(unittypeId);
        Profile profile = unittype.getProfiles().getById(profileId);
        Unit unit = new Unit(unitId, unittype, profile);
        for (int i = 0; i < parameters.size(); i++) {
          String tName = "up" + (i + 1);
          Parameter parameter = parameters.get(i);
          UnittypeParameter utp = parameter.getUnittypeParameter();
          String value = rs.getString(tName + ".value");
          if (value != null) {
            UnitParameter up = new UnitParameter(utp, unitId, value, p);
            unit.getUnitParameters().put(utp.getName(), up);
          }
        }
        units.put(unitId, unit);
        if (limit != null && limit > 0 && units.size() == limit) {
          break;
        }
      }
      return units;
    } catch (SQLException sqle) {
      logger.error("The sql that failed:" + ds.getSqlQuestionMarksSubstituted());
      throw sqle;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (pp != null) {
        pp.close();
      }
    }
  }

  public int getUnitCount(List<Parameter> parameters) throws SQLException {
    int count = 0;
    for (Profile profile : profiles) {
      count += getUnitCount(parameters, profile);
    }
    return count;
  }

  public Map<String, Unit> getUnits(List<Parameter> parameters, Integer limit) throws SQLException {
    Map<String, Unit> units;
    if (ACS.isStrictOrder()) {
      units = new TreeMap<>();
    } else {
      units = new HashMap<>();
    }
    for (Profile profile : profiles) {
      units = getUnits(units, parameters, profile, limit);
      if (limit != null && limit > 0 && units.size() == limit) {
        break;
      }
    }
    return units;
  }
}
