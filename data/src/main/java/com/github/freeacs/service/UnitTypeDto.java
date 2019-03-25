package com.github.freeacs.service;

import com.github.freeacs.shared.Protocol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeDto {
    private Long id;
    private String name;
    private String vendor;
    private String description;
    private Protocol protocol;
}
