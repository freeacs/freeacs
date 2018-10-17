package com.github.freeacs.web.app.page.file;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.util.DateUtils;
import java.util.Map;

/** The Class FirmwareData. */
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

  public Input getFileType() {
    return fileType;
  }

  public void setFileType(Input fileType) {
    this.fileType = fileType;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(Input name) {
    this.name = name;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public Input getName() {
    return name;
  }

  /**
   * Sets the version number.
   *
   * @param versionNumber the new version number
   */
  public void setVersionNumber(Input versionNumber) {
    this.versionNumber = versionNumber;
  }

  /**
   * Gets the version number.
   *
   * @return the version number
   */
  public Input getVersionNumber() {
    return versionNumber;
  }

  /**
   * Sets the softwaredate.
   *
   * @param softwaredate the new softwaredate
   */
  public void setSoftwaredate(Input softwaredate) {
    this.softwaredate = softwaredate;
  }

  /**
   * Gets the softwaredate.
   *
   * @return the softwaredate
   */
  public Input getSoftwaredate() {
    return softwaredate;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(Input description) {
    this.description = description;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public Input getDescription() {
    return description;
  }

  @Override
  public void bindForm(Map<String, Object> root) {}

  @Override
  public boolean validateForm() {
    return false;
  }

  public Input getType() {
    return type;
  }

  public void setType(Input type) {
    this.type = type;
  }

  public Input getTargetName() {
    return targetName;
  }

  public void setTargetName(Input targetName) {
    this.targetName = targetName;
  }

  public Input getId() {
    return id;
  }

  public Input getContent() {
    return content;
  }

  public void setContent(Input content) {
    this.content = content;
  }
}
