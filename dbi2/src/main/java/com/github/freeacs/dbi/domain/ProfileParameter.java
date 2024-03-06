package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class ProfileParameter {
    private Integer id;
    private Profile profile;
    private UnitTypeParameter unittypeParameter;
    private String value;
}
