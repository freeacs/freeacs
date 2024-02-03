package com.github.freeacs.web.app.page.file;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class FileData extends InputData {
  private Input name = Input.getStringInput("name");
  private Input type = Input.getStringInput("type");
  private Input versionNumber = Input.getStringInput("versionnumber");
  private Input softwaredate = Input.getDateInput("softwaredate", DateUtils.Format.DATE_ONLY);
  private Input description = Input.getStringInput("description");
  private Input targetName = Input.getStringInput("targetname");
  private Input content = Input.getStringInput("content");

  /** For preview of a file. */
  private Input id = Input.getIntegerInput("id");

  /** The file type filter. */
  private Input fileType = Input.getStringInput("filetype");

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

}
