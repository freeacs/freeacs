package com.github.freeacs.web.app.page.unit;

import lombok.Getter;
import lombok.Setter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a single row in the "provisioning history" table found on the unit
 * configuration page.
 */
@Getter
@Setter
public class HistoryElement {
  private enum EventCode {
    BOOTSTRAP(0),
    BOOT(1),
    PERIODIC(2),
    SCHEDULED(3),
    VALUECHANGE(4),
    KICKED(5),
    CONNECTIONREQUEST(6),
    TRANSFERCOMPLETE(7),
    DIAGNOSTICSCOMPLETE(8),
    REQUESTDOWNLOAD(9);

    private final int code;

    EventCode(int c) {
      code = c;
    }

    public static EventCode fromCode(int c) {
      for (EventCode eventcode : EventCode.values()) {
        if (eventcode.code == c) {
          return eventcode;
        }
      }
      throw new IllegalArgumentException(c + " is not a valid EventCode");
    }
  }

  private static final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private String timestamp;
  private String status;
  private String output;
  private String paramsWritten;
  private List<EventCode> eventCodes = new ArrayList<>();
  private String fileVersion;
  private String errorMessage;

  public HistoryElement(Date timestamp, String syslogMessage) {
    // Black magic below.
    String regex =
        "ProvMsg: PP:\\w+, ST:(?<ST>\\w+), PO:(?<PO>\\w+), SL:\\d+"
            + ",?\\s?(?:PR:(?<PR>\\d+))?"
            + ",?\\s?(?:PW:(?<PW>\\d+))?"
            + ",?\\s?(?:PI:(?<PI>\\d+))?"
            + ",?\\s?(?:JO:(?<JO>\\d+))?"
            + ",?\\s?(?:EV:(?<EV>.+?))?"
            + ",?\\s?(?:PM:(?<PM>\\w+))?"
            + ",?\\s?(?:FV:(?<FV>.+?))?"
            + ",?\\s?(?:ER:(?<ER>\\w+))?"
            + ",?\\s?(?:EC:(?<EC>\\w+))?"
            + ",?\\s?(?:EM:(?<EM>.+?))?";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(syslogMessage);
    if (matcher.matches()) {
      setStatus(matcher.group("ST"));
      setOutput(matcher.group("PO"));
      if (matcher.group("PW") != null) {
        setParamsWritten(matcher.group("PW"));
      } else {
        setParamsWritten("0");
      }
      String events = matcher.group("EV");
      if (events != null) {
        String[] eventParts = events.split(",");
        for (String event : eventParts) {
          try {
            eventCodes.add(EventCode.fromCode(Integer.parseInt(event)));
          } catch (IllegalArgumentException ignore) {
            continue;
          }
        }
      }
      setFileVersion(matcher.group("FV"));
      setErrorMessage(matcher.group("EM"));
      setTimestamp(dateformat.format(timestamp));
    } else {
      throw new IllegalArgumentException("Failed parsing string \"" + syslogMessage + "\"");
    }
  }

  public String toString() {
    StringBuilder events = new StringBuilder();
    for (EventCode event : eventCodes) {
      events.append(event).append(",");
    }
    System.out.println(events);
    events = new StringBuilder(events.substring(0, events.length() - 1));
    return "EventCode: "
        + events
        + ", ParamsWritten: "
        + paramsWritten
        + ", Output: "
        + output
        + ", Status: "
        + status
        + ", FileVersion: "
        + fileVersion
        + ", ErrorMessage: "
        + errorMessage
        + ", Timestamp: "
        + timestamp;
  }
}
