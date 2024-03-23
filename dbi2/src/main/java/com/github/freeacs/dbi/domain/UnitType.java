package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class UnitType {
    @ColumnName("unit_type_id")
    private Integer id;
    @ColumnName("unit_type_name")
    private String name;
    @ColumnName("vendor_name")
    private String vendor;
    private String description;
    private UnitTypeProvisioningProtocol protocol;
}