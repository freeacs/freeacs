package com.github.freeacs.web.app.page.file;

import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import java.util.List;

public class SoftwarePage extends FilePage {
  @Override
  public DropDownSingleSelect<FileType> getTypeSelect(FileType filetype) {
    return getTypeSelect(null, FileType.SOFTWARE);
  }

  @Override
  public List<File> getFiles() {
    return getFiles(FileType.SOFTWARE);
  }
}
