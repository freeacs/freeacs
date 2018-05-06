package com.owera.xaps.core.task;

import java.sql.SQLException;
import java.util.Calendar;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.core.Properties;
import com.owera.xaps.dbi.ScriptExecutions;

public class DeleteOldScripts extends DBIShare {

	public DeleteOldScripts(String taskName) throws SQLException, NoAvailableConnectionException {
		super(taskName);
	}

	private static Logger logger = new Logger();

	@Override
	public void runImpl() throws Exception {
		deleteOldScripts();
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	private void deleteOldScripts() throws NoAvailableConnectionException, SQLException {
		ScriptExecutions executions = new ScriptExecutions(getXapsCp());
		int days = Properties.getShellScriptLimit();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -days);
		int rowsDeleted = executions.deleteExecutions(c.getTime());
		if (rowsDeleted == 0)
			logger.debug("DeleteOldScripts: No old script executions deleted");
		else
			logger.info("DeleteOldScripts: " + rowsDeleted + " old script executions deleted");
	}

}
