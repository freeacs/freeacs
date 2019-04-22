package com.github.freeacs.tr069.xml;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventList {
  public static final String ID = "EventList";

  private List<EventStruct> eventList = new ArrayList<>();

  void addEvent(EventStruct event) {
    this.eventList.add(event);
  }

  public EventList clone() {
    EventList clonedEventList = new EventList();
    for (EventStruct event : eventList) {
      EventStruct clonedEvent = new EventStruct(event.getEventCode(), event.getCommandKey());
      clonedEventList.addEvent(clonedEvent);
    }
    return clonedEventList;
  }
}
