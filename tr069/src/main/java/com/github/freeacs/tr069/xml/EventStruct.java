package com.github.freeacs.tr069.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStruct {
  public static final String ID = "EventStruct";
  private String eventCode;
  private String commandKey;
}
