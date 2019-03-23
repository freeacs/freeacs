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
public class UnitTypeParameter implements Serializable {
    private Long id;
    private String name;
    private String flags;
    private Long unitTypeId;
}
