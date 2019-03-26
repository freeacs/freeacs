package com.github.freeacs.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private String secret;
    private String fullname;
    private String accesslist;
}
