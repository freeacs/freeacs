package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.dbi.ScriptExecutions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Calendar;

public class DeleteOldScripts extends DBIShare {

	public DeleteOldScripts(String taskName, DataSource mainDataSource, DataSource syslogDataSource) throws SQLException {
		super(taskName, mainDataSource, syslogDataSource);
	}

	private static Logger logger = LoggerFactory.getLogger(DeleteOldScripts.class);

	@Override
	public void runImpl() throws Exception {
		deleteOldScripts();
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	private void deleteOldScripts() throws SQLException {
		ScriptExecutions executions = new ScriptExecutions(getMainDataSource());
		int days = Properties.SHELL_SCRIPT_LIMIT;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -days);
		int rowsDeleted = executions.deleteExecutions(c.getTime());
		if (rowsDeleted == 0)
			logger.debug("DeleteOldScripts: No old script executions deleted");
		else
			logger.info("DeleteOldScripts: " + rowsDeleted + " old script executions deleted");
	}

}
