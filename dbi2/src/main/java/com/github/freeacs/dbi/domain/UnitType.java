package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class UnitType {
    private Integer id;
    private String name;
    private String vendor;
    private String description;
    private UnitTypeProvisioningProtocol protocol;
}