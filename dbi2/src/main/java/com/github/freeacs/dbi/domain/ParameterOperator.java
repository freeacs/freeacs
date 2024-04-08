package com.github.freeacs.dbi.domain;

public enum ParameterOperator {
    EQ("="),
    NE("<>"),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">=");
    private final String op;

    ParameterOperator(String op) {
        this.op = op;
    }

    public static ParameterOperator getOperator(String op) {
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

    public static ParameterOperator getOperatorFromLiteral(String op) {
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