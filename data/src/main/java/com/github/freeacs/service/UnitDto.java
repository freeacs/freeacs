package com.github.freeacs.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class UnitDto {
    private String unitId;
    private ProfileDto profile;
    private UnitTypeDto unitType;
}
