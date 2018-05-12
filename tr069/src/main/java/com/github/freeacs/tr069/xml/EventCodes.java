package com.owera.xaps.tr069.xml;

public class EventCodes {
	public static final String BOOTSTRAP = "0 BOOTSTRAP";
	public static final String BOOT = "1 BOOT";
	public static final String PERIODIC = "2 PERIODIC";
	public static final String SCHEDULED = "3 SCHEDULED";
	public static final String VALUE_CHANGE = "4 VALUE CHANGE";
	public static final String KICKED = "5 KICKED";
	public static final String CONNECTION_REQUEST = "6 CONNECTION REQUEST";
	public static final String TRANSFER_COMPLETE = "7 TRANSFER COMPLETE";
	public static final String DIAGNOSTICS_COMPLETE = "8 DIAGNOSTICS COMPLETE";
	public static final String REQUEST_DOWNLOAD = "9 REQUEST DOWNLOAD";

	public static final String M_REBOOT = "M Reboot";
	public static final String M_SCHEDULE_INFORM = "M ScheduleInform";
	public static final String M_DOWNLOAD = "M Download";
	public static final String M_UPLOAD = "M Upload";
	public static final String M_SET_PARAMETER_VALUES = "M SetParameterValues";
	public static final String M_ADD_OBJECT = "M AddObject";
	public static final String M_DELETE_OBJECT = "M DeleteObject";

	public static final String[] EVENT_CODE_LIST = { EventCodes.BOOTSTRAP, EventCodes.BOOT, EventCodes.PERIODIC, EventCodes.SCHEDULED,
			EventCodes.VALUE_CHANGE, EventCodes.KICKED, EventCodes.CONNECTION_REQUEST, EventCodes.TRANSFER_COMPLETE,
			EventCodes.DIAGNOSTICS_COMPLETE, EventCodes.REQUEST_DOWNLOAD, EventCodes.M_REBOOT, EventCodes.M_SCHEDULE_INFORM,
			EventCodes.M_DOWNLOAD, EventCodes.M_UPLOAD, EventCodes.M_SET_PARAMETER_VALUES, EventCodes.M_ADD_OBJECT,
			EventCodes.M_DELETE_OBJECT };

	public static boolean contains(String eventCode) {
		for (String code : EventCodes.EVENT_CODE_LIST) {
			if (code.equals(eventCode)) {
				return true;
			}
		}
		return false;
	}
}
