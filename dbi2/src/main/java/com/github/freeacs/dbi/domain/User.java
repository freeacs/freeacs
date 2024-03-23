package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String hashedSecret;
    private String fullName;
    private String access;
    private Boolean admin;
}