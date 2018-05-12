package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.File;

public class TestCaseFiles {

	private Integer id;
	private File inputFile;
	private File outputFile;
	
	public TestCaseFiles() {
		this(null, null, null);
	}
	
	public TestCaseFiles(Integer id, File inputFile, File outputFile) {
		super();
		this.id = id;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
}
