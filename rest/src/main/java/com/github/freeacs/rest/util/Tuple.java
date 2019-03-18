package com.github.freeacs.rest.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tuple<FIRST, SECOND> {
    FIRST first;
    SECOND second;
}
