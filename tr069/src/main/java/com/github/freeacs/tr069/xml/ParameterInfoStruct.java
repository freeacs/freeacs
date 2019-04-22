package com.github.freeacs.tr069.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterInfoStruct {
  private String name;
  private boolean writable;
  private boolean inspect;
}
