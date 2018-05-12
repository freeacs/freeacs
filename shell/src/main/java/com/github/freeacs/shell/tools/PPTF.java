package com.github.freeacs.shell.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PPTF {

	private static List<ColumnDesc> getColumnDescs(Arguments arguments) {
		List<ColumnDesc> columnDescs = new ArrayList<ColumnDesc>();
		String columnDescStr = arguments.getArgument("c");
		String[] columnDescArr = columnDescStr.split(",");
		for (String columnDesc : columnDescArr) {
			columnDescs.add(new ColumnDesc(columnDesc));
		}
		return columnDescs;
	}

	
	// Accepts these options
	// -f <filename>
	// -d <delimiter>
	// -c <column-desc>  (<column-desc> = (\\d+)(u|l|unitid)?-(\\d+)(.*) separated by comma)
	private static List<Row> firstPass(Arguments arguments) throws IOException {
		FileReader fr = new FileReader(arguments.getArgument("f"));
		BufferedReader br = new BufferedReader(fr);
		List<Row> rows = new ArrayList<Row>();
		List<ColumnDesc> columnDescs = getColumnDescs(arguments);
		String line = null;
		int lineCounter = 1;
		while ((line = br.readLine()) != null) {
			Row row = new Row(lineCounter, columnDescs.size());
			rows.add(row);
			String[] lineArr = line.split(arguments.getArgument("d"));
			String errorMsg = null;
			String warnMsg = null;
			for (ColumnDesc columnDesc : columnDescs) {
				String columnValue = lineArr[columnDesc.getFromIndex() - 1];
				columnValue = columnValue.trim();
				if (columnValue.startsWith("\""))
					columnValue = columnValue.substring(1);
				if (columnValue.endsWith("\""))
					columnValue = columnValue.substring(0, columnValue.length() - 1);
				if (columnDesc.getColumnPattern() != null) {
					if (!columnValue.matches(columnDesc.getColumnPattern().toString())) {
						errorMsg = "Does not match regex: " + columnDesc.getColumnPattern();
					}
				}
				if (columnDesc.isToUpperCase()) {
					if (!columnValue.toUpperCase().equals(columnValue)) {
						columnValue = columnValue.toUpperCase();
						warnMsg = "Changed to upper case";
					}
				} else if (columnDesc.isToLowerCase()) {
					if (!columnValue.toLowerCase().equals(columnValue)) {
						columnValue = columnValue.toLowerCase();
						warnMsg = "Changed to lower case";
					}
				} else if (columnDesc.isUnitid()) {
					if (columnValue.length() == 32) {
						if (!columnValue.toLowerCase().equals(columnValue)) {
							columnValue = columnValue.toLowerCase();
							warnMsg = "Changed to lower case";
						}
						columnValue = columnValue.substring(0, 8) + "-" + columnValue.substring(8, 12) + "-" + columnValue.substring(12, 16) + "-"
								+ columnValue.substring(16, 20) + "-" + columnValue.substring(20, 32);
					} else {
						errorMsg = "Too short for unitId";
					}
				}
				Data data = new Data(row, columnValue);
				if (errorMsg != null) {
					data.addErrorMessage(errorMsg);
					row.setError(true);
					errorMsg = null;
				}
				if (warnMsg != null) {
					data.addWarningMessage(warnMsg);
					row.setWarn(true);
					warnMsg = null;
				}
				row.addData(columnDesc.getToIndex() - 1, data);
			}
			lineCounter++;
		}
		return rows;
	}

	// -dc \d+[,\d+]*
	private static void duplicateCheck(Arguments arguments, List<Row> rows) {
		String duplicateColumns = arguments.getArgument("dc");
		if (duplicateColumns != null) {
			String[] dcs = duplicateColumns.split(",");
			for (String dc : dcs) {
				Map<String, Data> dataMap = new HashMap<String, Data>();
				Integer dcInt = new Integer(dc);
				for (Row row : rows) {
					Data data = row.getDataArray()[dcInt - 1];
					if (dataMap.get(data.getValue()) != null) {
						Data oldData = dataMap.get(data.getValue());
						oldData.addDuplicateRow(row);
						oldData.addErrorMessage("Duplicate exists in line " + row.getLineNumber());
						oldData.getRow().setError(true);
						row.setError(true);
					} else {
						dataMap.put(data.getValue(), data);
					}
				}
			}
		}
	}

	// -a
	// -e
	// -w
	private static void printResult(Arguments arguments, List<Row> rows) {
		String accepted = arguments.getArgument("a");
		String error = arguments.getArgument("e");
		String warning = arguments.getArgument("w");
		if (accepted != null) {
			for (Row row : rows) {
				if (!row.isError())
					System.out.println(row);
			}
		}
		int errorCount = 0;
		int warnCount = 0;
		for (Row row : rows) {
			if ((row.isError() && error != null) || (warning != null && row.isWarn())) {
				System.out.println(String.format("%-5d", row.getLineNumber()) + ": " + row);
				Data[] dataArray = row.getDataArray();
				for (int i = 0; i < dataArray.length; i++) {
					if ((error != null && dataArray[i].getErrorMessages().size() > 0)
							|| (warning != null && dataArray[i].getWarningMessages().size() > 0)) {
						System.out.println(row);

						System.out.println("\tColumn " + i + " : ");
						boolean counted = false;
						for (String errorMsg : dataArray[i].getErrorMessages()) {
							if (!counted) {
								errorCount++;
								counted = true;
							}
							System.out.println("\t\tError   : " + errorMsg);
						}
						counted = false;
						for (String warnMsg : dataArray[i].getWarningMessages()) {
							if (!counted) {
								warnCount++;
								counted = true;
							}
							System.out.println("\t\tWarning :" + warnMsg);
						}
					}
				}
			}
		}
		if (error != null)
			System.out.println("Number of lines affected by an error: " + errorCount);
		if (warning != null)
			System.out.println("Number of lines affected by a warning: " + warnCount);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Arguments arguments = new Arguments(args);
		try {
			// Many simple checks
			List<Row> rows = firstPass(arguments);
			// Duplicate checks
			duplicateCheck(arguments, rows);
			// Print result
			printResult(arguments, rows);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
