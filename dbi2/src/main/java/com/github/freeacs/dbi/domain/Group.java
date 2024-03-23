package com.github.freeacs.dbi.domain;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class Group {
    private Integer id;
    private String name;
    private String description;
    private Group parent;
    private UnitType unitType;
    @ToString.Exclude
    private List<Group> children;
    private Profile profile;
    private Integer count;
    @ToString.Exclude
    private List<GroupParameter> parameters;
}
