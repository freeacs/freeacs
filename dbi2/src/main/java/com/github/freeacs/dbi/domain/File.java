package com.github.freeacs.dbi.domain;

import lombok.*;

import java.util.Date;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class File {
  private UnitType unitType;
  private Integer id;
  private String name;
  private String oldName;
  private FileType type;
  private String description;
  private String version;
  private Date timestamp;
  private int length;
  private String targetName;
  private User owner;
  private byte[] content;

  public void setTargetName(String targetName) {
    if (type == FileType.TR069_SCRIPT && targetName == null) {
      targetName = name;
    }
    this.targetName = targetName;
  }
}
