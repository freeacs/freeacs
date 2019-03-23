package com.github.freeacs.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit implements Serializable {
    private String unitId;
    private Long profileId;
    private Long unitTypeId;
}
