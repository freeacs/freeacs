package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.regex.Pattern;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Parameter {
  private UnitTypeParameter unittypeParameter;
  private String value;
  private Pattern pattern;
  private Integer groupParameterId;
  private ParameterOperator op;
  private ParameterDataType type;

  public Parameter(UnitTypeParameter utp, String val, ParameterOperator op, ParameterDataType type) {
    this.unittypeParameter = utp;
    this.value = val;
    this.op = op;
    this.type = type;
  }

  public Parameter(UnitTypeParameter utp, String val) {
    this.unittypeParameter = utp;
    this.value = val;
    this.op = ParameterOperator.EQ;
    this.type = ParameterDataType.TEXT;
  }

}
