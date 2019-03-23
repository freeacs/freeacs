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
public class ProfileParameter implements Serializable {
    private Long profileId;
    private Long unitTypeParamId;
    private String value;
}
