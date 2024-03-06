package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jdbi.v3.core.mapper.Nested;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
  private Integer id;
  private String name;
  @Nested
  private UnitType unitType;
}
