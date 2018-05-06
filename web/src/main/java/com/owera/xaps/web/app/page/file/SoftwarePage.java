package com.owera.xaps.web.app.page.file;

import java.util.List;

import com.owera.xaps.dbi.File;
import com.owera.xaps.dbi.FileType;
import com.owera.xaps.web.app.input.DropDownSingleSelect;

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
