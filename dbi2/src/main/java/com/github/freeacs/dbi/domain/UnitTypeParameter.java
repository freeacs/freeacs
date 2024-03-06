package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeParameter {
    private Integer id;
    private String name;
    private UnitTypeParameterFlag flag;
    private UnitTypeParameterValues values;
    private UnitType unittype;
}
