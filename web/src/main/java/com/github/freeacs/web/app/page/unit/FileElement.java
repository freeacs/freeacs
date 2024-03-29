package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.dbi.File;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileElement {
  private String version;
  private File file;

  public FileElement(String v, File f) {
    if (v == null) {
      throw new NullPointerException("Sorry, can't have a FileElement without version.");
    }
    version = v;
    file = f;
  }

  public void setVersion(String version) {
    if (version == null) {
      throw new NullPointerException("Sorry, can't have a FileElement without version.");
    }
    this.version = version;
  }

  public int hashCode() {
    return version.hashCode();
  }

  public boolean equals(Object o) {
    if (!(o instanceof FileElement)) {
      return false;
    }
    FileElement fe = (FileElement) o;
    return fe.getVersion().equals(getVersion());
  }

  public String toString() {
    if (file != null) {
      return file.getName() + ", " + version;
    } else {
      return "<none>, " + version;
    }
  }
}
