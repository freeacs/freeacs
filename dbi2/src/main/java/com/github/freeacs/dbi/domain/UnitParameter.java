package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class UnitParameter {
    private String unitId;
    private Profile profile;
    private Parameter parameter;
}
