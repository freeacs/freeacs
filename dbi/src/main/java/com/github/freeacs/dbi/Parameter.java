package com.github.freeacs.dbi;

import java.util.regex.Pattern;

public class Parameter {
	public enum Operator {
		EQ("="), NE("<>"), LT("<"), LE("<="), GT(">"), GE(">=");
		private String op;

		Operator(String op) {
			this.op = op;
		}

		public static Operator getOperator(String op) {
			if (op.equals("="))
				return EQ;
			if (op.equals("!") || op.equals("<>"))
				return NE;
			if (op.equals("<"))
				return LT;
			if (op.equals("<="))
				return LE;
			if (op.equals(">="))
				return GE;
			if (op.equals(">"))
				return GT;
			throw new IllegalArgumentException("Operator " + op + " is not a valid operator");
		}

		public static Operator getOperatorFromLiteral(String op) {
			op = op.toUpperCase();
			if (op.equals("EQ"))
				return EQ;
			if (op.equals("NE"))
				return NE;
			if (op.equals("LT"))
				return LT;
			if (op.equals("LE"))
				return LE;
			if (op.equals("GE"))
				return GE;
			if (op.equals("GT"))
				return GT;
			throw new IllegalArgumentException("Operator " + op + " is not a valid operator");
		}

		public String getOperatorSign() {
			return this.op;
		}

		public String getOperatorLiteral() {
			if (this.equals(EQ))
				return "EQ";
			if (this.equals(NE))
				return "NE";
			if (this.equals(LT))
				return "LT";
			if (this.equals(LE))
				return "LE";
			if (this.equals(GE))
				return "GE";
			if (this.equals(GT))
				return "GT";
			throw new IllegalArgumentException("Operator " + op + " is not a valid operator");
		}

		public String getSQL(String operand) {
			if (operand == null) {
				if (this.equals(EQ) || this.equals(LE) || this.equals(GE))
					return "IS";
				if (this.equals(NE) || this.equals(LT) || this.equals(GT))
					return "IS NOT";
			} else if (operand.indexOf("%") > -1 || operand.indexOf("_") > -1) {
				if (this.equals(EQ))
					return "LIKE";
				else if (this.equals(NE))
					return "NOT LIKE";
				else
					return op;
			} else {
				return op;
			}
			throw new IllegalArgumentException("Cannot use operator " + op + " on operand " + operand);
		}
	}

	public enum ParameterDataType {
		TEXT("TEXT"), NUMBER("NUMBER");
		private String type;

		ParameterDataType(String type) {
			this.type = type;
		}

		public static ParameterDataType getDataType(String type) {
			if (type.equals("TEXT"))
				return TEXT;
			if (type.equals("NUMBER"))
				return NUMBER;
			throw new IllegalArgumentException("Data type " + type + " is not a valid type");
		}

		public String getType() {
			return this.type;
		}

		public String getSQL() {
			if (this.equals(TEXT)) {
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
	// if value = null, it is set to "". Introducing UnitParameterQuery, we want to be able
	// to use null-values in seraches, but we don't want to break the interface/behaviour (yet).
	// instead we keep this flag to inform UPQ about the value orginial state.
	private boolean valueWasNull = false;

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
		if (value == null)
			value = "";
		return value;
	}

	public void setValue(String value) {
		if (value == null) {
			value = "";
			valueWasNull = true;
		} else
			valueWasNull = false;
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
