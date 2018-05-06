package com.owera.common.db;

import java.sql.SQLException;

public class NoAvailableConnectionException extends SQLException {

	private static final long serialVersionUID = 6365246367824984258L;

	public NoAvailableConnectionException(int maxconn) {
		super("The number of connections to the database has exceeded its allowed maximum (which is " + maxconn + ") . Try again later.");
	}
}
