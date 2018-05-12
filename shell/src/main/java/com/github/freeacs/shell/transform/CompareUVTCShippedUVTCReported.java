package com.github.freeacs.shell.transform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class CompareUVTCShippedUVTCReported {

	private static String validateType(String value, int validLength) {
		if (value.length() == validLength)
			return value;
		else
			throw new IllegalArgumentException("The argument " + value + " has wrong length is according to type");
	}

	private static String getItem(String line, String type) {
		String[] arr = line.trim().split("\\s+");
		if (arr.length > 1) { // u-file: WAN-UUID MAC MOTOTECH-UUID
			if (type.equals("MAC"))
				return validateType(arr[1], 12);
			else if (type.equals("UUID_MAC"))
				return validateType(arr[0], 36);
			else if (type.equals("UUID_MOTOTECH"))
				return validateType(arr[0], 36);
			else
				throw new IllegalArgumentException("The type " + type + " is not allowed");

		} else {
			if (type.equals("MAC"))
				return validateType(arr[0], 12);
			else if (type.equals("UUID_MAC") || type.equals("UUID_MOTOTECH"))
				return validateType(arr[0], 36);
			else
				throw new IllegalArgumentException("The type " + type + " is not allowed");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			String type = "MAC";
			Set<String> set1 = new HashSet<String>();
			FileReader fr1 = new FileReader("UVTC2911Shipped.mac");
			//			FileReader fr1 = new FileReader("uvtc/units/3078.u");
			FileReader fr2 = new FileReader("uvtc/units/total_reported_by_uvtc.mac");
			BufferedReader br1 = new BufferedReader(fr1);
			BufferedReader br2 = new BufferedReader(fr2);
			String line = null;
			int counter1 = 0;
			int counter2 = 0;
			int counter3 = 0;

			while ((line = br1.readLine()) != null) {
				set1.add(getItem(line, type));
				counter1++;
			}
			while ((line = br2.readLine()) != null) {
				counter2++;
				String item = getItem(line, type);
				if (!set1.contains(item)) {
					System.out.println(item);
					counter3++;
				}
			}
			System.out.println("File1:" + counter1 + " items, File2:" + counter2 + " items, Items in file2 not found in file1:" + counter3);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
