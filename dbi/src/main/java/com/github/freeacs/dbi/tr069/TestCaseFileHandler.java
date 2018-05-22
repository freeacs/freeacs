package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.tr069.TestCaseParameter.TestCaseParameterType;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestCaseFileHandler {
	private Unittype unittype;

	public TestCaseFileHandler(Unittype unittype) {
		this.unittype = unittype;
	}

	public List<TestCase> parseDirectory(String directory) throws IOException, SQLException {
		directory = directory(directory, false);
		File dir = new File(directory);
		List<TestCase> list = new ArrayList<TestCase>();
		for (String filename : dir.list()) {
			TestCase tc = parseFile(directory + filename);
			if (tc != null)
				list.add(tc);
		}
		return list;
	}

	private int notification(String s) {
		if (s.equals("[NUMBER]"))
			return -1;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("The notification must be a number from 0 to 2, or [NUMBER]");
		}
	}

	public TestCase parseFile(String filename) throws IOException, SQLException {
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		TestCase tc = new TestCase();
		tc.setUnittype(unittype);
		String line = null;
		int lineCounter = 0;
		String array[];
		while ((line = br.readLine()) != null) {
			lineCounter++;
			if (line.trim().length() == 0 || line.startsWith("#"))
				continue;
			array = line.trim().split("\\s+");
			
			if (line.toUpperCase().startsWith("ID") && line.length() > 2)
				tc.setId(new Integer(line.substring(2).trim()));
			else if (array[0].toUpperCase().equals("METHOD")) {
				tc.setMethod(TestCase.TestCaseMethod.valueOf(array[1].toUpperCase()));
				if (tc.getMethod() == TestCase.TestCaseMethod.FILE) {
					tc.setFiles(new TestCaseFiles());
				}
			}
			else if (line.toUpperCase().startsWith("TAGS") && line.length() > 4)
				tc.setTags(line.substring(4).trim().toUpperCase());
			else if (line.toUpperCase().startsWith("EXPECTERROR") && line.length() > 11)
				tc.setExpectError(line.substring(11).trim().toUpperCase().equals("TRUE") ? Boolean.TRUE : Boolean.FALSE);
			else if (array[0].toUpperCase().equals("INPUT")) {
				if (array.length < 2) {
					throw new IllegalArgumentException("Line " + lineCounter + ": Does not have a mandatory INPUT argument.");
				}
				com.github.freeacs.dbi.File f = unittype.getFiles().getByName(array[1]);
				if (f == null) {
					File file = new File(array[1]);
					if (!file.exists()) {
						throw new IllegalArgumentException("Line " + lineCounter + ": File not found.");
					}
					byte[] b = new byte[(int) file.length()];
					FileInputStream fs = new FileInputStream(file);
					fs.read(b);
					fs.close();
					f = new com.github.freeacs.dbi.File(unittype, array[1], FileType.MISC,
							null, "1", null, null, unittype.getAcs().getUser());
					f.setBytes(b);
				}
				tc.getFiles().setInputFile(f);
				unittype.getFiles().addOrChangeFile(f, unittype.getAcs());
			}
			else if (array[0].toUpperCase().equals("OUTPUT")) {
				if (array.length >= 2) {
					tc.getFiles().setOutputFile(unittype.getFiles().getByName(array[1]));
				}
			}
				
			
			
			if (line.toUpperCase().startsWith(TestCaseParameterType.SET.toString()) && line.length() > 3) {
				if (array.length == 0 || array.length > 3)
					throw new IllegalArgumentException("Line " + lineCounter + ": Does not adhere to format (<SET> <PARAMETER-NAME> <VALUE>)");
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(array[1]);
				if (utp == null)
					throw new IllegalArgumentException("Line " + lineCounter + ": Specifies a parameter which is not found in unittype " + unittype.getName());
				if (tc.getMethod() == TestCase.TestCaseMethod.VALUE)
					tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, null, array[2], 0));
				else
					tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, null, null, notification(array[2])));
			}
			if (line.toUpperCase().startsWith(TestCaseParameterType.GET.toString()) && line.length() > 3) {
				if (array.length == 0 || array.length > 3)
					throw new IllegalArgumentException("Line " + lineCounter + ": Does not adhere to format (<GET> <PARAMETER-NAME> <VALUE>)");
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(array[1]);
				if (utp == null)
					throw new IllegalArgumentException("Line " + lineCounter + ": Specifies a parameter which is not found in unittype " + unittype.getName());
				if (tc.getMethod() == TestCase.TestCaseMethod.VALUE)
					tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, null, array[2], 0));
				else
					tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, null, null, notification(array[2])));
			}
			if (line.toUpperCase().startsWith(TestCaseParameterType.FAC.toString()) && line.length() > 3) {
				if (array.length == 0 || array.length > 3)
					throw new IllegalArgumentException("Line " + lineCounter + ": Does not adhere to format (<FAC> <PARAMETER-NAME> <VALUE>)");
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(array[1]);
				if (utp == null)
					throw new IllegalArgumentException("Line " + lineCounter + ": Specifies a parameter which is not found in unittype " + unittype.getName());
				if (tc.getMethod() == TestCase.TestCaseMethod.VALUE)
					tc.getParams().add(new TestCaseParameter(TestCaseParameterType.FAC, utp, null, array[2], 0));
				else
					tc.getParams().add(new TestCaseParameter(TestCaseParameterType.FAC, utp, null, null, notification(array[2])));
			}
		}
		if (tc.getMethod() == TestCase.TestCaseMethod.FILE && tc.getFiles().getInputFile() == null) {
			throw new IllegalArgumentException("Does not have a mandatory INPUT argument.");
		}
		return tc;
	}

	private String directory(String directory, boolean create) {
		if (directory != null) {
			if (directory.equals(".")) {
				directory = ".";
			} else if (directory.endsWith("/") || directory.endsWith("\\"))
				directory = directory.substring(0, directory.length() - 1);
		} else
			directory = ".";
		File dir = new File(directory);
		if (dir.exists() && !dir.isDirectory())
			throw new IllegalArgumentException("Cannot specify directory " + directory + ", beacuse it's a regular file");
		else if (!dir.exists() && create) {
			dir.mkdir();
		}
		return directory + File.separator;
	}

	public void toFile(String directory, TestCase testCase) throws IOException {
		FileWriter fw = new FileWriter(directory(directory, true) + testCase.getId() + ".tc");
		fw.write(testCase.toString());
		fw.close();
	}

}
