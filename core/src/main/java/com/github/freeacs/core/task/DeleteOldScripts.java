package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.dbi.ScriptExecutions;
import java.sql.SQLException;
import java.util.Calendar;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteOldScripts extends DBIShare {
  private final Properties properties;

  public DeleteOldScripts(
      String taskName,
      DataSource mainDataSource,
      Properties properties)
      throws SQLException {
    super(taskName, mainDataSource, mainDataSource);
    this.properties = properties;
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
    int days = properties.getShellScriptLimit();
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, -days);
    int rowsDeleted = executions.deleteExecutions(c.getTime());
    if (rowsDeleted == 0 && logger.isDebugEnabled()) {
      logger.debug("DeleteOldScripts: No old script executions deleted");
    } else if (logger.isInfoEnabled()) {
      logger.info("DeleteOldScripts: " + rowsDeleted + " old script executions deleted");
    }
  }
}
