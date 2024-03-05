package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitType {
    private Integer id;
    private String name;
    private String vendor;
    private String description;
    private UnitTypeProvisioningProtocol protocol;
}