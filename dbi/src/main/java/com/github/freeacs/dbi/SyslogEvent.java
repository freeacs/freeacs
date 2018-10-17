package com.github.freeacs.dbi;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This object has been made according to the latest standards of Fusion DBI development, April
 * 2012. The following standards should perhaps been enforced among other similar objects in DBI
 *
 * <p>- Offer at least an Empty Constructor - should make it easier to make readable add/update code
 * in the DBI-clients - Make a validate() method which should call upon all set-methods (except
 * setId()) - all set-methods shall make input-validation and throw IllegalArgumentException if
 * something is wrong - offer a validateInput(boolean) method and a corresponding
 * validateInput-field. Use this field in all set-methods to skip input-validation if validateInput
 * = false (will be used from ACS-object). In those cases were a value should be specified (or it
 * would cause NullPointerException or other things), set a reasonable default-value. In some cases
 * it doesn't make any sense to allow input, for example "name=null", since it will break
 * everything. In that case we will always throw an IllegalArgumentException. The validateInput()
 * method must be protected and must always be called from DBI upon write to Fusion database - to
 * ensure correct data goes into the database. - contstants enumeration should be represented as a
 * enum. - group all get-methods and set-methods together - the order of the set/get-methods should
 * always be the same, both inside the validate method and in the class itself - the order of the
 * set/get-methods is decided by which fields are important for other fields, the next step is to
 * order it in the same way as used in DBI-clients. - Unittype/id should be the first fields to be
 * listed
 *
 * @author Morten
 */
public class SyslogEvent implements Comparable<SyslogEvent> {
  public enum StorePolicy {
    STORE,
    DUPLICATE,
    DISCARD
  }

  public static int DUPLICATE_TIMEOUT = 60;

  private Unittype unittype;

  private Integer id;

  private Integer eventId;

  private String name;

  private String description;

  private Group group;

  private String expression;

  private Pattern expressionPattern;

  private StorePolicy storePolicy;

  private File script;

  private Integer deleteLimit;

  private boolean validateInput = true;

  public SyslogEvent() {}

  public SyslogEvent(
      Unittype unittype,
      Integer eventId,
      String name,
      String desc,
      Group group,
      String expression,
      StorePolicy storePolicy,
      File script,
      int deleteLimit) {
    setUnittype(unittype);
    setEventId(eventId);
    setName(name);
    setDescription(description);
    setGroup(group);
    setExpression(expression);
    setStorePolicy(storePolicy);
    setScript(script);
    setDeleteLimit(deleteLimit);
  }

  public void validate() {
    setUnittype(unittype);
    setEventId(eventId);
    setName(name);
    setDescription(description);
    setGroup(group);
    if (expression != null) {
      setExpression(expression);
    }
    setStorePolicy(storePolicy);
    setScript(script);
    setDeleteLimit(deleteLimit);
  }

  @Override
  public int compareTo(SyslogEvent o) {
    return getEventId() - o.getEventId();
  }

  /**
   * Only to be used by ACS object - to read from database. The idea is that data corruption in
   * database should not make it impossible to start Fusion
   */
  protected void validateInput(boolean validateInput) {
    this.validateInput = validateInput;
  }

  /** GET-methods. */
  public Unittype getUnittype() {
    return unittype;
  }

  public Integer getId() {
    return id;
  }

  public Integer getEventId() {
    return eventId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Group getGroup() {
    return group;
  }

  public String getExpression() {
    return expression;
  }

  public Pattern getExpressionPattern() {
    return expressionPattern;
  }

  public StorePolicy getStorePolicy() {
    return storePolicy;
  }

  public File getScript() {
    return script;
  }

  public Integer getDeleteLimit() {
    return deleteLimit;
  }

  /** SET-methods. */
  public void setUnittype(Unittype unittype) {
    if (validateInput && unittype == null) {
      throw new IllegalArgumentException("SyslogEvent unittype cannot be null");
    }
    this.unittype = unittype;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public void setEventId(Integer eventId) {
    if (validateInput) {
      if (eventId == null) {
        throw new IllegalArgumentException("SyslogEvent id cannot be null");
      }
      if (eventId < 1000) {
        throw new IllegalArgumentException(
            "Cannot add syslog events with id 0-999, they are restricted to Fusion");
      }
    }
    if (eventId == null) {
      eventId = 0;
    }
    this.eventId = eventId;
  }

  public void setName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("SyslogEvent name cannot be null");
    }
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  public void setExpression(String expression) {
    if (validateInput && expression == null) {
      throw new IllegalArgumentException("SyslogEvent expression cannot be null");
    }
    if (expression == null) {
      expression = "Specify an expression";
    }
    String expressionPatternStr = null;
    try {
      expressionPatternStr = expression.replace("*", ".*");
      expressionPatternStr = expressionPatternStr.replace("%", ".*");
      expressionPatternStr = expressionPatternStr.replace("_", ".{1}");
      this.expression = expression;
      this.expressionPattern = Pattern.compile(expressionPatternStr);
    } catch (PatternSyntaxException pse) {
      if (expressionPatternStr.equals(expression)) {
        throw new IllegalArgumentException(
            "SyslogEvent expression "
                + expression
                + " could not be parsed into a regular expression: "
                + pse);
      } else {
        throw new IllegalArgumentException(
            "SyslogEvent expression "
                + expression
                + " (auto-converted to "
                + expressionPatternStr
                + ") could not be parsed into a regular expression: "
                + pse);
      }
    }
  }

  public void setStorePolicy(StorePolicy storePolicy) {
    this.storePolicy = storePolicy;
  }

  public void setScript(File script) {
    if (validateInput && script != null && script.getType() != FileType.SHELL_SCRIPT) {
      throw new IllegalArgumentException(
          "The script file " + script.getName() + " must be of type SHELL_SCRIPT");
    }
    this.script = script;
  }

  public void setDeleteLimit(Integer deleteLimit) {
    if (validateInput && deleteLimit != null && deleteLimit < 0) {
      throw new IllegalArgumentException("Cannot set syslog limit to less than 0");
    }
    if (deleteLimit != null && deleteLimit < 0) {
      deleteLimit = null;
    }
    this.deleteLimit = deleteLimit;
  }
}
