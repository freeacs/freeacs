package com.github.freeacs.syslogserver;

import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummaryLogger extends TaskDefaultImpl {
  private final Properties properties;

  public SummaryLogger(String taskName, Properties properties) {
    super(taskName);
    this.properties = properties;
  }

  private static Logger logger = LoggerFactory.getLogger(SummaryLogger.class);
  private static Logger summary = LoggerFactory.getLogger("Summary");
  private static int summaryHeaderCount;
  private static boolean firstTime = true;
  private static Integer maxMessagesPrMinute;

  public static int getMaxMessagesPrMinute(Properties properties) {
    if (maxMessagesPrMinute == null) {
      maxMessagesPrMinute = properties.getMaxMessagesPerMinute();
    }
    return maxMessagesPrMinute;
  }

  private void updateProperties() {
    maxMessagesPrMinute = properties.getMaxMessagesPerMinute();
  }

  @Override
  public void runImpl() throws Throwable {
    updateProperties();
    if (summaryHeaderCount == 0) {
      String uua = properties.getUnknownUnitsAction();
      if ("allow".equals(uua)) {
        uua = "    Allow";
      } else if ("discard".equals(uua)) {
        uua = "  Discard";
      } else if ("allow-create".equals(uua)) {
        uua = "   Create";
      } else {
        uua = " Redirect";
      }
      summary.info(
          "            INCOMING             |  UNKNOWN  |                 SYSLOG EVENTS                     |   DATABASE  |       FAILED        ");
      summary.info(
          "Received BufSize 2Fail Discarded | "
              + uua
              + " | Allowed Duplicate Discard TriggerEvent ScriptExec |    OK 2Fail | Waiting 2Buf TooOld ");
      summary.info(
          "-------------------------------------------------------------------------------------------------------------------------------------");
    }
    summaryHeaderCount++;
    if (summaryHeaderCount == 20) {
      summaryHeaderCount = 0;
    }
    int received = SyslogServer.resetPacketCount();
    int discarded = received - maxMessagesPrMinute;
    if (discarded > 0) {
      logger.error(
          "Received "
              + received
              + " messages, discarded "
              + discarded
              + " messages due to maxMessagesPrMinute limit at "
              + maxMessagesPrMinute);
    }
    SyslogPackets.BufferCounter bc = SyslogPackets.getCounter().resetCounters();
    Syslog2DB.Syslog2DBCounter sc = Syslog2DB.getCounter().resetCounters();
    FailoverFileReader.FailoverCounter fc = FailoverFileReader.getCounter().resetCounters();
    String message = "";
    message += String.format("%8s ", received);
    message += String.format("%7s ", bc.getSize());
    message += String.format("%5s ", bc.getBufferOverflow());
    message += String.format("%9s | ", discarded > 0 ? discarded : 0);
    int unknown =
        sc.getUnknownAllowed()
            + sc.getUnknownAllowedCreated()
            + sc.getUnknownDiscarded()
            + sc.getUnknownRedirected();
    message += String.format("%9s | ", unknown);
    message += String.format("%7s ", sc.getKnownEventAllowed());
    message += String.format("%9s ", sc.getKnownEventDuplicated());
    message += String.format("%7s ", sc.getKnownEventDiscarded());
    message += String.format("%12s ", sc.getTriggerEvent());
    message += String.format("%10s | ", sc.getScriptExecuted());
    message += String.format("%5s ", sc.getOk());
    message += String.format("%5s | ", sc.getFailed());
    if (firstTime) {
      message += String.format("%7s ", FailoverFile.getFailoverCount(true));
      firstTime = false;
    } else {
      message += String.format("%7s ", FailoverFile.getFailoverCount(false));
    }
    message += String.format("%4s ", fc.getRecycled());
    message += String.format("%6s", fc.getTooOld());
    summary.info(message);
  }

  @Override
  public Logger getLogger() {
    return logger;
  }
}
