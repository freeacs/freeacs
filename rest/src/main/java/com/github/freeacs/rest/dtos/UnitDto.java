package com.github.freeacs.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.Nested;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitDto {
    private String unitId;
    @Nested("profile")
    private ProfileDto profile;
    @Nested("unit_type")
    private UnitTypeDto unitType;
}
