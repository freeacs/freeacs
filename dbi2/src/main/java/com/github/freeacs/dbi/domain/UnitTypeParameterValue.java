package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jdbi.v3.core.mapper.Nested;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeParameterValue {
  private String type;
  private String value;
  private Integer priority;
  private Integer unitTypeParamId;
}
