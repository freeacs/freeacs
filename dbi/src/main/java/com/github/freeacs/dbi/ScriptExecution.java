package com.github.freeacs.dbi;

import lombok.Data;

import java.util.Date;

@Data
public class ScriptExecution {
  private Integer id;
  private Unittype unittype;
  private File scriptFile;
  private String arguments;
  private Date requestTms;
  private String requestId;
  private Date startTms;
  private Date endTms;
  /**
   * ExitStatus == null : Not completed exitStatus == 0 : OK - SUCCESS exitStatus == 1 : ERROR -
   * could expect an errorMessage
   */
  private Boolean exitStatus;
  private String errorMessage;
}
