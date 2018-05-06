package com.owera.xaps.shell.transform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import com.owera.xaps.shell.util.StringUtil;

public class CompareUVTCBatchWithStaging {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Set<String> telavoxSet = new HashSet<String>();
			FileReader fr1 = new FileReader("UVTC2889.u");
			FileReader fr2 = new FileReader("Bahnhof78.u");
			BufferedReader br1 = new BufferedReader(fr1);
			BufferedReader br2 = new BufferedReader(fr2);
			String line = null;
			while ((line = br2.readLine()) != null) {
				telavoxSet.add(line);
			}
			while ((line = br1.readLine()) != null) {
				String uuid = StringUtil.split(line)[0];
				String mac = StringUtil.split(line)[1];
				if (!telavoxSet.contains(uuid))
					System.out.println(mac);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
