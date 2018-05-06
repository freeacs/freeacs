package com.owera.xaps.tr069.test.system1;

import com.owera.common.util.FileDatabase;
import com.owera.xaps.base.Log;

public class KillDatabase {
	public static FileDatabase database;
	static {
		try {
			database = new FileDatabase("xaps-tr069-kill.dat");
		} catch (Throwable t) {
			Log.error(KillDatabase.class, "Could not initiate database: " + t);
		}
	}

	public static void refresh() {
		try {
			database = new FileDatabase("xaps-tr069-kill.dat");
		} catch (Throwable t) {
			Log.error(KillDatabase.class, "Could not initiate database: " + t);
		}
	}
}
