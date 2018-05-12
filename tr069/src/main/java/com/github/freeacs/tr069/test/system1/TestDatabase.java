package com.github.freeacs.tr069.test.system1;

import com.github.freeacs.base.Log;
import com.github.freeacs.common.util.FileDatabase;

public class TestDatabase {
	public static FileDatabase database;
	static {
		try {
			database = new FileDatabase("xaps-tr069-test.dat");
		} catch (Throwable t) {
			Log.error(TestDatabase.class, "Could not initiate database: " + t);
		}
	}
}
