package com.github.freeacs.dbi.domain;

import lombok.*;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeParameter {
    @ColumnName("unit_type_param_id")
    private Integer id;
    private String name;
    private String flags;
    private Integer unitTypeId;
}
