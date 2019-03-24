package com.github.freeacs.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.io.Serializable;

@Data
@Builder
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class Profile implements Serializable {
    private Long id;
    private String name;
    private Long unitTypeId;
}
