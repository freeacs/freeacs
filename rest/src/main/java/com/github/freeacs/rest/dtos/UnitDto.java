package com.github.freeacs.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnitDto {
    private String unitId;
    private ProfileDto profile;
    private UnitTypeDto unitType;
}
