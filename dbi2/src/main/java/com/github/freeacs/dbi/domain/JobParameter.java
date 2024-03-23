package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class JobParameter {
    public static String ANY_UNIT_IN_GROUP = "ANY-UNIT-IN-GROUP";

    private Job job;
    private String unitId;
    private Parameter parameter;
}
