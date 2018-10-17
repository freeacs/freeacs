package com.github.freeacs.dbi;

import java.util.regex.Pattern;

public class Parameter {
  public enum Operator {
    EQ("="),
    NE("<>"),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">=");
    private String op;

    Operator(String op) {
      this.op = op;
    }

    public static Operator getOperator(String op) {
      if ("=".equals(op)) {
        return EQ;
      }
      if ("!".equals(op) || "<>".equals(op)) {
        return NE;
      }
      if ("<".equals(op)) {
        return LT;
      }
      if ("<=".equals(op)) {
        return LE;
      }
      if (">=".equals(op)) {
        return GE;
      }
      if (">".equals(op)) {
        return GT;
      }
      throw new IllegalArgumentException("Operator " + op + " is not a valid operator");
    }

    public static Operator getOperatorFromLiteral(String op) {
      op = op.toUpperCase();
      if ("EQ".equals(op)) {
        return EQ;
      }
      if ("NE".equals(op)) {
        return NE;
      }
      if ("LT".equals(op)) {
        return LT;
      }
      if ("LE".equals(op)) {
        return LE;
      }
      if ("GE".equals(op)) {
        return GE;
      }
      if ("GT".equals(op)) {
        return GT;
      }
      throw new IllegalArgumentException("Operator " + op + " is not a valid operator");
    }

    public String getOperatorSign() {
      return this.op;
    }

    public String getOperatorLiteral() {
      if (EQ.equals(this)) {
        return "EQ";
      }
      if (NE.equals(this)) {
        return "NE";
      }
      if (LT.equals(this)) {
        return "LT";
      }
      if (LE.equals(this)) {
        return "LE";
      }
      if (GE.equals(this)) {
        return "GE";
      }
      if (GT.equals(this)) {
        return "GT";
      }
      throw new IllegalArgumentException("Operator " + op + " is not a valid operator");
    }

    public String getSQL(String operand) {
      if (operand == null) {
        if (EQ.equals(this) || LE.equals(this) || GE.equals(this)) {
          return "IS";
        }
        if (NE.equals(this) || LT.equals(this) || GT.equals(this)) {
          return "IS NOT";
        }
      } else if (operand.indexOf('%') > -1 || operand.indexOf('_') > -1) {
        if (EQ.equals(this)) {
          return "LIKE";
        } else if (NE.equals(this)) {
          return "NOT LIKE";
        } else {
          return op;
        }
      } else {
        return op;
      }
      throw new IllegalArgumentException("Cannot use operator " + op + " on operand " + operand);
    }
  }

  public enum ParameterDataType {
    TEXT("TEXT"),
    NUMBER("NUMBER");
    private String type;

    ParameterDataType(String type) {
      this.type = type;
    }

    public static ParameterDataType getDataType(String type) {
      if ("TEXT".equals(type)) {
        return TEXT;
      }
      if ("NUMBER".equals(type)) {
        return NUMBER;
      }
      throw new IllegalArgumentException("Data type " + type + " is not a valid type");
    }

    public String getType() {
      return this.type;
    }

    public String getSQL() {
      if (TEXT.equals(this)) {
        return "?";
      } else {
        return "CONVERT(?,SIGNED)";
      }
    }
  }

  private UnittypeParameter unittypeParameter;
  private String value;
  private Pattern pattern;
  private Integer groupParameterId;
  private Operator op;
  private ParameterDataType type;
  /**
   * If value = null, it is set to "". Introducing UnitParameterQuery, we want to be able to use
   * null-values in seraches, but we don't want to break the interface/behaviour (yet). instead we
   * keep this flag to inform UPQ about the value orginial state.
   */
  private boolean valueWasNull;

  public Parameter(UnittypeParameter utp, String val, Operator op, ParameterDataType type) {
    this.unittypeParameter = utp;
    setValue(val);
    this.op = op;
    this.type = type;
  }

  public Parameter(UnittypeParameter utp, String val) {
    this.unittypeParameter = utp;
    setValue(val);
    this.op = Operator.EQ;
    this.type = ParameterDataType.TEXT;
  }

  public UnittypeParameter getUnittypeParameter() {
    return unittypeParameter;
  }

  public void setUnittypeParameter(UnittypeParameter unittypeParameter) {
    this.unittypeParameter = unittypeParameter;
  }

  public String getValue() {
    if (value == null) {
      value = "";
    }
    return value;
  }

  public void setValue(String value) {
    if (value == null) {
      value = "";
      valueWasNull = true;
    } else {
      valueWasNull = false;
    }
    this.value = value;
  }

  public String toString() {
    return "[" + unittypeParameter.getName() + " " + op.getOperatorSign() + " " + getValue() + "]";
  }

  protected Pattern getPattern() {
    return pattern;
  }

  protected void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  public Integer getGroupParameterId() {
    return groupParameterId;
  }

  public void setGroupParameterId(Integer groupParameterId) {
    this.groupParameterId = groupParameterId;
  }

  public Operator getOp() {
    return op;
  }

  public void setOp(Operator op) {
    this.op = op;
  }

  public ParameterDataType getType() {
    return type;
  }

  public void setType(ParameterDataType type) {
    this.type = type;
  }

  public boolean valueWasNull() {
    return valueWasNull;
  }

  public void setValueWasNull(boolean valueWasNull) {
    this.valueWasNull = valueWasNull;
  }
}
