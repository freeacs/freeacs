package com.github.freeacs.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDto {
    private Long id;
    private UserDto user;
    private UnitTypeDto unitType;
    private ProfileDto profile;
}
