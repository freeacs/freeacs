package com.github.freeacs.common.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;

public class FileDatabase {

	private File database;
	private HashMap<String, String> databaseMap = new HashMap<String, String>();

	/**
	 * Super simple database based on one file. 
	 * The operation works like this:
	 * 	You can only do row based operations (insert/change/delete row)
	 * 	A row consists of two Strings: Key and Value
	 *	Synchronized access - cannot read/write at the same time - hampers performance
	 *	File will be written all over for every write
	 *	File will be read once, then only read if IOException occurs
	 * @param args
	 */
	public FileDatabase(String filename) throws IOException {
		database = new File(filename);
		read();
	}

	private void read() throws IOException {
		HashMap<String, String> tmp = new HashMap<String, String>();
		if (database.exists()) {
			FileReader fr = new FileReader(database);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] array = line.split("¤¤¤¤");
				tmp.put(array[0], array[1]);
			}
			if (br != null)
				br.close();
		}
		databaseMap = tmp;

	}

	private void write() throws IOException {
		FileWriter fw = new FileWriter(database);
		for (Entry<String, String> entry : databaseMap.entrySet()) {
			fw.write(entry.getKey() + "¤¤¤¤" + entry.getValue() + "\n");
		}
		fw.flush();
		fw.close();
	}

	public synchronized boolean insert(String key, String value) throws IOException {
		if (databaseMap.get(key) == null || !databaseMap.get(key).equals(value)) {
			databaseMap.put(key, value);
			write();
			return true;
		}
		return false;
	}

	public synchronized boolean delete(String key) throws IOException {
		if (databaseMap.containsKey(key)) {
			databaseMap.remove(key);
			write();
			return true;
		}
		return false;
	}

	public synchronized String select(String key) {
		return databaseMap.get(key);
	}

	public static void main(String[] args) {
		try {
			FileDatabase db = new FileDatabase("xAPS-TR069-Test.dat");
			db.insert("Test", "Test");
			db.insert("Test2", "Test2");
			System.out.println(db.select("Test"));
			db.insert("Test", "Test3");
			System.out.println(db.select("Test"));
			db.delete("Test");
			System.out.println(db.select("Test"));
		} catch (Throwable t) {
			System.err.println("Error occured: " + t);
		}

	}

}
