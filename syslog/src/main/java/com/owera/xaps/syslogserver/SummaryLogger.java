package com.owera.xaps.syslogserver;

import com.owera.common.log.Logger;
import com.owera.common.scheduler.TaskDefaultImpl;
import com.owera.xaps.syslogserver.FailoverFileReader.FailoverCounter;
import com.owera.xaps.syslogserver.Syslog2DB.Syslog2DBCounter;
import com.owera.xaps.syslogserver.SyslogPackets.BufferCounter;

public class SummaryLogger extends TaskDefaultImpl {

	public SummaryLogger(String taskName) {
		super(taskName);
	}

	private Logger logger = new Logger(); // Logging of internal matters - if necessary
	private static Logger summary = new Logger("Summary");
	private static int summaryHeaderCount = 0;
	private static boolean firstTime = true;
	private static int maxMessagesPrMinute = Properties.getMaxMessagesPrMinute();

	public static int getMaxMessagesPrMinute() {
		return maxMessagesPrMinute;
	}

	private void updateProperties() {
		maxMessagesPrMinute = Properties.getMaxMessagesPrMinute();
	}

	@Override
	public void runImpl() throws Throwable {
		updateProperties();
		if (summaryHeaderCount == 0) {
			String uua = Properties.getUnknownUnitsAction();
			if (uua.equals("allow"))
				uua = "    Allow";
			else if (uua.equals("discard"))
				uua = "  Discard";
			else if (uua.equals("allow-create"))
				uua = "   Create";
			else
				uua = " Redirect";
			summary.info("            INCOMING             |  UNKNOWN  |                 SYSLOG EVENTS                     |   DATABASE  |       FAILED        ");
			summary.info("Received BufSize 2Fail Discarded | " + uua + " | Allowed Duplicate Discard TriggerEvent ScriptExec |    OK 2Fail | Waiting 2Buf TooOld ");
			summary.info("-------------------------------------------------------------------------------------------------------------------------------------");
		}
		summaryHeaderCount++;
		if (summaryHeaderCount == 20)
			summaryHeaderCount = 0;
		int received = SyslogServer.resetPacketCount();
		int discarded = received - maxMessagesPrMinute;
		if (discarded > 0)
			logger.error("Received " + received + " messages, discarded " + (discarded) + " messages due to maxMessagesPrMinute limit at " + maxMessagesPrMinute);
		BufferCounter bc = SyslogPackets.getCounter().resetCounters();
		Syslog2DBCounter sc = Syslog2DB.getCounter().resetCounters();
		FailoverCounter fc = FailoverFileReader.getCounter().resetCounters();
		String message = "";
		message += String.format("%8s ", received);
		message += String.format("%7s ", bc.getSize());
		message += String.format("%5s ", bc.getBufferOverflow());
		message += String.format("%9s | ", discarded > 0 ? discarded : 0);
		int unknown = sc.getUnknownAllowed() + sc.getUnknownAllowedCreated() + sc.getUnknownDiscarded() + sc.getUnknownRedirected();
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
		} else
			message += String.format("%7s ", FailoverFile.getFailoverCount(false));
		message += String.format("%4s ", fc.getRecycled());
		message += String.format("%6s", fc.getTooOld());
		summary.info(message);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
