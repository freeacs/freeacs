package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.Nested;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    private String id;
    @Nested
    private Profile profile;
}