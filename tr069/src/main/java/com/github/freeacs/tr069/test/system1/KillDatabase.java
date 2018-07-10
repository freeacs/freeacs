package com.github.freeacs.tr069.test.system1;

import com.github.freeacs.base.Log;
import com.github.freeacs.common.util.FileDatabase;

public class KillDatabase {
	public static FileDatabase database;
	static {
		try {
			database = new FileDatabase("acs-TR069-kill.dat");
		} catch (Throwable t) {
			Log.error(KillDatabase.class, "Could not initiate database: " + t);
		}
	}

	public static void refresh() {
		try {
			database = new FileDatabase("acs-TR069-kill.dat");
		} catch (Throwable t) {
			Log.error(KillDatabase.class, "Could not initiate database: " + t);
		}
	}
}
