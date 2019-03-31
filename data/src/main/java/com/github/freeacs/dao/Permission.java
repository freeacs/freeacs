package com.github.freeacs.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private Long id;
    private Long userId;
    private Long unitTypeId;
    private Long profileId;
}
